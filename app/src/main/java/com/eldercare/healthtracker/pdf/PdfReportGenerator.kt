package com.eldercare.healthtracker.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.eldercare.healthtracker.data.db.entities.BloodPressureEntry
import com.eldercare.healthtracker.data.db.entities.GlucoseEntry
import com.eldercare.healthtracker.data.db.entities.MedicineLogEntry
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.util.DateUtils
import com.eldercare.healthtracker.util.Level
import com.eldercare.healthtracker.util.RangeEvaluator
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a doctor-friendly PDF covering a period of logs, with averages
 * and abnormal values highlighted. Uses the stdlib android.graphics.pdf
 * API so we don't depend on heavy third-party PDF libraries.
 */
object PdfReportGenerator {

    private const val PAGE_WIDTH = 595   // A4 @ 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36
    private const val LINE = 18

    /**
     * @return the written PDF file (inside the app's cache dir, shareable via
     * FileProvider).
     */
    fun generate(
        context: Context,
        glucose: List<GlucoseEntry>,
        bp: List<BloodPressureEntry>,
        medLogs: List<MedicineLogEntry>,
        prefs: PreferenceManager,
        periodLabel: String
    ): File {
        val doc = PdfDocument()
        val titlePaint = Paint().apply {
            color = Color.BLACK; textSize = 20f; isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.BLACK; textSize = 14f; isFakeBoldText = true
        }
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 11f }
        val redPaint = Paint().apply { color = Color.rgb(200, 40, 40); textSize = 11f }
        val bluePaint = Paint().apply { color = Color.rgb(30, 80, 200); textSize = 11f }
        val greenPaint = Paint().apply { color = Color.rgb(30, 130, 60); textSize = 11f }

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN + LINE

        // Title
        canvas.drawText("Health Tracker — Doctor Report", MARGIN.toFloat(), y.toFloat(), titlePaint)
        y += LINE
        canvas.drawText("Period: $periodLabel", MARGIN.toFloat(), y.toFloat(), textPaint)
        y += LINE
        canvas.drawText("Generated: ${DateUtils.formatDateTime(System.currentTimeMillis())}",
            MARGIN.toFloat(), y.toFloat(), textPaint)
        y += LINE * 2

        fun newPageIfNeeded(): Boolean {
            if (y < PAGE_HEIGHT - MARGIN - LINE) return false
            doc.finishPage(page)
            pageNumber += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = MARGIN + LINE
            return true
        }

        // --- Summary ---
        canvas.drawText("Summary", MARGIN.toFloat(), y.toFloat(), headerPaint); y += LINE
        val gAvg = glucose.takeIf { it.isNotEmpty() }?.map { it.mgPerDl }?.average()
        val gMin = glucose.minByOrNull { it.mgPerDl }?.mgPerDl
        val gMax = glucose.maxByOrNull { it.mgPerDl }?.mgPerDl
        val sysAvg = bp.takeIf { it.isNotEmpty() }?.map { it.systolic }?.average()
        val diaAvg = bp.takeIf { it.isNotEmpty() }?.map { it.diastolic }?.average()
        canvas.drawText(
            "Glucose readings: ${glucose.size}${
                gAvg?.let { "  |  avg %.0f mg/dL (min %d, max %d)".format(it, gMin ?: 0, gMax ?: 0) } ?: ""
            }",
            MARGIN.toFloat(), y.toFloat(), textPaint
        ); y += LINE
        canvas.drawText(
            "BP readings: ${bp.size}${
                if (sysAvg != null && diaAvg != null) "  |  avg %.0f/%.0f mmHg".format(sysAvg, diaAvg) else ""
            }",
            MARGIN.toFloat(), y.toFloat(), textPaint
        ); y += LINE
        canvas.drawText("Medicine doses logged: ${medLogs.size}", MARGIN.toFloat(), y.toFloat(), textPaint)
        y += LINE * 2

        // --- Glucose table ---
        canvas.drawText("Glucose entries", MARGIN.toFloat(), y.toFloat(), headerPaint); y += LINE
        canvas.drawText("Date & time              Value (mg/dL)   Context",
            MARGIN.toFloat(), y.toFloat(), textPaint); y += LINE
        for (g in glucose) {
            newPageIfNeeded()
            val level = RangeEvaluator.evaluateGlucose(g.mgPerDl, g.contextTag, prefs)
            val paint = when (level) {
                Level.HIGH -> redPaint; Level.LOW -> bluePaint; Level.NORMAL -> greenPaint
            }
            val line = "%-24s %-15d %s".format(
                DateUtils.formatDateTime(g.takenAt),
                g.mgPerDl,
                g.contextTag.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
            )
            canvas.drawText(line, MARGIN.toFloat(), y.toFloat(), paint); y += LINE
        }
        y += LINE

        // --- BP table ---
        newPageIfNeeded()
        canvas.drawText("Blood pressure entries", MARGIN.toFloat(), y.toFloat(), headerPaint); y += LINE
        canvas.drawText("Date & time              Sys/Dia (mmHg)",
            MARGIN.toFloat(), y.toFloat(), textPaint); y += LINE
        for (b in bp) {
            newPageIfNeeded()
            val level = RangeEvaluator.evaluateBp(b.systolic, b.diastolic, prefs)
            val paint = when (level) {
                Level.HIGH -> redPaint; Level.LOW -> bluePaint; Level.NORMAL -> greenPaint
            }
            val line = "%-24s %d/%d".format(
                DateUtils.formatDateTime(b.takenAt), b.systolic, b.diastolic
            )
            canvas.drawText(line, MARGIN.toFloat(), y.toFloat(), paint); y += LINE
        }
        y += LINE

        // --- Medicine log ---
        newPageIfNeeded()
        canvas.drawText("Medicine log", MARGIN.toFloat(), y.toFloat(), headerPaint); y += LINE
        for (l in medLogs) {
            newPageIfNeeded()
            canvas.drawText(
                "${DateUtils.formatDateTime(l.takenAt)}  —  ${l.medicineName} (${l.timeOfDay.lowercase()})",
                MARGIN.toFloat(), y.toFloat(), textPaint
            ); y += LINE
        }

        doc.finishPage(page)
        val out = File(context.cacheDir, "reports/health-report-${System.currentTimeMillis()}.pdf")
        out.parentFile?.mkdirs()
        FileOutputStream(out).use { doc.writeTo(it) }
        doc.close()
        return out
    }
}
