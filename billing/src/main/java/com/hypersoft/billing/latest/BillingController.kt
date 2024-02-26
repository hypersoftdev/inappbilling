package com.hypersoft.billing.latest

import android.content.Context
import com.hypersoft.billing.latest.interfaces.BillingListener
import com.hypersoft.billing.latest.repository.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @Author: SOHAIB AHMED
 * @Date: 21/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

open class BillingController(private val context: Context) {

    private val billingRepository by lazy { BillingRepository(context) }
    private var billingListener: BillingListener? = null

    private var job: Job? = null

    protected fun startBillingConnection(
        userInAppPurchases: List<String>,
        userSubsPurchases: List<String>,
        billingListener: BillingListener? = null
    ) {
        this.billingListener = billingListener

        billingRepository.startConnection { isSuccess, message ->
            billingListener?.onConnectionResult(isSuccess, message)
            if (isSuccess) {
                fetchPurchases(userInAppPurchases, userSubsPurchases)

            }
        }
    }

    private fun fetchPurchases(userInAppPurchases: List<String>, userSubsPurchases: List<String>) {
        billingRepository.fetchPurchases(userInAppPurchases, userSubsPurchases)

        // Observe purchases
        job = CoroutineScope(Dispatchers.Main).launch {
            billingRepository.purchasesSharedFlow.collect {
                billingListener?.purchasesResult(it)
            }
        }
    }

    protected fun cleanBilling() {
        billingRepository.endConnection()
        job?.cancel()
    }
}