[![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)

# inappbilling

**inappbilling** is a [Google Play Billing](https://developer.android.com/google/play/billing/integrate) library that demonstrates how to implement in-app purchases and subscriptions in your Android application

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

### Step 1: Initialize Billing

Initialize the **BillingManager** with the application `context`:

```
private val billingManager by lazy { BillingManager(context) }
```

> **Cool Tip 💡**
> You can now pass your `viewModelScope` or `lifecycleScope` to keep it lifecycle-aware. Default fallback: internal `SupervisorJob + Main Dispatcher`.

### Step 2: Setup Product Lists & Listeners

```
billingManager
    .setNonConsumables(listOf("inapp_nonconsumable_1"))
    .setConsumables(listOf("inapp_consumable_1", "inapp_consumable_2"))
    .setSubscriptions(listOf("subs_id_1", "subs_id_2"))
    .setListener(object : BillingConnectionListener {
        override fun onBillingClientConnected(isSuccess: Boolean, message: String) {
            Log.d("BillingTAG", "Connected: $isSuccess - $message")
        }
    })
```

Then, establish the connection:

```
billingManager.startConnection()
```

### Step 3: Fetch Purchase History

```
billingManager.fetchPurchaseHistory(object : BillingPurchaseHistoryListener {
    override fun onSuccess(purchaseList: List<PurchaseDetail>) {
        // Loop through purchases
    }

    override fun onError(errorMessage: String) {
        // Handle error
    }
})
```

Access comprehensive details of the currently purchased item using the `PurchaseDetail` class:

```
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
    var planId: String,
    var productTitle: String,
    var planTitle: String,
    val purchaseToken: String,
    val productType: ProductType,
    val purchaseTime: String,
    val purchaseTimeMillis: Long,
    val isAutoRenewing: Boolean,
)
```

### Step 3: Query Product

Monitor all active in-app and subscription products:

```
billingManager.fetchProductDetails(object : BillingProductDetailsListener {
    override fun onSuccess(productDetailList: List<ProductDetail>) {
        productDetailList.forEach { productDetail ->
            Log.d("BillingTAG", "Fetched: $it")
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

    override fun onError(errorMessage: String) {
        Log.e("BillingTAG", "Error: $errorMessage")
    }
})
```

Or query a specific product:

```kotlin
billingManager.getProductDetail("subs_id_1", "plan_id_1", object : BillingProductDetailsListener {
    override fun onSuccess(productList: List<ProductDetail>) { /* handle product */ }
    override fun onError(errorMessage: String) {}
})
```

Retrieve comprehensive details of the item using the `ProductDetail` class:

```
@param productId: Unique ID (Console's ID) for product
@param planId: Unique ID (Console's ID) for plan
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
    var productTitle: String,
    var productType: ProductType,
    var pricingDetails: List<PricingPhase>

) {
    constructor() : this(
        productId = "",
        planId = "",
        productTitle = "",
        productType = ProductType.subs,
        pricingDetails = listOf(),
    )
}

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
) {
    constructor() : this(
        recurringMode = RecurringMode.ORIGINAL,
        price = "",
        currencyCode = "",
        planTitle = "",
        billingCycleCount = 0,
        billingPeriod = "",
        priceAmountMicros = 0,
        freeTrialPeriod = 0,
    )
}
```

### Step 4: Make Purchases

#### Purchasing In-App Products (one time)

```
billingManager.purchaseInApp(activity, "inapp_consumable_1", object : BillingPurchaseListener {
    override fun onPurchaseResult(message: String) {
        Log.d("BillingTAG", "InApp Result: $message")
    }

    override fun onError(errorMessage: String) {}
})
```

#### Purchasing Subscriptions

```
billingManager.purchaseSubs(activity, "subs_id_1", "plan_id_1", object : BillingPurchaseListener {
    override fun onPurchaseResult(message: String) {
        Log.d("BillingTAG", "Subs Result: $message")
    }

    override fun onError(errorMessage: String) {}
})
```

#### Updating Subscriptions

```
billingManager.updateSubs(
    activity,
    oldProductId = "subs_id_old",
    productId = "subs_id_new",
    planId = "plan_id_new",
    object : BillingPurchaseListener {
        override fun onPurchaseResult(message: String) {
            Log.d("BillingTAG", "Subs Update: $message")
        }

        override fun onError(errorMessage: String) {}
    }
)
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

---

> [!TIP]
> Note: Use the **BillingManager** tag to observe the states

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
