package com.hypersoft.billing.internal

import com.android.billingclient.api.BillingClient

/**
 * Created by: Sohaib Ahmed
 * Date: 7/7/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

@JvmInline
internal value class BillingResponseCode(private val code: Int) {

    val isOk: Boolean get() = code == BillingClient.BillingResponseCode.OK
    val isUserCancelled: Boolean get() = code == BillingClient.BillingResponseCode.USER_CANCELED
    val isAlreadyOwned: Boolean get() = code == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
}