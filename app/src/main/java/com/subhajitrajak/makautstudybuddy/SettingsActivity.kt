package com.subhajitrajak.makautstudybuddy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.databinding.ActivitySettingsBinding
import com.subhajitrajak.makautstudybuddy.models.SettingsModel
import com.subhajitrajak.makautstudybuddy.utils.showToast

class SettingsActivity : AppCompatActivity() {
    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

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

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseRef = firebaseDatabase.getReference("SettingsData").child("Home")

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
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rateUs))
                startActivity(intent)
            } else {
                showToast(this, "Rate Us link is unavailable.")
            }
        }

        binding.privacyPolicy.setOnClickListener {
            if (privacyPolicy.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicy))
                startActivity(intent)
            } else {
                showToast(this, "Privacy Policy is unavailable.")
            }
        }

        binding.terms.setOnClickListener {
            if (terms.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(terms))
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
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$contact"))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_contact))
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_contact))
                startActivity(Intent.createChooser(intent, "Contact Us"))
            } else {
                showToast(this, "Contact information is unavailable.")
            }
        }

        binding.feedback.setOnClickListener {
            if (feedback.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$feedback"))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_feedback))
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_feedback))
                startActivity(Intent.createChooser(intent, "Share your feedback"))
            } else {
                showToast(this, "Contact information for feedback is unavailable.")
            }
        }
    }
}