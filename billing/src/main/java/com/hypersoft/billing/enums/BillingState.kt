package com.hypersoft.billing.enums

/**
 * @Author: SOHAIB AHMED
 * @Date: 08,May,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

enum class BillingState(val message: String) {
    NONE("Not Stated"),
    NO_INTERNET_CONNECTION("No Internet Connection"),
    EMPTY_PRODUCT_ID_LIST("Product Ids list cannot be empty"),
    CONNECTION_ESTABLISHING("Connecting to Google Play Console"),
    CONNECTION_ALREADY_ESTABLISHING("Already connected to Google Play Console"),
    CONNECTION_DISCONNECTED("Connection disconnected to Google Play Console"),
    CONNECTION_ESTABLISHED("Connection has been established to Google Play Console"),
    CONNECTION_FAILED("Failed to connect Google Play Console"),
    FEATURE_NOT_SUPPORTED("Feature is not Supported! Cannot buy Subscription"),
    ACTIVITY_REFERENCE_NOT_FOUND("Activity reference is null"),

    CONSOLE_OLD_PRODUCTS_INAPP_FETCHING("InApp -> Fetching old products from google play console."),
    CONSOLE_OLD_PRODUCTS_INAPP_NOT_FOUND("InApp -> User hasn't owned any product yet."),
    CONSOLE_OLD_PRODUCTS_INAPP_OWNED("InApp -> Already owned requested products"),
    CONSOLE_OLD_PRODUCTS_INAPP_NOT_OWNED("InApp -> Doesn't owned requested products"),
    CONSOLE_OLD_PRODUCTS_INAPP_OWNED_BUT_NOT_ACKNOWLEDGE("InApp -> Product is owned but is not acknowledged yet"),
    CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_ACKNOWLEDGE("InApp -> Product is owned and is acknowledged as well"),
    CONSOLE_OLD_PRODUCTS_INAPP_OWNED_AND_FAILED_TO_ACKNOWLEDGE("InApp -> Product is owned and is failed to acknowledged"),

    CONSOLE_OLD_PRODUCTS_SUB_FETCHING("SUB -> Fetching old products from google play console."),
    CONSOLE_OLD_PRODUCTS_SUB_NOT_FOUND("SUB -> User hasn't owned any product yet."),
    CONSOLE_OLD_PRODUCTS_SUB_OWNED("SUB -> Already owned requested products"),
    CONSOLE_OLD_PRODUCTS_SUB_NOT_OWNED("SUB -> Doesn't owned requested products"),
    CONSOLE_OLD_PRODUCTS_SUB_OWNED_BUT_NOT_ACKNOWLEDGE("SUB -> Product is owned but is not acknowledged yet"),
    CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_ACKNOWLEDGE("SUB-> Product is owned and is acknowledged as well"),
    CONSOLE_OLD_PRODUCTS_SUB_OWNED_AND_FAILED_TO_ACKNOWLEDGE("SUB -> Product is owned and is failed to acknowledged"),

    CONSOLE_PRODUCTS_IN_APP_FETCHING("InApp -> Fetching all products from google play console. Try again in few moments"),
    CONSOLE_PRODUCTS_IN_APP_FETCHED_SUCCESSFULLY("InApp -> Successfully fetched queried products details"),
    CONSOLE_PRODUCTS_IN_APP_FETCHING_FAILED("InApp -> Failed to fetch products, response is not okay!"),
    CONSOLE_PRODUCTS_IN_APP_AVAILABLE("InApp -> Products list is not empty"),
    CONSOLE_PRODUCTS_IN_APP_NOT_EXIST("InApp -> No product has been found"),
    CONSOLE_PRODUCTS_IN_APP_NOT_FOUND("InApp -> All products are not found"),

    CONSOLE_PRODUCTS_SUB_FETCHING("SUB -> Fetching all products from google play console. Try again in few moments"),
    CONSOLE_PRODUCTS_SUB_FETCHED_SUCCESSFULLY("SUB -> Successfully fetched queried products details"),
    CONSOLE_PRODUCTS_SUB_FETCHING_FAILED("SUB -> Failed to fetch products, response is not okay!"),
    CONSOLE_PRODUCTS_SUB_AVAILABLE("SUB -> Products list is not empty"),
    CONSOLE_PRODUCTS_SUB_NOT_EXIST("SUB -> No product has been found"),
    CONSOLE_PRODUCTS_SUB_NOT_FOUND("SUB -> All products are not found"),

    LAUNCHING_FLOW_INVOCATION_SUCCESSFULLY("Google Play Billing has been launched successfully"),
    LAUNCHING_FLOW_INVOCATION_USER_CANCELLED("Cancelled by user"),
    LAUNCHING_FLOW_INVOCATION_EXCEPTION_FOUND("Exception Found, launching Google billing sheet"),
    PURCHASED_SUCCESSFULLY("Successfully Purchased"),
    PURCHASING_ALREADY_OWNED("Already owned this product! No need to purchase"),
    PURCHASING_USER_CANCELLED("Purchasing product has been cancelled by user"),
    PURCHASING_FAILURE("Failed to make transaction"),
    PURCHASING_ERROR("Error found while purchasing")
}
