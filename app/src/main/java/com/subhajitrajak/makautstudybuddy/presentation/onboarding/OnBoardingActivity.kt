package com.subhajitrajak.makautstudybuddy.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.Identity
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.databinding.ActivityOnBoardingBinding
import com.subhajitrajak.makautstudybuddy.presentation.home.MainActivity
import com.subhajitrajak.makautstudybuddy.data.repository.userLogin.GoogleAuthUiClient
import com.subhajitrajak.makautstudybuddy.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnBoardingActivity : AppCompatActivity() {
    private val binding: ActivityOnBoardingBinding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    private lateinit var googleAuthUiClient: GoogleAuthUiClient
    private lateinit var signInViewModel: SignInViewModel

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

        binding.getStarted.setOnActiveListener {
            loginUsingGoogle()
        }

        // Initialize ViewModel
        signInViewModel = ViewModelProvider(this)[SignInViewModel::class.java]

        // Initialize Google Auth UI Client
        googleAuthUiClient = GoogleAuthUiClient(
            context = this,
            oneTapClient = Identity.getSignInClient(this)
        )

        if(googleAuthUiClient.isUserLoggedIn()){
            navigateToHome()
        }

        // Set up the Google Sign-In button
        binding.googleButton.setOnClickListener {
            loginUsingGoogle()
        }
    }

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
        binding.googleButton.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.googleProgressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }
}