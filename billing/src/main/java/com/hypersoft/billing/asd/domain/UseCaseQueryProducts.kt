package com.hypersoft.billing.asd.domain

import com.android.billingclient.api.ProductDetails
import com.hypersoft.billing.asd.data.repository.BillingRepository
import com.hypersoft.billing.asd.entities.product.PricingPhase
import com.hypersoft.billing.asd.entities.product.ProductDetail
import com.hypersoft.billing.asd.entities.product.RecurringMode
import com.hypersoft.billing.asd.enums.ProductType
import com.hypersoft.billing.asd.states.BillingState
import com.hypersoft.billing.asd.states.QueryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

internal class UseCaseQueryProducts(private val repository: BillingRepository) {

    private val _productDetailList = mutableListOf<ProductDetail>()
    private val productDetailList: List<ProductDetail> get() = _productDetailList.toList()

    private val mutex = Mutex()
    private var isQueried = false

    suspend fun queryProducts(
        nonConsumableIds: List<String>,
        consumableIds: List<String>,
        subscriptionIds: List<String>
    ): QueryResponse<List<ProductDetail>> = withContext(Dispatchers.Default) {

        /* ─── Guard clauses ─────────────────────────── */
        if (!repository.isBillingClientReady) {
            repository.currentState = BillingState.CONNECTION_INVALID
            return@withContext QueryResponse.Error("Play Billing not ready. Try again.")
        }

        if (nonConsumableIds.isEmpty() && consumableIds.isEmpty() && subscriptionIds.isEmpty()) {
            repository.currentState = BillingState.USER_QUERY_LIST_EMPTY
            return@withContext QueryResponse.Error("No product IDs provided.")
        }

        /* ─── Ensure single flight using mutex ───────── */
        if (!mutex.tryLock()) return@withContext QueryResponse.Loading
        repository.currentState = BillingState.FETCHING_PRODUCTS
        isQueried = true

        val result = runCatching {
            coroutineScope {

                /* Fire Play queries in parallel */
                val inAppNonDeferred = nonConsumableIds
                    .takeIf { it.isNotEmpty() }
                    ?.let { async { repository.queryInAppProductDetails(it) } }

                val inAppConDeferred = consumableIds
                    .takeIf { it.isNotEmpty() }
                    ?.let { async { repository.queryInAppProductDetails(it) } }

                val subDeferred = subscriptionIds
                    .takeIf { it.isNotEmpty() }
                    ?.let { async { repository.querySubsProductDetails(it) } }

                val inAppNon = inAppNonDeferred?.await().orEmpty()
                val inAppCon = inAppConDeferred?.await().orEmpty()
                val subs = subDeferred?.await().orEmpty()

                inAppNon.map { it.toDomain(ProductType.inapp) } +
                        inAppCon.map { it.toDomain(ProductType.inapp) } +
                        subs.flatMap { it.toDomainList() }
            }
        }

        val response = result.fold(
            onSuccess = {
                _productDetailList.clear()
                _productDetailList.addAll(it)

                repository.currentState = BillingState.FETCHING_PRODUCTS_SUCCESS
                QueryResponse.Success(it)
            },
            onFailure = {
                repository.currentState = BillingState.FETCHING_PRODUCTS_FAILED
                QueryResponse.Error("Failed to fetch product details: ${it.message}")
            }
        )

        mutex.unlock()

        response
    }

    suspend fun queryProducts(productId: String, planId: String?): QueryResponse<List<ProductDetail>> = withContext(Dispatchers.Default) {

        if (!isQueried) {
            return@withContext QueryResponse.Error("Product details haven’t been fetched yet. Call fetchProductDetails() first.")
        }

        mutex.withLock {
            val found = productDetailList.firstOrNull { detail ->
                detail.productId == productId &&
                        (planId == null || detail.planId == planId)
            }

            return@withLock when (found) {
                null -> QueryResponse.Error("Product not found")
                else -> QueryResponse.Success(listOf(found))
            }
        }
    }

    /* ─── mapping helpers ─────────────────────────────────── */

    private fun ProductDetails.toDomain(type: ProductType) = ProductDetail(
        productId = productId,
        planId = "",
        productTitle = title,
        productType = type,
        pricingDetails = listOf(
            PricingPhase(
                recurringMode = RecurringMode.ORIGINAL,
                price = oneTimePurchaseOfferDetails?.formattedPrice.clean(),
                currencyCode = oneTimePurchaseOfferDetails?.priceCurrencyCode.orEmpty(),
                planTitle = "",
                billingCycleCount = 0,
                billingPeriod = "",
                priceAmountMicros = oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L,
                freeTrialPeriod = 0
            )
        )
    )

    /** Subscriptions may contain multiple offers (weekly, monthly, etc.). */
    private fun ProductDetails.toDomainList(): List<ProductDetail> = subscriptionOfferDetails?.map { offer ->
        ProductDetail(
            productId = productId,
            planId = offer.basePlanId,
            productTitle = title,
            productType = ProductType.subs,
            pricingDetails = offer.pricingPhases.pricingPhaseList.map { it.toPhase() }
        )
    }.orEmpty()

    private fun ProductDetails.PricingPhase.toPhase() = PricingPhase(
        recurringMode = when {
            formattedPrice.equals("Free", true) -> RecurringMode.FREE
            recurrenceMode == 2 -> RecurringMode.DISCOUNTED
            else -> RecurringMode.ORIGINAL
        },
        planTitle = billingPeriod.toPlanTitle(),
        price = formattedPrice.clean(),
        currencyCode = priceCurrencyCode,
        billingCycleCount = if (recurrenceMode == 2) billingCycleCount else 0,
        billingPeriod = billingPeriod,
        priceAmountMicros = priceAmountMicros,
        freeTrialPeriod = if (formattedPrice.equals("Free", true)) billingPeriod.toTrialDays() else 0
    )

    /* ─── small extension helpers ─────────────────────────── */

    private fun String?.clean(): String = this?.removeSuffix(".00").orEmpty()

    private fun String?.toPlanTitle(): String = when (this) {
        "P1W" -> "Weekly"
        "P4W" -> "Four weeks"
        "P1M" -> "Monthly"
        "P2M" -> "2 months"
        "P3M" -> "3 months"
        "P4M" -> "4 months"
        "P6M" -> "6 months"
        "P8M" -> "8 months"
        "P1Y" -> "Yearly"
        else -> ""
    }

    private fun String?.toTrialDays(): Int = when (this) {
        "P1D" -> 1; "P2D" -> 2; "P3D" -> 3; "P4D" -> 4
        "P5D" -> 5; "P6D" -> 6; "P7D", "P1W" -> 7
        "P2W" -> 14; "P3W" -> 21; "P4W" -> 28; "P1M" -> 30
        else -> 0
    }
}