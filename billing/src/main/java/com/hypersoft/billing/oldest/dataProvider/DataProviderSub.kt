package com.hypersoft.billing.oldest.dataProvider

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.hypersoft.billing.oldest.constants.SubscriptionProductIds
import com.hypersoft.billing.oldest.dataClasses.PurchaseDetail
import com.hypersoft.billing.common.dataClasses.ProductType
import java.text.SimpleDateFormat
import java.util.Date

internal class DataProviderSub {

    private var productDetailsList: List<ProductDetails> = listOf()

    /* ------------------------------------------------------ Product ID ------------------------------------------------------ */

    /**
     * @field productIdsList:   List of product Id's providing by the developer to check/retrieve-details if these products are existing in Google Play Console
     */

    val productIdsList = listOf(
        SubscriptionProductIds.basicProductWeekly,
        SubscriptionProductIds.basicProductFourWeeks,
        SubscriptionProductIds.basicProductMonthly,
        SubscriptionProductIds.basicProductQuarterly,
        SubscriptionProductIds.basicProductSemiYearly,
        SubscriptionProductIds.basicProductYearly,
        SubscriptionProductIds.basicProductLifeTime,
    )

    fun getProductList(): List<QueryProductDetailsParams.Product> {
        val arrayList = ArrayList<QueryProductDetailsParams.Product>()
        productIdsList.forEach {
            arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build())
        }
        return arrayList.toList()
    }

    /* ---------------------------------------------------- Product Details ---------------------------------------------------- */

    fun setProductDetailsList(productDetailsList: List<ProductDetails>) {
        this.productDetailsList = productDetailsList
    }

    fun getProductDetailsList(): List<ProductDetails> {
        return productDetailsList
    }


    /* ---------------------------------------------------- Purchase Details ---------------------------------------------------- */

    fun getPurchaseDetail(simpleDateFormat: SimpleDateFormat, purchase: Purchase): PurchaseDetail {
        val purchaseType = when (purchase.products[0]) {
            SubscriptionProductIds.basicProductWeekly -> "Weekly"
            SubscriptionProductIds.basicProductFourWeeks -> "Four Weeks"
            SubscriptionProductIds.basicProductMonthly -> "Monthly"
            SubscriptionProductIds.basicProductQuarterly -> "Quarterly"
            SubscriptionProductIds.basicProductSemiYearly -> "06 Months"
            SubscriptionProductIds.basicProductYearly -> "Yearly"
            else -> "-"
        }
        return PurchaseDetail(productType = ProductType.SUBS, purchaseType = purchaseType, purchaseTime = simpleDateFormat.format(Date(purchase.purchaseTime)))
    }
}