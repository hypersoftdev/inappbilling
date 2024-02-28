package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.common.interfaces.OnPurchaseListener
import com.hypersoft.billing.latest.dataClasses.PurchaseDetail
import com.hypersoft.billing.latest.interfaces.BillingListener
import com.hypersoft.billing.oldest.constants.SubscriptionPlans
import com.hypersoft.billing.oldest.constants.SubscriptionProductIds
import com.hypersoft.billing.oldest.dataClasses.ProductDetail
import com.hypersoft.billing.oldest.interfaces.OnConnectionListener
import com.hypersoft.billing.oldest.status.State

const val TAG = "MyTag"

class MainActivity : AppCompatActivity() {

    private val billingManager by lazy { BillingManager(this) }

    // Old way
    private lateinit var tvTitle: TextView
    private val productId: String = "Paste your original Product ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newWay()
        oldWay()
    }

    /* -------------------------------------------------- New Way -------------------------------------------------- */

    /**
     *  Suppose there's been a subscription structure as follow: - ProductID, -- PlanID
     *   - Bronze
     *      -- Weekly
     *      -- Monthly
     *      -- Quarterly (3 Months)
     *      -- Yearly
     *   - Silver
     *      -- Weekly
     *      -- Monthly
     *      -- Quarterly (3 Months)
     *      -- Yearly
     *   - Gold
     *      -- Weekly
     *      -- Monthly
     *      -- Quarterly (3 Months)
     *      -- Yearly
     *   - Platinum
     *      -- Weekly
     *      -- Monthly
     *      -- Quarterly (3 Months)
     *      -- Yearly
     */

    private fun newWay() {
        val productIds = when (BuildConfig.DEBUG) {
            true -> listOf(billingManager.debugProductId)
            false -> listOf("abc", "def")
        }

        billingManager.initialize(
            productInAppPurchases = productIds,
            productSubscriptions = listOf("Bronze", "Silver", "Gold", "Yearly"),
            billingListener = billingListener
        )
        initNewObserver()
    }

    private fun initNewObserver() {
        billingManager.observeQueryProducts().observe(this) { productDetailList ->
            Log.d(TAG, "initNewObserver: --------------------------------------")
            productDetailList.forEach { productDetail ->
                Log.d(TAG, "---: $productDetail")
            }
        }
    }

    private val billingListener = object : BillingListener {
        override fun onConnectionResult(isSuccess: Boolean, message: String) {
            Log.d(TAG, "onConnectionResult: isSuccess: $isSuccess, message: $message")
            if (!isSuccess) {
                proceedApp()
            }
        }

        override fun purchasesResult(purchaseDetailList: List<PurchaseDetail>) {
            proceedApp()
        }
    }

    private fun proceedApp() {
        // your code here...
    }

    private fun onPurchaseNewClick() {
        // In-App
        billingManager.makeInAppPurchase(this, billingManager.debugProductId, newPurchaseListener)

        // Subscription
        billingManager.makeSubPurchase(this, "product_abc", "plan_abc", newPurchaseListener)
    }

    private val newPurchaseListener = object : OnPurchaseListener {
        override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
            showMessage(message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroyBilling()
    }

    /* -------------------------------------------------- Old Way -------------------------------------------------- */

    private fun oldWay() {
        initBilling()
        initOldObserver()

        tvTitle = findViewById(R.id.tv_title)
        findViewById<Button>(R.id.btn_purchase).setOnClickListener {
            onPurchaseClick()
        }
    }

    private fun initOldObserver() {
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
        val productIds = when (BuildConfig.DEBUG) {
            true -> billingManager.getDebugProductIDList()
            false -> listOf(productId)
        }

        if (BuildConfig.DEBUG) {
            billingManager.startConnection(productIds, object : OnConnectionListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    showMessage(message)
                    Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                }

                override fun onOldPurchaseResult(isPurchased: Boolean, purchaseDetail: com.hypersoft.billing.oldest.dataClasses.PurchaseDetail?) {
                    // Update your shared-preferences here!
                    Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                }
            })
        } else {
            billingManager.startConnection(productIds, object : OnConnectionListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    showMessage(message)
                    Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                }

                override fun onOldPurchaseResult(isPurchased: Boolean, purchaseDetail: com.hypersoft.billing.oldest.dataClasses.PurchaseDetail?) {
                    // Update your shared-preferences here!
                    Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                }
            })
        }
    }


    private fun onPurchaseClick() {
        // In-App
        billingManager.makeInAppPurchase(this, oldPurchaseListener)

        // Subscription
        billingManager.makeSubPurchase(this, SubscriptionPlans.basicPlanMonthly, oldPurchaseListener)
    }

    private val oldPurchaseListener = object : OnPurchaseListener {
        override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
            showMessage(message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}