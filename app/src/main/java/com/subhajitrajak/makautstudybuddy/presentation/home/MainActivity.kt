package com.subhajitrajak.makautstudybuddy.presentation.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.data.repository.userLogin.GoogleAuthUiClient
import com.subhajitrajak.makautstudybuddy.data.repository.userLogin.UserData
import com.subhajitrajak.makautstudybuddy.databinding.ActivityMainBinding
import com.subhajitrajak.makautstudybuddy.presentation.downloads.DownloadedFilesActivity
import com.subhajitrajak.makautstudybuddy.presentation.notes.NotesActivity
import com.subhajitrajak.makautstudybuddy.presentation.organizers.OrganizerActivity
import com.subhajitrajak.makautstudybuddy.presentation.settings.SettingsActivity
import com.subhajitrajak.makautstudybuddy.presentation.upload.UploadActivity
import com.subhajitrajak.makautstudybuddy.presentation.videos.VideosActivity
import com.subhajitrajak.makautstudybuddy.utils.showToast

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var googleAuthUiClient: GoogleAuthUiClient

    // for in-app updates
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize GoogleAuthUiClient
        googleAuthUiClient = GoogleAuthUiClient(this, Identity.getSignInClient(this))

        // Fetch signed-in user data
        val userData: UserData? = googleAuthUiClient.getSignedInUser()

        if (userData != null) {
            val username = userData.username ?: "Guest"
            val name = username.substringBefore(" ")
            binding.greeting.text = buildString {
                append("Hey, ")
                append(name)
            }

            Glide.with(this)
                .load(userData.profilePictureUrl)
                .placeholder(R.drawable.men_avatar)
                .into(binding.profilePicture)
        }

        binding.apply {
            organizers.setOnClickListener {
                val intent = Intent(this@MainActivity, OrganizerActivity::class.java)
                startActivity(intent)
            }

            downloads.setOnClickListener {
                startActivity(Intent(this@MainActivity, DownloadedFilesActivity::class.java))
            }

            settings.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }

            githubContribute.setOnClickListener {
                val url = "https://github.com/subhajit-rajak/makaut-study-buddy"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }

            upload.setOnClickListener {
                startActivity(Intent(this@MainActivity, UploadActivity::class.java))
            }

            notes.setOnClickListener {
                startActivity(Intent(this@MainActivity, NotesActivity::class.java))
            }

            videos.setOnClickListener {
                startActivity(Intent(this@MainActivity, VideosActivity::class.java))
            }
        }

        checkForInAppUpdates()
    }

    // checking for in-app updates
    private fun checkForInAppUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                if (result.resultCode != RESULT_OK) {
                    showToast(this, "Update failed: ${result.resultCode}")
                    Log.d("Update", "Update flow failed! Result code: ${result.resultCode}")
                }
            }

        appUpdateManager.registerListener(listener)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {

                // requesting for update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }

    // in-app update download listener
    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            binding.root,
            "Update ready to install",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("Restart") { appUpdateManager.completeUpdate() }
            show()
        }
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(listener)
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }
            }
    }
}