package com.hypersoft.billing.asd.extensions

import com.android.billingclient.api.ProductDetails.PricingPhase
import com.android.billingclient.api.ProductDetails.PricingPhases

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

internal fun PricingPhases?.originalPhase(): PricingPhase? = this?.pricingPhaseList?.firstOrNull { phase ->
    phase.priceAmountMicros > 0 && phase.billingPeriod != "P0D"
}