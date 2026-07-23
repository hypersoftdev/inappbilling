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
 * Carried inside `Result.failure(...)` for the query-style [com.hypersoft.billing.BillingManager]
 * calls, so callers can inspect [responseCode] instead of string-matching [message].
 */
class BillingException(val responseCode: Int, message: String) : Exception(message)
