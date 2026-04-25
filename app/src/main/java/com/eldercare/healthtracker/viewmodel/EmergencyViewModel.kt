package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.eldercare.healthtracker.data.db.entities.EmergencyInfo
import com.eldercare.healthtracker.data.repo.HealthRepository
import kotlinx.coroutines.launch

class EmergencyViewModel(private val repo: HealthRepository) : ViewModel() {
    val info = repo.observeEmergency().asLiveData()
    fun save(name: String, phone: String, note: String) {
        viewModelScope.launch {
            repo.saveEmergency(EmergencyInfo(contactName = name, contactPhone = phone, note = note))
        }
    }
}
