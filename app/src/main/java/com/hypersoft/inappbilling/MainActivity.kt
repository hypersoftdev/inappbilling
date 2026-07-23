package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.model.PurchaseOutcome
import com.hypersoft.billing.model.UiState
import kotlinx.coroutines.launch

const val TAG = "MyTag"

class MainActivity : AppCompatActivity() {

    // BillingManager is an app-lifetime singleton owned by InAppBillingApplication, not created per-Activity.
    private val billingManager: BillingManager by lazy { (application as InAppBillingApplication).billingManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        observeBilling()

        findViewById<MaterialButton>(R.id.mbPurchase).setOnClickListener { onPurchaseClick() }
    }

    private fun observeBilling() {
        // Connection was already kicked off in InAppBillingApplication.onCreate(); productsState/purchasesState
        // refresh automatically once it succeeds, so screens only ever need to collect them.
        lifecycleScope.launch {
            billingManager.productsState.collect { state ->
                when (state) {
                    is UiState.Loading -> Log.d(TAG, "productsState: loading")
                    is UiState.Success -> Log.d(TAG, "productsState: ${state.data}") // render paywall prices from here
                    is UiState.Error -> Log.e(TAG, "productsState: ${state.message} (${state.responseCode})")
                }
            }
        }

        lifecycleScope.launch {
            billingManager.purchasesState.collect { state ->
                when (state) {
                    is UiState.Loading -> Log.d(TAG, "purchasesState: loading")
                    is UiState.Success -> Log.d(TAG, "purchasesState: ${state.data}") // derive premium status from here
                    is UiState.Error -> Log.e(TAG, "purchasesState: ${state.message} (${state.responseCode})")
                }
            }
        }

        // Purchases that settle outside a direct purchaseXxx() call (e.g. resumed after app restart)
        lifecycleScope.launch {
            billingManager.purchaseUpdates.collect { outcome ->
                Log.d(TAG, "purchaseUpdates: $outcome")
            }
        }
    }

    private fun onPurchaseClick() {
        lifecycleScope.launch {
            // In-App
            when (val outcome = billingManager.purchaseInApp(this@MainActivity, "android.test.purchased")) {
                is PurchaseOutcome.Success -> Log.d(TAG, "purchaseInApp: success: ${outcome.message}")
                is PurchaseOutcome.AlreadyOwned -> Log.d(TAG, "purchaseInApp: already owned")
                is PurchaseOutcome.UserCancelled -> Log.d(TAG, "purchaseInApp: cancelled by user")
                is PurchaseOutcome.Failed -> Log.e(TAG, "purchaseInApp: failed: ${outcome.message} (${outcome.responseCode})")
            }

            // Subscription (offerId is optional — omit it to get the plan's default offer,
            // or pass one to target a specific promo/free-trial offer, e.g. from productsState)
            //billingManager.purchaseSubs(this@MainActivity, APP_SUB_WEEKLY, SUB_PLAN_WEEKLY)
        }
    }
}
