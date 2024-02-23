package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.oldest.dataClasses.PurchaseDetail
import com.hypersoft.billing.latest.interfaces.BillingListener

const val TAG = "MyTag"

class MainActivity : AppCompatActivity() {

    private val billingManager by lazy {
        BillingManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBilling()
    }

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
        billingManager.initialize(
            productInAppPurchases = listOf("abc", "def"),
            productSubscriptions = listOf("Bronze", "Silver", "Gold", "Yearly"),
            billingListener = billingListener
        )
    }

    private val billingListener = object : BillingListener {
        override fun onConnectionResult(isSuccess: Boolean, message: String) {
            Log.d(TAG, "onConnectionResult: isSuccess: $isSuccess, message: $message")
            if (!isSuccess) {
                proceedApp()
            }
        }

        override fun purchaseHistoryResult(isPurchased: Boolean, purchaseDetail: PurchaseDetail?) {

        }
    }

    private fun proceedApp() {
        // your code here...
    }
}