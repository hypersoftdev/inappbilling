package com.hypersoft.billing.interfaces

import com.hypersoft.billing.dataClasses.PurchaseDetail

/**
 * @Author: SOHAIB AHMED
 * @Date: 21/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

interface BillingListener {
    fun onConnectionResult(isSuccess: Boolean, message: String)
    fun purchasesResult(purchaseDetailList: List<PurchaseDetail>)
}