package com.hypersoft.billing.oldest.dataClasses

import com.hypersoft.billing.common.dataClasses.ProductType

/**
 * @Author: SOHAIB AHMED
 * @Date: 8/1/2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

data class PurchaseDetail (
    val productType: ProductType,
    val purchaseType: String,
    val purchaseTime: String
)