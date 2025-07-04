package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hypersoft.billing.asd.BillingManager
import com.hypersoft.billing.asd.entities.PurchaseDetail
import com.hypersoft.billing.asd.entities.product.ProductDetail
import com.hypersoft.billing.asd.interfaces.BillingConnectionListener
import com.hypersoft.billing.asd.interfaces.BillingProductDetailsListener
import com.hypersoft.billing.asd.interfaces.BillingPurchaseHistoryListener

/**
 * Created by: Sohaib Ahmed
 * Date: 7/3/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

class Asd : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBilling()
    }

    private fun initBilling() {
        val billingManager = BillingManager(this, lifecycleScope)
        billingManager
            .setNonConsumables(emptyList())
            .setConsumables(emptyList())
            .setSubscriptions(emptyList())
            .setListener(object : BillingConnectionListener {
                override fun onBillingClientConnected(isSuccess: Boolean, message: String) {
                    Log.d(TAG, "onBillingClientConnected: isSuccess: $isSuccess, message: $message")
                    fetchData(billingManager)
                }
            })
            .startConnection()
    }

    private fun fetchData(billingManager: BillingManager) {
        billingManager.fetchPurchaseHistory(object : BillingPurchaseHistoryListener {
            override fun onSuccess(purchaseDetails: List<PurchaseDetail>) {

            }

            override fun onError(message: String) {

            }
        })
        billingManager.fetchProductDetails(object : BillingProductDetailsListener {
            override fun onSuccess(productDetails: List<ProductDetail>) {

            }

            override fun onError(message: String) {

            }
        })
    }
}