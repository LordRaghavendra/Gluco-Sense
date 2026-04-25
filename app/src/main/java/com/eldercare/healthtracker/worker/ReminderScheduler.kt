package com.eldercare.healthtracker.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules (or cancels) the glucose & medicine reminder workers based on
 * current user preferences. Safe to call repeatedly — uses KEEP policy when
 * the same schedule is already in place.
 */
object ReminderScheduler {

    private const val GLUCOSE_WORK = "glucose_reminder"
    private const val MEDICINE_WORK = "medicine_reminder"

    fun rescheduleAll(context: Context, prefs: PreferenceManager) {
        scheduleGlucose(context, prefs)
        scheduleMedicine(context, prefs)
    }

    fun scheduleGlucose(context: Context, prefs: PreferenceManager) {
        val wm = WorkManager.getInstance(context)
        if (prefs.glucoseReminderDays <= 0) {
            wm.cancelUniqueWork(GLUCOSE_WORK)
            return
        }
        val intervalHours = prefs.glucoseReminderDays * 24L
        // Minimum periodic interval is 15m; hours is always fine.
        val delay = initialDelayForTime(prefs.glucoseReminderTime)
        val req = PeriodicWorkRequestBuilder<GlucoseReminderWorker>(
            intervalHours, TimeUnit.HOURS
        ).setInitialDelay(delay, TimeUnit.MILLISECONDS).build()
        wm.enqueueUniquePeriodicWork(GLUCOSE_WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun scheduleMedicine(context: Context, prefs: PreferenceManager) {
        val wm = WorkManager.getInstance(context)
        if (!prefs.medicineReminderEnabled) {
            wm.cancelUniqueWork(MEDICINE_WORK)
            return
        }
        val delay = initialDelayForTime(prefs.medicineReminderTime)
        val req = PeriodicWorkRequestBuilder<MedicineReminderWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(delay, TimeUnit.MILLISECONDS).build()
        wm.enqueueUniquePeriodicWork(MEDICINE_WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    /** Milliseconds until the next HH:mm tick (next occurrence). */
    private fun initialDelayForTime(hhmm: String): Long {
        val parts = hhmm.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val now = Calendar.getInstance()
        val target = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
