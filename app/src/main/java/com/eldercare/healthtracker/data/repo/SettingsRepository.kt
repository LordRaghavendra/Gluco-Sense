package com.eldercare.healthtracker.data.repo

import com.eldercare.healthtracker.data.prefs.PreferenceManager

/**
 * Thin façade around PreferenceManager so UI code can depend on a narrow
 * "settings" surface rather than SharedPreferences directly.
 */
class SettingsRepository(private val prefs: PreferenceManager) {
    val preferences: PreferenceManager get() = prefs
}
