package com.hypersoft.billing.asd.domain

import com.hypersoft.billing.asd.data.repository.BillingRepository
import com.hypersoft.billing.asd.presentation.states.BillingState

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

internal class UseCaseConnection(private val repository: BillingRepository) {

    private var isConnecting = false

    fun startConnection(onResult: (Boolean, String?) -> Unit) {
        if (repository.isBillingClientReady) {
            repository.currentState = BillingState.ALREADY_CONNECTED
            onResult(true, null)
            return
        }

        if (isConnecting) {
            repository.currentState = BillingState.CONNECTING_IN_PROGRESS
            onResult(false, null)
            return
        }

        isConnecting = true
        repository.startConnection(onResult)
        isConnecting = false
    }
}