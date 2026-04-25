package com.eldercare.healthtracker.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eldercare.healthtracker.BuildConfig
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.ui.auth.SignInActivity
import com.eldercare.healthtracker.viewmodel.SettingsViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.eldercare.healthtracker.worker.ReminderScheduler
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private val vm: SettingsViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    private lateinit var glucoseTime: String
    private lateinit var medicineTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val prefs = vm.prefs

        val editFastLow = findViewById<TextInputEditText>(R.id.editFastLow)
        val editFastHigh = findViewById<TextInputEditText>(R.id.editFastHigh)
        val editPostLow = findViewById<TextInputEditText>(R.id.editPostLow)
        val editPostHigh = findViewById<TextInputEditText>(R.id.editPostHigh)
        val editSysLow = findViewById<TextInputEditText>(R.id.editSysLow)
        val editSysHigh = findViewById<TextInputEditText>(R.id.editSysHigh)
        val editDiaLow = findViewById<TextInputEditText>(R.id.editDiaLow)
        val editDiaHigh = findViewById<TextInputEditText>(R.id.editDiaHigh)
        val editGlucoseDays = findViewById<TextInputEditText>(R.id.editGlucoseDays)
        val btnGlucoseTime = findViewById<MaterialButton>(R.id.btnGlucoseTime)
        val switchMed = findViewById<MaterialSwitch>(R.id.switchMedEnabled)
        val btnMedicineTime = findViewById<MaterialButton>(R.id.btnMedicineTime)
        val txtCloudStatus = findViewById<TextView>(R.id.txtCloudStatus)
        val btnSignInOut = findViewById<MaterialButton>(R.id.btnSignInOut)
        val btnSyncNow = findViewById<MaterialButton>(R.id.btnSyncNow)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveSettings)

        editFastLow.setText(prefs.glucoseFastingLow.toString())
        editFastHigh.setText(prefs.glucoseFastingHigh.toString())
        editPostLow.setText(prefs.glucosePostMealLow.toString())
        editPostHigh.setText(prefs.glucosePostMealHigh.toString())
        editSysLow.setText(prefs.bpSysLow.toString())
        editSysHigh.setText(prefs.bpSysHigh.toString())
        editDiaLow.setText(prefs.bpDiaLow.toString())
        editDiaHigh.setText(prefs.bpDiaHigh.toString())
        editGlucoseDays.setText(prefs.glucoseReminderDays.toString())
        switchMed.isChecked = prefs.medicineReminderEnabled

        glucoseTime = prefs.glucoseReminderTime
        medicineTime = prefs.medicineReminderTime
        btnGlucoseTime.text = "${getString(R.string.label_glucose_time)}: $glucoseTime"
        btnMedicineTime.text = "${getString(R.string.label_medicine_time)}: $medicineTime"

        btnGlucoseTime.setOnClickListener {
            pickTime(glucoseTime) { newVal ->
                glucoseTime = newVal
                btnGlucoseTime.text = "${getString(R.string.label_glucose_time)}: $glucoseTime"
            }
        }
        btnMedicineTime.setOnClickListener {
            pickTime(medicineTime) { newVal ->
                medicineTime = newVal
                btnMedicineTime.text = "${getString(R.string.label_medicine_time)}: $medicineTime"
            }
        }

        renderCloudStatus(txtCloudStatus, btnSignInOut, btnSyncNow)

        btnSignInOut.setOnClickListener {
            if (!BuildConfig.FIREBASE_ENABLED) {
                Toast.makeText(this, R.string.label_firebase_disabled, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (vm.sync.isSignedIn) {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                renderCloudStatus(txtCloudStatus, btnSignInOut, btnSyncNow)
            } else {
                startActivity(android.content.Intent(this, SignInActivity::class.java))
            }
        }

        btnSyncNow.setOnClickListener {
            if (!BuildConfig.FIREBASE_ENABLED) {
                Toast.makeText(this, R.string.label_firebase_disabled, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                btnSyncNow.isEnabled = false
                val res = withContext(Dispatchers.IO) { runCatching { vm.sync.sync() } }
                btnSyncNow.isEnabled = true
                res.onSuccess { Toast.makeText(this@SettingsActivity, R.string.msg_saved, Toast.LENGTH_SHORT).show() }
                res.onFailure { Toast.makeText(this@SettingsActivity, "Sync failed: ${it.message}", Toast.LENGTH_LONG).show() }
            }
        }

        btnSave.setOnClickListener {
            prefs.glucoseFastingLow = editFastLow.text?.toString()?.toIntOrNull() ?: prefs.glucoseFastingLow
            prefs.glucoseFastingHigh = editFastHigh.text?.toString()?.toIntOrNull() ?: prefs.glucoseFastingHigh
            prefs.glucosePostMealLow = editPostLow.text?.toString()?.toIntOrNull() ?: prefs.glucosePostMealLow
            prefs.glucosePostMealHigh = editPostHigh.text?.toString()?.toIntOrNull() ?: prefs.glucosePostMealHigh
            prefs.bpSysLow = editSysLow.text?.toString()?.toIntOrNull() ?: prefs.bpSysLow
            prefs.bpSysHigh = editSysHigh.text?.toString()?.toIntOrNull() ?: prefs.bpSysHigh
            prefs.bpDiaLow = editDiaLow.text?.toString()?.toIntOrNull() ?: prefs.bpDiaLow
            prefs.bpDiaHigh = editDiaHigh.text?.toString()?.toIntOrNull() ?: prefs.bpDiaHigh
            prefs.glucoseReminderDays = editGlucoseDays.text?.toString()?.toIntOrNull() ?: prefs.glucoseReminderDays
            prefs.glucoseReminderTime = glucoseTime
            prefs.medicineReminderTime = medicineTime
            prefs.medicineReminderEnabled = switchMed.isChecked
            ReminderScheduler.rescheduleAll(this, prefs)
            Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun renderCloudStatus(status: TextView, btn: MaterialButton, btnSync: MaterialButton) {
        when {
            !BuildConfig.FIREBASE_ENABLED -> {
                status.text = getString(R.string.label_firebase_disabled)
                btn.text = getString(R.string.label_sign_in)
                btn.isEnabled = false
                btnSync.isEnabled = false
            }
            vm.sync.isSignedIn -> {
                val email = vm.sync.currentUserEmail.orEmpty()
                status.text = getString(R.string.label_signed_in_as, email)
                btn.text = getString(R.string.label_sign_out)
                btn.isEnabled = true
                btnSync.isEnabled = true
            }
            else -> {
                status.text = getString(R.string.label_not_signed_in)
                btn.text = getString(R.string.label_sign_in)
                btn.isEnabled = true
                btnSync.isEnabled = false
            }
        }
    }

    private fun pickTime(current: String, onPicked: (String) -> Unit) {
        val parts = current.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            this,
            { _, nh, nm -> onPicked(String.format("%02d:%02d", nh, nm)) },
            h, m, false
        ).show()
    }

    override fun onResume() {
        super.onResume()
        val txtCloudStatus = findViewById<TextView>(R.id.txtCloudStatus)
        val btnSignInOut = findViewById<MaterialButton>(R.id.btnSignInOut)
        val btnSyncNow = findViewById<MaterialButton>(R.id.btnSyncNow)
        renderCloudStatus(txtCloudStatus, btnSignInOut, btnSyncNow)
    }
}
