package com.hypersoft.billing.model

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/**
 * Result of a purchase/update-subscription attempt.
 *
 * Kept as a dedicated sealed type instead of [Result] because "user cancelled" and
 * "already owned" are expected outcomes, not errors — callers shouldn't have to
 * string-match a failure message to tell them apart from a real failure.
 */
sealed interface PurchaseOutcome {
    /** Purchase acknowledged/consumed successfully. Call [com.hypersoft.billing.BillingManager.queryPurchaseHistory] for the up-to-date, typed purchase list. */
    data class Success(val message: String) : PurchaseOutcome
    data object AlreadyOwned : PurchaseOutcome
    data object UserCancelled : PurchaseOutcome
    data class Failed(val message: String, val responseCode: Int) : PurchaseOutcome
}