package com.subhajitrajak.makautstudybuddy.presentation.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.SettingsModel
import com.subhajitrajak.makautstudybuddy.data.repository.userLogin.GoogleAuthUiClient
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySettingsBinding
import com.subhajitrajak.makautstudybuddy.presentation.onboarding.OnBoardingActivity
import com.subhajitrajak.makautstudybuddy.utils.Constants.HOME
import com.subhajitrajak.makautstudybuddy.utils.Constants.SETTINGS_DATA
import com.subhajitrajak.makautstudybuddy.utils.showToast
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    private lateinit var googleAuthUiClient: GoogleAuthUiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.settingsBackButton.setOnClickListener {
            finish()
        }

        googleAuthUiClient = GoogleAuthUiClient(
            context = this,
            oneTapClient = Identity.getSignInClient(this)
        )

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        @Suppress("DEPRECATION")
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode
        }

        val text = "App version $versionName ($versionCode)"
        binding.appVersion.text = text

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseRef = firebaseDatabase.getReference(SETTINGS_DATA).child(HOME)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(SettingsModel::class.java)
                if (settings != null) {
                    setView(
                        settings.rateUs ?: "",
                        settings.shareApp ?: "",
                        settings.privacyPolicy ?: "",
                        settings.terms ?: "",
                        settings.contact ?: "",
                        settings.feedback ?: ""
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "onCancelled: ${error.message}")
            }
        })
    }

    private fun setView(
        rateUs: String,
        shareApp: String,
        privacyPolicy: String,
        terms: String,
        contact: String,
        feedback: String
    ) {
        binding.rateUs.setOnClickListener {
            if (rateUs.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, rateUs.toUri())
                startActivity(intent)
            } else {
                showToast(this, "Rate Us link is unavailable.")
            }
        }

        binding.privacyPolicy.setOnClickListener {
            if (privacyPolicy.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, privacyPolicy.toUri())
                startActivity(intent)
            } else {
                showToast(this, "Privacy Policy is unavailable.")
            }
        }

        binding.terms.setOnClickListener {
            if (terms.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, terms.toUri())
                startActivity(intent)
            } else {
                showToast(this, "Terms of Service are unavailable.")
            }
        }

        binding.share.setOnClickListener {
            if (shareApp.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, shareApp)
                startActivity(Intent.createChooser(intent, "Share"))
            } else {
                showToast(this, "Share link is unavailable.")
            }
        }

        binding.contact.setOnClickListener {
            if (contact.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, "mailto:$contact".toUri())
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_contact))
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_contact))
                startActivity(Intent.createChooser(intent, "Contact Us"))
            } else {
                showToast(this, "Contact information is unavailable.")
            }
        }

        binding.feedback.setOnClickListener {
            if (feedback.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, "mailto:$feedback".toUri())
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_feedback))
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_feedback))
                startActivity(Intent.createChooser(intent, "Share your feedback"))
            } else {
                showToast(this, "Contact information for feedback is unavailable.")
            }
        }

        binding.logout.setOnClickListener {
            lifecycleScope.launch {
                googleAuthUiClient.signOut()
                showToast(this@SettingsActivity, "Logged out successfully.")
                startActivity(Intent(this@SettingsActivity, OnBoardingActivity::class.java))
                finishAffinity()
            }
        }
    }
}