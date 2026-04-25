package com.eldercare.healthtracker.ui.history

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.ui.common.MedicineLogAdapter
import com.eldercare.healthtracker.ui.common.ReadingAdapter
import com.eldercare.healthtracker.viewmodel.HistoryViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tabbed history: Sugar, BP, Medicine lists and a Chart tab with trend lines.
 * All items use the shared color-coded [ReadingAdapter] so a user can glance
 * at the page and spot red/blue rows immediately.
 */
class HistoryActivity : AppCompatActivity() {

    private val vm: HistoryViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    private lateinit var readingAdapter: ReadingAdapter
    private lateinit var logAdapter: MedicineLogAdapter

    private var currentGlucose: List<GlucoseEntry> = emptyList()
    private var currentBp: List<BloodPressureEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val tabs = findViewById<TabLayout>(R.id.tabs)
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val chart = findViewById<LineChart>(R.id.chart)
        val empty = findViewById<TextView>(R.id.txtEmpty)

        readingAdapter = ReadingAdapter()
        logAdapter = MedicineLogAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = readingAdapter
        configureChart(chart)

        fun showTab(pos: Int) {
            when (pos) {
                0 -> {
                    recycler.visibility = View.VISIBLE
                    chart.visibility = View.GONE
                    recycler.adapter = readingAdapter
                    val rows = ReadingAdapter.fromGlucose(currentGlucose, vm.prefs)
                    readingAdapter.submitList(rows)
                    empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
                }
                1 -> {
                    recycler.visibility = View.VISIBLE
                    chart.visibility = View.GONE
                    recycler.adapter = readingAdapter
                    val rows = ReadingAdapter.fromBp(currentBp, vm.prefs)
                    readingAdapter.submitList(rows)
                    empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
                }
                2 -> {
                    recycler.visibility = View.VISIBLE
                    chart.visibility = View.GONE
                    recycler.adapter = logAdapter
                    // reset glucose/bp list
                    vm.medicineLogs.value?.let { logs ->
                        logAdapter.submitList(logs)
                        empty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                3 -> {
                    recycler.visibility = View.GONE
                    chart.visibility = View.VISIBLE
                    empty.visibility = if (currentGlucose.isEmpty() && currentBp.isEmpty()) View.VISIBLE else View.GONE
                    renderChart(chart)
                }
            }
        }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab) = showTab(t.position)
            override fun onTabUnselected(t: TabLayout.Tab) {}
            override fun onTabReselected(t: TabLayout.Tab) {}
        })

        vm.glucose.observe(this) {
            currentGlucose = it
            if (tabs.selectedTabPosition == 0) showTab(0)
            if (tabs.selectedTabPosition == 3) showTab(3)
        }
        vm.bp.observe(this) {
            currentBp = it
            if (tabs.selectedTabPosition == 1) showTab(1)
            if (tabs.selectedTabPosition == 3) showTab(3)
        }
        vm.medicineLogs.observe(this) { logs ->
            if (tabs.selectedTabPosition == 2) {
                logAdapter.submitList(logs)
                empty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun configureChart(chart: LineChart) {
        chart.description = Description().apply { text = "" }
        chart.setNoDataText("Add readings to see trends here.")
        chart.setPinchZoom(true)
        chart.axisRight.isEnabled = false
        chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                fmt.format(Date(value.toLong()))
        }
    }

    private fun renderChart(chart: LineChart) {
        val sets = mutableListOf<LineDataSet>()
        if (currentGlucose.isNotEmpty()) {
            val entries = currentGlucose.sortedBy { it.takenAt }
                .map { Entry(it.takenAt.toFloat(), it.mgPerDl.toFloat()) }
            sets += LineDataSet(entries, "Sugar (mg/dL)").apply {
                color = getColor(R.color.brand_primary)
                setCircleColor(getColor(R.color.brand_primary))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
            }
        }
        if (currentBp.isNotEmpty()) {
            val sorted = currentBp.sortedBy { it.takenAt }
            val sys = sorted.map { Entry(it.takenAt.toFloat(), it.systolic.toFloat()) }
            val dia = sorted.map { Entry(it.takenAt.toFloat(), it.diastolic.toFloat()) }
            sets += LineDataSet(sys, "Systolic").apply {
                color = getColor(R.color.level_high)
                setCircleColor(getColor(R.color.level_high))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
            }
            sets += LineDataSet(dia, "Diastolic").apply {
                color = getColor(R.color.level_low)
                setCircleColor(getColor(R.color.level_low))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
            }
        }
        chart.data = if (sets.isNotEmpty()) LineData(sets.toList()) else null
        chart.invalidate()
    }
}
