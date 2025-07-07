package com.hypersoft.billing.asd.presentation.states

/**
 * Created by: Sohaib Ahmed
 * Date: 7/4/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

sealed class QueryResponse<out T> {
    object Loading : QueryResponse<Nothing>()
    data class Success<out T>(val data: T) : QueryResponse<T>()
    data class Error(val errorMessage: String) : QueryResponse<Nothing>()
}