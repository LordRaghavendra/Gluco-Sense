package com.eldercare.healthtracker.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Handles the "I took it" quick-action on the medicine reminder notification.
 * Logs every active medicine as taken for the slot whose time we're in, then
 * dismisses the notification.
 */
class MedicineTakenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HealthTrackerApp
        val slot = intent.getStringExtra(EXTRA_SLOT) ?: currentSlot()
        CoroutineScope(Dispatchers.IO).launch {
            val meds = app.healthRepository.activeMedicines()
            val now = System.currentTimeMillis()
            meds.forEach { m ->
                app.healthRepository.logMedicineTaken(
                    MedicineLogEntry(
                        medicineId = m.id,
                        medicineName = m.name,
                        takenAt = now,
                        timeOfDay = slot
                    )
                )
            }
        }
        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
        nm?.cancel(NotificationHelper.NOTIF_MEDICINE)
        Toast.makeText(context, R.string.msg_marked_taken, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_SLOT = "slot"
        const val ACTION = "com.eldercare.healthtracker.MEDICINE_TAKEN"

        private fun currentSlot(): String {
            val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                h < 11 -> "MORNING"
                h < 17 -> "AFTERNOON"
                else -> "NIGHT"
            }
        }
    }
}

