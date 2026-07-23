[![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)

# inappbilling

**inappbilling** is a [Google Play Billing](https://developer.android.com/google/play/billing/integrate) library (Play Billing Library 9.1.0) that demonstrates how to implement in-app purchases and subscriptions in your Android application using Kotlin coroutines and `Flow`.

> [!IMPORTANT]
> **Version 4.0.0 is a breaking rewrite.** The listener-based API (`BillingConnectionListener`, `BillingPurchaseListener`, etc.) has been replaced with `suspend fun`s and `Flow`s, and moved to Play Billing Library 9.1.0. See [Migrating from 3.x](#migrating-from-3x) below.

## Gradle Integration

### Step A: Add Maven Repository

In your project-level **build.gradle** or **settings.gradle** file, add the JitPack repository:

```
repositories {
    google()
    mavenCentral()
    maven { url "https://jitpack.io" }
}
```  

### Step B: Add Dependencies

In your app-level **build.gradle** file, add the library dependency. Use the latest version: [![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)

```
implementation 'com.github.hypersoftdev:inappbilling:x.x.x'
```

---

## Technical Implementation

`BillingManager` is meant to be created **once**, as a singleton scoped to your `Application` — not per-`Activity`/`ViewModel`. Play's purchase callback can fire at any time, including after the screen that started a purchase is gone, so `BillingManager` owns its own internal coroutine scope rather than accepting one from the caller. Connect once, early, and let every screen just *observe* state from then on.

### Step 1: Create the singleton and connect (in your `Application`)

```kotlin
class MyApplication : Application() {

    val billingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()

        billingManager
            .setNonConsumables(listOf("inapp_nonconsumable_1"))
            .setConsumables(listOf("inapp_consumable_1", "inapp_consumable_2"))
            .setSubscriptions(listOf("subs_id_1", "subs_id_2"))

        appScope.launch { billingManager.connect() }
    }
}
```

On a successful `connect()`, `productsState` and `purchasesState` (below) refresh **automatically** in the background — there's no separate "now fetch products/purchases" step to remember.

### Reading `connectionState`

`connect()`'s return value is a one-shot snapshot — useful right after you call it, but it won't tell you if the connection later drops and (with `enableAutoServiceReconnection()` always on internally) reconnects. For that, collect `connectionState: StateFlow<ConnectionState>` instead:

```kotlin
lifecycleScope.launch {
    billingManager.connectionState.collect { state ->
        when (state) {
            ConnectionState.DISCONNECTED -> { /* not connected yet, or the connection dropped */ }
            ConnectionState.CONNECTING -> { /* connection attempt in progress */ }
            ConnectionState.CONNECTED -> { /* ready — safe to call purchaseXxx()/refreshXxx() */ }
        }
    }
}
```

A few things worth knowing about it:
- It's a `StateFlow`, so any new collector immediately gets the current state, then every change after that — you don't need to call `connect()` again just to start observing.
- Gate purchase buttons on `connectionState == CONNECTED` (or `UiState` from `productsState`/`purchasesState`, which can only reach `Success` once connected) rather than assuming `connect()` having returned once means the connection stays up forever.
- This is purely about the *connection*, not your data — `productsState`/`purchasesState` staying on `Loading` while `connectionState` is `CONNECTING` is expected; they refresh once `connectionState` reaches `CONNECTED`.

### Step 2: Check premium status as early as possible

```kotlin
lifecycleScope.launch {
    billingManager.purchasesState.collect { state ->
        when (state) {
            is UiState.Loading -> { /* still resolving — don't assume non-premium yet */ }
            is UiState.Success -> { /* derive your premium/owned-products check from state.data */ }
            is UiState.Error -> { /* e.g. offline — decide your own fallback policy */ }
        }
    }
}
```

`purchasesState` also re-refreshes automatically right after any purchase settles (whether initiated in this session or resumed from a pending state), so this collector is the single source of truth for "is the user premium" — no manual re-fetch needed after a purchase completes.

Access comprehensive details of the currently purchased item using the `PurchaseDetail` class:

```kotlin
/**
 productId: Product Id for both inapp/subs (e.g. product_ads/product_weekly_ads)
 planId: Plan Id for subs (e.g. plan_weekly_ads)
 productTitle: Title of the Product
 planTitle: Title of the Plan
 productType: Product purchase type (e.g. InApp/Subs)
 purchaseToken: a unique token for this purchase
 purchaseTime: For subscriptions, this is the subscription signup time. It won't change after renewal.
 purchaseTimeMillis: UnixTimeStamp (starts from Jan 1, 1970)
 isAutoRenewing: Only in case of 'BillingClient.ProductType.SUBS'
*/

data class PurchaseDetail(
    val productId: String,
    val productType: ProductType,
    val purchaseToken: String,
    var productTitle: String?,
    var planTitle: String?,
    var planId: String?,
    val purchaseTime: String,
    val purchaseTimeMillis: Long,
    val isAutoRenewing: Boolean,
    val isAcknowledged: Boolean,
)
```

### Step 3: Preload prices for a paywall shown later

Because `productsState` was already warming up since `connect()`, a paywall shown 1-2 screens later usually finds it already resolved — collect it, don't fetch-and-wait:

```kotlin
lifecycleScope.launch {
    billingManager.productsState.collect { state ->
        when (state) {
            is UiState.Loading -> showSpinner()
            is UiState.Error -> showError(state.message)
            is UiState.Success -> state.data.forEach { productDetail ->
                if (productDetail.productType == ProductType.inapp) {
                    when (productDetail.productId) {
                        "inapp_product_id_1" -> { /* Handle in-app product 1 */ }
                        "inapp_product_id_2" -> { /* Handle in-app product 2 */ }
                    }
                } else {
                    when (productDetail.productId) {
                        "subs_product_id_1" -> if (productDetail.planId == "subs-plan-id-1") {
                            val freeTrial = productDetail.pricingDetails.find { it.recurringMode == RecurringMode.FREE }
                            val discounted = productDetail.pricingDetails.find { it.recurringMode == RecurringMode.DISCOUNTED }
                            val original = productDetail.pricingDetails.find { it.recurringMode == RecurringMode.ORIGINAL }
                        }
                        "subs_product_id_2" -> if (productDetail.planId == "subs-plan-id-2") { /* Handle plan2 subscription */ }
                        "subs_product_id_3" -> if (productDetail.planId == "subs-plan-id-3") { /* Handle plan3 subscription */ }
                    }
                }
            }
        }
    }
}
```

If you register a new set of IDs later (or just want to force a re-fetch, e.g. pull-to-refresh), call `billingManager.refreshProducts()` — it republishes `productsState` the same way the automatic post-connect refresh does.

Or query a specific product (read from the in-memory cache — no network call):

```kotlin
lifecycleScope.launch {
    billingManager.getProductDetail("subs_id_1", "plan_id_1")
        .onSuccess { productDetail -> /* handle product */ }
        .onFailure { error -> }
}
```

**Multiple offers on the same plan (free trials, event discounts, winback offers):** a base plan can carry several concurrent offers in the Play Console — a default price, a free-trial offer, a time-limited "Christmas Sale" discount, a winback offer for lapsed subscribers, etc. Play only ever returns the ones the *current user* is eligible for. Each eligible offer shows up as its own `ProductDetail` entry sharing the same `productId`/`planId` but a distinct `offerId`, so filter `productsState`'s list by `offerId` to find promotional entries:

```kotlin
val promoOffers = productDetailList.filter { it.productId == "subs_id_1" && it.planId == "plan-id-1" && it.offerId.isNotEmpty() }
```

`getProductDetail(productId, planId, offerId)` — pass `offerId = null` (the default) for the plan's default offer, or a specific `offerId` to target one particular promo.

Retrieve comprehensive details of the item using the `ProductDetail` class:

```kotlin
@param productId: Unique ID (Console's ID) for product
@param planId: Unique ID (Console's ID) for plan
@param offerId: Distinguishes multiple concurrent offers on the same plan; empty = default offer
@param productTitle: e.g. Gold Tier
@param productType: e.g. InApp / Subs
@param pricingDetails: e.g. list of pricing containing price, currencyCode, planTitle, billingCycleCount. etc
    - Weekly: P1W (One week)
    - Every 4 weeks: P4W (Four weeks)
    - Monthly: P1M (One month)
    - Every 2 months (Bimonthly): P2M (Two months)
    - Every 3 months (Quarterly): P3M (Three months)
    - Every 4 months: P4M (Four months)
    - Every 6 months (Semiannually): P6M (Six months)
    - Every 8 months: P8M (Eight months)
    - Yearly: P1Y (One year)

data class ProductDetail(
    var productId: String,
    var planId: String,
    var offerId: String,
    var productTitle: String,
    var productType: ProductType,
    var pricingDetails: List<PricingPhase>
)

@param recurringMode: e.g. FREE, DISCOUNTED, ORIGINAL
@param price: e.g. Rs 750.00
@param currencyCode: e.g. USD, PKR, etc
@param planTitle: e.g. Weekly, Monthly, Yearly, etc
@param billingCycleCount: e.g. 1, 2, 3, etc
@param billingPeriod: e.g. P1W, P1M, P1Y, etc
@param priceAmountMicros: e.g. 750000000
@param freeTrialPeriod: e.g. 3, 5, 7, etc

data class PricingPhase(
    var recurringMode: RecurringMode,
    var price: String,
    var currencyCode: String,
    var planTitle: String,
    var billingCycleCount: Int,
    var billingPeriod: String,
    var priceAmountMicros: Long,
    var freeTrialPeriod: Int,
)
```

### Step 4: Make Purchases

Each purchase call suspends until the purchase flow resolves and returns a `PurchaseOutcome`:

```kotlin
sealed interface PurchaseOutcome {
    data class Success(val message: String) : PurchaseOutcome
    data object AlreadyOwned : PurchaseOutcome
    data object UserCancelled : PurchaseOutcome
    data class Failed(val message: String, val responseCode: Int) : PurchaseOutcome
}
```

After a `Success`/`AlreadyOwned` result, `purchasesState` has already been refreshed automatically — no manual re-fetch needed.

#### Purchasing In-App Products (one time)

```kotlin
lifecycleScope.launch {
    when (val outcome = billingManager.purchaseInApp(activity, "inapp_consumable_1")) {
        is PurchaseOutcome.Success -> Log.d("BillingTAG", "InApp Result: ${outcome.message}")
        is PurchaseOutcome.AlreadyOwned -> { /* already owned */ }
        is PurchaseOutcome.UserCancelled -> { /* user backed out */ }
        is PurchaseOutcome.Failed -> Log.e("BillingTAG", "InApp Error: ${outcome.message}")
    }
}
```

#### Purchasing Subscriptions

`offerId` is optional — omit it (or pass `null`) to purchase the plan's default offer, or pass a specific `offerId` (e.g. one you picked from a promotional entry in `productsState`, see [Step 3](#step-3-preload-prices-for-a-paywall-shown-later)) to purchase that particular free-trial/discount/winback offer instead:

```kotlin
lifecycleScope.launch {
    val outcome = billingManager.purchaseSubs(activity, "subs_id_1", "plan_id_1", offerId = "christmas-sale-2026")
    Log.d("BillingTAG", "Subs Result: $outcome")
}
```

#### Updating Subscriptions

```kotlin
lifecycleScope.launch {
    val outcome = billingManager.updateSubs(
        activity,
        oldProductId = "subs_id_old",
        productId = "subs_id_new",
        planId = "plan_id_new",
        offerId = null // or a specific offer id, same semantics as purchaseSubs()
    )
    Log.d("BillingTAG", "Subs Update: $outcome")
}
```

#### Observing purchases outside a direct call

`billingManager.purchaseUpdates: SharedFlow<PurchaseOutcome>` emits purchases that settle without a corresponding `purchaseXxx()` call in flight (e.g. a pending purchase completing after the app restarts):

```kotlin
lifecycleScope.launch {
    billingManager.purchaseUpdates.collect { outcome -> /* handle late purchase updates */ }
}
```

## Guidance

### Subscription Tags

To add products and plans on the Play Console, consider using the following recommended subscription tags to generate plans.

#### Option 1

##### Note: One-to-One ids

    Product ID: product_id_weekly
    - Plan ID: plan-id-weekly
    
    Product ID: product_id_monthly
    - Plan ID: plan-id-monthly
    
    Product ID: product_id_yearly
    - Plan ID: plan-id-yearly

#### Option 2

##### Note:

If you purchase a product and want to retrieve an old purchase from Google, it won't return the plan ID, making it impossible to identify which plan was purchased. To address this, you should save the purchase information on your server, including the product and plan IDs. This way, you can maintain a purchase list for future reference. Alternatively, you can use `Option 1`, where each product ID is associated with only one plan ID. This ensures that when you fetch a product ID, you can easily determine the corresponding plan that was purchased

For Gold Subscription

    Product ID: gold_product
    - Plan ID: gold-plan-weekly
    - Plan ID: gold-plan-monthly
    - Plan ID: gold-plan-yearly

and so on...

### Billing Period (Subscription)

Fixed billing periods for subscriptions:

    - Weekly
    - Every 4 weeks
    - Monthly
    - Every 2 months (Bimonthly)
    - Every 3 months (Quarterly)
    - Every 4 months 
    - Every 6 months (Semiannually)
    - Every 8 months
    - Yearly

### Lifecycle & cleanup

`BillingManager` should be a singleton for the app's lifetime (see Step 1) — do **not** construct it per-`Activity`/`ViewModel` and do **not** pass it a `viewModelScope`/`lifecycleScope`; the constructor doesn't accept one. It owns its billing connection and internal coroutine scope until you call `billingManager.close()`, which is typically never needed for an app-lifetime singleton.

---

> [!TIP]
> Note: Use the **BillingManager** tag to observe the internal connection/purchase/mapping logs.

## Migrating from 3.x

Version 4.0.0 removes the listener interfaces in favor of `suspend fun`s, `StateFlow`s, and automatic preloading. Package layout also changed:
`com.hypersoft.billing.data.entities.*` / `presentation.enums.ProductType` → `com.hypersoft.billing.model.*`.

| 3.x | 4.0.0 |
|---|---|
| `BillingManager(context, scope)` | `BillingManager(context)` — no external scope; create once as an app-lifetime singleton, call `close()` when truly done |
| `setListener(BillingConnectionListener).startConnection()` | `connect(): ConnectionState` (suspend) — auto-refreshes `productsState`/`purchasesState` on success; or observe `connectionState: StateFlow<ConnectionState>` |
| `fetchPurchaseHistory(BillingPurchaseHistoryListener)` | Collect `purchasesState: StateFlow<UiState<List<PurchaseDetail>>>` (auto-refreshed); `refreshPurchases()` to force a re-fetch |
| `fetchProductDetails(BillingProductDetailsListener)` | Collect `productsState: StateFlow<UiState<List<ProductDetail>>>` (auto-refreshed); `refreshProducts()` to force a re-fetch |
| `getProductDetail(id, planId, BillingProductDetailsListener)` | `getProductDetail(id, planId, offerId = null): Result<ProductDetail>` (suspend) |
| `purchaseInApp/purchaseSubs/updateSubs(..., BillingPurchaseListener)` | Same names, now `suspend fun` returning `PurchaseOutcome`; `purchaseSubs`/`updateSubs` gained an optional `offerId` param for multi-offer plans |
| N/A | `purchaseUpdates: SharedFlow<PurchaseOutcome>` for purchases that settle outside a direct call |
| N/A | `ProductDetail.offerId` — distinguishes multiple concurrent offers (free trial, event discount, winback, ...) on the same plan |

# LICENSE

Copyright 2023 Hypersoft Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
