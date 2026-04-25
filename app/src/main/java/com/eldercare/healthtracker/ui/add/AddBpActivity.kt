package com.eldercare.healthtracker.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.SymptomEntry
import com.eldercare.healthtracker.util.DateUtils
import com.eldercare.healthtracker.viewmodel.AddBpViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddBpActivity : AppCompatActivity() {

    private val vm: AddBpViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    private val chosen: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bp)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val editSys = findViewById<TextInputEditText>(R.id.editSys)
        val editDia = findViewById<TextInputEditText>(R.id.editDia)
        val editPulse = findViewById<TextInputEditText>(R.id.editPulse)
        val editNote = findViewById<TextInputEditText>(R.id.editNote)
        val btnDate = findViewById<MaterialButton>(R.id.btnPickDate)
        val btnTime = findViewById<MaterialButton>(R.id.btnPickTime)
        val chipDizziness = findViewById<Chip>(R.id.chipDizziness)
        val chipHeadache = findViewById<Chip>(R.id.chipHeadache)
        val chipSweating = findViewById<Chip>(R.id.chipSweating)
        val chipFatigue = findViewById<Chip>(R.id.chipFatigue)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        updateDateTimeButtons(btnDate, btnTime)

        btnDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    chosen.set(Calendar.YEAR, y)
                    chosen.set(Calendar.MONTH, m)
                    chosen.set(Calendar.DAY_OF_MONTH, d)
                    updateDateTimeButtons(btnDate, btnTime)
                },
                chosen.get(Calendar.YEAR),
                chosen.get(Calendar.MONTH),
                chosen.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        btnTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, min ->
                    chosen.set(Calendar.HOUR_OF_DAY, h)
                    chosen.set(Calendar.MINUTE, min)
                    updateDateTimeButtons(btnDate, btnTime)
                },
                chosen.get(Calendar.HOUR_OF_DAY),
                chosen.get(Calendar.MINUTE),
                false
            ).show()
        }

        btnSave.setOnClickListener {
            val sys = editSys.text?.toString()?.trim()?.toIntOrNull()
            val dia = editDia.text?.toString()?.trim()?.toIntOrNull()
            val pulse = editPulse.text?.toString()?.trim()?.toIntOrNull()
            if (sys == null || dia == null || sys <= 0 || dia <= 0) {
                Toast.makeText(this, R.string.msg_enter_value, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val symptoms = SymptomEntry(
                takenAt = chosen.timeInMillis,
                dizziness = chipDizziness.isChecked,
                headache = chipHeadache.isChecked,
                sweating = chipSweating.isChecked,
                fatigue = chipFatigue.isChecked
            )
            vm.save(
                systolic = sys,
                diastolic = dia,
                pulse = pulse,
                takenAt = chosen.timeInMillis,
                note = editNote.text?.toString()?.trim()?.takeIf { it.isNotEmpty() },
                symptoms = symptoms
            ) {
                runOnUiThread {
                    Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun updateDateTimeButtons(btnDate: MaterialButton, btnTime: MaterialButton) {
        btnDate.text = DateUtils.formatDate(chosen.timeInMillis)
        btnTime.text = DateUtils.formatTime(chosen.timeInMillis)
    }
}
