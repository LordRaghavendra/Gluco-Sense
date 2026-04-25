package com.eldercare.healthtracker.ui.medicines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.google.android.material.button.MaterialButton

class MedicineAdapter(
    private val onTake: (MedicineEntry) -> Unit,
    private val onRemove: (MedicineEntry) -> Unit
) : ListAdapter<MedicineEntry, MedicineAdapter.VH>(DIFF) {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtName)
        val details: TextView = v.findViewById(R.id.txtDetails)
        val btnTaken: MaterialButton = v.findViewById(R.id.btnTaken)
        val btnRemove: MaterialButton = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_medicine, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val m = getItem(pos)
        h.name.text = m.name
        val times = m.timesOfDay.split(",").filter { it.isNotBlank() }
            .joinToString(", ") { timeLabel(it) }
        val food = when (m.foodTiming) {
            "BEFORE_FOOD" -> h.itemView.context.getString(R.string.food_before)
            "AFTER_FOOD" -> h.itemView.context.getString(R.string.food_after)
            else -> h.itemView.context.getString(R.string.food_anytime)
        }
        h.details.text = "${m.dosage} · $times · $food"
        h.btnTaken.setOnClickListener { onTake(m) }
        h.btnRemove.setOnClickListener { onRemove(m) }
    }

    private fun timeLabel(code: String) = when (code) {
        "MORNING" -> "Morning"
        "AFTERNOON" -> "Afternoon"
        "NIGHT" -> "Night"
        else -> code
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MedicineEntry>() {
            override fun areItemsTheSame(a: MedicineEntry, b: MedicineEntry) = a.id == b.id
            override fun areContentsTheSame(a: MedicineEntry, b: MedicineEntry) = a == b
        }
    }
}
