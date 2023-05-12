package com.hypersoft.billing.dataProvider

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams

internal class DataProviderSub {

    private var productDetailsList: List<ProductDetails> = listOf()

    /* ------------------------------------------------------ Product ID ------------------------------------------------------ */

    /**
     * @field productIdsList:   List of product Id's providing by the developer to check/retrieve-details if these products are existing in Google Play Console
     */

    val productIdsList = listOf(
        "basic_product_monthly",
        "basic_product_yearly"
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
}