package com.hypersoft.billing.internal.mapper

import com.android.billingclient.api.ProductDetails
import com.hypersoft.billing.model.PricingPhase
import com.hypersoft.billing.model.ProductDetail
import com.hypersoft.billing.model.ProductType
import com.hypersoft.billing.model.RecurringMode

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/**
 * Single source of truth for mapping Play Billing's [ProductDetails] into the library's
 * public [ProductDetail]/[PricingPhase] models. Previously this logic was duplicated
 * independently across two use-case classes.
 */
internal object ProductMapper {

    /**
     * A one-time product can carry multiple purchase options (default price, a time-limited
     * discount, a preorder, a rental, etc.) — Play only returns the ones the current user is
     * eligible for. Each one becomes its own [ProductDetail], distinguished by [ProductDetail.offerId].
     */
    fun toOneTimeProducts(details: ProductDetails): List<ProductDetail> = details.oneTimePurchaseOfferDetailsList?.map { offer ->
        ProductDetail(
            productId = details.productId,
            planId = "",
            offerId = offer.offerId.orEmpty(),
            productTitle = details.title,
            productType = ProductType.inapp,
            pricingDetails = listOf(
                PricingPhase(
                    recurringMode = if (offer.fullPriceMicros != null) RecurringMode.DISCOUNTED else RecurringMode.ORIGINAL,
                    price = offer.formattedPrice.clean(),
                    currencyCode = offer.priceCurrencyCode,
                    planTitle = "",
                    billingCycleCount = 0,
                    billingPeriod = "",
                    priceAmountMicros = offer.priceAmountMicros,
                    freeTrialPeriod = 0
                )
            )
        )
    }.orEmpty()

    /**
     * A base plan can carry multiple concurrent offers (default price, a free-trial offer, a
     * time-limited discount, a winback offer, etc.) — Play only returns the ones the current user
     * is eligible for. Each one becomes its own [ProductDetail], distinguished by [ProductDetail.offerId].
     */
    fun toSubscriptionProducts(details: ProductDetails): List<ProductDetail> = details.subscriptionOfferDetails?.map { offer ->
        ProductDetail(
            productId = details.productId,
            planId = offer.basePlanId,
            offerId = offer.offerId.orEmpty(),
            productTitle = details.title,
            productType = ProductType.subs,
            pricingDetails = offer.pricingPhases.pricingPhaseList.map { it.toPricingPhase() }
        )
    }.orEmpty()

    fun ProductDetails.PricingPhase.toPricingPhase() = PricingPhase(
        recurringMode = when {
            formattedPrice.equals("Free", true) -> RecurringMode.FREE
            recurrenceMode == 2 -> RecurringMode.DISCOUNTED
            else -> RecurringMode.ORIGINAL
        },
        planTitle = billingPeriod.toPlanTitle(),
        price = formattedPrice.clean(),
        currencyCode = priceCurrencyCode,
        billingCycleCount = if (recurrenceMode == 2) billingCycleCount else 0,
        billingPeriod = billingPeriod,
        priceAmountMicros = priceAmountMicros,
        freeTrialPeriod = if (formattedPrice.equals("Free", true)) billingPeriod.toTrialDays() else 0
    )

    fun String?.clean(): String = this?.removeSuffix(".00").orEmpty()

    fun String?.toPlanTitle(): String = when (this) {
        "P1W" -> "Weekly"
        "P4W" -> "Four weeks"
        "P1M" -> "Monthly"
        "P2M" -> "2 months"
        "P3M" -> "3 months"
        "P4M" -> "4 months"
        "P6M" -> "6 months"
        "P8M" -> "8 months"
        "P1Y" -> "Yearly"
        else -> ""
    }

    private fun String?.toTrialDays(): Int = when (this) {
        "P1D" -> 1; "P2D" -> 2; "P3D" -> 3; "P4D" -> 4
        "P5D" -> 5; "P6D" -> 6; "P7D", "P1W" -> 7
        "P2W" -> 14; "P3W" -> 21; "P4W" -> 28; "P1M" -> 30
        else -> 0
    }
}
