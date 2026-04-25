package com.eldercare.healthtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Optional symptoms the user reports alongside a glucose or BP reading.
 * The flag columns are independent so combinations are possible
 * (e.g. dizziness + sweating).
 */
@Entity(tableName = "symptom_entries")
data class SymptomEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val takenAt: Long,
    val dizziness: Boolean = false,
    val headache: Boolean = false,
    val sweating: Boolean = false,
    val fatigue: Boolean = false,
    val note: String? = null,
    val syncedAt: Long? = null
)
