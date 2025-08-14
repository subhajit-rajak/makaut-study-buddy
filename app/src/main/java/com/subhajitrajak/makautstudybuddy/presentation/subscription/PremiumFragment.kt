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
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import com.subhajitrajak.makautstudybuddy.R
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
            updateViewIfPremium(isPremium)
        }
        viewModel.refresh()

        // Initialize Firebase Auth and RevenueCat login
        initializeRevenueCatWithFirebaseAuth()

        binding.apply {
            getPremium.setOnClickListener {
                showRevenueCatPaywall()
            }

            cancelSubscription.setOnClickListener {
                openSubscriptionManagementPage()
            }

            backButton.setOnClickListener {
                handleBackButtonPress()
            }
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

    private fun openSubscriptionManagementPage() {
        val packageName = requireContext().packageName
        val subscriptionUrl =
            "https://play.google.com/store/account/subscriptions?package=$packageName"
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

    private fun updateViewIfPremium(isPremium: Boolean) {
        binding.apply {
            availablePlans.visibility = if (isPremium) View.GONE else View.VISIBLE
            whyJoinPremium.visibility = if (isPremium) View.GONE else View.VISIBLE
            getPremium.visibility = if (isPremium) View.GONE else View.VISIBLE
            manageSubscription.visibility = if (isPremium) View.VISIBLE else View.GONE
            cancelSubscription.visibility = if (isPremium) View.VISIBLE else View.GONE
            renewsOn.visibility = if (isPremium) View.VISIBLE else View.GONE
            pageTitle.text = if (isPremium) getString(R.string.premium_catchy_headline_premium_version) else getString(R.string.premium_catchy_headline)

            if (isPremium) {
                // Fetch subscription details
                Purchases.sharedInstance.getCustomerInfoWith(
                    onError = { error ->
                        log("Error fetching subscription details: ${error.message}")
                    },
                    onSuccess = { customerInfo ->
                        val activeSubscriptions = customerInfo.activeSubscriptions

                        if (activeSubscriptions.isNotEmpty()) {
                            val firstSubscription = activeSubscriptions.first()
                            val entitlement = customerInfo.entitlements.active.values.firstOrNull()

                            val productId =
                                firstSubscription // e.g., "premium_monthly" or "premium_yearly"
                            val planName = if (productId.contains("year", true)) {
                                "Premium Yearly"
                            } else {
                                "Premium Monthly"
                            }

                            // Expiration date with time
                            val expiryDate = entitlement?.expirationDate
                            val expiryFormatted = expiryDate?.let {
                                android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", it)
                                    .toString()
                            } ?: "N/A"

                            // Purchase date (latest purchase)
                            val purchaseDate = entitlement?.latestPurchaseDate
                            val purchaseFormatted = purchaseDate?.let {
                                android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", it)
                                    .toString()
                            } ?: "N/A"

                            // First seen in RevenueCat
                            val firstSeen = customerInfo.firstSeen
                            val firstSeenFormatted = firstSeen.let {
                                android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", it)
                                    .toString()
                            }

                            tvRenewDate.text = "Renews on - $expiryFormatted"
                            tvPlanName.text = "Current Plan - $planName"
                            tvPurchaseDate.text = "Purchased on - $purchaseFormatted"
                            tvMemberSince.text = "Member since - $firstSeenFormatted"
                            renewsOn.text = "Renews on $expiryFormatted"
                        }
                    },
                )
            } else {
                // Fetch and display prices for available offerings
                Purchases.sharedInstance.getOfferingsWith(
                    onError = { error ->
                        log("Error fetching offerings: ${error.message}")
                    },
                    onSuccess = { offerings ->
                        val currentOffering = offerings.current
                        if (currentOffering != null) {
                            val monthlyPackage = currentOffering.monthly
                            val annualPackage = currentOffering.annual

                            monthlyPrice.text = monthlyPackage?.product?.price?.formatted ?: "N/A"
                            annualPrice.text = annualPackage?.product?.price?.formatted ?: "N/A"
                        } else {
                            log("No current offering available")
                        }
                    }
                )
            }
        }
    }
}