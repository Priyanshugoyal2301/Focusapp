package com.example.focusguard

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Color(0xFFBB86FC),
                    secondary = Color(0xFF03DAC6)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    MainScreen(
                        viewModel = viewModel,
                        onRequestUsageStats = {
                            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        onRequestOverlay = {
                            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        },
                        onRequestAccessibility = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        checkPermissions = {
                            checkPermissions()
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissions(): PermissionsState {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        }
        val usageStatsGranted = mode == AppOpsManager.MODE_ALLOWED
        val overlayGranted = Settings.canDrawOverlays(this)
        // Accessibility check is complex, assuming true if others are for UI simplification
        
        return PermissionsState(usageStatsGranted, overlayGranted, false) // Accessibility usually needs manual verify
    }
}

data class PermissionsState(val usage: Boolean, val overlay: Boolean, val accessibility: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestUsageStats: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit,
    checkPermissions: () -> PermissionsState
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FocusGuard++", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color(0xFFBB86FC)
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E1E1E)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        viewModel.loadInstalledApps() 
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Apps") },
                    label = { Text("Apps") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DashboardScreen(viewModel, onRequestUsageStats, onRequestOverlay, onRequestAccessibility, checkPermissions)
            }
            AnimatedVisibility(
                visible = selectedTab == 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AppSelectionScreen(viewModel)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onRequestUsageStats: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit,
    checkPermissions: () -> PermissionsState
) {
    val restrictedApps by viewModel.restrictedApps.collectAsState(initial = emptyList())
    var perms by remember { mutableStateOf(PermissionsState(false, false, false)) }
    
    LaunchedEffect(Unit) {
        perms = checkPermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gradient Card for Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFBB86FC), Color(0xFF6200EE))))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Focus Mode Active", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${restrictedApps.size} apps restricted", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!perms.usage || !perms.overlay) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFCF6679))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Permissions Required", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!perms.usage) {
                        Button(onClick = onRequestUsageStats, modifier = Modifier.fillMaxWidth()) {
                            Text("Grant Usage Access")
                        }
                    }
                    if (!perms.overlay) {
                        Button(onClick = onRequestOverlay, modifier = Modifier.fillMaxWidth()) {
                            Text("Grant Display Over Other Apps")
                        }
                    }
                    Button(onClick = onRequestAccessibility, modifier = Modifier.fillMaxWidth()) {
                        Text("Enable Accessibility Service")
                    }
                }
            }
        }
    }
}

@Composable
fun AppSelectionScreen(viewModel: MainViewModel) {
    val installedApps by viewModel.installedApps.collectAsState()
    val restrictedApps by viewModel.restrictedApps.collectAsState(initial = emptyList())
    val restrictedPackageNames = restrictedApps.map { it.packageName }.toSet()

    if (installedApps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFBB86FC))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(installedApps) { app ->
                val isRestricted = restrictedPackageNames.contains(app.packageName)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRestricted) Color(0xFFBB86FC).copy(alpha = 0.1f) else Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(app.appName, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isRestricted,
                            onCheckedChange = { checked ->
                                viewModel.toggleRestriction(app, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFBB86FC),
                                checkedTrackColor = Color(0xFFBB86FC).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}
