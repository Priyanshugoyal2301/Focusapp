package com.example.focusguard

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusguard.data.AppDatabase
import com.example.focusguard.data.RestrictedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.restrictedAppDao()

    val restrictedApps = dao.getAll()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                packages.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // Exclude system apps
                    .map { appInfo ->
                        AppInfo(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString()
                        )
                    }.sortedBy { it.appName }
            }
            _installedApps.value = apps
        }
    }

    fun toggleRestriction(appInfo: AppInfo, isRestricted: Boolean) {
        viewModelScope.launch {
            if (isRestricted) {
                dao.insert(RestrictedApp(packageName = appInfo.packageName, appName = appInfo.appName))
            } else {
                dao.delete(RestrictedApp(packageName = appInfo.packageName, appName = appInfo.appName))
            }
        }
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String
)
