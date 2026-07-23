package com.hypersoft.billing.utilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

internal fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy", locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(Date(this))
}

/* ——— List extension to overwrite contents ——— */
internal fun <T> MutableList<T>.setAll(newItems: List<T>) {
    clear(); addAll(newItems)
}