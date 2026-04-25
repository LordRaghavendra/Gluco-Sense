package com.eldercare.healthtracker.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.util.DateUtils
import com.eldercare.healthtracker.util.Level
import com.eldercare.healthtracker.util.RangeEvaluator

/**
 * Shared RecyclerView row for glucose + BP readings. Callers pass a list of
 * pre-built [ReadingRow]s so the adapter itself stays trivial.
 */
data class ReadingRow(
    val id: String,
    val valueText: String,
    val metaText: String,
    val level: Level
)

class ReadingAdapter : ListAdapter<ReadingRow, ReadingAdapter.VH>(DIFF) {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val root: View = v.findViewById(R.id.root)
        val value: TextView = v.findViewById(R.id.txtValue)
        val meta: TextView = v.findViewById(R.id.txtMeta)
        val badge: TextView = v.findViewById(R.id.txtBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val row = getItem(position)
        h.value.text = row.valueText
        h.meta.text = row.metaText
        val ctx = h.itemView.context
        val (bg, badgeText) = when (row.level) {
            Level.HIGH -> R.drawable.bg_level_high to ctx.getString(R.string.label_high)
            Level.LOW -> R.drawable.bg_level_low to ctx.getString(R.string.label_low)
            Level.NORMAL -> R.drawable.bg_level_normal to ctx.getString(R.string.label_normal)
        }
        h.root.setBackgroundResource(bg)
        h.badge.setBackgroundResource(bg)
        h.badge.text = badgeText
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ReadingRow>() {
            override fun areItemsTheSame(a: ReadingRow, b: ReadingRow) = a.id == b.id
            override fun areContentsTheSame(a: ReadingRow, b: ReadingRow) = a == b
        }

        fun fromGlucose(
            entries: List<com.eldercare.healthtracker.data.db.entities.GlucoseEntry>,
            prefs: PreferenceManager
        ): List<ReadingRow> = entries.map { g ->
            val contextLabel = when (g.contextTag) {
                "FASTING" -> "Fasting"
                "BEFORE_MEAL" -> "Before meal"
                else -> "After meal"
            }
            ReadingRow(
                id = g.id,
                valueText = "${g.mgPerDl} mg/dL",
                metaText = "$contextLabel · ${DateUtils.formatDateTime(g.takenAt)}",
                level = RangeEvaluator.evaluateGlucose(g.mgPerDl, g.contextTag, prefs)
            )
        }

        fun fromBp(
            entries: List<com.eldercare.healthtracker.data.db.entities.BloodPressureEntry>,
            prefs: PreferenceManager
        ): List<ReadingRow> = entries.map { b ->
            val pulse = b.pulse?.let { " · ${it} bpm" } ?: ""
            ReadingRow(
                id = b.id,
                valueText = "${b.systolic} / ${b.diastolic} mmHg",
                metaText = "${DateUtils.formatDateTime(b.takenAt)}$pulse",
                level = RangeEvaluator.evaluateBp(b.systolic, b.diastolic, prefs)
            )
        }
    }
}
