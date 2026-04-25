package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eldercare.healthtracker.HealthTrackerApp

/** Tiny factory so ViewModels can receive repositories without a DI framework. */
class ViewModelFactory(private val app: HealthTrackerApp) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(app.healthRepository, app.prefs) as T
            modelClass.isAssignableFrom(AddGlucoseViewModel::class.java) ->
                AddGlucoseViewModel(app.healthRepository, app.syncManager) as T
            modelClass.isAssignableFrom(AddBpViewModel::class.java) ->
                AddBpViewModel(app.healthRepository, app.syncManager) as T
            modelClass.isAssignableFrom(AddMedicineViewModel::class.java) ->
                AddMedicineViewModel(app.healthRepository, app.syncManager) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(app.healthRepository, app.prefs) as T
            modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
                CalendarViewModel(app.healthRepository, app.prefs) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(app.prefs, app.syncManager) as T
            modelClass.isAssignableFrom(EmergencyViewModel::class.java) ->
                EmergencyViewModel(app.healthRepository) as T
            modelClass.isAssignableFrom(ReportViewModel::class.java) ->
                ReportViewModel(app.healthRepository, app.prefs) as T
            else -> throw IllegalArgumentException("Unknown VM class: $modelClass")
        }
    }
}
