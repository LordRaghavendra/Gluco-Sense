package com.eldercare.healthtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MedicineEntry)

    @Query("SELECT * FROM medicines WHERE active = 1 ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<MedicineEntry>>

    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    suspend fun getAll(): List<MedicineEntry>

    @Query("SELECT * FROM medicines WHERE active = 1")
    suspend fun getActive(): List<MedicineEntry>

    @Query("SELECT * FROM medicines WHERE syncedAt IS NULL")
    suspend fun getUnsynced(): List<MedicineEntry>

    @Query("UPDATE medicines SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>, syncedAt: Long)

    @Query("UPDATE medicines SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: MedicineLogEntry)

    @Query("SELECT * FROM medicine_logs ORDER BY takenAt DESC")
    fun observeLogs(): Flow<List<MedicineLogEntry>>

    @Query("SELECT * FROM medicine_logs WHERE takenAt BETWEEN :startMs AND :endMs ORDER BY takenAt DESC")
    suspend fun getLogsBetween(startMs: Long, endMs: Long): List<MedicineLogEntry>

    @Query("SELECT * FROM medicine_logs WHERE syncedAt IS NULL")
    suspend fun getUnsyncedLogs(): List<MedicineLogEntry>

    @Query("UPDATE medicine_logs SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markLogsSynced(ids: List<String>, syncedAt: Long)
}
