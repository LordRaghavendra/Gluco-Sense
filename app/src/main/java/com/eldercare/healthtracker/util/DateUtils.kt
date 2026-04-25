package com.eldercare.healthtracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Formatting helpers shared by UI + PDF. */
object DateUtils {
    private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatDate(ms: Long): String = dateFmt.format(Date(ms))
    fun formatTime(ms: Long): String = timeFmt.format(Date(ms))
    fun formatDateTime(ms: Long): String = dateTimeFmt.format(Date(ms))
    fun dayKey(ms: Long): String = dayKeyFmt.format(Date(ms))

    /** Return [startMs, endMs] covering the calendar day containing `ms`. */
    fun dayRange(ms: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + 24L * 60 * 60 * 1000 - 1
        return start to end
    }
}
