package com.hypersoft.billing.latest.dataClasses

import com.hypersoft.billing.latest.enums.ProductType

/**
 * @Author: SOHAIB AHMED
 * @Date: 13,June,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

/**
 * @property productId: Unique ID (Console's ID) for product
 * @property planId: Unique ID (Console's ID) for plan
 * @property productTitle: e.g. Gold Tier
 * @property planTitle: e.g. Weekly, Monthly, Yearly, etc
 * @property productType: e.g. InApp / Subs
 * @property currencyCode: e.g. USD, PKR, etc
 * @property price: e.g. Rs 750.00
 * @property priceAmountMicros: e.g. 750000000
 * @property trialDays: e.g. 3, 5, 7, etc
 * @property billingPeriod
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
    val productType: ProductType,
    var currencyCode: String,
    var price: String,
    var priceAmountMicros: Long = 0,
    var billingPeriod: String,
) {
    constructor() : this(
        productId = "",
        planId = "",
        productTitle = "",
        planTitle = "",
        productType = ProductType.SUBS,
        currencyCode = "",
        price = "",
        priceAmountMicros = 0,
        billingPeriod = "",
    )
}