package com.hypersoft.billing.presentation.interfaces

import com.hypersoft.billing.data.entities.product.ProductDetail

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

interface BillingProductDetailsListener {
    fun onSuccess(productDetails: List<ProductDetail>)
    fun onError(message: String) {}
}