package com.eldercare.healthtracker.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.ui.add.AddBpActivity
import com.eldercare.healthtracker.ui.add.AddGlucoseActivity
import com.eldercare.healthtracker.ui.calendar.CalendarActivity
import com.eldercare.healthtracker.ui.emergency.EmergencyActivity
import com.eldercare.healthtracker.ui.history.HistoryActivity
import com.eldercare.healthtracker.ui.medicines.MedicinesActivity
import com.eldercare.healthtracker.ui.report.ReportActivity
import com.eldercare.healthtracker.ui.settings.SettingsActivity
import com.eldercare.healthtracker.util.DateUtils
import com.eldercare.healthtracker.util.Level
import com.eldercare.healthtracker.util.RangeEvaluator
import com.eldercare.healthtracker.viewmodel.HomeViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.button.MaterialButton

/**
 * Landing screen. Shows the two most-recent readings and big primary buttons
 * so the user can log a new one in at most two taps.
 */
class HomeActivity : AppCompatActivity() {

    private val vm: HomeViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        maybeRequestNotificationPermission()

        val latestGlucose = findViewById<TextView>(R.id.txtLatestGlucose)
        val latestGlucoseMeta = findViewById<TextView>(R.id.txtLatestGlucoseMeta)
        val latestBp = findViewById<TextView>(R.id.txtLatestBp)
        val latestBpMeta = findViewById<TextView>(R.id.txtLatestBpMeta)

        vm.latestGlucose.observe(this) { list ->
            renderLatestGlucose(list.firstOrNull(), latestGlucose, latestGlucoseMeta)
        }
        vm.latestBp.observe(this) { list ->
            renderLatestBp(list.firstOrNull(), latestBp, latestBpMeta)
        }

        findViewById<MaterialButton>(R.id.btnAddSugar).setOnClickListener {
            startActivity(Intent(this, AddGlucoseActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAddBp).setOnClickListener {
            startActivity(Intent(this, AddBpActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnMedicines).setOnClickListener {
            startActivity(Intent(this, MedicinesActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnReport).setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }
    }

    private fun renderLatestGlucose(g: GlucoseEntry?, value: TextView, meta: TextView) {
        if (g == null) {
            value.text = "—"
            meta.text = getString(R.string.home_no_readings)
            return
        }
        val level = RangeEvaluator.evaluateGlucose(g.mgPerDl, g.contextTag, vm.prefs)
        value.text = "${g.mgPerDl} mg/dL · ${levelLabel(level)}"
        value.setTextColor(levelColor(level))
        val ctxLabel = when (g.contextTag) {
            "FASTING" -> "Fasting"
            "BEFORE_MEAL" -> "Before meal"
            else -> "After meal"
        }
        meta.text = "$ctxLabel · ${DateUtils.formatDateTime(g.takenAt)}"
    }

    private fun renderLatestBp(b: BloodPressureEntry?, value: TextView, meta: TextView) {
        if (b == null) {
            value.text = "—"
            meta.text = getString(R.string.home_no_readings)
            return
        }
        val level = RangeEvaluator.evaluateBp(b.systolic, b.diastolic, vm.prefs)
        value.text = "${b.systolic}/${b.diastolic} mmHg · ${levelLabel(level)}"
        value.setTextColor(levelColor(level))
        meta.text = DateUtils.formatDateTime(b.takenAt)
    }

    private fun levelLabel(l: Level) = when (l) {
        Level.HIGH -> getString(R.string.label_high)
        Level.LOW -> getString(R.string.label_low)
        Level.NORMAL -> getString(R.string.label_normal)
    }

    private fun levelColor(l: Level) = when (l) {
        Level.HIGH -> getColor(R.color.level_high)
        Level.LOW -> getColor(R.color.level_low)
        Level.NORMAL -> getColor(R.color.level_normal)
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101
                )
            }
        }
    }
}
