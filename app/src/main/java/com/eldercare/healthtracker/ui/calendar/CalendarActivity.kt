package com.eldercare.healthtracker.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.ui.common.ReadingAdapter
import com.eldercare.healthtracker.ui.common.ReadingRow
import com.eldercare.healthtracker.util.DateUtils
import com.eldercare.healthtracker.util.Level
import com.eldercare.healthtracker.viewmodel.CalendarViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private val vm: CalendarViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val calendar = findViewById<CalendarView>(R.id.calendar)
        val txtSelected = findViewById<TextView>(R.id.txtSelected)
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val empty = findViewById<TextView>(R.id.txtEmpty)

        val adapter = ReadingAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val today = System.currentTimeMillis()
        calendar.date = today
        txtSelected.text = DateUtils.formatDate(today)
        vm.loadDay(today)

        calendar.setOnDateChangeListener { _, y, m, d ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
            }
            txtSelected.text = DateUtils.formatDate(cal.timeInMillis)
            vm.loadDay(cal.timeInMillis)
        }

        vm.selected.observe(this) { snap ->
            val rows = mutableListOf<ReadingRow>()
            rows += ReadingAdapter.fromGlucose(snap.glucose, vm.prefs)
            rows += ReadingAdapter.fromBp(snap.bp, vm.prefs)
            rows += snap.medLogs.map { l ->
                ReadingRow(
                    id = l.id,
                    valueText = l.medicineName,
                    metaText = "Taken · ${DateUtils.formatTime(l.takenAt)}",
                    level = Level.NORMAL
                )
            }
            adapter.submitList(rows)
            empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
