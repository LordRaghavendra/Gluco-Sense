package com.eldercare.healthtracker

import android.app.Application
import com.eldercare.healthtracker.data.db.AppDatabase
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.data.repo.HealthRepository
import com.eldercare.healthtracker.data.repo.SettingsRepository
import com.eldercare.healthtracker.data.sync.FirebaseSyncManager
import com.eldercare.healthtracker.notifications.NotificationHelper
import com.eldercare.healthtracker.worker.ReminderScheduler

/**
 * Application class. Acts as a lightweight service locator so the whole app
 * can get access to singletons (database, repositories, sync manager) without
 * pulling in a DI framework. Keeping this simple matches the elderly-friendly
 * nature of the app and keeps the APK small.
 */
class HealthTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val prefs: PreferenceManager by lazy { PreferenceManager(this) }
    val healthRepository: HealthRepository by lazy { HealthRepository(database) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(prefs) }
    val syncManager: FirebaseSyncManager by lazy { FirebaseSyncManager(this, database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.ensureChannels(this)
        // Schedule any reminders that the user has already configured.
        ReminderScheduler.rescheduleAll(this, prefs)
        // Attempt a sync on app start; silently no-ops when Firebase is disabled
        // or the device is offline.
        syncManager.trySyncInBackground()
    }

    companion object {
        lateinit var instance: HealthTrackerApp
            private set
    }
}
