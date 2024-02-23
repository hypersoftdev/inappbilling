package com.hypersoft.billing

import android.content.Context
import com.hypersoft.billing.latest.BillingController
import com.hypersoft.billing.latest.interfaces.BillingListener

/**
 * @param context: Context can be of Application class
 */

class BillingManager(private val context: Context) : BillingController(context) {

    /**
     *  @param productInAppPurchases: Pass list of in-app product's ID
     *  @param productSubscriptions: Pass list of subscription product's ID
     */

    fun initialize(
        productInAppPurchases: List<String>,
        productSubscriptions: List<String>,
        billingListener: BillingListener? = null
    ) {
        startBillingConnection(
            userInAppPurchases = productInAppPurchases,
            userSubsPurchases = productSubscriptions,
            billingListener = billingListener
        )
    }

    fun destroyBilling() = cleanBilling()
}


/*
class BillingManager(context: Context) : BillingHelper(context) {

    */
/*override fun setCheckForSubscription(isCheckRequired: Boolean) {
        checkForSubscription = isCheckRequired
    }

    override fun startConnection(productIdsList: List<String>, onConnectionListener: OnConnectionListener) = startBillingConnection(productIdsList, onConnectionListener)

    fun makeInAppPurchase(activity: Activity?, onPurchaseListener: OnPurchaseListener) = purchaseInApp(activity, onPurchaseListener)

    fun makeSubPurchase(activity: Activity?, subscriptionPlans: String, onPurchaseListener: OnPurchaseListener) = purchaseSub(activity, subscriptionPlans, onPurchaseListener)*//*


}*/
