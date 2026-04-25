package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.data.repo.HealthRepository

class HomeViewModel(
    private val repo: HealthRepository,
    val prefs: PreferenceManager
) : ViewModel() {
    val latestGlucose = repo.observeGlucose().asLiveData()
    val latestBp = repo.observeBp().asLiveData()
}
