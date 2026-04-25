package com.eldercare.healthtracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.ui.add.AddGlucoseActivity
import com.eldercare.healthtracker.ui.home.HomeActivity

/**
 * Central place for notification channels & builders so reminders across the
 * app look and feel consistent.
 */
object NotificationHelper {

    const val CHANNEL_REMINDERS = "reminders"
    const val NOTIF_GLUCOSE = 1001
    const val NOTIF_MEDICINE = 1002

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_REMINDERS) == null) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_reminders_desc)
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun showGlucoseReminder(context: Context) {
        val intent = Intent(context, AddGlucoseActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_drop)
            .setContentTitle(context.getString(R.string.notif_glucose_title))
            .setContentText(context.getString(R.string.notif_glucose_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIF_GLUCOSE, n)
    }

    fun showMedicineReminder(context: Context, medicineNames: String, slot: String = "") {
        val intent = Intent(context, HomeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val tookIntent = Intent(context, MedicineTakenReceiver::class.java).apply {
            action = MedicineTakenReceiver.ACTION
            putExtra(MedicineTakenReceiver.EXTRA_SLOT, slot)
        }
        val tookPi = PendingIntent.getBroadcast(
            context, 1, tookIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(context.getString(R.string.notif_medicine_title))
            .setContentText(
                if (medicineNames.isBlank())
                    context.getString(R.string.notif_medicine_text)
                else medicineNames
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(medicineNames))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .addAction(
                R.drawable.ic_pill,
                context.getString(R.string.action_mark_taken),
                tookPi
            )
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIF_MEDICINE, n)
    }
}
