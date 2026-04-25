package com.eldercare.healthtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A medicine the user is currently taking. This defines the *schedule*; actual
 * "I took it" events are represented by MedicineLogEntry so we can show doctor-
 * ready adherence on the report.
 *
 * `timesOfDay` is a comma-separated list of:  "MORNING", "AFTERNOON", "NIGHT".
 * `foodTiming` is one of: "BEFORE_FOOD", "AFTER_FOOD", "ANYTIME".
 */
@Entity(tableName = "medicines")
data class MedicineEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val timesOfDay: String,
    val foodTiming: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
