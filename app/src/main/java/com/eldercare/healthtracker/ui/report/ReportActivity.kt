package com.eldercare.healthtracker.ui.report

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.viewmodel.ReportViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.io.File

class ReportActivity : AppCompatActivity() {

    private val vm: ReportViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val chips = findViewById<ChipGroup>(R.id.chipsPeriod)
        chips.check(R.id.chip30)

        val btnGenerate = findViewById<MaterialButton>(R.id.btnGenerate)
        val cardResult = findViewById<View>(R.id.cardResult)
        val txtResult = findViewById<TextView>(R.id.txtResult)
        val txtResultMeta = findViewById<TextView>(R.id.txtResultMeta)
        val btnShare = findViewById<MaterialButton>(R.id.btnShare)
        val progress = findViewById<ProgressBar>(R.id.progress)

        btnGenerate.setOnClickListener {
            val days = when (chips.checkedChipId) {
                R.id.chip7 -> 7
                R.id.chip90 -> 90
                else -> 30
            }
            progress.visibility = View.VISIBLE
            cardResult.visibility = View.GONE
            btnGenerate.isEnabled = false
            Toast.makeText(this, R.string.msg_generating, Toast.LENGTH_SHORT).show()
            vm.generate(application, days)
        }

        vm.generated.observe(this) { file ->
            progress.visibility = View.GONE
            btnGenerate.isEnabled = true
            if (file != null) {
                cardResult.visibility = View.VISIBLE
                txtResult.text = file.name
                txtResultMeta.text = getString(R.string.msg_pdf_ready)
                btnShare.setOnClickListener { shareFile(file) }
            }
        }
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this, "${packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
    }
}
