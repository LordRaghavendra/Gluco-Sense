package com.eldercare.healthtracker.util

import com.eldercare.healthtracker.data.prefs.PreferenceManager

/**
 * Classifies readings as LOW / NORMAL / HIGH based on user-configured ranges.
 * The UI uses this to color-code values (blue / green / red) in list items
 * and the doctor-friendly report.
 */
enum class Level { LOW, NORMAL, HIGH }

object RangeEvaluator {

    fun evaluateGlucose(mgPerDl: Int, contextTag: String, prefs: PreferenceManager): Level {
        val (low, high) = when (contextTag) {
            "FASTING", "BEFORE_MEAL" -> prefs.glucoseFastingLow to prefs.glucoseFastingHigh
            else -> prefs.glucosePostMealLow to prefs.glucosePostMealHigh
        }
        return when {
            mgPerDl < low -> Level.LOW
            mgPerDl > high -> Level.HIGH
            else -> Level.NORMAL
        }
    }

    fun evaluateBp(sys: Int, dia: Int, prefs: PreferenceManager): Level {
        val sysLevel = when {
            sys < prefs.bpSysLow -> Level.LOW
            sys > prefs.bpSysHigh -> Level.HIGH
            else -> Level.NORMAL
        }
        val diaLevel = when {
            dia < prefs.bpDiaLow -> Level.LOW
            dia > prefs.bpDiaHigh -> Level.HIGH
            else -> Level.NORMAL
        }
        // Worst of the two dominates: HIGH > LOW > NORMAL for visibility.
        return when {
            sysLevel == Level.HIGH || diaLevel == Level.HIGH -> Level.HIGH
            sysLevel == Level.LOW || diaLevel == Level.LOW -> Level.LOW
            else -> Level.NORMAL
        }
    }
}
