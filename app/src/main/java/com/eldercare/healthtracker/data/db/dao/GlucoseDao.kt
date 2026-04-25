package com.eldercare.healthtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: GlucoseEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<GlucoseEntry>)

    @Query("SELECT * FROM glucose_entries ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<GlucoseEntry>>

    @Query("SELECT * FROM glucose_entries ORDER BY takenAt DESC")
    suspend fun getAll(): List<GlucoseEntry>

    @Query("SELECT * FROM glucose_entries WHERE takenAt BETWEEN :startMs AND :endMs ORDER BY takenAt DESC")
    suspend fun getBetween(startMs: Long, endMs: Long): List<GlucoseEntry>

    @Query("SELECT * FROM glucose_entries WHERE syncedAt IS NULL")
    suspend fun getUnsynced(): List<GlucoseEntry>

    @Query("UPDATE glucose_entries SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>, syncedAt: Long)

    @Query("DELETE FROM glucose_entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM glucose_entries")
    suspend fun count(): Int

    @Query("SELECT MAX(takenAt) FROM glucose_entries")
    suspend fun lastTakenAt(): Long?
}
