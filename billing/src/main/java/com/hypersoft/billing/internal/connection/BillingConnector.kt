package com.hypersoft.billing.internal.connection

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.hypersoft.billing.internal.BillingResponseCode
import com.hypersoft.billing.model.ConnectionState
import com.hypersoft.billing.utilities.Constants.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/** Wraps [BillingClient]'s connection lifecycle as a suspend call + observable [connectionState]. */
internal class BillingConnector(private val billingClient: BillingClient) {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    suspend fun connect(): ConnectionState {
        if (billingClient.isReady) {
            _connectionState.value = ConnectionState.CONNECTED
            return ConnectionState.CONNECTED
        }

        _connectionState.value = ConnectionState.CONNECTING
        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Log.d(TAG, "BillingConnector: onBillingServiceDisconnected")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    if (continuation.isActive) continuation.resume(ConnectionState.DISCONNECTED)
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    val state = if (BillingResponseCode(billingResult.responseCode).isOk) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
                    Log.d(TAG, "BillingConnector: onBillingSetupFinished: $state, ${billingResult.debugMessage}")
                    _connectionState.value = state
                    if (continuation.isActive) continuation.resume(state)
                }
            })
        }
    }
}
