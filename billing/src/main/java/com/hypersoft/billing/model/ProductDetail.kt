package com.hypersoft.billing.model

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/**
 * Represents a product available for purchase along with its associated pricing phases.
 *
 * @param productId The unique product ID, as defined in the Play Console (e.g., "basic_product_id").
 * @param planId The unique plan ID for subscriptions, if applicable (e.g., "basic-plan-id").
 * @param offerId The specific offer ID within [planId], if applicable. A base plan can carry multiple
 *   concurrent offers (default price, a free-trial offer, a time-limited discount, a winback offer, etc.) —
 *   Play only returns the ones the current user is eligible for. Empty string means the plan's default
 *   offer (Play's `offerId == null`) or, for one-time products, "not applicable".
 * @param productTitle The display name of the product, such as "Gold Tier" or "Pro Plan".
 * @param productType The type of product — either [ProductType.inapp] for one-time purchases or [ProductType.subs] for subscriptions.
 * @param pricingDetails A list of [PricingPhase] entries describing pricing for each billing phase.
 */
data class ProductDetail(
    var productId: String,
    var planId: String,
    var offerId: String,
    var productTitle: String,
    var productType: ProductType,
    var pricingDetails: List<PricingPhase>
) {
    constructor() : this(
        productId = "",
        planId = "",
        offerId = "",
        productTitle = "",
        productType = ProductType.subs,
        pricingDetails = emptyList()
    )
}
