package com.hypersoft.billing.asd

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.hypersoft.billing.asd.data.dataSource.BillingService
import com.hypersoft.billing.asd.data.repository.BillingRepository
import com.hypersoft.billing.asd.domain.UseCaseAcknowledgePurchase
import com.hypersoft.billing.asd.domain.UseCaseConnection
import com.hypersoft.billing.asd.domain.UseCaseQueryProducts
import com.hypersoft.billing.asd.domain.UseCaseQueryPurchases
import com.hypersoft.billing.asd.extensions.setAll
import com.hypersoft.billing.asd.interfaces.BillingConnectionListener
import com.hypersoft.billing.asd.interfaces.BillingProductDetailsListener
import com.hypersoft.billing.asd.interfaces.BillingPurchaseHistoryListener
import com.hypersoft.billing.asd.states.QueryResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Created by: Sohaib Ahmed
 * Date: 7/3/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

/**
 *  @param context: Can be of application class
 *  @param scope: Pass in a lifecycleScope / viewModelScope if you want the manager to follow an Android lifecycle.
 *              Otherwise the default internal SupervisorJob + Main dispatcher is used.
 */

class BillingManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {

    /* ▾ lazy‑built Play client ---------------------------------------------------- */
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases: List<Purchase>? ->

    }

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    /* ───────── Clean‑Arch stack ────────────────────────────────────────── */
    private val billingService by lazy { BillingService(billingClient) }
    private val billingRepository by lazy { BillingRepository(billingService) }
    private val useCaseConnection by lazy { UseCaseConnection(billingRepository) }
    private val useCaseQueryPurchases by lazy { UseCaseQueryPurchases(billingRepository) }
    private val useCaseQueryProducts by lazy { UseCaseQueryProducts(billingRepository) }
    private val useCasePurchase by lazy { UseCaseQueryProducts(billingRepository) }
    private val useCaseAcknowledgePurchase by lazy { UseCaseAcknowledgePurchase(billingRepository) }

    /* ───────── Config & listener ───────────────────────────────────────── */
    private val _nonConsumables = mutableListOf<String>()
    private val _consumables = mutableListOf<String>()
    private val _subscriptions = mutableListOf<String>()
    private var connectionListener: BillingConnectionListener? = null

    val nonConsumableIds: List<String> get() = _nonConsumables
    val consumableIds: List<String> get() = _consumables
    val subscriptionIds: List<String> get() = _subscriptions

    fun setNonConsumables(ids: List<String>) = apply { _nonConsumables.setAll(ids) }
    fun setConsumables(ids: List<String>) = apply { _consumables.setAll(ids) }
    fun setSubscriptions(ids: List<String>) = apply { _subscriptions.setAll(ids) }
    fun setListener(listener: BillingConnectionListener?) = apply { connectionListener = listener }

    /* ───────── Public API ──────────────────────────────────────────────── */
    fun startConnection() {
        useCaseConnection.startConnection { isSuccess, message ->
            connectionListener?.onBillingClientConnected(isSuccess, message ?: billingService.currentState.message)
        }
    }

    fun fetchPurchaseHistory(listener: BillingPurchaseHistoryListener) {
        scope.launch {
            when (val response = useCaseQueryPurchases.queryPurchases()) {
                is QueryResponse.Loading -> {}
                is QueryResponse.Success -> listener.onSuccess(response.data)
                is QueryResponse.Error -> listener.onError(response.errorMessage)
            }
        }
    }

    fun fetchProductDetails(listener: BillingProductDetailsListener) {
        scope.launch {
            when (val response = useCaseQueryProducts.queryProducts(nonConsumableIds, consumableIds, subscriptionIds)) {
                is QueryResponse.Loading -> {}
                is QueryResponse.Success -> listener.onSuccess(response.data)
                is QueryResponse.Error -> listener.onError(response.errorMessage)
            }
        }
    }

    /**
     * Returns a single product (optionally scoped by plan) from the in‑memory cache.
     *
     * If a bulk fetch is running, this call suspends until that fetch
     *  completes, ensuring you never read a half‑built list.
     */
    fun fetchProductDetail(productId: String, planId: String, listener: BillingProductDetailsListener) {
        scope.launch {
            when (val response = useCaseQueryProducts.queryProducts(productId, planId)) {
                is QueryResponse.Loading -> {}
                is QueryResponse.Success -> listener.onSuccess(response.data)
                is QueryResponse.Error -> listener.onError(response.errorMessage)
            }
        }
    }

    companion object {
        const val TAG = "BillingManager"
    }
}