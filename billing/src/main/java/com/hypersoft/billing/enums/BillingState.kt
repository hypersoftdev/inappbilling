package com.hypersoft.billing.enums

enum class BillingState(val message: String) {
    NONE("Not Stated"),
    NO_INTERNET_CONNECTION("No Internet Connection"),
    EMPTY_PRODUCT_ID_LIST("Product Ids list cannot be empty"),
    CONNECTION_ESTABLISHING("Connecting to Google Play Console"),
    CONNECTION_DISCONNECTED("Connection disconnected to Google Play Console"),
    CONNECTION_ESTABLISHED("Connection has been established to Google Play Console"),
    CONNECTION_FAILED("Failed to connect Google Play Console"),
    FEATURE_NOT_SUPPORTED("Feature is not Supported! Cannot buy Subscription"),
    CONSOLE_PRODUCTS_FETCHING("Fetching all products from google play console. Try again in few moments"),
    CONSOLE_PRODUCTS_FETCHED_SUCCESSFULLY("Successfully fetched queried products details"),
    CONSOLE_PRODUCTS_FETCHING_FAILED("Failed to fetch products, response is not okay!"),
    CONSOLE_PRODUCTS_AVAILABLE("Products list is not empty"),
    CONSOLE_PRODUCTS_NOT_EXIST("No product has been found"),
    CONSOLE_PRODUCTS_NOT_FOUND("All products are not found"),
    LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY("Google Play Billing has been launched successfully"),
    LAUNCHING_FLOW_INVOCATION_USER_CANCELLED("Cancelled by user"),
    LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND("Exception Found, launching Google billing sheet"),
    PURCHASED_SUCCESSFULLY("Successfully Purchased"),
    PURCHASING_ALREADY_OWNED("Already owned this product! No need to purchase"),
    PURCHASING_USER_CANCELLED("Purchasing product has been cancelled by user"),
    PURCHASING_FAILURE("Failed to make transaction"),
    PURCHASING_ERROR("Error found while purchasing")
}
