package com.eldercare.healthtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * One blood-glucose measurement. `contextTag` is one of:
 *   "FASTING", "BEFORE_MEAL", "AFTER_MEAL".
 *
 * All rows carry a stable UUID `id` so Firestore sync can upsert by key
 * without relying on Room's auto-increment ints (which would collide
 * across devices).
 */
@Entity(tableName = "glucose_entries")
data class GlucoseEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mgPerDl: Int,
    val contextTag: String,
    val takenAt: Long,
    val note: String? = null,
    val syncedAt: Long? = null
)
