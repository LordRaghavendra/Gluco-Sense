package com.eldercare.healthtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eldercare.healthtracker.data.db.entities.EmergencyInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(info: EmergencyInfo)

    @Query("SELECT * FROM emergency_info WHERE id = 1")
    fun observe(): Flow<EmergencyInfo?>

    @Query("SELECT * FROM emergency_info WHERE id = 1")
    suspend fun get(): EmergencyInfo?
}
