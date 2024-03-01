package com.hypersoft.billing.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @Author: SOHAIB AHMED
 * @Date: 22/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

internal fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy", locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(Date(this))
}