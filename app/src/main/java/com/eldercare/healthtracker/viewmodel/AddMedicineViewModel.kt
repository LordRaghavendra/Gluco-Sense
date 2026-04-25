package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.eldercare.healthtracker.data.db.entities.MedicineEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.data.repo.HealthRepository
import com.eldercare.healthtracker.data.sync.FirebaseSyncManager
import kotlinx.coroutines.launch

class AddMedicineViewModel(
    private val repo: HealthRepository,
    private val sync: FirebaseSyncManager
) : ViewModel() {

    val medicines = repo.observeMedicines().asLiveData()

    fun save(
        name: String, dosage: String, timesOfDay: List<String>,
        foodTiming: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repo.addMedicine(
                MedicineEntry(
                    name = name,
                    dosage = dosage,
                    timesOfDay = timesOfDay.joinToString(","),
                    foodTiming = foodTiming
                )
            )
            sync.trySyncInBackground()
            onDone()
        }
    }

    fun markTaken(medicine: MedicineEntry, timeOfDay: String) {
        viewModelScope.launch {
            repo.logMedicineTaken(
                MedicineLogEntry(
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    takenAt = System.currentTimeMillis(),
                    timeOfDay = timeOfDay
                )
            )
            sync.trySyncInBackground()
        }
    }

    fun deactivate(medicine: MedicineEntry) {
        viewModelScope.launch { repo.deactivateMedicine(medicine.id) }
    }
}
