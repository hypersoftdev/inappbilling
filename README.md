[![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)
## inappbilling

inappbilling is a [Google Play Billing](https://developer.android.com/google/play/billing/integrate) library that demonstrates InApp purchase in your Android application

### Getting Started

#### Step A

Add maven repository in project level `build.gradle(project)` or in latest project `setting.gradle` file
```
repositories {
    google()
    mavenCentral()
    maven { url "https://jitpack.io" }
}
```  

#### Step B

Add dependencies in App level `build.gradle(app)`. Latest Version [![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)
```
dependencies {
    implementation 'com.github.hypersoftdev:inappbilling:x.x.x'
}
```

---

## Pre-Release IMPLEMENTATION

> [!TIP]
> Observe the states by using `BillingManager` TAG

### Step 1 (Initialize Billing)

Initialize the `BillingManager` class, where the parameter 'context' refers to an application context.

```
private val billingManager by lazy { BillingManager(context) }
```

### Step # 2 (Billing Connection)

Retrieve a debugging ID for testing purposes by utilizing the `getDebugProductIDList()` method. Ensure that the parameter `purchaseDetailList` represents a list containing all active purchases along with their respective details.

```
val subsProductIdList = listOf("subs_product_id_1", "subs_product_id_2", "subs_product_id_3")
val inAppProductIdList = when (BuildConfig.DEBUG) {
    true -> listOf(billingManager.getDebugProductIDList())
    false -> listOf("inapp_product_id_1", "inapp_product_id_2")
}

billingManager.initialize(
    productInAppPurchases = inAppProductIdList,
    productSubscriptions = subsProductIdList,
    billingListener = object : BillingListener {
        override fun onConnectionResult(isSuccess: Boolean, message: String) {
            Log.d(TAG, "Billing: initBilling: onConnectionResult: isSuccess = $isSuccess - message = $message")
            if (!isSuccess) {
                proceedApp()
            }
        }
        override fun purchasesResult(purchaseDetailList: List<PurchaseDetail>) {
            if (purchaseDetailList.isEmpty()) {
                // No purchase found, reset all sharedPreferences (premium properties)
            }
            purchaseDetailList.forEachIndexed { index, purchaseDetail ->
                Log.d(TAG, "Billing: initBilling: purchasesResult: $index) $purchaseDetail ")
            }
            proceedApp()
        }
    }
)

```
Access comprehensive details of the currently purchased item using the `PurchaseDetail` class.

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

### Step # 3 (Query Product)

This observer monitors all active in-app and subscription products.

```
val subsProductIdList = listOf("subs_product_id_1", "subs_product_id_2", "subs_product_id_3")
val subsPlanIdList = listOf("subs_plan_id_1", "subs_plan_id_2", "subs_plan_id_3")

billingManager.productDetailsLiveData.observe(viewLifecycleOwner) { productDetailList ->
    Log.d(TAG, "Billing: initObservers: $productDetailList")

    productDetailList.forEach { productDetail ->
        if (productDetail.productType == ProductType.inapp) {
            if (productDetail.productId == "inapp_product_id_1") {
                // productDetail
            } else if (productDetail.productId == "inapp_product_id_2") {
                // productDetail
            }
        } else {
            if (productDetail.productId == "subs_product_id_1" && productDetail.planId == "subs_plan_id_1") {
                // productDetail (monthly)
            } else if (productDetail.productId == "subs_product_id_2" && productDetail.planId == "subs_plan_id_2") {
                // productDetail (3 months)
            } else if (productDetail.productId == "subs_product_id_3" && productDetail.planId == "subs_plan_id_3") {
                // productDetail (yearly)
            }
        }
    }
}

```
Retrieve comprehensive details of the item using the `ProductDetail` class.

```
@param productId: Unique ID (Console's ID) for product
@param planId: Unique ID (Console's ID) for plan
@param productTitle: e.g. Gold Tier
@param planTitle: e.g. Weekly, Monthly, Yearly, etc
@param productType: e.g. InApp / Subs
@param currencyCode: e.g. USD, PKR, etc
@param price: e.g. Rs 750.00
@param priceAmountMicros: e.g. 750000000
@param freeTrialDays: e.g. 3, 5, 7, etc
@param billingPeriod
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
    var planTitle: String,
    var productType: ProductType,
    var currencyCode: String,
    var price: String,
    var priceAmountMicros: Long = 0,
    var freeTrialDays: Int = 0,
    var billingPeriod: String,
)
```

### Step # 4 (Make purchases)
### Purchasing InApp

```
billingManager.makeInAppPurchase(activity, productId, object : OnPurchaseListener {
    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
        Log.d(TAG, "makeInAppPurchase: $isPurchaseSuccess - $message")
    }
})

```

### Purchasing Subscription

```
billingManager.makeSubPurchase(activity, productId, planId, object : OnPurchaseListener {
    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
        Log.d(TAG, "makeSubPurchase: $isPurchaseSuccess - $message")
    }
})
```

### Updating Subscription

```
billingManager.updateSubPurchase(
    activity,
    oldProductId = "subs_product_id_1",
    oldPlanId = "subs_plan_id_1",
    productId = "subs_product_id_2",
    planId = "subs_plan_id_2",
    object : OnPurchaseListener {
        override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
            Log.d(TAG, "updateSubPurchase: $isPurchaseSuccess - $message")
        }
    }
)

```
## Guidance

### SubscriptionTags

To add products and plans on the Play Console, consider using the following recommended subscription tags to generate plans.

- Product (Bronze)
  
    -- Plan (Weekly)
    -- Plan (Monthly)
    -- Plan (Yearly)
  
- Product (Silver)
  
    -- Plan (Weekly)
    -- Plan (Monthly)
    -- Plan (Yearly)
  
- Product (Gold)
  
    -- Plan (Weekly)
    -- Plan (Monthly)
    -- Plan (Yearly)


For Bronze Subscription

    -> Product ID:          bronze_product
        -- Plan ID:             bronze-plan-weekly
        -- Plan ID:             bronze-plan-monthly
        -- Plan ID:             bronze-plan-yearly

For Silver Subscription

    -> Product ID:          silver_product
        -- Plan ID:             silver-plan-weekly
        -- Plan ID:             silver-plan-monthly
        -- Plan ID:             silver-plan-yearly

For Gold Subscription

    -> Product ID:          gold_product
        -- Plan ID:             gold-plan-weekly
        -- Plan ID:             gold-plan-monthly
        -- Plan ID:             gold-plan-yearly

and so on...

### Billing Period (subscription)

The following billing periods for subscriptions are fixed and cannot be altered.

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
