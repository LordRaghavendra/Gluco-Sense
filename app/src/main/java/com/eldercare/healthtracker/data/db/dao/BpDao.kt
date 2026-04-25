package com.eldercare.healthtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BloodPressureEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<BloodPressureEntry>)

    @Query("SELECT * FROM bp_entries ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<BloodPressureEntry>>

    @Query("SELECT * FROM bp_entries ORDER BY takenAt DESC")
    suspend fun getAll(): List<BloodPressureEntry>

    @Query("SELECT * FROM bp_entries WHERE takenAt BETWEEN :startMs AND :endMs ORDER BY takenAt DESC")
    suspend fun getBetween(startMs: Long, endMs: Long): List<BloodPressureEntry>

    @Query("SELECT * FROM bp_entries WHERE syncedAt IS NULL")
    suspend fun getUnsynced(): List<BloodPressureEntry>

    @Query("UPDATE bp_entries SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>, syncedAt: Long)

    @Query("DELETE FROM bp_entries WHERE id = :id")
    suspend fun delete(id: String)
}
