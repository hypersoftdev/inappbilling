package com.hypersoft.billing.oldest.interfaces

import com.hypersoft.billing.oldest.dataClasses.PurchaseDetail

/**
 * @Author: SOHAIB AHMED
 * @Date: 16,April,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

interface OnConnectionListener {

    fun onConnectionResult(isSuccess: Boolean, message: String) {}
    fun onOldPurchaseResult(isPurchased: Boolean, purchaseDetail: PurchaseDetail?)

}