package com.hypersoft.billing.latest.repository

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.hypersoft.billing.latest.dataClasses.CompletePurchase
import com.hypersoft.billing.latest.dataClasses.ProductDetail
import com.hypersoft.billing.latest.dataClasses.PurchaseDetail
import com.hypersoft.billing.latest.dataClasses.QueryProductDetail
import com.hypersoft.billing.latest.enums.ProductType
import com.hypersoft.billing.latest.enums.ResultState
import com.hypersoft.billing.latest.extensions.toFormattedDate
import com.hypersoft.billing.latest.utils.Result
import com.hypersoft.billing.oldest.interfaces.OnPurchaseListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

    private val queryUtils: QueryUtils by lazy { QueryUtils(billingClient) }
    private val validationUtils: ValidationUtils by lazy { ValidationUtils(billingClient) }

    /**
     * @property _purchasesSharedFlow: Collect (observe) this, to get user's purchase list currently owns
     */
    private val _purchasesSharedFlow = MutableSharedFlow<List<PurchaseDetail>>()
    val purchasesSharedFlow = _purchasesSharedFlow.asSharedFlow()


    private val _productDetailsLiveData = MutableLiveData<List<ProductDetail>>()
    val productDetailsLiveData: LiveData<List<ProductDetail>> = _productDetailsLiveData

    /**
     * @property _userQueryList: List of products that the user has requested or queried (key, value) pair.
     */
    private val _userQueryList: ArrayList<Pair<String, String>> = arrayListOf()
    private val userQueryList: List<Pair<String, String>> get() = _userQueryList.toList()

    /**
     * @property _purchases: List of all purchases that this user has ever made and currently owns.
     */
    private val _purchases = arrayListOf<Purchase>()
    private val purchases: List<Purchase> get() = _purchases.toList()

    /**
     * @property _storeProductDetailsList: List of product details from server
     */
    private val _storeProductDetailsList: ArrayList<QueryProductDetail> = arrayListOf()
    private val storeProductDetailsList: List<QueryProductDetail> get() = _storeProductDetailsList.toList()

    private val job = Job()

    // Listeners
    private var onPurchaseListener: OnPurchaseListener? = null


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

    /* ---------------------------------------------------------------------------------------------------- */
    /* ---------------------------------------- Billing Connection ---------------------------------------- */
    /* ---------------------------------------------------------------------------------------------------- */

    /**
     * In order to start working with Google play billing,
     * we need to initialize 'billingClient' and start connecting to server.
     *
     *  isReady: Used to check, if billing has already been initialized or not
     *  @see [BillingClient.isReady]
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


    /* -------------------------------------------------------------------------------------------------- */
    /* ---------------------------------------- Purchase History ---------------------------------------- */
    /* -------------------------------------------------------------------------------------------------- */

    fun setUserQueries(userInAppPurchases: List<String>, userSubsPurchases: List<String>) {
        _userQueryList.clear()

        // Save user queries
        userInAppPurchases.forEach { _userQueryList.add(Pair(BillingClient.ProductType.INAPP, it)) }
        userSubsPurchases.forEach { _userQueryList.add(Pair(BillingClient.ProductType.SUBS, it)) }
    }

    /* -------------------------------------------------------------------------------------------------- */
    /* ---------------------------------------- Purchase History ---------------------------------------- */
    /* -------------------------------------------------------------------------------------------------- */

    /**
     * Only active subscriptions and non-consumed one-time purchases are returned.
     * This method uses a cache of Google Play Store app without initiating a network request.
     */

    fun fetchPurchases() {
        // Clear lists
        _purchases.clear()

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
        CoroutineScope(Dispatchers.IO + job).launch {
            Result.setResultState(ResultState.CONSOLE_PURCHASE_PRODUCTS_RESPONSE_PROCESSING)
            val resultList = ArrayList<PurchaseDetail>()

            val completePurchaseList = purchases.map { purchase ->
                val productParams = queryUtils.getPurchaseParams(userQueryList, purchase.products)
                val productDetailsList = queryUtils.queryProductDetailsAsync(productParams)
                CompletePurchase(purchase, productDetailsList)
            }

            completePurchaseList.forEach { completePurchase ->
                completePurchase.productDetailList.forEach { productDetails ->
                    val productType = if (productDetails.productType == ProductType.INAPP.toString()) ProductType.INAPP else ProductType.SUBS
                    val planId = queryUtils.getPlanId(productDetails.subscriptionOfferDetails)

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

            queryUtils.checkForAcknowledgements(purchases)

            withContext(Dispatchers.Main) {
                _purchasesSharedFlow.emit(resultList)
            }
        }
    }

    /* ---------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------ Query Products ------------------------------------------ */
    /* ---------------------------------------------------------------------------------------------------- */

    fun fetchStoreProducts() {
        CoroutineScope(Dispatchers.IO + job).launch {
            Result.setResultState(ResultState.CONSOLE_QUERY_PRODUCTS_FETCHING)

            val productDetailList = arrayListOf<ProductDetail>()
            val queryProductDetail = arrayListOf<QueryProductDetail>()

            val productParams = queryUtils.getProductParams(userQueryList)
            val productDetailsList = queryUtils.queryProductDetailsAsync(productParams)

            productDetailsList.forEach { productDetails ->
                when (productDetails.productType) {
                    ProductType.INAPP.toString() -> {
                        val productDetail = ProductDetail(
                            productId = productDetails.productId,
                            planId = "",
                            productTitle = productDetails.title,
                            planTitle = "",
                            productType = ProductType.INAPP,
                            currencyCode = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode.toString(),
                            price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice.toString().removeSuffix(".00"),
                            priceAmountMicros = productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L,
                            billingPeriod = ""
                        )
                        productDetailList.add(productDetail)
                        queryProductDetail.add(QueryProductDetail(productDetail, productDetails))
                    }

                    ProductType.SUBS.toString() -> {
                        productDetails.subscriptionOfferDetails?.let { offersList ->
                            offersList.forEach { offer ->       // Weekly, Monthly, etc
                                val planTitle = queryUtils.getPlanTitle(offer)
                                val pricingPhase = queryUtils.getPricingOffer(offer)
                                pricingPhase?.let {
                                    val productDetail = ProductDetail(
                                        productId = productDetails.productId,
                                        planId = offer.basePlanId,
                                        productTitle = productDetails.title,
                                        planTitle = planTitle,
                                        productType = ProductType.SUBS,
                                        currencyCode = pricingPhase.priceCurrencyCode,
                                        price = pricingPhase.formattedPrice,
                                        priceAmountMicros = pricingPhase.priceAmountMicros,
                                        billingPeriod = pricingPhase.billingPeriod,
                                    )
                                    productDetailList.add(productDetail)
                                    queryProductDetail.add(QueryProductDetail(productDetail, productDetails))
                                }
                            }
                        }
                    }
                }
            }
            Result.setResultState(ResultState.CONSOLE_QUERY_PRODUCTS_COMPLETED)

            _storeProductDetailsList.clear()
            _storeProductDetailsList.addAll(queryProductDetail)
            _productDetailsLiveData.postValue(productDetailList)
        }
    }

    /* ---------------------------------------------------------------------------------------------------- */
    /* ---------------------------------------- Product Purchases ----------------------------------------- */
    /* ---------------------------------------------------------------------------------------------------- */

    fun purchaseInApp(activity: Activity?, productId: String, onPurchaseListener: OnPurchaseListener) {
        this.onPurchaseListener = onPurchaseListener

        val errorMessage = validationUtils.checkForInApp(activity, productId)

        if (errorMessage != null) {
            onPurchaseListener.onPurchaseResult(false, message = errorMessage)
            return
        }

        val productDetail = storeProductDetailsList.find {
            it.productDetail.productId == productId
                    && it.productDetail.productType == ProductType.INAPP
        }
        productDetail?.let {
            launchFlow(activity = activity!!, it.productDetails)
        } ?: run {
            Result.setResultState(ResultState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST)
            onPurchaseListener.onPurchaseResult(false, message = ResultState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST.message)
        }
    }

    fun purchaseSubs(activity: Activity?, productId: String, planId: String, onPurchaseListener: OnPurchaseListener) {
        this.onPurchaseListener = onPurchaseListener

        val errorMessage = validationUtils.checkForSubs(activity, productId)

        if (errorMessage != null) {
            onPurchaseListener.onPurchaseResult(false, message = errorMessage)
            return
        }

        val productDetail = storeProductDetailsList.find {
            it.productDetail.productId == productId
                    && it.productDetail.planId == planId
                    && it.productDetail.productType == ProductType.SUBS
        }
        productDetail?.let {
            launchFlow(activity = activity!!, it.productDetails)
        } ?: run {
            Result.setResultState(ResultState.CONSOLE_PRODUCTS_SUB_NOT_EXIST)
            onPurchaseListener.onPurchaseResult(false, message = ResultState.CONSOLE_PRODUCTS_SUB_NOT_EXIST.message)
        }
    }

    private fun launchFlow(activity: Activity, productDetails: ProductDetails) {
        val paramsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build())
        val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    private val purchasesListener = PurchasesUpdatedListener { billingResult, purchasesList ->
        val response = BillingResponse(billingResult.responseCode)
        when {
            response.isOk -> {
                Result.setResultState(ResultState.PURCHASING_SUCCESSFULLY)
                handlePurchase(purchasesList)
                return@PurchasesUpdatedListener
            }

            response.canFailGracefully -> {
                Result.setResultState(ResultState.PURCHASING_ALREADY_OWNED)
                onPurchaseListener?.onPurchaseResult(true, ResultState.PURCHASING_ALREADY_OWNED.message)
                return@PurchasesUpdatedListener
            }

            response.isUserCancelled -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_USER_CANCELLED)
            response.isRecoverableError -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
            response.isNonrecoverableError -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
            response.isTerribleFailure -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
        }
        onPurchaseListener?.onPurchaseResult(false, Result.getResultState().message)
    }

    private fun handlePurchase(purchasesList: MutableList<Purchase>?) = CoroutineScope(Dispatchers.Main).launch {
        if (purchasesList == null) {
            Result.setResultState(ResultState.PURCHASING_NO_PURCHASES_FOUND)
            onPurchaseListener?.onPurchaseResult(false, ResultState.PURCHASING_NO_PURCHASES_FOUND.message)
            return@launch
        }
        queryUtils.checkForAcknowledgements(purchasesList)

        purchasesList.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    Result.setResultState(ResultState.PURCHASING_SUCCESSFULLY)
                    onPurchaseListener?.onPurchaseResult(true, ResultState.PURCHASING_SUCCESSFULLY.message)
                }

                else -> {
                    Result.setResultState(ResultState.PURCHASING_FAILURE)
                    onPurchaseListener?.onPurchaseResult(true, ResultState.PURCHASING_FAILURE.message)
                }
            }
        }
    }

    /**
     * End billing and release memory
     */

    fun endConnection() {
        job.cancel()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

@JvmInline
value class BillingResponse(private val code: Int) {
    val isOk: Boolean
        get() = code == BillingClient.BillingResponseCode.OK

    val isUserCancelled: Boolean
        get() = code == BillingClient.BillingResponseCode.USER_CANCELED

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