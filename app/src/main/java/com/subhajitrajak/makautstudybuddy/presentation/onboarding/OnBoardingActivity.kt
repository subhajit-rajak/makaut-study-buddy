package com.subhajitrajak.makautstudybuddy.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.models.SettingsModel
import com.subhajitrajak.makautstudybuddy.data.auth.GoogleAuthUiClient
import com.subhajitrajak.makautstudybuddy.databinding.ActivityOnBoardingBinding
import com.subhajitrajak.makautstudybuddy.presentation.home.MainActivity
import com.subhajitrajak.makautstudybuddy.utils.Constants.HOME
import com.subhajitrajak.makautstudybuddy.utils.Constants.SETTINGS_DATA
import com.subhajitrajak.makautstudybuddy.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class OnBoardingActivity : AppCompatActivity() {
    private val binding: ActivityOnBoardingBinding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    private lateinit var googleAuthUiClient: GoogleAuthUiClient
    private lateinit var signInViewModel: SignInViewModel

    private lateinit var termsOfServiceUrl: String
    private lateinit var privacyPolicyUrl: String

    // Activity result launcher for handling sign-in response
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                CoroutineScope(Dispatchers.Main).launch {
                    showLoading(true)

                    val signInResult = googleAuthUiClient.signInWithIntent(intent)
                    signInViewModel.onSignInResult(signInResult)

                    if (signInResult.data != null) {
                        val username = signInResult.data.username
                        val firstName = username?.substring(0, username.indexOf(' '))
                        showToast(this@OnBoardingActivity, "Welcome, $firstName")
                        navigateToHome()
                    } else {
                        showToast(this@OnBoardingActivity, "Sign-in failed: ${signInResult.errorMessage}")
                        showLoading(false)
                    }
                }
            }
        } else {
            showToast(this@OnBoardingActivity, "Sign-in cancelled")
            showLoading(false)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showToast(this, "Notification permission granted")
        } else {
            showToast(this, "Please allow notifications to get important announcements")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // force dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkNotificationPermission()

        binding.googleButton.setOnActiveListener {
            loginUsingGoogle()
        }

        getUrlsFromFirebase()
        setupClickableLinks(binding.tvAgreement)

        // Initialize ViewModel
        signInViewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        // Initialize Google Auth UI Client
        googleAuthUiClient = GoogleAuthUiClient(
            context = this,
            oneTapClient = Identity.getSignInClient(this)
        )

        if(googleAuthUiClient.isUserLoggedIn() && !googleAuthUiClient.isUserAnonymous()){
            navigateToHome()
        }

        // Set up the Google Sign-In button
        binding.getStarted.setOnClickListener {
            loginAsGuest()
        }
    }

    // anonymous login
    private fun loginAsGuest() {
        CoroutineScope(Dispatchers.Main).launch {
            showLoading(true)
            val signInResult = googleAuthUiClient.signInAnonymously()
            signInViewModel.onSignInResult(signInResult)

            if (signInResult.data != null) {
                showToast(this@OnBoardingActivity, "Welcome, Guest")
                navigateToHome()
                showLoading(false)
            } else {
                showToast(this@OnBoardingActivity, "Guest login failed: ${signInResult.errorMessage}")
                showLoading(false)
            }
        }
    }

    // google login
    private fun loginUsingGoogle() {
        CoroutineScope(Dispatchers.Main).launch {
            showLoading(true)
            val intentSender = googleAuthUiClient.signIn()
            if (intentSender != null) {
                signInLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            } else {
                showToast(this@OnBoardingActivity, "Google Sign-In failed")
                showLoading(false)
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.getStarted.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.googleProgressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    private fun setupClickableLinks(textView: TextView) {
        // The full text
        val text = "By continuing, you agree to our Terms of Service and Privacy Policy."

        // Find the start and end indices
        val termsStart = text.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length

        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        // Create SpannableString
        val spannableString = SpannableString(text)

        // Color from resources
        val hyperlinkColor = textView.context.getColor(R.color.text)

        // Terms clickable span
        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl(termsOfServiceUrl, widget)
            }
        }
        spannableString.setSpan(termsClickable, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(hyperlinkColor), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Privacy clickable span
        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl(privacyPolicyUrl, widget)
            }
        }
        spannableString.setSpan(privacyClickable, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(hyperlinkColor), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set to TextView
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    fun openUrl(url: String, view: View) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        view.context.startActivity(intent)
    }

    private fun getUrlsFromFirebase() {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseRef = firebaseDatabase.getReference(SETTINGS_DATA).child(HOME)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(SettingsModel::class.java)
                if (settings != null) {
                    termsOfServiceUrl = settings.terms ?: ""
                    privacyPolicyUrl = settings.privacyPolicy ?: ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "onCancelled: ${error.message}")
            }
        })
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Optional: show rationale dialog before requesting
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}