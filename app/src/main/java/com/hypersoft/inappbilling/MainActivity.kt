package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.dataClasses.PurchaseDetail
import com.hypersoft.billing.interfaces.BillingListener
import com.hypersoft.billing.interfaces.OnPurchaseListener

const val TAG = "MyTag"

class MainActivity : AppCompatActivity() {

    private val billingManager by lazy { BillingManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBilling()
        initObserver()

        findViewById<Button>(R.id.btn_purchase).setOnClickListener { onPurchaseClick() }
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

    private fun initBilling() {
        val productIds = when (BuildConfig.DEBUG) {
            true -> listOf(billingManager.getDebugProductIDList())
            false -> listOf("abc", "def")
        }

        billingManager.initialize(
            productInAppPurchases = productIds,
            productSubscriptions = listOf("Bronze", "Silver", "Gold", "Yearly"),
            billingListener = billingListener
        )
    }

    private fun initObserver() {
        billingManager.productDetailsLiveData.observe(this) { productDetailList ->
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

    private fun onPurchaseClick() {
        // In-App
        billingManager.makeInAppPurchase(this, billingManager.getDebugProductIDList(), onPurchaseListener)

        // Subscription
        //billingManager.makeSubPurchase(this, "product_abc", "plan_abc", onPurchaseListener)
    }

    private val onPurchaseListener = object : OnPurchaseListener {
        override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
            showMessage(message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroyBilling()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}