package com.subhajitrajak.makautstudybuddy.presentation.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.subhajitrajak.makautstudybuddy.BuildConfig
import com.subhajitrajak.makautstudybuddy.R
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModel
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModelFactory
import com.subhajitrajak.makautstudybuddy.data.auth.GoogleAuthUiClient
import com.subhajitrajak.makautstudybuddy.data.auth.UserData
import com.subhajitrajak.makautstudybuddy.databinding.ActivityMainBinding
import com.subhajitrajak.makautstudybuddy.presentation.books.BooksActivity
import com.subhajitrajak.makautstudybuddy.presentation.downloads.DownloadedFilesActivity
import com.subhajitrajak.makautstudybuddy.presentation.notes.NotesActivity
import com.subhajitrajak.makautstudybuddy.presentation.organizers.OrganizerActivity
import com.subhajitrajak.makautstudybuddy.presentation.askAi.PdfAssistantFragment
import com.subhajitrajak.makautstudybuddy.presentation.settings.SettingsActivity
import com.subhajitrajak.makautstudybuddy.presentation.syllabus.SyllabusActivity
import com.subhajitrajak.makautstudybuddy.presentation.upload.UploadActivity
import com.subhajitrajak.makautstudybuddy.presentation.videos.VideosActivity
import com.subhajitrajak.makautstudybuddy.utils.Constants.TEST_AD_UNIT_ID
import com.subhajitrajak.makautstudybuddy.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val subscriptionViewModel: SubscriptionViewModel by viewModels {
        SubscriptionViewModelFactory(applicationContext)
    }

    private var adUnitId = if(BuildConfig.DEBUG) TEST_AD_UNIT_ID else BuildConfig.MAIN_ADMOB_UNIT_ID

    private lateinit var googleAuthUiClient: GoogleAuthUiClient

    // for in-app updates
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to the root view to lift all children
            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                0
            )
            insets
        }
        setContentView(binding.root)

        // Initialize GoogleAuthUiClient
        googleAuthUiClient = GoogleAuthUiClient(activity, Identity.getSignInClient(activity))

        // Fetch signed-in user data
        val userData: UserData? = googleAuthUiClient.getSignedInUser()

        if (userData != null) {
            Glide.with(activity)
                .load(userData.profilePictureUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(binding.profilePicture)
        }

        binding.apply {
            organizers.setOnClickListener {
                val intent = Intent(activity, OrganizerActivity::class.java)
                startActivity(intent)
            }

            downloads.setOnClickListener {
                startActivity(Intent(activity, DownloadedFilesActivity::class.java))
            }

            books.setOnClickListener {
                startActivity(Intent(activity, BooksActivity::class.java))
            }

            settings.setOnClickListener {
                startActivity(Intent(activity, SettingsActivity::class.java))
            }

            githubContribute.setOnClickListener {
                val url = "https://github.com/subhajit-rajak/makaut-study-buddy"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = url.toUri()
                startActivity(intent)
            }

            upload.setOnClickListener {
                // not allowing contributions for guest users
                if (googleAuthUiClient.isUserAnonymous()) {
                    showToast(activity, "Login with google for contributions")
                } else {
                    startActivity(Intent(activity, UploadActivity::class.java))
                }
            }

            notes.setOnClickListener {
                startActivity(Intent(activity, NotesActivity::class.java))
            }

            videos.setOnClickListener {
                startActivity(Intent(activity, VideosActivity::class.java))
            }

            syllabus.setOnClickListener {
                startActivity(Intent(activity, SyllabusActivity::class.java))
            }

            talkToAi.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.slide_out_right
                            )
                            .replace(android.R.id.content, PdfAssistantFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }

        subscriptionViewModel.isPremium.observe(this) { premium ->
            if (premium) {
                binding.adView.visibility = View.GONE
            } else {
                loadAd()
            }
        }
        checkForInAppUpdates()
    }

    // checking for in-app updates
    private fun checkForInAppUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                if (result.resultCode != RESULT_OK) {
                    showToast(activity, "Update failed: ${result.resultCode}")
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

    private fun loadAd() {
        val adView = AdView(activity)
        adView.adUnitId = adUnitId
        adView.setAdSize(AdSize.BANNER)

        binding.adView.removeAllViews()
        binding.adView.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    /*
    * Function to get ad Size
    private fun getAdSize(): AdSize {
        val displayMetrics = resources.displayMetrics
        var adWidthPixels = displayMetrics.widthPixels

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            adWidthPixels = windowMetrics.bounds.width()
        }

        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
     */
}