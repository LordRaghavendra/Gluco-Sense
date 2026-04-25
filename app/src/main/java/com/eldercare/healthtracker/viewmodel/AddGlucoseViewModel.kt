package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.data.db.entities.SymptomEntry
import com.eldercare.healthtracker.data.repo.HealthRepository
import com.eldercare.healthtracker.data.sync.FirebaseSyncManager
import kotlinx.coroutines.launch

class AddGlucoseViewModel(
    private val repo: HealthRepository,
    private val sync: FirebaseSyncManager
) : ViewModel() {

    fun save(
        mgPerDl: Int,
        contextTag: String,
        takenAt: Long,
        note: String?,
        symptoms: SymptomEntry?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repo.addGlucose(
                GlucoseEntry(
                    mgPerDl = mgPerDl,
                    contextTag = contextTag,
                    takenAt = takenAt,
                    note = note
                )
            )
            if (symptoms != null && (symptoms.dizziness || symptoms.headache ||
                    symptoms.sweating || symptoms.fatigue)) {
                repo.addSymptoms(symptoms)
            }
            sync.trySyncInBackground()
            onDone()
        }
    }
}
