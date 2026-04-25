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
import com.eldercare.healthtracker.viewmodel.AddGlucoseViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddGlucoseActivity : AppCompatActivity() {

    private val vm: AddGlucoseViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    /** The currently-selected date/time for the reading, defaults to now. */
    private val chosen: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_glucose)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val editValue = findViewById<TextInputEditText>(R.id.editValue)
        val chipsCtx = findViewById<ChipGroup>(R.id.chipsContext)
        val chipFasting = findViewById<Chip>(R.id.chipFasting)
        val chipBefore = findViewById<Chip>(R.id.chipBeforeMeal)
        val chipAfter = findViewById<Chip>(R.id.chipAfterMeal)
        chipFasting.isChecked = true
        val btnDate = findViewById<MaterialButton>(R.id.btnPickDate)
        val btnTime = findViewById<MaterialButton>(R.id.btnPickTime)
        val editNote = findViewById<TextInputEditText>(R.id.editNote)
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
            val value = editValue.text?.toString()?.trim()?.toIntOrNull()
            if (value == null || value <= 0) {
                Toast.makeText(this, R.string.msg_enter_value, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val ctx = when (chipsCtx.checkedChipId) {
                R.id.chipBeforeMeal -> "BEFORE_MEAL"
                R.id.chipAfterMeal -> "AFTER_MEAL"
                else -> "FASTING"
            }
            val symptoms = SymptomEntry(
                takenAt = chosen.timeInMillis,
                dizziness = chipDizziness.isChecked,
                headache = chipHeadache.isChecked,
                sweating = chipSweating.isChecked,
                fatigue = chipFatigue.isChecked
            )
            vm.save(
                mgPerDl = value,
                contextTag = ctx,
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
