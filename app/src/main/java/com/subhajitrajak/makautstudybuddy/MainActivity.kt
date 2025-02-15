package com.subhajitrajak.makautstudybuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.subhajitrajak.makautstudybuddy.adapters.HomeAdapter
import com.subhajitrajak.makautstudybuddy.databinding.ActivityMainBinding
import com.subhajitrajak.makautstudybuddy.models.HomeModel
import com.subhajitrajak.makautstudybuddy.repository.MainRepo
import com.subhajitrajak.makautstudybuddy.utils.MyResponses
import com.subhajitrajak.makautstudybuddy.utils.removeWithAnim
import com.subhajitrajak.makautstudybuddy.utils.showToast
import com.subhajitrajak.makautstudybuddy.utils.showWithAnim
import com.subhajitrajak.makautstudybuddy.viewModels.MainViewModel
import com.subhajitrajak.makautstudybuddy.viewModels.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private val activity = this
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val list = ArrayList<HomeModel>()
    private val adapter = HomeAdapter(list, activity)

    private val repo = MainRepo(activity)
    private val viewModel by lazy {
        ViewModelProvider(activity, MainViewModelFactory(repo))[MainViewModel::class.java]
    }

    // for in-app updates
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            rv.adapter=adapter
            viewModel.getHomeData()
            handleHomeBackend()
            mErrorLayout.mTryAgainBtn.setOnClickListener {
                viewModel.getHomeData()
            }

            settings.setOnClickListener {
                startActivity(Intent(activity, SettingsActivity::class.java))
            }

            github.setOnClickListener {
                val url = "https://github.com/subhajit-rajak/makaut-study-buddy"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        }

        checkForInAppUpdates()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleHomeBackend() {
        viewModel.homeLiveData.observe(activity) { it ->
            when (it) {
                is MyResponses.Error -> {
                    binding.mErrorHolder.showWithAnim()
                }
                is MyResponses.Loading -> {}
                is MyResponses.Success -> {
                    binding.mErrorHolder.removeWithAnim()
                    val tempList = it.data
                    list.clear()
                    tempList?.forEach{
                        list.add(it)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
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