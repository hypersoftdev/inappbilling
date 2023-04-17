package com.hypersoft.billing.interfaces

/**
 * @Author: SOHAIB AHMED
 * @Date: 16,April,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

interface OnPurchaseListener {

    fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String)

}