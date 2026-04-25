package com.eldercare.healthtracker.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eldercare.healthtracker.BuildConfig
import com.eldercare.healthtracker.HealthTrackerApp
import com.eldercare.healthtracker.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Email+password sign-in backed by Firebase Auth. Only reachable when
 * BuildConfig.FIREBASE_ENABLED is true (i.e. google-services.json is present);
 * otherwise the caller should prevent navigation here.
 */
class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnSignIn = findViewById<MaterialButton>(R.id.btnSignIn)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val txtError = findViewById<TextView>(R.id.txtError)

        if (!BuildConfig.FIREBASE_ENABLED) {
            txtError.visibility = View.VISIBLE
            txtError.text = getString(R.string.label_firebase_disabled)
            btnSignIn.isEnabled = false
            btnRegister.isEnabled = false
            return
        }

        btnSignIn.setOnClickListener {
            doAuth(editEmail, editPassword, txtError, register = false)
        }
        btnRegister.setOnClickListener {
            doAuth(editEmail, editPassword, txtError, register = true)
        }
    }

    private fun doAuth(
        editEmail: TextInputEditText,
        editPassword: TextInputEditText,
        txtError: TextView,
        register: Boolean
    ) {
        val email = editEmail.text?.toString()?.trim().orEmpty()
        val pwd = editPassword.text?.toString().orEmpty()
        if (email.isEmpty() || pwd.isEmpty()) {
            txtError.visibility = View.VISIBLE
            txtError.text = getString(R.string.msg_enter_value)
            return
        }
        txtError.visibility = View.GONE
        lifecycleScope.launch {
            val auth = FirebaseAuth.getInstance()
            val result = runCatching {
                if (register) auth.createUserWithEmailAndPassword(email, pwd).await()
                else auth.signInWithEmailAndPassword(email, pwd).await()
            }
            result.onSuccess {
                Toast.makeText(this@SignInActivity, R.string.msg_saved, Toast.LENGTH_SHORT).show()
                (application as HealthTrackerApp).syncManager.trySyncInBackground()
                finish()
            }
            result.onFailure {
                txtError.visibility = View.VISIBLE
                txtError.text = getString(R.string.msg_auth_failed)
            }
        }
    }
}
