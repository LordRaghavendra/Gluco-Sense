package com.eldercare.healthtracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.notifications.NotificationHelper

/** Shows the daily "take your medicines" reminder, listing active meds. */
class MedicineReminderWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext.applicationContext as HealthTrackerApp
        val meds = app.healthRepository.activeMedicines()
        val text = if (meds.isEmpty()) {
            "Remember to take your medicines."
        } else {
            meds.joinToString(separator = "\n") { "• ${it.name} (${it.dosage})" }
        }
        val slot = currentSlot()
        NotificationHelper.showMedicineReminder(applicationContext, text, slot)
        return Result.success()
    }

    private fun currentSlot(): String {
        val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            h < 11 -> "MORNING"
            h < 17 -> "AFTERNOON"
            else -> "NIGHT"
        }
    }
}
