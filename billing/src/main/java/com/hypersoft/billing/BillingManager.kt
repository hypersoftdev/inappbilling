package com.hypersoft.billing

import android.app.Activity
import com.hypersoft.billing.helper.BillingHelper
import com.hypersoft.billing.interfaces.OnPurchaseListener
import dev.epegasus.billinginapppurchases.interfaces.OnConnectionListener

/**
 * @param context: Context can be of Application class
 */

class BillingManager(private val activity: Activity) : BillingHelper(activity) {

    override fun setCheckForSubscription(isCheckRequired: Boolean) {
        checkForSubscription = isCheckRequired
    }

    override fun startConnection(productIdsList: List<String>, onConnectionListener: OnConnectionListener) = startBillingConnection(productIdsList, onConnectionListener)

    fun makeInAppPurchase(onPurchaseListener: OnPurchaseListener) = purchaseInApp(onPurchaseListener)

    fun makeSubPurchase(subscriptionTags: String, onPurchaseListener: OnPurchaseListener) = purchaseSub(subscriptionTags, onPurchaseListener)

}