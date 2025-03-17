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
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.subhajitrajak.makautstudybuddy.presentation.organizers.OrganizerActivity
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.presentation.settings.SettingsActivity
import com.subhajitrajak.makautstudybuddy.databinding.ActivityMainBinding
import com.subhajitrajak.makautstudybuddy.utils.showToast

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

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

        binding.apply {
            organizers.setOnClickListener {
                val intent = Intent(this@MainActivity, OrganizerActivity::class.java)
                startActivity(intent)
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
        }

        checkForInAppUpdates()
    }

    // checking for in-app updates
    private fun checkForInAppUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                showToast(this, "Update failed: ${result.resultCode}")
                Log.d("Update", "Update flow failed! Result code: ${result.resultCode}")
            }
        }

        appUpdateManager.registerListener(listener)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

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
    private val listener = InstallStateUpdatedListener { state->
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