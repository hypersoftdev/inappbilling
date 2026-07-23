package com.hypersoft.billing.model

/**
 * Created by: Sohaib Ahmed
 * Date: 7/23/2026
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/itssohaibahmed">LinkedIn</a>
 * - GitHub: <a href="https://github.com/itssohaibahmed">GitHub</a>
 */

/** Current state of the connection to the Google Play Billing service. */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
}