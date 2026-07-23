package com.hypersoft.inappbilling

import android.app.Application
import com.hypersoft.billing.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BillingManager is meant to live exactly as long as the app process does, so it's created
 * here — once — rather than per-Activity. Connection is kicked off immediately so
 * `purchasesState`/`productsState` are already warm (or warming) by the time any screen needs them.
 */
class InAppBillingApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val billingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()

        billingManager
            .setNonConsumables(emptyList())
            .setConsumables(emptyList())
            .setSubscriptions(emptyList())

        appScope.launch { billingManager.connect() }
    }
}