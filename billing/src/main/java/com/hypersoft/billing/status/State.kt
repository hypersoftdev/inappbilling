package com.hypersoft.billing.status

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypersoft.billing.enums.BillingState

object State {

    private var BILLING_STATE = BillingState.NONE

    private var _billingState = MutableLiveData<BillingState>()
    val billingState: LiveData<BillingState> get() = _billingState

    fun setBillingState(billingState: BillingState) {
        Log.d("BillingManager", "setBillingState: $billingState")
        BILLING_STATE = billingState
        _billingState.postValue(BILLING_STATE)
    }

    fun getBillingState(): BillingState {
        return BILLING_STATE
    }

    override fun toString(): String {
        return when (BILLING_STATE) {
            BillingState.NONE -> BillingState.NONE.message
            BillingState.NO_INTERNET_CONNECTION -> BillingState.NO_INTERNET_CONNECTION.message
            BillingState.EMPTY_PRODUCT_ID_LIST -> BillingState.EMPTY_PRODUCT_ID_LIST.message
            BillingState.CONNECTION_ESTABLISHING -> BillingState.CONNECTION_ESTABLISHING.message
            BillingState.CONNECTION_ALREADY_ESTABLISHING -> BillingState.CONNECTION_ALREADY_ESTABLISHING.message
            BillingState.CONNECTION_DISCONNECTED -> BillingState.CONNECTION_DISCONNECTED.message
            BillingState.CONNECTION_ESTABLISHED -> BillingState.CONNECTION_ESTABLISHED.message
            BillingState.CONNECTION_FAILED -> BillingState.CONNECTION_FAILED.message
            BillingState.FEATURE_NOT_SUPPORTED -> BillingState.FEATURE_NOT_SUPPORTED.message
            BillingState.ACTIVITY_REFERENCE_NOT_FOUND -> BillingState.ACTIVITY_REFERENCE_NOT_FOUND.message

            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_FETCHING -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_FETCHING.message
            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_NOT_FOUND -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_NOT_FOUND.message
            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED.message
            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_NOT_OWNED -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_NOT_OWNED.message
            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_BUT_NOT_ACKNOWLEDGE -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_BUT_NOT_ACKNOWLEDGE.message
            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_ACKNOWLEDGE -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_ACKNOWLEDGE.message
            BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_FAILED_TO_ACKNOWLEDGE -> BillingState.CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_FAILED_TO_ACKNOWLEDGE.message

            BillingState.CONSOLE_OLD_PRODUCTS_SUB_FETCHING -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_FETCHING.message
            BillingState.CONSOLE_OLD_PRODUCTS_SUB_NOT_FOUND -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_NOT_FOUND.message
            BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED.message
            BillingState.CONSOLE_OLD_PRODUCTS_SUB_NOT_OWNED -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_NOT_OWNED.message
            BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_BUT_NOT_ACKNOWLEDGE -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_BUT_NOT_ACKNOWLEDGE.message
            BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_ACKNOWLEDGE -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_ACKNOWLEDGE.message
            BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_FAILED_TO_ACKNOWLEDGE -> BillingState.CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_FAILED_TO_ACKNOWLEDGE.message

            BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING -> BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING.message
            BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHED_SUCCESSFULLY -> BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHED_SUCCESSFULLY.message
            BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING_FAILED -> BillingState.CONSOLE_PRODUCTS_IN_APP_FETCHING_FAILED.message
            BillingState.CONSOLE_PRODUCTS_IN_APP_AVAILABLE -> BillingState.CONSOLE_PRODUCTS_IN_APP_AVAILABLE.message
            BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST -> BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_EXIST.message
            BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_FOUND -> BillingState.CONSOLE_PRODUCTS_IN_APP_NOT_FOUND.message

            BillingState.CONSOLE_PRODUCTS_SUB_FETCHING -> BillingState.CONSOLE_PRODUCTS_SUB_FETCHING.message
            BillingState.CONSOLE_PRODUCTS_SUB_FETCHED_SUCCESSFULLY -> BillingState.CONSOLE_PRODUCTS_SUB_FETCHED_SUCCESSFULLY.message
            BillingState.CONSOLE_PRODUCTS_SUB_FETCHING_FAILED -> BillingState.CONSOLE_PRODUCTS_SUB_FETCHING_FAILED.message
            BillingState.CONSOLE_PRODUCTS_SUB_AVAILABLE -> BillingState.CONSOLE_PRODUCTS_SUB_AVAILABLE.message
            BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST -> BillingState.CONSOLE_PRODUCTS_SUB_NOT_EXIST.message
            BillingState.CONSOLE_PRODUCTS_SUB_NOT_FOUND -> BillingState.CONSOLE_PRODUCTS_SUB_NOT_FOUND.message

            BillingState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY -> BillingState.LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY.message
            BillingState.LAUNCHING_FLOW_INVOCATION_USER_CANCELLED -> BillingState.LAUNCHING_FLOW_INVOCATION_USER_CANCELLED.message
            BillingState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND -> BillingState.LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND.message
            BillingState.PURCHASED_SUCCESSFULLY -> BillingState.PURCHASED_SUCCESSFULLY.message
            BillingState.PURCHASING_ALREADY_OWNED -> BillingState.PURCHASING_ALREADY_OWNED.message
            BillingState.PURCHASING_USER_CANCELLED -> BillingState.PURCHASING_USER_CANCELLED.message
            BillingState.PURCHASING_FAILURE -> BillingState.PURCHASING_FAILURE.message
            BillingState.PURCHASING_ERROR -> BillingState.PURCHASING_ERROR.message
        }
    }
}