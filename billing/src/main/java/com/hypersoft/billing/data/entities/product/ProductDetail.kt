package com.hypersoft.billing.data.entities.product

import com.hypersoft.billing.presentation.enums.ProductType

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

/**
 * Represents a product available for purchase along with its associated pricing phases.
 *
 * @param productId The unique product ID, as defined in the Play Console (e.g., "basic_product_id").
 * @param planId The unique plan ID for subscriptions, if applicable (e.g., "basic-plan-id").
 * @param productTitle The display name of the product, such as "Gold Tier" or "Pro Plan".
 * @param productType The type of product — either [ProductType.inApp] for one-time purchases or [ProductType.subs] for subscriptions.
 * @param pricingDetails A list of [PricingPhase] entries describing pricing for each billing phase.
 *
 * Each [PricingPhase] includes:
 * - `price`: The formatted price string (e.g., "Rs 750.00").
 * - `priceAmountMicros`: The raw price in micros (1,000,000 micros = 1 currency unit).
 * - `currencyCode`: Currency in ISO 4217 format (e.g., "USD", "PKR").
 * - `planTitle`: A human-readable label for the plan, like "Monthly" or "Yearly".
 * - `freeTrialPeriod`: Number of free trial days offered (e.g., 3, 7).
 * - `billingPeriod`: The billing interval in ISO-8601 duration format:
 *     - "P1W" = Weekly
 *     - "P4W" = Every 4 weeks
 *     - "P1M" = Monthly
 *     - "P2M" = Bimonthly
 *     - "P3M" = Quarterly
 *     - "P4M" = Every 4 months
 *     - "P6M" = Semiannually
 *     - "P8M" = Every 8 months
 *     - "P1Y" = Yearly
 */
data class ProductDetail(
    var productId: String,
    var planId: String,
    var productTitle: String,
    var productType: ProductType,
    var pricingDetails: List<PricingPhase>
) {
    constructor() : this(
        productId = "",
        planId = "",
        productTitle = "",
        productType = ProductType.subs,
        pricingDetails = emptyList()
    )
}
