package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.interfaces.OnPurchaseListener
import com.hypersoft.billing.status.State
import com.hypersoft.billing.status.SubscriptionTags
import dev.epegasus.billinginapppurchases.interfaces.OnConnectionListener

class MainActivity : AppCompatActivity() {

    private val billingManager by lazy { BillingManager(this) }
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
    }

    private fun initBilling() {
        billingManager.setCheckForSubscription(true)
        if (BuildConfig.DEBUG) {
            billingManager.startConnection(billingManager.getDebugProductIDList(), object : OnConnectionListener {
                override fun onConnectionResult(isSuccess: Boolean, message: String) {
                    showMessage(message)
                    Log.d("TAG", "onConnectionResult: $isSuccess - $message")
                }

                override fun onOldPurchaseResult(isPurchased: Boolean) {
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

                override fun onOldPurchaseResult(isPurchased: Boolean) {
                    // Update your shared-preferences here!
                    Log.d("TAG", "onOldPurchaseResult: $isPurchased")
                }
            })
        }
    }


    private fun onPurchaseClick() {
        // In-App
        billingManager.makeInAppPurchase(this, object : OnPurchaseListener {
            override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                showMessage(message)
            }
        })

        // Subscription
        billingManager.makeSubPurchase(this, SubscriptionTags.basicMonthly, object : OnPurchaseListener {
            override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
                showMessage(message)
            }
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}