package com.hypersoft.billing

import android.app.Activity

import com.hypersoft.billing.helper.BillingHelper

/**
 * @param activity: Must be a reference of an Activity
 */
class BillingManager(private val activity: Activity) : BillingHelper(activity) {

    override fun startConnection(productIdsList: List<String>, callback: (connectionResult: Boolean, message: String) -> Unit) = startBillingConnection(productIdsList = productIdsList, autoPurchase = false, callback = callback)

    fun makePurchase(callback: (isPurchased: Boolean, message: String) -> Unit) = purchase(callback)

}
