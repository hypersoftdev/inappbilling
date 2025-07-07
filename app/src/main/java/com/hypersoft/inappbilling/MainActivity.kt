package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.data.entities.product.ProductDetail
import com.hypersoft.billing.data.entities.purchase.PurchaseDetail
import com.hypersoft.billing.presentation.interfaces.BillingConnectionListener
import com.hypersoft.billing.presentation.interfaces.BillingProductDetailsListener
import com.hypersoft.billing.presentation.interfaces.BillingPurchaseHistoryListener
import com.hypersoft.billing.presentation.interfaces.BillingPurchaseListener

const val TAG = "MyTag"

class MainActivity : AppCompatActivity() {

    val inAppProductIdList = listOf(PRO_OCR_PRODUCT_ID, REMOVE_ADS_PRODUCT_ID)

    val subsProductIdList = listOf(
        OLD_APP_PRODUCT_MONTHLY, OLD_APP_PRODUCT_3MONTHS, OLD_APP_PRODUCT_YEARLY,               // Gen 1
        OLD_AI_CHAT_PRODUCT_WEEKLY, OLD_AI_CHAT_PRODUCT_MONTHLY, OLD_AI_CHAT_PRODUCT_YEARLY,    // Gen 2
        APP_PRODUCT_BRONZE, APP_PRODUCT_SILVER, APP_PRODUCT_GOLD,                               // Gen 3
        APP_SUB_WEEKLY, APP_SUB_MONTHLY, APP_SUB_YEARLY, APP_SUB_OFFER_YEARLY                   // Gen 4 (Current)
    )

    private val billingManager by lazy { BillingManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBilling()

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
        billingManager
            .setNonConsumables(inAppProductIdList)
            .setConsumables(emptyList())
            .setSubscriptions(subsProductIdList)
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
            override fun onError(message: String) {
                Log.e(TAG, "fetchPurchaseHistory: Error: $message")
            }

            override fun onSuccess(purchaseDetails: List<PurchaseDetail>) {
                Log.d(TAG, "fetchPurchaseHistory: onSuccess: $purchaseDetails")
            }
        })

        billingManager.fetchProductDetails(object : BillingProductDetailsListener {
            override fun onError(message: String) {
                Log.e(TAG, "fetchProductDetails: Error: $message")
            }

            override fun onSuccess(productDetails: List<ProductDetail>) {
                //Log.d(TAG, "fetchProductDetails: onSuccess: $productDetails")
                productDetails.forEach {
                    //Log.d(TAG, "fetchProductDetails: onSuccess: productDetail: $it")
                }
            }
        })

        billingManager.getProductDetail(APP_SUB_WEEKLY, APP_PLAN_MONTHLY, object : BillingProductDetailsListener {
            override fun onError(message: String) {
                Log.e(TAG, "getProductDetail: Error: $message")
            }

            override fun onSuccess(productDetails: List<ProductDetail>) {
                Log.d(TAG, "fetchProductDetail: onSuccess: $productDetails")
                productDetails.forEach {
                    Log.d(TAG, "fetchProductDetail: onSuccess: productDetail: $it")
                }
            }
        })
    }

    private fun onPurchaseClick() {
        // In-App
        billingManager.purchaseInApp(this, REMOVE_ADS_PRODUCT_ID, purchaseListener)

        // Subscription
        //billingManager.purchaseSubs(this, APP_SUB_WEEKLY, SUB_PLAN_WEEKLY, purchaseListener)
    }

    private val purchaseListener = object : BillingPurchaseListener {
        override fun onPurchaseResult(message: String) {
            Log.d(TAG, "purchaseListener: onPurchaseResult: message: $message")
        }

        override fun onError(message: String) {
            Log.e(TAG, "purchaseListener: onError: message: $message")
        }

    }

    companion object {

        /**
         * Old InApp Products
         */
        const val REMOVE_ADS_PRODUCT_ID = "com.mobiletranstorapps.all.languages.translator"
        const val PRO_OCR_PRODUCT_ID = "ocr_awarded_points"


        /**
         * Old Subscription for App (Product Ids & Plan Ids)
         */
        const val OLD_APP_PRODUCT_MONTHLY = "1_month_subscription"
        const val OLD_APP_PRODUCT_3MONTHS = "3_months_subscription"
        const val OLD_APP_PRODUCT_YEARLY = "1_year_subscription"

        const val OLD_APP_PLAN_MONTHLY = "p1m"
        const val OLD_APP_PLAN_3MONTHS = "p3m"
        const val OLD_APP_PLAN_YEARLY = "p1y"

        /**
         * Old Subscription for ChatGPT (Product Ids & Plan Ids)
         */
        const val OLD_AI_CHAT_PRODUCT_WEEKLY = "basic_product_weekly"
        const val OLD_AI_CHAT_PRODUCT_MONTHLY = "basic_product_monthly"
        const val OLD_AI_CHAT_PRODUCT_YEARLY = "basic_product_yearly"

        const val OLD_AI_CHAT_PLAN_WEEKLY = "basic-plan-weekly"
        const val OLD_AI_CHAT_PLAN_MONTHLY = "basic-plan-monthly"
        const val OLD_AI_CHAT_PLAN_YEARLY = "basic-plan-yearly"

        /**
         * NEW Currently implemented App Subscription (Product Ids & Plan Ids)
         */
        const val APP_PRODUCT_BRONZE = "app_product_bronze"
        const val APP_PRODUCT_SILVER = "app_product_silver"
        const val APP_PRODUCT_GOLD = "app_product_gold"

        const val APP_PLAN_MONTHLY = "app-plan-monthly"
        const val APP_PLAN_3MONTHS = "app-plan-3months"
        const val APP_PLAN_YEARLY = "app-plan-yearly"

        /**
         * NEW Currently implemented App Subscription (Product Ids & Plan Ids) By Hamza Arshad
         */
        const val APP_SUB_WEEKLY = "app_sub_weekly"
        const val APP_SUB_MONTHLY = "app_sub_monthly"
        const val APP_SUB_YEARLY = "app_sub_yearly"
        const val APP_SUB_OFFER_YEARLY = "app_sub_offer_yearly"

        const val SUB_PLAN_WEEKLY = "sub-plan-weekly"
        const val SUB_PLAN_MONTHLY = "sub-plan-monthly"
        const val SUB_PLAN_YEARLY = "sub-plan-yearly"
        const val SUB_PLAN_YEARLY_OFFER = "sub-plan-yearly-offer"
    }

}