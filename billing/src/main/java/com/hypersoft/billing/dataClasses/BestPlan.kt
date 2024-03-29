package com.hypersoft.billing.dataClasses

import com.android.billingclient.api.ProductDetails.PricingPhase

/**
 * @Author: SOHAIB AHMED
 * @Date: 26/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

internal data class BestPlan(
    val trialDays: Int,
    val pricingPhase: PricingPhase?
)
