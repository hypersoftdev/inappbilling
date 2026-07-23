package com.hypersoft.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.hypersoft.billing.internal.cache.ProductCache
import com.hypersoft.billing.internal.connection.BillingConnector
import com.hypersoft.billing.internal.products.ProductRepository
import com.hypersoft.billing.internal.purchases.PurchaseHandler
import com.hypersoft.billing.model.BillingException
import com.hypersoft.billing.model.ConnectionState
import com.hypersoft.billing.model.ProductDetail
import com.hypersoft.billing.model.PurchaseDetail
import com.hypersoft.billing.model.PurchaseOutcome
import com.hypersoft.billing.model.UiState
import com.hypersoft.billing.utilities.Constants.TAG
import com.hypersoft.billing.utilities.setAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Created by: Sohaib Ahmed
 * Date: 7/3/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/**
 * Single entry point for Google Play Billing. Intended to be created **once**, as a
 * singleton scoped to your `Application` (e.g. wired through your DI graph or a lazy
 * property on your `Application` subclass), not per-`Activity`/`ViewModel`. Play's purchase
 * callback can fire at any time — including after the screen that started a purchase is
 * gone — so this class owns its own coroutine scope internally rather than accepting one
 * from the caller. Call [close] when you're done with it (typically never, for an
 * app-lifetime singleton).
 *
 *  @param context: Any [Context]; the application context is retained internally.
 *
 * Typical integration:
 * ```
 * // Application.onCreate()
 * billingManager
 *     .setNonConsumables(...)
 *     .setConsumables(...)
 *     .setSubscriptions(...)
 * appScope.launch { billingManager.connect() }
 *
 * // Any screen, any time after connect() succeeds
 * billingManager.purchasesState.collect { state -> /* check premium status */ }
 * billingManager.productsState.collect { state -> /* render paywall prices */ }
 * ```
 */
class BillingManager(context: Context) {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val appContext = context.applicationContext

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases: List<Purchase>? ->
        Log.d(TAG, "BillingManager: purchasesUpdatedListener: ${billingResult.responseCode} -- ${billingResult.debugMessage}")
        scope.launch {
            val outcome = purchaseHandler.onPurchasesUpdated(billingResult, purchases)
            if (outcome is PurchaseOutcome.Success || outcome is PurchaseOutcome.AlreadyOwned) {
                refreshPurchases()
            }
        }
    }

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()
    }

    private val billingConnector: BillingConnector by lazy { BillingConnector(billingClient) }
    private val productCache: ProductCache by lazy { ProductCache() }
    private val productRepository: ProductRepository by lazy { ProductRepository(billingClient, productCache) }
    private val purchaseHandler: PurchaseHandler by lazy { PurchaseHandler(billingClient, productRepository) { consumableIds } }

    /* ───────── Config ──────────────────────────────────────────────────── */
    private val _nonConsumables = mutableListOf<String>()
    private val _consumables = mutableListOf<String>()
    private val _subscriptions = mutableListOf<String>()

    val nonConsumableIds: List<String> get() = _nonConsumables
    val consumableIds: List<String> get() = _consumables
    val subscriptionIds: List<String> get() = _subscriptions

    fun setNonConsumables(ids: List<String>) = apply { _nonConsumables.setAll(ids) }
    fun setConsumables(ids: List<String>) = apply { _consumables.setAll(ids) }
    fun setSubscriptions(ids: List<String>) = apply { _subscriptions.setAll(ids) }

    /* ───────── Observable state ──────────────────────────────────────────── */
    val connectionState: StateFlow<ConnectionState> get() = billingConnector.connectionState

    /** Purchases that settle outside a direct `purchaseXxx()` call (e.g. resumed after app restart). */
    val purchaseUpdates: SharedFlow<PurchaseOutcome> get() = purchaseHandler.purchaseUpdates

    private val _productsState = MutableStateFlow<UiState<List<ProductDetail>>>(UiState.Loading)

    /** Auto-refreshed after every successful [connect]; also updated by [refreshProducts]. */
    val productsState: StateFlow<UiState<List<ProductDetail>>> = _productsState.asStateFlow()

    private val _purchasesState = MutableStateFlow<UiState<List<PurchaseDetail>>>(UiState.Loading)

    /** Auto-refreshed after every successful [connect] and after every purchase settles; also updated by [refreshPurchases]. */
    val purchasesState: StateFlow<UiState<List<PurchaseDetail>>> = _purchasesState.asStateFlow()

    /* ───────── Public API ──────────────────────────────────────────────── */

    /** Connects to Play Billing. On success, [productsState] and [purchasesState] refresh automatically in the background. */
    suspend fun connect(): ConnectionState {
        val state = billingConnector.connect()
        if (state == ConnectionState.CONNECTED) {
            scope.launch { refreshProducts() }
            scope.launch { refreshPurchases() }
        }
        return state
    }

    /** Re-queries [nonConsumableIds]/[consumableIds]/[subscriptionIds] and republishes [productsState]. */
    suspend fun refreshProducts(): Result<List<ProductDetail>> {
        _productsState.value = UiState.Loading

        val result = productRepository.queryAll(nonConsumableIds, consumableIds, subscriptionIds)
        _productsState.value = result.toUiState()
        return result
    }

    /** Re-queries owned purchases and republishes [purchasesState]. */
    suspend fun refreshPurchases(): Result<List<PurchaseDetail>> {
        _purchasesState.value = UiState.Loading
        val result = purchaseHandler.queryPurchaseHistory()
        _purchasesState.value = result.toUiState()
        return result
    }

    /**
     * Returns a single product from the in-memory cache populated by [refreshProducts]/[connect].
     * @param offerId Disambiguates between multiple concurrent offers on the same [planId] (e.g. a
     *   default offer vs. a time-limited discount or free-trial offer). Null returns the plan's
     *   default offer if present, otherwise whichever offer is available.
     */
    suspend fun getProductDetail(productId: String, planId: String? = null, offerId: String? = null): Result<ProductDetail> = productRepository.find(productId, planId, offerId)

    /** @param offerId See [getProductDetail]. */
    suspend fun purchaseInApp(activity: Activity, productId: String, offerId: String? = null): PurchaseOutcome = purchaseHandler.purchaseInApp(activity, productId, offerId)

    /** @param offerId See [getProductDetail]. */
    suspend fun purchaseSubs(activity: Activity, productId: String, planId: String, offerId: String? = null): PurchaseOutcome = purchaseHandler.purchaseSubs(activity, productId, planId, offerId)

    /** @param offerId See [getProductDetail]. */
    suspend fun updateSubs(activity: Activity, oldProductId: String, productId: String, planId: String, offerId: String? = null): PurchaseOutcome = purchaseHandler.updateSubs(activity, oldProductId, productId, planId, offerId)

    /** Tears down the billing connection and this manager's internal coroutine scope. Call when your app no longer needs billing (rarely, for an app-lifetime singleton). */
    fun close() {
        scope.cancel()
        billingClient.endConnection()
    }

    private fun <T> Result<T>.toUiState(): UiState<T> = fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { UiState.Error(it.message ?: "Unknown error", (it as? BillingException)?.responseCode) }
    )
}
