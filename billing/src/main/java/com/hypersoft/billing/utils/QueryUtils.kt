package com.hypersoft.billing.utils

import android.os.Build
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.hypersoft.billing.BillingManager.Companion.TAG
import com.hypersoft.billing.dataClasses.BestPlan
import com.hypersoft.billing.enums.ResultState
import com.hypersoft.billing.repository.BillingResponse
import com.hypersoft.billing.states.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @Author: SOHAIB AHMED
 * @Date: 23/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

internal class QueryUtils(private val billingClient: BillingClient) {

    /* ------------------------------- Query Product Details ------------------------------- */

    fun getPurchaseParams(userQueryList: List<Pair<String, String>>, productIdList: List<String>): List<QueryProductDetailsParams.Product> {
        return productIdList.mapNotNull { productId ->
            val productType = userQueryList.find { it.second == productId }
            productType?.let {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(it.first)
                    .build()
            }
        }
    }

    fun getProductParams(userQueryList: List<Pair<String, String>>): List<QueryProductDetailsParams.Product> {
        return userQueryList.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.second)
                .setProductType(it.first)
                .build()
        }
    }

    suspend fun queryProductDetailsAsync(params: List<QueryProductDetailsParams.Product>): List<ProductDetails> {
        if (billingClient.isReady.not()) {
            Result.setResultState(ResultState.CONNECTION_INVALID)
            return emptyList()
        }
        if (params.isEmpty()) {
            Result.setResultState(ResultState.USER_QUERY_LIST_EMPTY)
            return emptyList()
        }
        val queryParams = QueryProductDetailsParams.newBuilder().setProductList(params).build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
                if (BillingResponse(billingResult.responseCode).isOk) {
                    if (continuation.isActive) {
                        try {
                            continuation.resume(productDetailsList)
                        } catch (ignore: Exception) {
                        }
                    }
                } else {
                    Log.e(TAG, "queryProductDetailsAsync: Failed to query product details. Response code: ${billingResult.responseCode}")
                    if (continuation.isActive) {
                        try {
                            val list = ArrayList<ProductDetails>()
                            continuation.resume(list)
                        } catch (ignore: Exception) {
                        }
                    }
                }
            }
        }
    }

    /* ------------------------------- Fetch Plan Detail ------------------------------- */

    /**
     * @param subscriptionOfferDetailList: find reliable base plan id for a product
     *
     * @return base plan id for a product
     */

    fun getPlanId(subscriptionOfferDetailList: MutableList<ProductDetails.SubscriptionOfferDetails>?): String {
        return try {
            subscriptionOfferDetailList?.let { offerList ->
                val offer = offerList.find { it.basePlanId.isNotEmpty() }
                offer?.basePlanId ?: throw NullPointerException("SubscriptionOfferDetails list does not provide a valid planId")
            } ?: throw NullPointerException("SubscriptionOfferDetails list is empty")
        } catch (ex: Exception) {
            Log.e(TAG, "Exception (manual): returning empty planID -> ", ex)
            ""
        }
    }

    /**
     * @param subscriptionOfferDetailList: find reliable base plan id for a product
     *
     * @return base plan title according to billingPeriod
     * @see [com.hypersoft.billing.dataClasses.ProductDetail]
     */

    fun getPlanTitle(subscriptionOfferDetailList: ProductDetails.SubscriptionOfferDetails): String {
        val pricingPhase = getPricingOffer(subscriptionOfferDetailList)
        return when (pricingPhase?.billingPeriod) {
            "P1W" -> "Weekly"
            "P4W" -> "Four weeks"
            "P1M" -> "Monthly"
            "P2M" -> "2 months"
            "P3M" -> "3 months"
            "P4M" -> "4 months"
            "P6M" -> "6 months"
            "P8M" -> "8 months"
            "P1Y" -> "Yearly"
            else -> ""
        }
    }

    fun getPlanTitle(billingPeriod: String): String {
        return when (billingPeriod) {
            "P1W" -> "Weekly"
            "P4W" -> "Four weeks"
            "P1M" -> "Monthly"
            "P2M" -> "2 months"
            "P3M" -> "3 months"
            "P4M" -> "4 months"
            "P6M" -> "6 months"
            "P8M" -> "8 months"
            "P1Y" -> "Yearly"
            else -> ""
        }
    }

    fun getTrialDay(subscriptionOfferDetailList: ProductDetails.SubscriptionOfferDetails): Int {
        val pricingPhase = getPricingOffer(subscriptionOfferDetailList)
        return when (pricingPhase?.billingPeriod) {
            "P1D" -> 1
            "P2D" -> 2
            "P3D" -> 3
            "P4D" -> 4
            "P5D" -> 5
            "P6D" -> 6
            "P7D" -> 7
            "P1W" -> 7
            "P2W" -> 14
            "P3W" -> 21
            "P4W" -> 28
            "P1M" -> 30
            else -> 0
        }
    }

    /**
     *  - The first item in the List returned by my getSubscriptionOfferDetails() is the offer scheme,
     *  - The second item is the regular scheme without any offer.
     *
     * @param subscriptionOfferDetailList: find reliable offer for this base plan
     *
     * @return best plan pricing details
     */

    fun getBestPlan(subscriptionOfferDetailList: MutableList<ProductDetails.SubscriptionOfferDetails>): BestPlan {
        if (subscriptionOfferDetailList.isEmpty()) {
            return BestPlan(0, null)
        }
        if (subscriptionOfferDetailList.size == 1) {
            val leastPricingPhase = getPricingOffer(subscriptionOfferDetailList[0])
            return BestPlan(0, leastPricingPhase)
        }
        // Offers available
        var trialDays = 0
        val trialPricingPhase = getPricingOffer(subscriptionOfferDetailList[0])
        val regularPricingPhase = getPricingOffer(subscriptionOfferDetailList[1])

        if (trialPricingPhase?.priceAmountMicros == 0L) {
            trialDays = when (trialPricingPhase.billingPeriod) {
                "P1D" -> 1
                "P2D" -> 2
                "P3D" -> 3
                "P4D" -> 4
                "P5D" -> 5
                "P6D" -> 6
                "P7D" -> 7
                "P1W" -> 7
                "P2W" -> 14
                "P3W" -> 21
                "P4W" -> 28
                "P1M" -> 30
                else -> 0
            }
        }
        return BestPlan(trialDays, regularPricingPhase)
    }

    /**
     * Calculates the lowest priced offer amongst all eligible offers.
     * In this implementation the lowest price of all offers' pricing phases is returned.
     * It's possible the logic can be implemented differently.
     * For example, the lowest average price in terms of month could be returned instead.
     */

    private fun getPricingOffer(offer: ProductDetails.SubscriptionOfferDetails): ProductDetails.PricingPhase? {
        if (offer.pricingPhases.pricingPhaseList.size == 1) {
            return offer.pricingPhases.pricingPhaseList[0]
        }
        var leastPricingPhase: ProductDetails.PricingPhase? = null
        var lowestPrice = Int.MAX_VALUE
        offer.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
            if (pricingPhase.priceAmountMicros < lowestPrice) {
                lowestPrice = pricingPhase.priceAmountMicros.toInt()
                leastPricingPhase = pricingPhase
            }
        }
        return leastPricingPhase
    }

    /* ------------------------------- Purchase Subs ------------------------------- */

    fun getOfferToken(subscriptionOfferDetails: List<ProductDetails.SubscriptionOfferDetails>?, planId: String): String {
        val eligibleOffers = arrayListOf<ProductDetails.SubscriptionOfferDetails>()
        subscriptionOfferDetails?.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(planId)) {
                eligibleOffers.add(offerDetail)
            }
        }

        var offerToken = String()
        var leastPricedOffer: ProductDetails.SubscriptionOfferDetails
        var lowestPrice = Int.MAX_VALUE

        eligibleOffers.forEach { offer ->
            for (price in offer.pricingPhases.pricingPhaseList) {
                if (price.priceAmountMicros < lowestPrice) {
                    lowestPrice = price.priceAmountMicros.toInt()
                    leastPricedOffer = offer
                    offerToken = leastPricedOffer.offerToken
                }
            }
        }

        return offerToken
    }

    /* ------------------------------- Acknowledge purchases ------------------------------- */

    /**
     *  An acknowledgement must need to be made after payment within 3 days,
     *  otherwise user will get his/her cost back after 3 days.
     */

    fun checkForAcknowledgements(purchases: List<Purchase>) {
        val count = purchases.count { it.isAcknowledged.not() }
        Log.i(TAG, "checkForAcknowledgements: $count purchase(s) needs to be acknowledge")

        // Start acknowledging...
        purchases.forEach { purchase ->
            if (purchase.isAcknowledged.not()) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->
                    when (BillingResponse(billingResult.responseCode).isOk) {
                        true -> Log.d(TAG, "checkForAcknowledgements: Payment has been successfully acknowledged for these products: ${purchase.products}")
                        false -> Log.e(TAG, "checkForAcknowledgements: Payment has been failed to acknowledge for these products: ${purchase.products}")
                    }
                }
            }
        }
    }

    fun checkForAcknowledgements(purchases: List<Purchase>, consumableList: List<String>) {
        val count = purchases.count { it.isAcknowledged.not() }
        Log.i(TAG, "checkForAcknowledgements: $count purchase(s) needs to be acknowledge")

        // Start acknowledging...
        purchases.forEach { purchase ->
            if (purchase.isAcknowledged.not()) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->
                    consumeProduct(purchase, consumableList)
                    when (BillingResponse(billingResult.responseCode).isOk) {
                        true -> Log.d(TAG, "checkForAcknowledgements: Payment has been successfully acknowledged for these products: ${purchase.products}")
                        false -> Log.e(TAG, "checkForAcknowledgements: Payment has been failed to acknowledge for these products: ${purchase.products}")
                    }
                }
            } else {
                consumeProduct(purchase, consumableList)
            }
        }
    }

    private fun consumeProduct(purchase: Purchase, consumableList: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            var isConsumable = false
            purchase.products.forEach inner@{
                if (consumableList.contains(it)) {
                    isConsumable = true
                    return@inner
                }
            }
            if (isConsumable.not()) return@launch

            // Consume only consumable products given by developers
            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                Log.d(TAG, "queryPurchases: consumeProduct: Status Code: ${billingResult.responseCode} -- ${billingResult.debugMessage}")
            }
        }
    }

    fun checkForAcknowledgementsAndConsumable(purchases: List<Purchase>, callback: (Boolean) -> Unit) {
        val count = purchases.count { it.isAcknowledged.not() }
        Log.i(TAG, "checkForAcknowledgements: $count purchase(s) needs to be acknowledge")

        val hashMap = HashMap<Int, Boolean>()

        // Start acknowledging...
        purchases.forEachIndexed { index, purchase ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hashMap.putIfAbsent(index, false)
            } else {
                if (hashMap.containsKey(index).not()) {
                    hashMap[index] = false
                }
            }
            if (purchase.isAcknowledged.not()) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { billingResult ->
                    consumeProductPurchase(purchase, hashMap, index, callback)
                    when (BillingResponse(billingResult.responseCode).isOk) {
                        true -> Log.d(TAG, "checkForAcknowledgements: Payment has been successfully acknowledged for these products: ${purchase.products}")
                        false -> Log.e(TAG, "checkForAcknowledgements: Payment has been failed to acknowledge for these products: ${purchase.products}")
                    }
                }
            } else {
                consumeProductPurchase(purchase, hashMap, index, callback)
            }
        }
    }

    private fun consumeProductPurchase(purchase: Purchase, hashMap: HashMap<Int, Boolean>, index: Int, callback: (Boolean) -> Unit) {
        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            Log.d(TAG, "queryPurchases: consumeProductPurchase: consumed: Code: ${billingResult.responseCode} -- ${billingResult.debugMessage}")
            hashMap[index] = true
            hashMap.forEach {
                if (!it.value) {
                    return@consumeAsync
                }
            }
            callback.invoke(BillingResponse(billingResult.responseCode).isOk)
        }
    }
}