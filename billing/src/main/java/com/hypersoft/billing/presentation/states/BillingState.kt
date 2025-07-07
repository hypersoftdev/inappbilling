package com.hypersoft.billing.presentation.states

/**
 * Created by: Sohaib Ahmed
 * Date: 7/3/2025
 * <p>
 * Links:
 * - LinkedIn: <a href="https://linkedin.com/in/epegasus">Linkedin</a>
 * - GitHub: <a href="https://github.com/epegasus">Github</a>
 */

enum class BillingState(val message: String) {

    NONE("Not Started"),

    // ▾ Connection lifecycle
    CONNECTING("Connecting to Google Play Billing"),
    CONNECTING_IN_PROGRESS("Connection attempt already in progress"),
    CONNECTED("Connected to Google Play Billing"),
    ALREADY_CONNECTED("Already connected to Google Play Billing"),
    DISCONNECTED("Disconnected from Google Play Billing"),
    CONNECT_FAILED("Failed to connect to Google Play Billing"),
    CONNECTION_INVALID("Billing client is not ready. Please try again shortly"),

    // ▾ Purchases
    FETCHING_INAPP_PURCHASES("InApp -> Fetching In-App purchases from Google Play Console..."),
    FETCHING_INAPP_PURCHASES_FAILED("InApp -> Failed to fetch In-App purchases from Google Play Console."),
    FETCHING_INAPP_PURCHASES_SUCCESS("InApp -> Successfully fetched In-App purchases from Google Play Console."),

    FETCHING_SUBSCRIPTION_PURCHASES("Subs -> Fetching Subscription purchases from Google Play Console..."),
    FETCHING_SUBSCRIPTION_PURCHASES_FAILED("Subs -> Failed to fetch Subscription purchases from Google Play Console."),
    FETCHING_SUBSCRIPTION_PURCHASES_SUCCESS("Subs -> Successfully fetched Subscription purchases from Google Play Console."),

    CONSOLE_PURCHASE_PROCESSING("Processing purchases and their product details (InApp, Subs)"),
    CONSOLE_PURCHASE_COMPLETED("Completed processing purchases; returning purchase details (InApp, Subs)"),
    CONSOLE_PURCHASE_FAILED("Failed to process purchases."),
    CONSOLE_PURCHASE_ACKNOWLEDGEMENT_CHECK("Checking and acknowledging unacknowledged purchases (InApp, Subs)"),

    // ▾ Products
    USER_QUERY_LIST_EMPTY("Product ID list is empty. Please provide at least one product to query."),

    FETCHING_PRODUCTS("Fetching products from Google Play Console..."),
    FETCHING_PRODUCTS_SUCCESS("Successfully fetched products from Google Play Console."),
    FETCHING_PRODUCTS_FAILED("Failed to fetch products from Google Play Console."),

    FETCHING_INAPP_PRODUCTS("InApp -> Fetching In-App products from Google Play Console..."),
    FETCHING_INAPP_PRODUCTS_FAILED("InApp -> Failed to fetch In-App products from Google Play Console."),
    FETCHING_INAPP_PRODUCTS_SUCCESS("InApp -> Successfully fetched In-App products from Google Play Console."),

    FETCHING_SUBSCRIPTION_PRODUCTS("Subs -> Fetching Subscription products from Google Play Console..."),
    FETCHING_SUBSCRIPTION_PRODUCTS_FAILED("Subs -> Failed to fetch Subscription products from Google Play Console."),
    FETCHING_SUBSCRIPTION_PRODUCTS_SUCCESS("Subs -> Successfully fetched Subscription products from Google Play Console."),

    // Buying (Validations)
    ACTIVITY_REFERENCE_NOT_FOUND("Activity reference is null"),
    CONSOLE_BUY_PRODUCT_EMPTY_ID("In-App or Subs purchase failed: Product ID must not be empty"),
    CONSOLE_PRODUCTS_IN_APP_NOT_EXIST("InApp -> No product has been found"),
    CONSOLE_PRODUCTS_SUB_NOT_EXIST("Subs -> No product has been found"),

    // Billing Flows
    BILLING_FLOW_LAUNCHED_SUCCESSFULLY("Billing Flow: Launched successfully via Google Play"),
    BILLING_FLOW_USER_CANCELLED("Billing Flow: Cancelled by user"),
    BILLING_FLOW_EXCEPTION("Billing Flow: Exception occurred while launching purchase sheet"),
    PURCHASES_NOT_FOUND("Purchase Check: Purchase list is empty or null"),
    PURCHASE_ALREADY_OWNED("Purchase Check: Product already owned, skipping transaction"),
    PURCHASE_SUCCESS("Purchase Result: Transaction completed successfully"),
    PURCHASE_FAILED("Purchase Result: Failed to complete the transaction"),


    // Acknowledgements
    ACKNOWLEDGE_PURCHASE("Acknowledging purchases"),
    ACKNOWLEDGE_PURCHASE_SUCCESS("Successfully acknowledged purchases"),
    ACKNOWLEDGE_PURCHASE_FAILURE("Failed to acknowledged purchases"),

    CONSUME_PURCHASE("Acknowledging purchases"),
    CONSUME_PURCHASE_SUCCESS("Successfully Consumed"),
    CONSUME_PURCHASE_FAILURE("Failed to consume product")
}