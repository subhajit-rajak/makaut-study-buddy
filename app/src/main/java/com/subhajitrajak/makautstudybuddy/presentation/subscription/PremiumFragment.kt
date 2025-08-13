package com.subhajitrajak.makautstudybuddy.presentation.subscription

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModel
import com.subhajitrajak.makautstudybuddy.billing.SubscriptionViewModelFactory
import com.subhajitrajak.makautstudybuddy.databinding.FragmentPremiumBinding
import com.subhajitrajak.makautstudybuddy.utils.log
import com.subhajitrajak.makautstudybuddy.utils.showToast

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
class PremiumFragment : Fragment(), PaywallResultHandler {

    private val viewModel: SubscriptionViewModel by viewModels {
        SubscriptionViewModelFactory(requireContext().applicationContext)
    }
    private lateinit var paywallActivityLauncher: PaywallActivityLauncher

    private val binding: FragmentPremiumBinding by lazy {
        FragmentPremiumBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the paywall launcher (must be created in onCreate)
        paywallActivityLauncher = PaywallActivityLauncher(this, this)

        // Observe premium status
        viewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
            // adds premium service
        }
        viewModel.refresh()

        // Initialize Firebase Auth and RevenueCat login
        initializeRevenueCatWithFirebaseAuth()

        binding.getPremium.setOnClickListener {
            // Check if user is premium and show appropriate action
            if (viewModel.isPremium.value == true) {
                // User is premium, show subscription management
                showManageSubscription()
            } else {
                // User is not premium, show paywall
                showRevenueCatPaywall()
            }
        }

        binding.backButton.setOnClickListener {
            handleBackButtonPress()
        }
    }

    private fun showRevenueCatPaywall() {
        try {
            // launch() always attempts to present the paywall
            // launchIfNeeded(requiredEntitlementIdentifier = "pro") will only launch if entitlement not active
            paywallActivityLauncher.launch()
        } catch (e: Exception) {
            log("Error launching paywall: ${e.localizedMessage}")
            showToast(requireContext(), "Something went wrong. Please try again.")
        }
    }

    override fun onActivityResult(result: PaywallResult) {
        when (result) {
            is PaywallResult.Purchased -> {
                showToast(requireContext(), "Premium activated successfully!")
                // Refresh premium status after successful purchase
                viewModel.refresh()
                log("Purchase completed for user: ${FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"}")
            }
            is PaywallResult.Restored -> {
                showToast(requireContext(), "Purchases restored")
                viewModel.refresh()
                log("Purchases restored for user: ${FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"}")
            }
            is PaywallResult.Cancelled -> {
                log("Purchase cancelled by user: ${FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"}")
            }
            is PaywallResult.Error -> {
                showToast(requireContext(), "Error: ${result.error.message}")
                log("Paywall error for user: ${FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"}")
            }
        }
    }

    private fun initializeRevenueCatWithFirebaseAuth() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // User is logged in, log them into RevenueCat with their Firebase UID
            logInToRevenueCat(currentUser.uid)
        } else {
            // User is not logged in, RevenueCat will use anonymous user
            log("No Firebase user logged in, using anonymous RevenueCat user")
        }

        // Listen for auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User signed in, log them into RevenueCat
                logInToRevenueCat(user.uid)
            } else {
                // User signed out, log out from RevenueCat
                Purchases.sharedInstance.logOut()
                log("User signed out, logged out from RevenueCat")
            }
        }
    }

    // Log in to RevenueCat with Firebase user ID
    private fun logInToRevenueCat(firebaseUserId: String) {
        Purchases.sharedInstance.logInWith(firebaseUserId) { customerInfo, created ->
            if (created) {
                log("New RevenueCat user created for Firebase user: $firebaseUserId")
            } else {
                log("Existing RevenueCat user logged in for Firebase user: $firebaseUserId")
            }
            // Refresh premium status after login
            viewModel.refresh()
        }
    }

    private fun showManageSubscription() {
        val packageName = requireContext().packageName
        val subscriptionUrl = "https://play.google.com/store/account/subscriptions?package=$packageName"
        val intent = Intent(Intent.ACTION_VIEW, subscriptionUrl.toUri())
        startActivity(intent)
    }

    private fun handleBackButtonPress() {
        if (isAdded) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }
}