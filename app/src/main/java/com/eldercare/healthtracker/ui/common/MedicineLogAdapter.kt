package com.eldercare.healthtracker.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.util.DateUtils

class MedicineLogAdapter : ListAdapter<MedicineLogEntry, MedicineLogAdapter.VH>(DIFF) {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtName)
        val meta: TextView = v.findViewById(R.id.txtMeta)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_medicine_log, p, false))

    override fun onBindViewHolder(h: VH, i: Int) {
        val log = getItem(i)
        h.name.text = log.medicineName
        h.meta.text = "${timeLabel(log.timeOfDay)} · ${DateUtils.formatDateTime(log.takenAt)}"
    }

    private fun timeLabel(code: String) = when (code) {
        "MORNING" -> "Morning"
        "AFTERNOON" -> "Afternoon"
        "NIGHT" -> "Night"
        else -> code
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MedicineLogEntry>() {
            override fun areItemsTheSame(a: MedicineLogEntry, b: MedicineLogEntry) = a.id == b.id
            override fun areContentsTheSame(a: MedicineLogEntry, b: MedicineLogEntry) = a == b
        }
    }
}
