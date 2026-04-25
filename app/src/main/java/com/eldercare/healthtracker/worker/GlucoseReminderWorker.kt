package com.eldercare.healthtracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eldercare.healthtracker.notifications.NotificationHelper

/** Shows the "please log your sugar" reminder. Scheduled periodically. */
class GlucoseReminderWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showGlucoseReminder(applicationContext)
        return Result.success()
    }
}
