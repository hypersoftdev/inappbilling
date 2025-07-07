package com.hypersoft.billing.data.entities.product

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

/**
 * Defines the type of a subscription pricing phase.
 *
 * A subscription may transition through multiple pricing modes over time,
 * and this enum distinguishes between those modes:
 *
 * - [FREE]       → A 100% free trial period, typically limited in duration.
 * - [DISCOUNTED] → A temporarily reduced price (e.g., intro offer).
 * - [ORIGINAL]   → The full, standard price after any trials or discounts.
 */
enum class RecurringMode {
    FREE,
    DISCOUNTED,
    ORIGINAL
}