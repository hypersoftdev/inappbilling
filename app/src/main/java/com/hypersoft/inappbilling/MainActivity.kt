package com.hypersoft.inappbilling

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hypersoft.billing.BillingManager
import com.hypersoft.billing.status.State

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
        if (BuildConfig.DEBUG) {
            billingManager.startConnection(billingManager.getDebugProductIDList()) { isConnectionEstablished, alreadyPurchased, message ->
                showMessage(message)
                if (alreadyPurchased) {
                    // Save settings for purchased product
                }
            }
        } else {
            billingManager.startConnection(listOf(productId)) { isConnectionEstablished, alreadyPurchased, message ->
                showMessage(message)
                if (alreadyPurchased) {
                    // Save settings for purchased product
                }
            }
        }
    }


    private fun onPurchaseClick() {
        billingManager.makePurchase(this) { isSuccess, message ->
            showMessage(message)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}