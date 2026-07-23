package com.hypersoft.billing.internal.mapper

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.PricingPhases
import com.android.billingclient.api.Purchase
import com.hypersoft.billing.internal.mapper.ProductMapper.toPlanTitle
import com.hypersoft.billing.model.ProductType
import com.hypersoft.billing.model.PurchaseDetail
import com.hypersoft.billing.utilities.toFormattedDate

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/** Single source of truth for mapping Play Billing's [Purchase] into the library's public [PurchaseDetail] model. */
internal object PurchaseMapper {

    fun Purchase.toPurchaseDetails(productDetailsList: List<ProductDetails>, type: ProductType): List<PurchaseDetail> = products.mapNotNull { id ->
        val productDetails = productDetailsList.firstOrNull { it.productId == id }
        val offer = productDetails?.subscriptionOfferDetails?.firstOrNull()
        val planTitle = offer?.pricingPhases?.originalPhase()?.billingPeriod.toPlanTitle()

        PurchaseDetail(
            productId = id,
            planId = offer?.basePlanId,
            productTitle = productDetails?.title,
            planTitle = planTitle,
            purchaseToken = purchaseToken,
            productType = type,
            purchaseTime = purchaseTime.toFormattedDate(),
            purchaseTimeMillis = purchaseTime,
            isAutoRenewing = isAutoRenewing,
            isAcknowledged = isAcknowledged
        )
    }

    private fun PricingPhases.originalPhase() = pricingPhaseList.firstOrNull { it.priceAmountMicros > 0 && it.billingPeriod != "P0D" }
}