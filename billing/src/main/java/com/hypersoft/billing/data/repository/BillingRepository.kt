package com.hypersoft.billing.data.repository

import android.app.Activity
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.hypersoft.billing.data.dataSource.BillingService
import com.hypersoft.billing.presentation.states.BillingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

internal class BillingRepository(private val billingService: BillingService) {

    val isBillingClientReady = billingService.isBillingClientReady
    var currentState: BillingState = billingService.currentState
        internal set(value) {
            billingService.currentState = value
        }

    fun startConnection(onResult: (Boolean, String?) -> Unit) = billingService.startConnection(onResult)

    /* ------------------------------- Purchase History ------------------------------- */

    suspend fun queryInAppPurchases() = withContext(Dispatchers.IO) {
        billingService.queryInAppPurchases()
    }

    suspend fun querySubsPurchases() = withContext(Dispatchers.IO) {
        billingService.querySubsPurchases()
    }

    /* ----------------------------------- Products ----------------------------------- */

    suspend fun queryInAppProductDetails(productIds: List<String>) = withContext(Dispatchers.IO) {
        billingService.queryInAppProductDetails(productIds)
    }

    suspend fun querySubsProductDetails(productIds: List<String>) = withContext(Dispatchers.IO) {
        billingService.querySubsProductDetails(productIds)
    }

    /* ----------------------------------- Purchases ----------------------------------- */

    suspend fun purchaseFlow(activity: Activity, params: BillingFlowParams) = withContext(Dispatchers.Main) {
        billingService.purchaseFlow(activity, params)
    }

    /* ------------------------------- Acknowledgements ------------------------------- */

    suspend fun consumePurchases(list: List<Purchase>) = withContext(Dispatchers.IO) {
        billingService.consumePurchases(list)
    }

    /* ------------------------------- Acknowledgements ------------------------------- */
    suspend fun acknowledgePurchases(list: List<Purchase>) = withContext(Dispatchers.IO) {
        billingService.acknowledgePurchases(list)
    }
}