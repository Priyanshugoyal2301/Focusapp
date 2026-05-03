package com.example.focusguard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RestrictedAppDao {
    @Query("SELECT * FROM restricted_apps")
    fun getAll(): Flow<List<RestrictedApp>>

    @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): RestrictedApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: RestrictedApp)

    @Delete
    suspend fun delete(app: RestrictedApp)
}
