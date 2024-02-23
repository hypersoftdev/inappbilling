package com.hypersoft.billing.latest.repository

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.hypersoft.billing.latest.dataClasses.CompletePurchase
import com.hypersoft.billing.latest.dataClasses.PurchaseDetail
import com.hypersoft.billing.latest.enums.ProductType
import com.hypersoft.billing.latest.enums.ResultState
import com.hypersoft.billing.latest.extensions.toFormattedDate
import com.hypersoft.billing.latest.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @Author: SOHAIB AHMED
 * @Date: 21/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

internal class BillingRepository(context: Context) {

    private val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesListener)
            .enablePendingPurchases()
            .build()
    }

    private val billingUtility by lazy { BillingUtility(billingClient) }

    /**
     * @property _purchasesSharedFlow: Collect (observe) this, to get user's purchase list currently owns
     */
    private val _purchasesSharedFlow = MutableSharedFlow<List<PurchaseDetail>>()
    val purchasesSharedFlow = _purchasesSharedFlow.asSharedFlow()


    /**
     * @property _purchases: List of all purchases that this user has ever made and currently owns.
     */
    private val _purchases = arrayListOf<Purchase>()
    private val purchases: List<Purchase> get() = _purchases.toList()

    /**
     * @property _userQueryList: List of products that the user has requested or queried (key, value) pair.
     */
    private val _userQueryList: ArrayList<Pair<String, String>> = arrayListOf()
    private val userQueryList: List<Pair<String, String>> get() = _userQueryList.toList()


    /**
     *  Step 1: Billing Connection
     *   @see startConnection
     *   @see onConnectionResult
     *
     *  Step 2: Purchase History -> InApps / Subs
     *   @see fetchPurchases
     *   @see queryPurchases
     *   @see processPurchases
     *
     *  Step 3: Purchase History -> Subs
     *
     */

    /* ---------------------------------------- Billing Connection ---------------------------------------- */

    /**
     * In order to start working with Google play billing,
     * we need to initialize 'billingClient' and start connecting to server.
     *
     *  @see billingClient.isReady: Used to check, if billing has already been initialized or not
     *
     */
    fun startConnection(callback: (isSuccess: Boolean, message: String) -> Unit) {

        // Check if connection is already being establishing
        if (Result.getResultState() == ResultState.CONNECTION_ESTABLISHING) {
            Result.setResultState(ResultState.CONNECTION_ESTABLISHING_IN_PROGRESS)
            onConnectionResult(callback, false, ResultState.CONNECTION_ESTABLISHING_IN_PROGRESS.message)
            return
        }
        Result.setResultState(ResultState.CONNECTION_ESTABLISHING)

        if (billingClient.isReady) {
            Result.setResultState(ResultState.CONNECTION_ALREADY_ESTABLISHED)
            onConnectionResult(callback, true, ResultState.CONNECTION_ALREADY_ESTABLISHED.message)
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Result.setResultState(ResultState.CONNECTION_DISCONNECTED)
                    onConnectionResult(callback, isSuccess = false, message = ResultState.CONNECTION_DISCONNECTED.message)
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    when (BillingResponse(billingResult.responseCode).isOk) {
                        true -> Result.setResultState(ResultState.CONNECTION_ESTABLISHED)
                        false -> Result.setResultState(ResultState.CONNECTION_FAILED)
                    }
                    onConnectionResult(
                        callback = callback,
                        isSuccess = BillingResponse(billingResult.responseCode).isOk,
                        message = billingResult.debugMessage
                    )
                }
            })
        }
    }

    private fun onConnectionResult(callback: (isSuccess: Boolean, message: String) -> Unit, isSuccess: Boolean, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            callback.invoke(isSuccess, message)
        }
    }

    /**
     * Only active subscriptions and non-consumed one-time purchases are returned.
     * This method uses a cache of Google Play Store app without initiating a network request.
     */

    /* ---------------------------------------- Purchase History ---------------------------------------- */

    fun fetchPurchases(userInAppPurchases: List<String>, userSubsPurchases: List<String>) {
        // Clear lists
        _purchases.clear()
        _userQueryList.clear()

        // Save user queries
        userInAppPurchases.forEach { _userQueryList.add(Pair(BillingClient.ProductType.INAPP, it)) }
        userSubsPurchases.forEach { _userQueryList.add(Pair(BillingClient.ProductType.SUBS, it)) }

        // Determine product types to be fetched
        val hasInApp = userQueryList.any { it.first == BillingClient.ProductType.INAPP }
        val hasSubs = userQueryList.any { it.first == BillingClient.ProductType.SUBS }
        val hasBoth = userQueryList.any { it.first == BillingClient.ProductType.INAPP } &&
                userQueryList.any { it.first == BillingClient.ProductType.SUBS }

        // Query for product types to be fetched
        when {
            hasBoth -> queryPurchases(BillingClient.ProductType.INAPP, true)
            hasInApp -> queryPurchases(BillingClient.ProductType.INAPP, false)
            hasSubs -> queryPurchases(BillingClient.ProductType.SUBS, false)
            else -> processPurchases()
        }
    }

    private fun queryPurchases(productType: String, hasBoth: Boolean) {
        when (productType) {
            BillingClient.ProductType.INAPP -> Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_INAPP_FETCHING)
            BillingClient.ProductType.SUBS -> Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_SUB_FETCHING)
        }

        billingClient
            .queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(productType).build())
            { billingResult, purchases ->
                if (BillingResponse(billingResult.responseCode).isOk) {
                    _purchases.addAll(purchases)
                    when (productType) {
                        BillingClient.ProductType.INAPP -> Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_INAPP_FETCHING_SUCCESS)
                        BillingClient.ProductType.SUBS -> Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_SUB_FETCHING_SUCCESS)
                    }
                } else {
                    when (productType) {
                        BillingClient.ProductType.INAPP -> Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_INAPP_FETCHING_FAILED)
                        BillingClient.ProductType.SUBS -> Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_SUB_FETCHING_FAILED)
                    }
                }

                if (productType == BillingClient.ProductType.INAPP && hasBoth) {
                    queryPurchases(BillingClient.ProductType.SUBS, false)
                    return@queryPurchasesAsync
                }
                processPurchases()
            }
    }

    /**
     * Scenarios where a purchase might have 0 products:
     *  ->  Refunded purchase
     *  ->  Subscription Cancel
     *  ->  Promo code (discount to zero purchasing cost)
     */

    private fun processPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_RESPONSE_PROCESSING)
            val resultList = ArrayList<PurchaseDetail>()

            val completePurchaseList = purchases.map { purchase ->
                val productParams = billingUtility.getParams(userQueryList, purchase.products)
                val productDetailsList = billingUtility.queryProductDetailsAsync(productParams)
                CompletePurchase(purchase, productDetailsList)
            }

            completePurchaseList.forEach { completePurchase ->
                completePurchase.productDetailList.forEach { productDetails ->
                    val productType = if (productDetails.productType == ProductType.INAPP.toString()) ProductType.INAPP else ProductType.SUBS
                    val planId = billingUtility.getPlanId(productDetails.subscriptionOfferDetails)

                    val purchaseDetail = PurchaseDetail(
                        productId = productDetails.productId,
                        planId = planId,
                        productTitle = productDetails.title,
                        productType = productType,
                        purchaseTime = completePurchase.purchase.purchaseTime.toFormattedDate(),
                        purchaseTimeMillis = completePurchase.purchase.purchaseTime,
                        isAutoRenewing = completePurchase.purchase.isAutoRenewing
                    )
                    resultList.add(purchaseDetail)
                }
            }
            Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_RESPONSE_COMPLETE)
            Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_CHECKED_FOR_ACKNOWLEDGEMENT)

            billingUtility.checkForAcknowledgements(purchases)
            _purchasesSharedFlow.emit(resultList)
        }
    }


    private val purchasesListener = PurchasesUpdatedListener { billingResult, purchasesList ->

    }

    /**
     * End billing and release memory
     */

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}


@JvmInline
value class BillingResponse(private val code: Int) {
    val isOk: Boolean
        get() = code == BillingClient.BillingResponseCode.OK

    val canFailGracefully: Boolean
        get() = code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED

    val isRecoverableError: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
        )

    val isNonrecoverableError: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
        )

    val isTerribleFailure: Boolean
        get() = code in setOf(
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED,
            BillingClient.BillingResponseCode.USER_CANCELED,
        )
}