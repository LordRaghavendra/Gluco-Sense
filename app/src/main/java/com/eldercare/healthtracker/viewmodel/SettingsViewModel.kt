package com.eldercare.healthtracker.viewmodel

import androidx.lifecycle.ViewModel
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.data.sync.FirebaseSyncManager

class SettingsViewModel(
    val prefs: PreferenceManager,
    val sync: FirebaseSyncManager
) : ViewModel()
