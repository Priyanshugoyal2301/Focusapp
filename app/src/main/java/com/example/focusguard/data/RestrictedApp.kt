package com.example.focusguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restricted_apps")
data class RestrictedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 0 // 0 means just blocked immediately
)
