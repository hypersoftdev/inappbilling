package com.hypersoft.billing.internal.purchases

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import com.hypersoft.billing.internal.BillingResponseCode
import com.hypersoft.billing.internal.mapper.PurchaseMapper.toPurchaseDetails
import com.hypersoft.billing.internal.products.ProductRepository
import com.hypersoft.billing.model.BillingException
import com.hypersoft.billing.model.ProductType
import com.hypersoft.billing.model.PurchaseDetail
import com.hypersoft.billing.model.PurchaseOutcome
import com.hypersoft.billing.utilities.Constants.TAG
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/**
 * Launches purchase/subscription-update flows and bridges Play's callback-based
 * `PurchasesUpdatedListener` into a suspend result via [onPurchasesUpdated].
 */
internal class PurchaseHandler(
    private val billingClient: BillingClient,
    private val productRepository: ProductRepository,
    private val consumableIds: () -> List<String>,
) {

    private val inApp = BillingClient.ProductType.INAPP
    private val subs = BillingClient.ProductType.SUBS

    /** Only one billing flow may be in-flight at a time. */
    private val purchaseMutex = Mutex()
    private var pendingResult: CompletableDeferred<PurchaseOutcome>? = null

    private val _purchaseUpdates = MutableSharedFlow<PurchaseOutcome>(extraBufferCapacity = 1)
    val purchaseUpdates: SharedFlow<PurchaseOutcome> = _purchaseUpdates.asSharedFlow()

    suspend fun purchaseInApp(activity: Activity, productId: String): PurchaseOutcome {
        if (productId.isEmpty()) return PurchaseOutcome.Failed("Product Id can't be empty", BillingClient.BillingResponseCode.DEVELOPER_ERROR)
        if (!billingClient.isReady) return PurchaseOutcome.Failed("Play Billing not ready. Try again.", BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)

        val details = productRepository.queryInAppProductDetails(listOf(productId)).firstOrNull()
            ?: return PurchaseOutcome.Failed("No in-app product found for id: $productId", BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)

        val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build())
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()
        return launchAndAwait(activity, params)
    }

    suspend fun purchaseSubs(activity: Activity, productId: String, planId: String, offerId: String? = null): PurchaseOutcome {
        if (productId.isEmpty()) return PurchaseOutcome.Failed("Product Id can't be empty", BillingClient.BillingResponseCode.DEVELOPER_ERROR)
        if (!billingClient.isReady) return PurchaseOutcome.Failed("Play Billing not ready. Try again.", BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)

        val details = productRepository.querySubsProductDetails(listOf(productId)).firstOrNull()
            ?: return PurchaseOutcome.Failed("No subscription product found for id: $productId", BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
        val offerToken = details.findOfferToken(planId, offerId)
            ?: return PurchaseOutcome.Failed("No offer found for planId: $planId" + (offerId?.let { ", offerId: $it" }.orEmpty()), BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)

        val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(offerToken).build())
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()
        return launchAndAwait(activity, params)
    }

    suspend fun updateSubs(activity: Activity, oldProductId: String, productId: String, planId: String, offerId: String? = null): PurchaseOutcome {
        if (oldProductId.isEmpty()) return PurchaseOutcome.Failed("Old Product Id can't be empty", BillingClient.BillingResponseCode.DEVELOPER_ERROR)
        if (productId.isEmpty()) return PurchaseOutcome.Failed("Product Id can't be empty", BillingClient.BillingResponseCode.DEVELOPER_ERROR)
        if (!billingClient.isReady) return PurchaseOutcome.Failed("Play Billing not ready. Try again.", BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)

        val details = productRepository.querySubsProductDetails(listOf(productId)).firstOrNull()
            ?: return PurchaseOutcome.Failed("No subscription product found for id: $productId", BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
        val oldPurchase = queryRawSubsPurchases().find { it.products.contains(oldProductId) }
            ?: return PurchaseOutcome.Failed("Old subscription purchase not found for id: $oldProductId", BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)
        val offerToken = details.findOfferToken(planId, offerId)
            ?: return PurchaseOutcome.Failed("No offer found for planId: $planId" + (offerId?.let { ", offerId: $it" }.orEmpty()), BillingClient.BillingResponseCode.ITEM_UNAVAILABLE)

        val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(offerToken).build())
        val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
            .setOldPurchaseToken(oldPurchase.purchaseToken)
            .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setSubscriptionUpdateParams(updateParams)
            .build()
        return launchAndAwait(activity, params)
    }

    suspend fun queryPurchaseHistory(): Result<List<PurchaseDetail>> {
        if (!billingClient.isReady) return Result.failure(BillingException(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED, "Play Billing not ready."))

        return runCatching {
            coroutineScope {
                val inAppAsync = async { queryRawInAppPurchases() }
                val subsAsync = async { queryRawSubsPurchases() }
                val inAppPurchases = inAppAsync.await()
                val subsPurchases = subsAsync.await()

                val inAppIds = inAppPurchases.flatMap { it.products }
                val subsIds = subsPurchases.flatMap { it.products }

                val inAppPdDeferred = inAppIds.takeIf { it.isNotEmpty() }?.let { async { productRepository.queryInAppProductDetails(it) } }
                val subsPdDeferred = subsIds.takeIf { it.isNotEmpty() }?.let { async { productRepository.querySubsProductDetails(it) } }
                val inAppPd = inAppPdDeferred?.await().orEmpty()
                val subsPd = subsPdDeferred?.await().orEmpty()

                acknowledgeUnacknowledged(inAppPurchases + subsPurchases)

                inAppPurchases.flatMap { it.toPurchaseDetails(inAppPd, ProductType.inapp) } +
                        subsPurchases.flatMap { it.toPurchaseDetails(subsPd, ProductType.subs) }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                Log.e(TAG, "PurchaseHandler: queryPurchaseHistory: ", it)
                Result.failure(BillingException(BillingClient.BillingResponseCode.ERROR, it.localizedMessage ?: "Unknown error"))
            }
        )
    }

    /** Called by [com.hypersoft.billing.BillingManager]'s `PurchasesUpdatedListener`. Returns the resolved outcome so the caller can decide whether owned purchases changed. */
    suspend fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?): PurchaseOutcome {
        val response = BillingResponseCode(billingResult.responseCode)
        val outcome: PurchaseOutcome = when {
            response.isOk -> {
                handlePurchases(purchases)
                PurchaseOutcome.Success("Transaction completed successfully")
            }

            response.isAlreadyOwned -> {
                handlePurchases(purchases)
                PurchaseOutcome.AlreadyOwned
            }

            response.isUserCancelled -> PurchaseOutcome.UserCancelled
            else -> PurchaseOutcome.Failed(billingResult.debugMessage, billingResult.responseCode)
        }

        val waiter = pendingResult
        pendingResult = null
        if (waiter != null) waiter.complete(outcome) else _purchaseUpdates.emit(outcome)
        return outcome
    }

    private suspend fun launchAndAwait(activity: Activity, params: BillingFlowParams): PurchaseOutcome = purchaseMutex.withLock {
        val deferred = CompletableDeferred<PurchaseOutcome>()
        pendingResult = deferred

        val launchResult = billingClient.launchBillingFlow(activity, params)
        if (!BillingResponseCode(launchResult.responseCode).isOk) {
            pendingResult = null
            return@withLock PurchaseOutcome.Failed(launchResult.debugMessage, launchResult.responseCode)
        }
        deferred.await()
    }

    /**
     * @param offerId When null, prefers the plan's default offer (Play's `offerId == null`),
     *   falling back to whichever offer is available. When non-null, matches that specific offer exactly.
     */
    private fun ProductDetails.findOfferToken(planId: String, offerId: String?): String? {
        val offers = subscriptionOfferDetails?.filter { it.basePlanId == planId } ?: return null
        return when (offerId) {
            null -> offers.firstOrNull { it.offerId == null } ?: offers.firstOrNull()
            else -> offers.firstOrNull { it.offerId == offerId }
        }?.offerToken
    }

    private suspend fun queryRawInAppPurchases(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder().setProductType(inApp).build()
        return billingClient.queryPurchasesAsync(params).purchasesList
    }

    private suspend fun queryRawSubsPurchases(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder().setProductType(subs).build()
        return billingClient.queryPurchasesAsync(params).purchasesList
    }

    private fun handlePurchases(purchases: List<Purchase>?) {
        if (purchases.isNullOrEmpty()) return
        val ids = consumableIds()
        acknowledgeUnacknowledged(purchases.filter { purchase -> purchase.products.none(ids::contains) })
        consumeConsumables(purchases.filter { purchase -> purchase.products.any(ids::contains) })
    }

    private fun acknowledgeUnacknowledged(purchases: List<Purchase>) {
        val unacknowledged = purchases.filter { it.isAcknowledged.not() }
        if (unacknowledged.isEmpty()) return
        Log.i(TAG, "PurchaseHandler: acknowledging ${unacknowledged.size} purchase(s)")
        unacknowledged.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.acknowledgePurchase(params) { result ->
                when (BillingResponseCode(result.responseCode).isOk) {
                    true -> Log.d(TAG, "PurchaseHandler: acknowledged: ${purchase.products}")
                    false -> Log.e(TAG, "PurchaseHandler: failed to acknowledge: ${purchase.products}")
                }
            }
        }
    }

    private fun consumeConsumables(purchases: List<Purchase>) {
        if (purchases.isEmpty()) return
        Log.i(TAG, "PurchaseHandler: consuming ${purchases.size} purchase(s)")
        purchases.forEach { purchase ->
            val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.consumeAsync(params) { result, _ ->
                when (BillingResponseCode(result.responseCode).isOk) {
                    true -> Log.d(TAG, "PurchaseHandler: consumed: ${purchase.products}")
                    false -> Log.e(TAG, "PurchaseHandler: failed to consume: ${purchase.products}")
                }
            }
        }
    }
}