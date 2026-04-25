package com.eldercare.healthtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** One blood-pressure measurement (systolic / diastolic in mmHg). */
@Entity(tableName = "bp_entries")
data class BloodPressureEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int? = null,
    val takenAt: Long,
    val note: String? = null,
    val syncedAt: Long? = null
)
