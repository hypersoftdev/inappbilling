package com.hypersoft.billing.latest.enums

/**
 * @Author: SOHAIB AHMED
 * @Date: 21/02/2024
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

enum class ResultState(val message: String) {

    NONE("Not Started"),
    NO_INTERNET_CONNECTION("No Internet Connection"),
    EMPTY_PRODUCT_ID_LIST("Product Ids list cannot be empty"),

    /* ------------------------------------------------------ New ------------------------------------------------------ */

    CONNECTION_INVALID("Billing is not ready, seems to be disconnected"),
    CONNECTION_ESTABLISHING("Connecting to Google Play Console"),
    CONNECTION_ESTABLISHING_IN_PROGRESS("An attempt to connect to Google Play Console is already in progress."),
    CONNECTION_ALREADY_ESTABLISHED("Already connected to Google Play Console"),
    CONNECTION_DISCONNECTED("Connection disconnected to Google Play Console"),
    CONNECTION_ESTABLISHED("Connection has been established to Google Play Console"),
    CONNECTION_FAILED("Failed to connect Google Play Console"),

    FEATURE_NOT_SUPPORTED("Feature is not Supported! Cannot buy Subscription"),
    ACTIVITY_REFERENCE_NOT_FOUND("Activity reference is null"),

    CONSOLE_PURCHASE_PRODUCTS_INAPP_FETCHING("InApp -> Fetching purchased products from google play console."),
    CONSOLE_PURCHASE_PRODUCTS_INAPP_FETCHING_FAILED("InApp -> Failed to fetch purchased products from google play console."),
    CONSOLE_PURCHASE_PRODUCTS_INAPP_FETCHING_SUCCESS("InApp -> Successfully fetched purchased products from google play console."),
    CONSOLE_PURCHASE_PRODUCTS_INAPP_FOUND("InApp -> User owns products"),
    CONSOLE_PURCHASE_PRODUCTS_INAPP_NOT_FOUND("InApp -> User does not owned any product yet."),

    CONSOLE_PURCHASE_PRODUCTS_SUB_FETCHING("SUB -> Fetching purchased products from google play console."),
    CONSOLE_PURCHASE_PRODUCTS_SUB_FETCHING_FAILED("SUB ->Failed to fetch purchased products from google play console."),
    CONSOLE_PURCHASE_PRODUCTS_SUB_FETCHING_SUCCESS("SUB -> Successfully fetched purchased products from google play console."),
    CONSOLE_PURCHASE_PRODUCTS_SUB_FOUND("SUB -> User owns products"),
    CONSOLE_PURCHASE_PRODUCTS_SUB_NOT_FOUND("SUB -> User does not owned any product yet"),

    CONSOLE_PURCHASE_PRODUCTS_RESPONSE_PROCESSING("InApp, Subs -> Processing purchases and their product details"),
    CONSOLE_PURCHASE_PRODUCTS_RESPONSE_COMPLETE("InApp, Subs -> Returning result with each purchase product's detail"),
    CONSOLE_PURCHASE_PRODUCTS_CHECKED_FOR_ACKNOWLEDGEMENT("InApp, Subs -> Acknowledging purchases if not acknowledge yet"),


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