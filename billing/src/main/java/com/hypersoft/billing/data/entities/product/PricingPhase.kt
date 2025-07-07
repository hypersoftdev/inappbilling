package com.hypersoft.billing.data.entities.product

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

/**
 * Represents a single pricing phase of a subscription plan.
 *
 * A subscription can go through multiple phases (e.g., free trial → discounted → full price),
 * and this class defines the details of one such phase.
 *
 * @property recurringMode Indicates the type of phase:
 * - [RecurringMode.FREE]       → A completely free trial period.
 * - [RecurringMode.DISCOUNTED] → A limited-time discounted offer.
 * - [RecurringMode.ORIGINAL]   → The standard, full-price phase.
 *
 * @property planTitle A human-friendly label for the plan, such as "Weekly", "Monthly", "Yearly", etc.
 * @property currencyCode The ISO 4217 currency code (e.g., "USD", "PKR", "EUR").
 * @property price The formatted display price (e.g., "Rs 750.00", "$4.99").
 * @property priceAmountMicros The raw price in micros (1,000,000 micros = 1 unit of currency).
 *                             For example, `750_000_000L` = Rs 750.00
 * @property billingCycleCount Number of billing cycles this phase spans (e.g., `1` means it lasts for one billing period).
 * @property freeTrialPeriod Duration of any free trial (in days), applicable only to free trial phases. (e.g., 3, 5, 7, etc)
 * @property billingPeriod Duration of one billing cycle, in ISO‑8601 format:
 * - "P1W" = 1 week
 * - "P4W" = 4 weeks
 * - "P1M" = 1 month
 * - "P2M" = 2 months (bimonthly)
 * - "P3M" = 3 months (quarterly)
 * - "P6M" = 6 months (semi-annually)
 * - "P8M" = 8 months
 * - "P1Y" = 1 year
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
    /** No-arg constructor for serialization or default usage */
    constructor() : this(
        recurringMode = RecurringMode.ORIGINAL,
        price = "",
        currencyCode = "",
        planTitle = "",
        billingCycleCount = 0,
        billingPeriod = "",
        priceAmountMicros = 0L,
        freeTrialPeriod = 0
    )
}