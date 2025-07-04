package com.hypersoft.billing.asd.entities

import com.hypersoft.billing.asd.enums.ProductType


/**
 * Created by: Sohaib Ahmed
 * Date: 7/3/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

/**
 * @param productId          The product ID for both in-app and subscriptions (e.g. "product_ads", "product_weekly_ads")
 * @param productType        The type of product: INAPP or SUBS
 * @param purchaseToken      Unique token identifying the purchase

 * @param productTitle       The display title of the product
 * @param planTitle          The display title of the plan (if applicable)
 * @param planId             The plan ID (only applicable for subscriptions, e.g. "plan_weekly_ads")
 *
 * @param purchaseTime       Human-readable subscription start time (constant across renewals)
 * @param purchaseTimeMillis Unix timestamp representing the purchase time
 * @param isAutoRenewing     Indicates if the subscription is set to auto-renew (SUBS only)
 */

data class PurchaseDetail(
    val productId: String,
    val productType: ProductType,
    val purchaseToken: String,

    var productTitle: String?,
    var planTitle: String?,
    var planId: String?,

    val purchaseTime: String,
    val purchaseTimeMillis: Long,
    val isAutoRenewing: Boolean,
    val isAcknowledged: Boolean,
)
