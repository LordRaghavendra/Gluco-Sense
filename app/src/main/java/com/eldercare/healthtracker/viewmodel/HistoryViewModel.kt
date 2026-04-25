package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.data.repo.HealthRepository

class HistoryViewModel(
    repo: HealthRepository,
    val prefs: PreferenceManager
) : ViewModel() {
    val glucose = repo.observeGlucose().asLiveData()
    val bp = repo.observeBp().asLiveData()
    val medicineLogs = repo.observeMedicineLogs().asLiveData()
}
