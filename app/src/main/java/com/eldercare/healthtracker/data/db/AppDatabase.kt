package com.eldercare.healthtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eldercare.healthtracker.data.db.dao.BpDao
import com.eldercare.healthtracker.data.db.dao.EmergencyDao
import com.eldercare.healthtracker.data.db.dao.GlucoseDao
import com.eldercare.healthtracker.data.db.dao.MedicineDao
import com.eldercare.healthtracker.data.db.dao.SymptomDao
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.EmergencyInfo
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.data.db.entities.SymptomEntry

@Database(
    entities = [
        GlucoseEntry::class,
        BloodPressureEntry::class,
        MedicineEntry::class,
        MedicineLogEntry::class,
        SymptomEntry::class,
        EmergencyInfo::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun glucoseDao(): GlucoseDao
    abstract fun bpDao(): BpDao
    abstract fun medicineDao(): MedicineDao
    abstract fun symptomDao(): SymptomDao
    abstract fun emergencyDao(): EmergencyDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "healthtracker.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
