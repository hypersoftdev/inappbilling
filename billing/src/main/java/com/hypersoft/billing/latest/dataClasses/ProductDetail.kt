package com.hypersoft.billing.latest.dataClasses

import com.hypersoft.billing.common.dataClasses.ProductType

/**
 * @Author: SOHAIB AHMED
 * @Date: 13,June,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

/**
 * @param productId: Unique ID (Console's ID) for product
 * @param planId: Unique ID (Console's ID) for plan
 * @param productTitle: e.g. Gold Tier
 * @param planTitle: e.g. Weekly, Monthly, Yearly, etc
 * @param productType: e.g. InApp / Subs
 * @param currencyCode: e.g. USD, PKR, etc
 * @param price: e.g. Rs 750.00
 * @param priceAmountMicros: e.g. 750000000
 * @param freeTrialDays: e.g. 3, 5, 7, etc
 * @param billingPeriod
 * - Weekly: P1W (One week)
 * - Every 4 weeks: P4W (Four weeks)
 * - Monthly: P1M (One month)
 * - Every 2 months (Bimonthly): P2M (Two months)
 * - Every 3 months (Quarterly): P3M (Three months)
 * - Every 4 months: P4M (Four months)
 * - Every 6 months (Semiannually): P6M (Six months)
 * - Every 8 months: P8M (Eight months)
 * - Yearly: P1Y (One year)
 */

data class ProductDetail(
    var productId: String,
    var planId: String,
    var productTitle: String,
    var planTitle: String,
    var productType: ProductType,
    var currencyCode: String,
    var price: String,
    var priceAmountMicros: Long = 0,
    var freeTrialDays: Int = 0,
    var billingPeriod: String,
) {
    constructor() : this(
        productId = "",
        planId = "",
        productTitle = "",
        planTitle = "",
        productType = ProductType.subs,
        currencyCode = "",
        price = "",
        priceAmountMicros = 0,
        freeTrialDays = 0,
        billingPeriod = "",
    )
}