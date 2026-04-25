package com.eldercare.healthtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/** One "I took my medicine" event, linked to a MedicineEntry. */
@Entity(tableName = "medicine_logs")
data class MedicineLogEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val medicineId: String,
    val medicineName: String,
    val takenAt: Long,
    val timeOfDay: String,
    val syncedAt: Long? = null
)
