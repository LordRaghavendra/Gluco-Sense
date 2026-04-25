package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.data.repo.HealthRepository
import com.eldercare.healthtracker.util.DateUtils
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val repo: HealthRepository,
    val prefs: PreferenceManager
) : ViewModel() {

    data class DaySnapshot(
        val glucose: List<GlucoseEntry>,
        val bp: List<BloodPressureEntry>,
        val medLogs: List<MedicineLogEntry>
    )

    private val _selected = MutableLiveData<DaySnapshot>(DaySnapshot(emptyList(), emptyList(), emptyList()))
    val selected: LiveData<DaySnapshot> = _selected

    fun loadDay(ms: Long) {
        viewModelScope.launch {
            val (start, end) = DateUtils.dayRange(ms)
            _selected.postValue(
                DaySnapshot(
                    glucose = repo.getGlucoseBetween(start, end),
                    bp = repo.getBpBetween(start, end),
                    medLogs = repo.getMedicineLogsBetween(start, end)
                )
            )
        }
    }
}
