package com.hypersoft.billing.model

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/** Backs [com.hypersoft.billing.BillingManager]'s `productsState`/`purchasesState` — populated automatically after `connect()`. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val responseCode: Int? = null) : UiState<Nothing>
}