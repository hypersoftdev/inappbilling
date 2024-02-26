package com.hypersoft.billing.latest

import android.app.Activity
import android.content.Context
import com.hypersoft.billing.latest.interfaces.BillingListener
import com.hypersoft.billing.latest.repository.BillingRepository
import com.hypersoft.billing.common.interfaces.OnPurchaseListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @Author: SOHAIB AHMED
 * @Date: 21/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

internal class BillingController(context: Context) : BillingRepository(context) {

    private var billingListener: BillingListener? = null

    private var job: Job? = null

    val debugProductId =  getDebugProductIDList()
    val productsObserver get() =   productDetailsLiveData

    fun startBillingConnection(
        userInAppPurchases: List<String>,
        userSubsPurchases: List<String>,
        billingListener: BillingListener? = null
    ) {
        this.billingListener = billingListener

        startConnection { isSuccess, message ->
            billingListener?.onConnectionResult(isSuccess, message)
            if (isSuccess) {
                fetchPurchases(userInAppPurchases, userSubsPurchases)
            }
        }
    }

    private fun fetchPurchases(userInAppPurchases: List<String>, userSubsPurchases: List<String>) {
        setUserQueries(userInAppPurchases, userSubsPurchases)
        fetchPurchases()
        fetchStoreProducts()

        // Observe purchases
        job = CoroutineScope(Dispatchers.Main).launch {
            purchasesSharedFlow.collect {
                billingListener?.purchasesResult(it)
            }
        }
    }

    fun makePurchaseInApp(
        activity: Activity?,
        productId: String,
        onPurchaseListener: OnPurchaseListener
    ) {
        purchaseInApp(activity = activity, productId = productId, onPurchaseListener = onPurchaseListener)
    }

    fun makePurchaseSub(
        activity: Activity?,
        productId: String,
        planId: String,
        onPurchaseListener: OnPurchaseListener
    ) {
        purchaseSubs(activity = activity, productId = productId, planId = planId, onPurchaseListener = onPurchaseListener)
    }

    fun cleanBilling() {
        endConnection()
        job?.cancel()
    }
}