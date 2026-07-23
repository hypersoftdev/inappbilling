package com.hypersoft.billing.internal.cache

import com.hypersoft.billing.model.ProductDetail
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/** Mutex-guarded, in-memory cache of the last successful [ProductRepository.queryAll] result. */
internal class ProductCache {

    private val mutex = Mutex()

    private var products: List<ProductDetail> = emptyList()
    private var isPopulated = false

    suspend fun replaceAll(list: List<ProductDetail>) = mutex.withLock {
        products = list
        isPopulated = true
    }

    /**
     * @param offerId When null, prefers the plan's default offer (empty [ProductDetail.offerId]),
     *   falling back to whichever offer is available. When non-null, matches that specific offer exactly.
     */
    suspend fun find(productId: String, planId: String?, offerId: String? = null): ProductDetail? = mutex.withLock {
        if (!isPopulated) return@withLock null
        val matches = products.filter { it.productId == productId && (planId == null || it.planId == planId) }
        when (offerId) {
            null -> matches.firstOrNull { it.offerId.isEmpty() } ?: matches.firstOrNull()
            else -> matches.firstOrNull { it.offerId == offerId }
        }
    }
}
