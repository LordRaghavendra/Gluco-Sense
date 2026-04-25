package com.eldercare.healthtracker.ui.medicines

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.eldercare.healthtracker.viewmodel.AddMedicineViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Shows the user's current medicine list and lets them:
 *   • add a new medicine (FAB opens a dialog),
 *   • mark one as taken (logged to medicine_logs),
 *   • remove (deactivate) a medicine.
 * Supports multiple medicines — list can grow arbitrarily.
 */
class MedicinesActivity : AppCompatActivity() {

    private val vm: AddMedicineViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicines)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val txtEmpty = findViewById<TextView>(R.id.txtEmpty)
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabAdd)

        val adapter = MedicineAdapter(
            onTake = { m ->
                vm.markTaken(m, defaultTimeTag(m))
                Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
            },
            onRemove = { m ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.action_remove_med)
                    .setMessage(m.name)
                    .setPositiveButton(R.string.action_remove_med) { _, _ -> vm.deactivate(m) }
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        vm.medicines.observe(this) { list ->
            adapter.submitList(list)
            txtEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        fab.setOnClickListener { showAddDialog() }
    }

    /** Picks the most relevant slot for an "I took it" action based on the current hour. */
    private fun defaultTimeTag(m: MedicineEntry): String {
        val slots = m.timesOfDay.split(",").filter { it.isNotBlank() }
        if (slots.isEmpty()) return "MORNING"
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val best = when {
            hour < 11 -> "MORNING"
            hour < 17 -> "AFTERNOON"
            else -> "NIGHT"
        }
        return if (slots.contains(best)) best else slots.first()
    }

    private fun showAddDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_medicine, null, false)
        val editName = view.findViewById<TextInputEditText>(R.id.editName)
        val editDosage = view.findViewById<TextInputEditText>(R.id.editDosage)
        val chipsTimes = view.findViewById<ChipGroup>(R.id.chipsTimes)
        val chipsFood = view.findViewById<ChipGroup>(R.id.chipsFood)
        view.findViewById<Chip>(R.id.chipAnytime).isChecked = true

        AlertDialog.Builder(this)
            .setTitle(R.string.action_add_new_medicine)
            .setView(view)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val name = editName.text?.toString()?.trim().orEmpty()
                val dosage = editDosage.text?.toString()?.trim().orEmpty()
                if (name.isEmpty() || dosage.isEmpty()) {
                    Toast.makeText(this, R.string.msg_please_fill_required, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val times = buildList {
                    if (view.findViewById<Chip>(R.id.chipMorning).isChecked) add("MORNING")
                    if (view.findViewById<Chip>(R.id.chipAfternoon).isChecked) add("AFTERNOON")
                    if (view.findViewById<Chip>(R.id.chipNight).isChecked) add("NIGHT")
                }.ifEmpty { listOf("MORNING") }
                val food = when (chipsFood.checkedChipId) {
                    R.id.chipBeforeFood -> "BEFORE_FOOD"
                    R.id.chipAfterFood -> "AFTER_FOOD"
                    else -> "ANYTIME"
                }
                vm.save(name, dosage, times, food) {
                    runOnUiThread {
                        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
