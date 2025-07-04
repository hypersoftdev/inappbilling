package com.hypersoft.billing.asd.data.repository

import com.hypersoft.billing.asd.data.dataSource.BillingService
import com.hypersoft.billing.asd.states.BillingState
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

    suspend fun queryInAppPurchases() = withContext(Dispatchers.IO) {
        billingService.queryInAppPurchases()
    }

    suspend fun querySubsPurchases() = withContext(Dispatchers.IO) {
        billingService.querySubsPurchases()
    }

    suspend fun queryInAppProductDetails(productIds: List<String>) = withContext(Dispatchers.IO) {
        billingService.queryInAppProductDetails(productIds)
    }

    suspend fun querySubsProductDetails(productIds: List<String>) = withContext(Dispatchers.IO) {
        billingService.querySubsProductDetails(productIds)
    }
}