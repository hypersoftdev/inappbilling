[![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)
# inappbilling

inappbilling is a [Google Play Billing](https://developer.android.com/google/play/billing/integrate) library that demonstrates InApp purchase in your Android application

## Getting Started

### Step 1

Add maven repository in project level build.gradle or in latest project setting.gradle file
```
repositories {
    google()
    mavenCentral()
    maven { url "https://jitpack.io" }
}
```  


# Pre-Release IMPLEMENTATION

### Step 2

Add inappbilling dependencies in App level build.gradle. Latest Version [![](https://jitpack.io/v/hypersoftdev/inappbilling.svg)](https://jitpack.io/#hypersoftdev/inappbilling)
```
dependencies {
    implementation 'com.github.hypersoftdev:inappbilling:x.x.x'
}
``` 

### Step 3

Declare BillingManger Variable, "this" can be an application context.

also declare your original productId e.g. packageName

```
private val billingManager by lazy { BillingManager(this) }
private val productId:String = "Paste your original Product ID"
```  

#### Enable Subscription Check for Old Purchases

```
billingManager.setCheckForSubscription(true)
```  

#### Billing Initializaiton

Get debugging ids for testing using "getDebugProductIDList()" method

```
val productId = when (BuildConfig.DEBUG) {
    true -> diComponent.billingManager.getDebugProductIDList()
    false -> listOf(globalContext.packageName)
}

diComponent.billingManager.startConnection(productId, object : OnConnectionListener {
    override fun onConnectionResult(isSuccess: Boolean, message: String) {
        Log.d("TAG", "onConnectionResult: $isSuccess - $message")
        binding.mbMakePurchase.isEnabled = isSuccess
    }

    override fun onOldPurchaseResult(isPurchased: Boolean) {
        // Update your shared-preferences here!
        Log.d("TAG", "onOldPurchaseResult: $isPurchased")
    }
})


```
#### Billing State Observer

Observe the states by using `BillingManager` TAG

### Purchasing InApp

```
billingManager.makeInAppPurchase(activity, object : OnPurchaseListener {
    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        Log.d("TAG", "makeInAppPurchase: $isPurchaseSuccess - $message")
    }
})
```

### Purchasing Subscription & Observe

```
billingManager.makeSubPurchase(activity, SubscriptionPlans.basicPlanMonthly, object : OnPurchaseListener {
    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        binding.mbMakePurchase.isEnabled = !isPurchaseSuccess
    }
})
```

#### SubscriptionTags

Add plans and tags on Play Console
There are few subsciption tags as follow, to generate plans.

For Montly Subscription

    -> Product ID:          basic_product_monthly
    -> Plan ID:             basic-plan-monthly

For Yearly Subscription

    -> Product ID:          basic_product_yearly
    -> Plan ID:             basic-plan-yearly

For 06 Months Subscription

    -> Product ID:          basic_product_semi_yearly
    -> Plan ID:             basic-plan-semi-yearly


### Observe Dynamic Pricing

The model class for `ProductDetail`

```
data class ProductDetail(
    var productId: String,
    var price: String,
    var currencyCode: String,
    var freeTrialPeriod: Int,
    var priceAmountMicros: Long = 0,
    var freeTrial: Boolean = false,
    var productType: ProductType = ProductType.SUBS
)
```

Following observer observes all the active subscription and in-App Product

```
billingManager.productDetailsLiveData.observe(this) { list ->
    var month = 0L
    var year = 0L
    list.forEach { productDetail: ProductDetail ->
        Log.d(TAG, "initObservers: $productDetail")
        when (productDetail.productId) {
            SubscriptionProductIds.basicProductMonthly -> {
                //binding.mtvOfferPrice1.text = productDetail.price
                month = productDetail.priceAmountMicros / 1000000
            }

            SubscriptionProductIds.basicProductYearly -> {
                //binding.mtvOfferPrice2.text = productDetail.price
                year = productDetail.priceAmountMicros / 1000000
            }

            SubscriptionProductIds.basicProductSemiYearly -> {
                //binding.mtvOfferPrice3Premium.text = productDetail.price
            }

            productId -> {
                //binding.mtvOfferPrice3Premium.text = productDetail.price
            }
        }
    }
    // Best Offer
    if (month == 0L || year == 0L) return@observe
    val result = 100 - (year * 100 / (12 * month))
    val text = "Save $result%"
    //binding.mtvBestOffer.text = text

    val perMonth = (year / 12L).toString()
    //binding.mtvOffer.text = perMonth
}
```

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
