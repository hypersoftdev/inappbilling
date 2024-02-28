package com.hypersoft.billing.latest.dataClasses

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

/**
 * @Author: SOHAIB AHMED
 * @Date: 23/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

/**
 *      A single purchase can hold multiple products,
 *      this class create a new complete data class with
 *      product detail as per purchase.
 */

internal data class CompletePurchase(
    val purchase: Purchase,
    val productDetailList: List<ProductDetails>
)