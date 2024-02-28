package com.hypersoft.billing.latest.repository

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.hypersoft.billing.common.dataClasses.ProductType
import com.hypersoft.billing.latest.dataClasses.CompletePurchase
import com.hypersoft.billing.latest.dataClasses.ProductDetail
import com.hypersoft.billing.latest.dataClasses.PurchaseDetail
import com.hypersoft.billing.latest.dataClasses.QueryProductDetail
import com.hypersoft.billing.latest.enums.ResultState
import com.hypersoft.billing.latest.extensions.toFormattedDate
import com.hypersoft.billing.latest.utils.Result
import com.hypersoft.billing.oldest.helper.BillingHelper.Companion.TAG
import com.hypersoft.billing.common.interfaces.OnPurchaseListener
import com.hypersoft.billing.latest.utils.QueryUtils
import com.hypersoft.billing.latest.utils.ValidationUtils
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

internal open class BillingRepository(context: Context) {

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
     * @property _purchaseDetailList: List of all purchases with detail that this user has ever made and currently owns.
     */
    private val _purchaseDetailList = arrayListOf<PurchaseDetail>()
    private val purchaseDetailList: List<PurchaseDetail> get() = _purchaseDetailList.toList()

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

    /**
     *  Get a single testing product_id ("android.test.purchased")
     *  Get a single testing product_id ("android.test.item_unavailable")
     */
    protected fun getDebugProductIDList() = "android.test.purchased"

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
    protected fun startConnection(callback: (isSuccess: Boolean, message: String) -> Unit) {

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

    protected fun setUserQueries(userInAppPurchases: List<String>, userSubsPurchases: List<String>) {
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

    protected fun fetchPurchases() {
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
                Log.i(TAG, "BillingRepository: $productType -> Purchases: $purchases")
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
     * Calls once, Scenarios where a purchase might have 0 products:
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
                    val productType = if (productDetails.productType == BillingClient.ProductType.INAPP) ProductType.inapp else ProductType.subs
                    val planId = queryUtils.getPlanId(productDetails.subscriptionOfferDetails)
                    val planTitle = productDetails.subscriptionOfferDetails?.get(0)?.let { queryUtils.getPlanTitle(it) } ?: ""

                    val purchaseDetail = PurchaseDetail(
                        productId = productDetails.productId,
                        planId = planId,
                        productTitle = productDetails.title,
                        planTitle = planTitle,
                        purchaseToken = completePurchase.purchase.purchaseToken,
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

            _purchaseDetailList.clear()
            _purchaseDetailList.addAll(resultList)

            withContext(Dispatchers.Main) {
                _purchasesSharedFlow.emit(resultList)
            }
        }
    }

    /* ---------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------ Query Products ------------------------------------------ */
    /* ---------------------------------------------------------------------------------------------------- */

    protected fun fetchStoreProducts() {
        CoroutineScope(Dispatchers.IO + job).launch {
            _storeProductDetailsList.clear()

            // Determine product types to be fetched
            val hasInApp = userQueryList.any { it.first == BillingClient.ProductType.INAPP }
            val hasSubs = userQueryList.any { it.first == BillingClient.ProductType.SUBS }
            val hasBoth = userQueryList.any { it.first == BillingClient.ProductType.INAPP } &&
                    userQueryList.any { it.first == BillingClient.ProductType.SUBS }

            // Query for product types to be fetched
            when {
                hasBoth -> queryStoreProducts(BillingClient.ProductType.INAPP, true)
                hasInApp -> queryStoreProducts(BillingClient.ProductType.INAPP, false)
                hasSubs -> queryStoreProducts(BillingClient.ProductType.SUBS, false)
                else -> processStoreProducts(emptyList(), isCompleted = true)
            }
        }
    }

    private suspend fun queryStoreProducts(productType: String, hasBoth: Boolean) {
        when (productType) {
            BillingClient.ProductType.INAPP -> Result.setResultState(ResultState.CONSOLE_QUERY_PRODUCTS_INAPP_FETCHING)
            BillingClient.ProductType.SUBS -> Result.setResultState(ResultState.CONSOLE_QUERY_PRODUCTS_SUB_FETCHING)
        }
        val productIdList = userQueryList.filter { it.first == productType }

        val productParams = queryUtils.getProductParams(productIdList)
        val productDetailsList = queryUtils.queryProductDetailsAsync(productParams)
        Log.i(TAG, "BillingRepository: $productType -> Query: $productDetailsList")

        processStoreProducts(productDetailsList, hasBoth.not())

        if (productType == BillingClient.ProductType.INAPP && hasBoth) {
            queryStoreProducts(BillingClient.ProductType.SUBS, false)
        }
    }

    private fun processStoreProducts(productDetailsList: List<ProductDetails>, isCompleted: Boolean) {
        val productDetailList = arrayListOf<ProductDetail>()
        val queryProductDetail = arrayListOf<QueryProductDetail>()

        productDetailsList.forEach { productDetails ->
            when (productDetails.productType) {
                BillingClient.ProductType.INAPP -> {
                    val productDetail = ProductDetail(
                        productId = productDetails.productId,
                        planId = "",
                        productTitle = productDetails.title,
                        planTitle = "",
                        productType = ProductType.inapp,
                        currencyCode = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode.toString(),
                        price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice.toString().removeSuffix(".00"),
                        priceAmountMicros = productDetails.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L,
                        billingPeriod = ""
                    )
                    productDetailList.add(productDetail)
                    queryProductDetail.add(QueryProductDetail(productDetail, productDetails))
                }

                BillingClient.ProductType.SUBS -> {
                    productDetails.subscriptionOfferDetails?.let { offersList ->
                        offersList.forEach tag@{ offer ->       // Weekly, Monthly, etc // Free-Regular  // Regular

                            val isExist = productDetailList.any { it.productId == productDetails.productId && it.planId == offer.basePlanId }
                            if (isExist) {
                                return@tag
                            }

                            val productDetail = ProductDetail().apply {
                                productId = productDetails.productId
                                planId = offer.basePlanId
                                productTitle = productDetails.title
                                productType = ProductType.subs
                            }

                            offer.pricingPhases.pricingPhaseList.forEach { pricingPhase ->
                                if (pricingPhase.formattedPrice.equals("Free", ignoreCase = true)) {
                                    productDetail.freeTrialDays = queryUtils.getTrialDay(offer)
                                } else {
                                    productDetail.planTitle = queryUtils.getPlanTitle(pricingPhase.billingPeriod)
                                    productDetail.currencyCode = pricingPhase.priceCurrencyCode
                                    productDetail.price = pricingPhase.formattedPrice
                                    productDetail.priceAmountMicros = pricingPhase.priceAmountMicros
                                    productDetail.billingPeriod = pricingPhase.billingPeriod
                                }
                            }

                            productDetailList.add(productDetail)
                            queryProductDetail.add(QueryProductDetail(productDetail, productDetails))
                        }
                    }
                }
            }
        }

        _storeProductDetailsList.addAll(queryProductDetail)

        if (isCompleted) {
            Result.setResultState(ResultState.CONSOLE_QUERY_PRODUCTS_COMPLETED)
            _productDetailsLiveData.postValue(productDetailList)
        }
    }

    /* ---------------------------------------------------------------------------------------------------- */
    /* ---------------------------------------- Product Purchases ----------------------------------------- */
    /* ---------------------------------------------------------------------------------------------------- */

    protected fun purchaseInApp(activity: Activity?, productId: String, onPurchaseListener: OnPurchaseListener) {
        this.onPurchaseListener = onPurchaseListener

        val errorMessage = validationUtils.checkForInApp(activity, productId)

        if (errorMessage != null) {
            onPurchaseListener.onPurchaseResult(false, message = errorMessage)
            return
        }

        val queryProductDetail = storeProductDetailsList.find {
            it.productDetail.productId == productId
                    && it.productDetail.productType == ProductType.inapp
        }
        queryProductDetail?.let {
            launchFlow(activity = activity!!, it.productDetails, offerToken = null)
        } ?: run {
            Result.setResultState(ResultState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST)
            onPurchaseListener.onPurchaseResult(false, message = ResultState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST.message)
        }
    }

    protected fun purchaseSubs(activity: Activity?, productId: String, planId: String, onPurchaseListener: OnPurchaseListener) {
        this.onPurchaseListener = onPurchaseListener

        val errorMessage = validationUtils.checkForSubs(activity, productId)

        if (errorMessage != null) {
            onPurchaseListener.onPurchaseResult(false, message = errorMessage)
            return
        }

        val productDetail = storeProductDetailsList.find {
            it.productDetail.productId == productId
                    && it.productDetail.planId == planId
                    && it.productDetail.productType == ProductType.subs
        }
        productDetail?.let {
            val offerToken = queryUtils.getOfferToken(it.productDetails.subscriptionOfferDetails, planId)
            launchFlow(activity = activity!!, it.productDetails, offerToken = offerToken)
        } ?: run {
            Result.setResultState(ResultState.CONSOLE_PRODUCTS_SUB_NOT_EXIST)
            onPurchaseListener.onPurchaseResult(false, message = ResultState.CONSOLE_PRODUCTS_SUB_NOT_EXIST.message)
        }
    }

    private fun launchFlow(activity: Activity, productDetails: ProductDetails, offerToken: String?) {
        Log.i(TAG, "launchFlow: Product Details about to be purchase: $productDetails")
        val paramsList = when (offerToken == null) {
            true -> listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build())
            false -> listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).setOfferToken(offerToken).build())
        }
        val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(paramsList).build()
        billingClient.launchBillingFlow(activity, flowParams)
        Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY)
    }

    /**
     * Upgrade or downgrade subscription plan
     */

    protected fun updateSubs(
        activity: Activity?,
        oldProductId: String,
        oldPlanId: String,
        productId: String,
        planId: String,
        onPurchaseListener: OnPurchaseListener
    ) {
        this.onPurchaseListener = onPurchaseListener

        val errorMessage = validationUtils.checkForSubs(activity, productId)
        val oldPurchase= purchaseDetailList.find { it.productId == oldProductId && it.planId == oldPlanId }

        if (errorMessage != null) {
            onPurchaseListener.onPurchaseResult(false, message = errorMessage)
            return
        }

        if (oldPurchase == null) {
            Result.setResultState(ResultState.CONSOLE_PRODUCTS_OLD_SUB_NOT_FOUND)
            onPurchaseListener.onPurchaseResult(false, message = ResultState.CONSOLE_PRODUCTS_OLD_SUB_NOT_FOUND.message)
            return
        }

        val productDetail = storeProductDetailsList.find {
            it.productDetail.productId == productId
                    && it.productDetail.planId == planId
                    && it.productDetail.productType == ProductType.subs
        }

        productDetail?.let {
            val offerToken = queryUtils.getOfferToken(it.productDetails.subscriptionOfferDetails, planId)
            val paramsList = listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(it.productDetails).setOfferToken(offerToken).build())

            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(oldPurchase.purchaseToken)
                .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE)
                .build()

            val flowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(paramsList)
                    .setSubscriptionUpdateParams(updateParams)
                    .build()

            billingClient.launchBillingFlow(activity!!, flowParams)
            Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY)
        } ?: run {
            Result.setResultState(ResultState.CONSOLE_PRODUCTS_SUB_NOT_EXIST)
            onPurchaseListener.onPurchaseResult(false, message = ResultState.CONSOLE_PRODUCTS_SUB_NOT_EXIST.message)
        }
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

            response.isTerribleFailure -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
            response.isRecoverableError -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
            response.isNonrecoverableError -> Result.setResultState(ResultState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND)
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

    protected fun endConnection() {
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
        )
}