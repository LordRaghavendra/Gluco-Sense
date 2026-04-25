package com.eldercare.healthtracker.ui.emergency

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.eldercare.healthtracker.viewmodel.EmergencyViewModel
import com.eldercare.healthtracker.viewmodel.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EmergencyActivity : AppCompatActivity() {

    private val vm: EmergencyViewModel by viewModels {
        ViewModelFactory(application as HealthTrackerApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val editName = findViewById<TextInputEditText>(R.id.editName)
        val editPhone = findViewById<TextInputEditText>(R.id.editPhone)
        val editNote = findViewById<TextInputEditText>(R.id.editNote)
        val btnCall = findViewById<MaterialButton>(R.id.btnCall)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        vm.info.observe(this) { info ->
            if (info != null) {
                if (editName.text.isNullOrEmpty()) editName.setText(info.contactName)
                if (editPhone.text.isNullOrEmpty()) editPhone.setText(info.contactPhone)
                if (editNote.text.isNullOrEmpty()) editNote.setText(info.note)
            }
        }

        btnSave.setOnClickListener {
            vm.save(
                editName.text?.toString().orEmpty(),
                editPhone.text?.toString().orEmpty(),
                editNote.text?.toString().orEmpty()
            )
            Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
        }

        btnCall.setOnClickListener {
            val phone = editPhone.text?.toString()?.trim().orEmpty()
            if (phone.isEmpty()) {
                Toast.makeText(this, R.string.hint_emergency_phone, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
                try {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")))
                } catch (_: SecurityException) {
                    startActivity(dial)
                }
            } else {
                // Fall back to the dialer so the call can still be placed even
                // without CALL_PHONE permission. Request the permission next time.
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CALL_PHONE), 201
                )
                startActivity(dial)
            }
        }
    }
}
