package com.hypersoft.billing.utilities.extensions

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

/* ——— List extension to overwrite contents ——— */
internal fun <T> MutableList<T>.setAll(newItems: List<T>) {
    clear(); addAll(newItems)
}