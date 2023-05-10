package com.hypersoft.billing

import android.app.Activity
import android.content.Context
import com.hypersoft.billing.helper.BillingHelper
import com.hypersoft.billing.interfaces.OnPurchaseListener
import dev.epegasus.billinginapppurchases.interfaces.OnConnectionListener

/**
 * @param context: Context can be of Application class
 */

class BillingManager(context: Context) : BillingHelper(context) {

    override fun setCheckForSubscription(isCheckRequired: Boolean) {
        checkForSubscription = isCheckRequired
    }

    override fun startConnection(productIdsList: List<String>, onConnectionListener: OnConnectionListener) = startBillingConnection(productIdsList, onConnectionListener)

    fun makeInAppPurchase(activity: Activity?, onPurchaseListener: OnPurchaseListener) = purchaseInApp(activity, onPurchaseListener)

    fun makeSubPurchase(activity: Activity?, subscriptionTags: String, onPurchaseListener: OnPurchaseListener) = purchaseSub(activity, subscriptionTags, onPurchaseListener)

}