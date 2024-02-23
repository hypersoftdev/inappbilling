package com.hypersoft.billing.latest.interfaces

import com.hypersoft.billing.oldest.dataClasses.PurchaseDetail

/**
 * @Author: SOHAIB AHMED
 * @Date: 21/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

interface BillingListener {
    fun onConnectionResult(isSuccess: Boolean, message: String)
    fun purchasesResult(isSuccess: Boolean, message: String, purchaseDetail: PurchaseDetail?)
}