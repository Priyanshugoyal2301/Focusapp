package com.example.focusguard

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.focusguard.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppDetectionService : AccessibilityService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var db: AppDatabase

    override fun onServiceConnected() {
        super.onServiceConnected()
        db = AppDatabase.getDatabase(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d("AppDetection", "Foreground app changed: $packageName")
            
            scope.launch {
                val isRestricted = db.restrictedAppDao().getApp(packageName) != null
                if (isRestricted) {
                    launchOverlay(packageName)
                }
            }
        }
    }

    private fun launchOverlay(packageName: String) {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("RESTRICTED_APP", packageName)
        startService(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
