package com.hypersoft.billing.utils

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.hypersoft.billing.enums.ResultState
import com.hypersoft.billing.states.Result

/**
 * @Author: SOHAIB AHMED
 * @Date: 26/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

internal class ValidationUtils(private val billingClient: BillingClient) {

    fun checkForInApp(activity: Activity?, productId: String): String? {
        if (activity == null) {
            Result.setResultState(ResultState.ACTIVITY_REFERENCE_NOT_FOUND)
            return ResultState.ACTIVITY_REFERENCE_NOT_FOUND.message
        }

        if (productId.trim().isEmpty()) {
            Result.setResultState(ResultState.CONSOLE_BUY_PRODUCT_EMPTY_ID)
            return ResultState.CONSOLE_BUY_PRODUCT_EMPTY_ID.message
        }

        if (billingClient.isReady.not()) {
            Result.setResultState(ResultState.CONNECTION_INVALID)
            return ResultState.CONNECTION_INVALID.message
        }

        return null
    }

    fun checkForSubs(activity: Activity?, productId: String): String? {
        if (activity == null) {
            Result.setResultState(ResultState.ACTIVITY_REFERENCE_NOT_FOUND)
            return ResultState.ACTIVITY_REFERENCE_NOT_FOUND.message
        }

        if (productId.trim().isEmpty()) {
            Result.setResultState(ResultState.CONSOLE_BUY_PRODUCT_EMPTY_ID)
            return ResultState.CONSOLE_BUY_PRODUCT_EMPTY_ID.message
        }

        if (billingClient.isReady.not()) {
            Result.setResultState(ResultState.CONNECTION_INVALID)
            return ResultState.CONNECTION_INVALID.message
        }

        return null
    }
}