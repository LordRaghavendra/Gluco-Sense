package com.eldercare.healthtracker.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eldercare.healthtracker.HealthTrackerApp

/**
 * Re-register periodic reminder workers on device boot. WorkManager already
 * persists its queue, but our schedule depends on current preferences so a
 * reschedule is the safest thing to do on restart.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as? HealthTrackerApp ?: return
        ReminderScheduler.rescheduleAll(context, app.prefs)
    }
}
