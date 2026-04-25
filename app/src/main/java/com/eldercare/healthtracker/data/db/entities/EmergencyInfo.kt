package com.eldercare.healthtracker.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding the user's emergency info.
 * Deliberately uses a fixed primary key of 1 so updates overwrite.
 */
@Entity(tableName = "emergency_info")
data class EmergencyInfo(
    @PrimaryKey val id: Int = 1,
    val contactName: String = "",
    val contactPhone: String = "",
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
