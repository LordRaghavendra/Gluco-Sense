package com.eldercare.healthtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eldercare.healthtracker.data.prefs.PreferenceManager
import com.eldercare.healthtracker.data.repo.HealthRepository
import com.eldercare.healthtracker.pdf.PdfReportGenerator
import com.eldercare.healthtracker.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ReportViewModel(
    private val repo: HealthRepository,
    private val prefs: PreferenceManager
) : androidx.lifecycle.ViewModel() {

    private val _generated = MutableLiveData<File?>()
    val generated: LiveData<File?> = _generated

    fun generate(app: Application, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val end = System.currentTimeMillis()
            val start = end - days * 24L * 60 * 60 * 1000
            val glucose = repo.getGlucoseBetween(start, end)
            val bp = repo.getBpBetween(start, end)
            val logs = repo.getMedicineLogsBetween(start, end)
            val file = PdfReportGenerator.generate(
                app, glucose, bp, logs, prefs,
                periodLabel = "${DateUtils.formatDate(start)} – ${DateUtils.formatDate(end)}"
            )
            _generated.postValue(file)
        }
    }

    fun clearGenerated() { _generated.value = null }
}
