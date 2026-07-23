package com.hypersoft.billing.internal.products

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.hypersoft.billing.internal.cache.ProductCache
import com.hypersoft.billing.internal.mapper.ProductMapper
import com.hypersoft.billing.model.BillingException
import com.hypersoft.billing.model.ProductDetail
import com.hypersoft.billing.utilities.Constants.TAG
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

/** Queries and caches [ProductDetail]s for the non-consumable/consumable/subscription IDs the app registered. */
internal class ProductRepository(
    private val billingClient: BillingClient,
    private val cache: ProductCache
) {

    private val inApp = BillingClient.ProductType.INAPP
    private val subs = BillingClient.ProductType.SUBS
    private val mutex = Mutex()

    suspend fun queryAll(nonConsumableIds: List<String>, consumableIds: List<String>, subscriptionIds: List<String>): Result<List<ProductDetail>> {
        if (!billingClient.isReady) {
            return Result.failure(BillingException(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED, "Play Billing not ready. Try again."))
        }
        if (nonConsumableIds.isEmpty() && consumableIds.isEmpty() && subscriptionIds.isEmpty()) {
            return Result.failure(BillingException(BillingClient.BillingResponseCode.DEVELOPER_ERROR, "No product IDs provided."))
        }

        return mutex.withLock {
            coroutineScope {
                runCatching {
                    val inAppNonDeferred = nonConsumableIds.takeIf { it.isNotEmpty() }?.let { async { queryInAppProductDetails(it) } }
                    val inAppConDeferred = consumableIds.takeIf { it.isNotEmpty() }?.let { async { queryInAppProductDetails(it) } }
                    val subsDeferred = subscriptionIds.takeIf { it.isNotEmpty() }?.let { async { querySubsProductDetails(it) } }

                    val inAppNon = inAppNonDeferred?.await().orEmpty()
                    val inAppCon = inAppConDeferred?.await().orEmpty()
                    val subsResult = subsDeferred?.await().orEmpty()

                    inAppNon.flatMap { ProductMapper.toOneTimeProducts(it) } +
                        inAppCon.flatMap { ProductMapper.toOneTimeProducts(it) } +
                        subsResult.flatMap { ProductMapper.toSubscriptionProducts(it) }
                }.fold(
                    onSuccess = {
                        cache.replaceAll(it)
                        Result.success(it)
                    },
                    onFailure = {
                        Log.e(TAG, "ProductRepository: queryAll: ", it)
                        Result.failure(BillingException(BillingClient.BillingResponseCode.ERROR, "Failed to fetch product details: ${it.message}"))
                    }
                )
            }
        }
    }

    suspend fun find(productId: String, planId: String?, offerId: String? = null): Result<ProductDetail> {
        val found = cache.find(productId, planId, offerId)
            ?: return Result.failure(BillingException(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE, "Product not found. Call refreshProducts() first."))
        return Result.success(found)
    }

    suspend fun queryInAppProductDetails(productIds: List<String>): List<ProductDetails> {
        val productList = productIds.map { productId -> QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(inApp).build() }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val result = billingClient.queryProductDetails(params)
        Log.i(TAG, "ProductRepository: queryInAppProductDetails: ProductDetailsList: ${result.productDetailsList}, Error: ${result.billingResult.debugMessage}")
        return result.productDetailsList.orEmpty()
    }

    suspend fun querySubsProductDetails(productIds: List<String>): List<ProductDetails> {
        val productList = productIds.map { productId -> QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(subs).build() }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val result = billingClient.queryProductDetails(params)
        Log.i(TAG, "ProductRepository: querySubsProductDetails: ProductDetailsList: ${result.productDetailsList}, Error: ${result.billingResult.debugMessage}")
        return result.productDetailsList.orEmpty()
    }
}