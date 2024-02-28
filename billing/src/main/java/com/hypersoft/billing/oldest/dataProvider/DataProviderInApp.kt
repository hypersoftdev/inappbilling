package com.hypersoft.billing.oldest.dataProvider


import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.hypersoft.billing.oldest.dataClasses.PurchaseDetail
import com.hypersoft.billing.common.dataClasses.ProductType
import java.text.SimpleDateFormat
import java.util.Date

internal class DataProviderInApp {

    private var productIdsList: List<String> = listOf()
    private var productDetailsList: List<ProductDetails> = listOf()

    /* ------------------------------------------------------ Product ID ------------------------------------------------------ */

    /**
     * @param productIdsList:   List of product Id's providing by the developer to check/retrieve-details if these products are existing in Google Play Console
     */

    fun setProductIdsList(productIdsList: List<String>) {
        this.productIdsList = productIdsList
    }

    fun getProductIdsList(): List<String> {
        return productIdsList
    }

    fun getProductList(): List<QueryProductDetailsParams.Product> {
        val arrayList = ArrayList<QueryProductDetailsParams.Product>()
        productIdsList.forEach {
            arrayList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build())
        }
        return arrayList.toList()
    }

    fun getDebugProductIDList(): List<String> = listOf(
        "android.test.item_unavailable"
    )

    fun getDebugProductIDsList(): List<String> = listOf(
        "android.test.item_unavailable",
        "android.test.refunded",
        "android.test.canceled",
        "android.test.purchased"
    )

    /* ---------------------------------------------------- Product Details ---------------------------------------------------- */

    fun setProductDetailsList(productDetailsList: List<ProductDetails>) {
        this.productDetailsList = productDetailsList
    }

    fun getProductDetail(): ProductDetails? {
        if (productDetailsList.isEmpty())
            return null
        return productDetailsList[0]
    }

    fun getProductDetailsList(): List<ProductDetails> {
        return productDetailsList
    }

    /* ---------------------------------------------------- Purchase Details ---------------------------------------------------- */

    fun getPurchaseDetail(simpleDateFormat: SimpleDateFormat, purchase: Purchase): PurchaseDetail {
        return PurchaseDetail(productType = ProductType.inapp, purchaseType = "Lifetime", purchaseTime = simpleDateFormat.format(Date(purchase.purchaseTime)))
    }
}