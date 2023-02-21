package com.hypersoft.billing.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypersoft.billing.enums.BillingState

object State {

    private var BILLING_STATE = BillingState.NONE

    private var _billingState = MutableLiveData<BillingState>()
    val billingState: LiveData<BillingState> get() = _billingState

    fun setBillingState(billingState: BillingState) {
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
            BillingState.CONNECTION_DISCONNECTED -> BillingState.CONNECTION_DISCONNECTED.message
            BillingState.CONNECTION_ESTABLISHED -> BillingState.CONNECTION_ESTABLISHED.message
            BillingState.CONNECTION_FAILED -> BillingState.CONNECTION_FAILED.message
            BillingState.FEATURE_NOT_SUPPORTED -> BillingState.FEATURE_NOT_SUPPORTED.message
            BillingState.CONSOLE_PRODUCTS_FETCHING -> BillingState.CONSOLE_PRODUCTS_FETCHING.message
            BillingState.CONSOLE_PRODUCTS_FETCHED_SUCCESSFULLY -> BillingState.CONSOLE_PRODUCTS_FETCHED_SUCCESSFULLY.message
            BillingState.CONSOLE_PRODUCTS_FETCHING_FAILED -> BillingState.CONSOLE_PRODUCTS_FETCHING_FAILED.message
            BillingState.CONSOLE_PRODUCTS_AVAILABLE -> BillingState.CONSOLE_PRODUCTS_AVAILABLE.message
            BillingState.CONSOLE_PRODUCTS_NOT_EXIST -> BillingState.CONSOLE_PRODUCTS_NOT_EXIST.message
            BillingState.CONSOLE_PRODUCTS_NOT_FOUND -> BillingState.CONSOLE_PRODUCTS_NOT_FOUND.message
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