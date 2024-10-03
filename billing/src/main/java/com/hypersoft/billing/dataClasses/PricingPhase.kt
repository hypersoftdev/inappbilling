package com.hypersoft.billing.dataClasses

import com.hypersoft.billing.enums.RecurringMode

/**
 *   Developer: Sohaib Ahmed
 *   Date: 10/3/2024
 *   Profile:
 *     -> github.com/epegasus
 *     -> linkedin.com/in/epegasus
 */


/**
 * @param
 * @param planTitle: e.g. Weekly, Monthly, Yearly, etc
 * @param currencyCode: e.g. USD, PKR, etc
 * @param price: e.g. Rs 750.00
 * @param priceAmountMicros: e.g. 750000000
 * @param freeTrialPeriod: e.g. 3, 5, 7, etc
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

/**
 * Data class representing the pricing phase for a subscription plan.
 *
 * @param recurringMode: The recurring mode of the pricing phase, which can be:
 * - FREE: Represents a free phase of the subscription.
 * - DISCOUNTED: Represents a discounted phase of the subscription for specific time period.
 * - ORIGINAL: Represents the original price phase of the subscription.
 *
 * @param planTitle: The title of the subscription plan, e.g., "Weekly", "Monthly", "Yearly", etc.
 * @param currencyCode: The ISO 4217 currency code, e.g., "USD" for US Dollars, "PKR" for Pakistani Rupee, etc.
 * @param price: The formatted price string of the subscription, e.g., "Rs 750.00".
 * @param priceAmountMicros: The price of the subscription in micros (1,000,000 micros = 1 unit of currency), e.g., "750000000" represents Rs 750.00.
 * @param billingCycleCount: The number of billing cycles this phase lasts, e.g., 1 for one cycle.
 * @param freeTrialPeriod: The length of the free trial period in days, e.g., 3, 5, 7, etc.
 *
 * @param billingPeriod: The duration of the billing period in ISO-8601 format, e.g.,
 * - Weekly: "P1W" (One week)
 * - Every 4 weeks: "P4W" (Four weeks)
 * - Monthly: "P1M" (One month)
 * - Bimonthly: "P2M" (Two months)
 * - Quarterly: "P3M" (Three months)
 * - Every 4 months: "P4M" (Four months)
 * - Semiannually: "P6M" (Six months)
 * - Every 8 months: "P8M" (Eight months)
 * - Yearly: "P1Y" (One year)
 */

data class PricingPhase(
    var recurringMode: RecurringMode,
    var price: String,
    var currencyCode: String,
    var planTitle: String,
    var billingCycleCount: Int,
    var billingPeriod: String,
    var priceAmountMicros: Long,
    var freeTrialPeriod: Int,
) {
    constructor() : this(
        recurringMode = RecurringMode.ORIGINAL,
        price = "",
        currencyCode = "",
        planTitle = "",
        billingCycleCount = 0,
        billingPeriod = "",
        priceAmountMicros = 0,
        freeTrialPeriod = 0,
    )
}