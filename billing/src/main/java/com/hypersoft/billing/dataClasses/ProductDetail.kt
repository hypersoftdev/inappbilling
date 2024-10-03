package com.hypersoft.billing.dataClasses

/**
 * @Author: SOHAIB AHMED
 * @Date: 13,June,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

/**
 * Data class representing the detailed information of a product and its associated pricing plans.
 *
 * @param productId: The unique identifier for the product, as defined in the console (e.g., Google Play Console).
 * @param planId: The unique identifier for the subscription plan, as defined in the console.
 * @param productTitle: The name or title of the product, e.g., "Gold Tier".
 * @param productType: The type of product, either "InApp" (for in-app purchases) or "Subs" (for subscriptions).
 * @param pricingDetails: A list of `PricingPhase` objects that contain pricing information for each phase of the product's billing cycles.
 *
 * Each `PricingPhase` contains:
 * - price: The formatted price string, e.g., "Rs 750.00".
 * - priceAmountMicros: The price of the subscription in micros (1,000,000 micros = 1 unit of currency).
 * - currencyCode: ISO 4217 currency code, e.g., "USD" or "PKR".
 * - planTitle: The title of the plan, e.g., "Weekly", "Monthly", "Yearly".
 * - freeTrialPeriod: Number of days for the free trial, e.g., 3, 5, 7, etc.
 * - billingPeriod: Duration of the billing period in ISO-8601 format:
 *     - Weekly: "P1W" (One week)
 *     - Every 4 weeks: "P4W" (Four weeks)
 *     - Monthly: "P1M" (One month)
 *     - Bimonthly: "P2M" (Two months)
 *     - Quarterly: "P3M" (Three months)
 *     - Every 4 months: "P4M" (Four months)
 *     - Semiannually: "P6M" (Six months)
 *     - Every 8 months: "P8M" (Eight months)
 *     - Yearly: "P1Y" (One year)
 */

data class ProductDetail(
    var productId: String,
    var planId: String,
    var productTitle: String,
    var productType: ProductType,
    var pricingDetails : List<PricingPhase>

) {
    constructor() : this(
        productId = "",
        planId = "",
        productTitle = "",
        productType = ProductType.subs,
        pricingDetails = listOf(),
    )
}