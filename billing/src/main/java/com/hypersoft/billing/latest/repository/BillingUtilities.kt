package com.hypersoft.billing.latest.repository

import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.hypersoft.billing.latest.enums.ResultState
import com.hypersoft.billing.latest.utils.Result
import com.hypersoft.billing.oldest.helper.BillingHelper.Companion.TAG
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @Author: SOHAIB AHMED
 * @Date: 23/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

class BillingUtility(private val billingClient: BillingClient) {

    /* ------------------------------- Query Product Details ------------------------------- */

    suspend fun queryProductDetailsAsync(params: List<QueryProductDetailsParams.Product>): List<ProductDetails> {
        if (billingClient.isReady.not()) {
            Result.setResultState(ResultState.CONNECTION_INVALID)
            return emptyList()
        }
        val queryParams = QueryProductDetailsParams.newBuilder().setProductList(params).build()
        return suspendCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetailsList ->
                if (BillingResponse(billingResult.responseCode).isOk) {
                    continuation.resume(productDetailsList)
                } else {
                    Log.e(TAG, "queryProductDetailsAsync: Failed to query product details. Response code: ${billingResult.responseCode}")
                    continuation.resume(emptyList())
                }
            }
        }
    }

    fun getParams(userQueryList: List<Pair<String, String>>, productIdList: List<String>): List<QueryProductDetailsParams.Product> {
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
}

