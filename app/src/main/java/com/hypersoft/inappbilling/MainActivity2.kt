package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.oldest.constants.SubscriptionPlans
import com.hypersoft.billing.oldest.constants.SubscriptionProductIds
import com.hypersoft.billing.oldest.dataClasses.ProductDetail
import com.hypersoft.billing.oldest.dataClasses.PurchaseDetail
import com.hypersoft.billing.oldest.helper.BillingHelper.Companion.TAG
import com.hypersoft.billing.oldest.interfaces.OnPurchaseListener
import com.hypersoft.billing.oldest.status.State
import com.hypersoft.billing.oldest.interfaces.OnConnectionListener

class MainActivity2 : AppCompatActivity() {

    /*private val billingManager by lazy { BillingManager(this) }
    private lateinit var tvTitle: TextView

    // mostly people use package name in their
    private val productId: String = "Paste your original Product ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBilling()
        initObserver()

        tvTitle = findViewById(R.id.tv_title)
        findViewById<Button>(R.id.btn_purchase).setOnClickListener {
            onPurchaseClick()
        }
    }

    private fun initObserver() {
        State.billingState.observe(this) {
            Log.d("BillingManager", "initObserver: $it")
            tvTitle.text = it.toString()
        }

        //Observe Your Products
        billingManager.productDetailsLiveData.observe(this) { list ->
            var month = 0L
            var year = 0L
            list.forEach { productDetail: ProductDetail ->
                Log.d(TAG, "initObservers: $productDetail")
                when (productDetail.productId) {
                    SubscriptionProductIds.basicProductMonthly -> {
                        //binding.mtvOfferPrice1.text = productDetail.price
                        month = productDetail.priceAmountMicros / 1000000
                    }

                    SubscriptionProductIds.basicProductYearly -> {
                        //binding.mtvOfferPrice2.text = productDetail.price
                        year = productDetail.priceAmountMicros / 1000000
                    }

                    SubscriptionProductIds.basicProductSemiYearly -> {
                        //binding.mtvOfferPrice3Premium.text = productDetail.price
                    }

                    productId -> {
                        //binding.mtvOfferPrice3Premium.text = productDetail.price
                    }
                }
            }
            // Best Offer
            if (month == 0L || year == 0L) return@observe
            val result = 100 - (year * 100 / (12 * month))
            val text = "Save $result%"
            //binding.mtvBestOffer.text = text

            val perMonth = (year / 12L).toString()
            //binding.mtvOffer.text = perMonth
        }
    }

    private fun initBilling() {
        billingManager.setCheckForSubscription(true)
        if (BuildConfig.DEBUG) {
            billingManager.startConnection(billingManager.getDebugProductIDList(), object : OnConnectionListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    showMessage(message)
                    Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                }

                override fun onOldPurchaseResult(isPurchased: Boolean, purchaseDetail: PurchaseDetail?) {
                    // Update your shared-preferences here!
                    Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                }
            })
        } else {
            billingManager.startConnection(listOf(productId), object : OnConnectionListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    showMessage(message)
                    Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                }

                override fun onOldPurchaseResult(isPurchased: Boolean, purchaseDetail: PurchaseDetail?) {
                    // Update your shared-preferences here!
                    Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                }
            })
        }
    }


    private fun onPurchaseClick() {
        // In-App
        *//*billingManager.makeInAppPurchase(this, object : OnPurchaseListener {
            override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                showMessage(message)
            }
        })*//*

        // Subscription
        billingManager.makeSubPurchase(this, SubscriptionPlans.basicPlanMonthly, object : OnPurchaseListener {
            override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                showMessage(message)
            }
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }*/
}