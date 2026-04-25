package com.eldercare.healthtracker.data.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Thin wrapper around SharedPreferences for user-configurable values.
 * Keeping this in one place makes it obvious which knobs the user can change
 * and ensures sensible defaults.
 */
class PreferenceManager(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("health_prefs", Context.MODE_PRIVATE)

    // --- Safe ranges (glucose mg/dL) ---
    var glucoseFastingLow: Int
        get() = sp.getInt(KEY_GLUCOSE_FAST_LOW, 70)
        set(v) = sp.edit().putInt(KEY_GLUCOSE_FAST_LOW, v).apply()
    var glucoseFastingHigh: Int
        get() = sp.getInt(KEY_GLUCOSE_FAST_HIGH, 100)
        set(v) = sp.edit().putInt(KEY_GLUCOSE_FAST_HIGH, v).apply()
    var glucosePostMealLow: Int
        get() = sp.getInt(KEY_GLUCOSE_POST_LOW, 70)
        set(v) = sp.edit().putInt(KEY_GLUCOSE_POST_LOW, v).apply()
    var glucosePostMealHigh: Int
        get() = sp.getInt(KEY_GLUCOSE_POST_HIGH, 140)
        set(v) = sp.edit().putInt(KEY_GLUCOSE_POST_HIGH, v).apply()

    // --- Safe ranges (BP mmHg) ---
    var bpSysLow: Int
        get() = sp.getInt(KEY_BP_SYS_LOW, 90)
        set(v) = sp.edit().putInt(KEY_BP_SYS_LOW, v).apply()
    var bpSysHigh: Int
        get() = sp.getInt(KEY_BP_SYS_HIGH, 130)
        set(v) = sp.edit().putInt(KEY_BP_SYS_HIGH, v).apply()
    var bpDiaLow: Int
        get() = sp.getInt(KEY_BP_DIA_LOW, 60)
        set(v) = sp.edit().putInt(KEY_BP_DIA_LOW, v).apply()
    var bpDiaHigh: Int
        get() = sp.getInt(KEY_BP_DIA_HIGH, 85)
        set(v) = sp.edit().putInt(KEY_BP_DIA_HIGH, v).apply()

    // --- Reminders ---
    /** How many days between glucose reminder notifications. 0 disables. */
    var glucoseReminderDays: Int
        get() = sp.getInt(KEY_GLUCOSE_REM_DAYS, 1)
        set(v) = sp.edit().putInt(KEY_GLUCOSE_REM_DAYS, v).apply()
    /** "HH:mm" (24h) when the glucose reminder should fire. */
    var glucoseReminderTime: String
        get() = sp.getString(KEY_GLUCOSE_REM_TIME, "08:00") ?: "08:00"
        set(v) = sp.edit().putString(KEY_GLUCOSE_REM_TIME, v).apply()
    /** Daily medicine reminder time. */
    var medicineReminderTime: String
        get() = sp.getString(KEY_MED_REM_TIME, "09:00") ?: "09:00"
        set(v) = sp.edit().putString(KEY_MED_REM_TIME, v).apply()
    var medicineReminderEnabled: Boolean
        get() = sp.getBoolean(KEY_MED_REM_ENABLED, true)
        set(v) = sp.edit().putBoolean(KEY_MED_REM_ENABLED, v).apply()

    // --- Text scale (elderly UX) ---
    /** Multiplier applied on top of system fontScale. 1.0 = default. */
    var textScale: Float
        get() = sp.getFloat(KEY_TEXT_SCALE, 1.15f)
        set(v) = sp.edit().putFloat(KEY_TEXT_SCALE, v).apply()

    // --- Last sync timestamp ---
    var lastSyncAt: Long
        get() = sp.getLong(KEY_LAST_SYNC, 0L)
        set(v) = sp.edit().putLong(KEY_LAST_SYNC, v).apply()

    companion object {
        private const val KEY_GLUCOSE_FAST_LOW = "glucose_fast_low"
        private const val KEY_GLUCOSE_FAST_HIGH = "glucose_fast_high"
        private const val KEY_GLUCOSE_POST_LOW = "glucose_post_low"
        private const val KEY_GLUCOSE_POST_HIGH = "glucose_post_high"
        private const val KEY_BP_SYS_LOW = "bp_sys_low"
        private const val KEY_BP_SYS_HIGH = "bp_sys_high"
        private const val KEY_BP_DIA_LOW = "bp_dia_low"
        private const val KEY_BP_DIA_HIGH = "bp_dia_high"
        private const val KEY_GLUCOSE_REM_DAYS = "glucose_rem_days"
        private const val KEY_GLUCOSE_REM_TIME = "glucose_rem_time"
        private const val KEY_MED_REM_TIME = "med_rem_time"
        private const val KEY_MED_REM_ENABLED = "med_rem_enabled"
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_LAST_SYNC = "last_sync"
    }
}
