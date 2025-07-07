package com.hypersoft.billing.asd.presentation.interfaces

/**
 * Created by: Sohaib Ahmed
 * Date: 7/7/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

interface BillingPurchaseListener {
    fun onPurchaseResult(message: String)
    fun onError(message: String)
}