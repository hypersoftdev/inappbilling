package com.hypersoft.billing.enums

/**
 *   Developer: Sohaib Ahmed
 *   Date: 10/3/2024
 *   Profile:
 *     -> github.com/epegasus
 *     -> linkedin.com/in/epegasus
 */

/**
 * The recurring mode of the pricing phase, which can be:
 * - FREE: Represents a free phase of the subscription.
 * - DISCOUNTED: Represents a discounted phase of the subscription for specific time period.
 * - ORIGINAL: Represents the original price phase of the subscription.
 */
enum class RecurringMode {
    FREE,
    DISCOUNTED,
    ORIGINAL
}