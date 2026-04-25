package com.eldercare.healthtracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eldercare.healthtracker.data.db.entities.SymptomEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SymptomEntry)

    @Query("SELECT * FROM symptom_entries ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<SymptomEntry>>

    @Query("SELECT * FROM symptom_entries WHERE takenAt BETWEEN :startMs AND :endMs ORDER BY takenAt DESC")
    suspend fun getBetween(startMs: Long, endMs: Long): List<SymptomEntry>

    @Query("SELECT * FROM symptom_entries WHERE syncedAt IS NULL")
    suspend fun getUnsynced(): List<SymptomEntry>

    @Query("UPDATE symptom_entries SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>, syncedAt: Long)
}
