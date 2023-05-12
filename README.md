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
    implementation 'com.github.hypersoftdev:inappbilling:2.2.6'
}
``` 

### Step 3

Declare BillingManger Variable, "this" should be the reference of an activity.

also declare your original productId

```
private val billingManager by lazy { BillingManager(this) }
private val productId:String = "Paste your original Product ID"
```  

#### Enable Subscription Check

```
billingManager.setCheckForSubscription(true)
```  

#### Billing Initializaiton

Get debugging ids for testing using "getDebugProductIDList()" method

```
if (BuildConfig.DEBUG) {
    billingManager.startConnection(billingManager.getDebugProductIDList(), object : OnConnectionListener {
        override fun onConnectionResult(isSuccess: Boolean, message: String) {
            binding.mbMakePurchase.isEnabled = isSuccess
            Log.d("TAG", "onConnectionResult: $isSuccess - $message")
        }

        override fun onOldPurchaseResult(isPurchased: Boolean) {
            // Update your shared-preferences here!
            Log.d("TAG", "onOldPurchaseResult: $isPurchased")
        }
    })
} else {
    billingManager.startConnection(listOf(packageName), object : OnConnectionListener {
        override fun onConnectionResult(isSuccess: Boolean, message: String) {
            Log.d("TAG", "onConnectionResult: $isSuccess - $message")
        }

        override fun onOldPurchaseResult(isPurchased: Boolean) {
            // Update your shared-preferences here!
            Log.d("TAG", "onOldPurchaseResult: $isPurchased")
        }
    })
}

```
#### Billing State Observer

observe the states of establishing connections

```
State.billingState.observe(this) {
    Log.d("BillingManager", "initObserver: $it")
}
```
#### Purchasing InApp

```
billingManager.makeInAppPurchase(object : OnPurchaseListener {
    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        Log.d("TAG", "makeInAppPurchase: $isPurchaseSuccess - $message")
    }
})
```

#### Purchasing Subscription

```
billingManager.makeSubPurchase(SubscriptionTags.basicMonthly, object : OnPurchaseListener {
    override fun onPurchaseResult(isPurchaseSuccess: Boolean, message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        binding.mbMakePurchase.isEnabled = !isPurchaseSuccess
    }
})
```

#### SubscriptionTags

Add plans and tags on Play Console
There are few subsciption tags as follow to generate any offer relevant to plans.
    Two Types of Plans
        1) basic_subscription
        2) premium_subscription

    Tags for both plans as follow:
        
        1) basicWeekly,
        2) basicBimonthly,
        3) basicMonthly,
        4) basicQuarterly,
        5) basicSemiYearly,
        6) basicYearly,

        1) premiumWeekly,
        2) premiumBimonthly,
        3) premiumMonthly,
        4) premiumQuarterly,
        5) premiumSemiYearly,
        6) premiumYearly,

# STABLE IMPLEMENTATION

### Step 2

Add inappbilling dependencies in App level build.gradle.
```
    dependencies {
                implementation 'com.github.hypersoftdev:inappbilling:1.1.6'
    }
``` 

### Step 3

Declare BillingManger Variable, "this" can be of Application Context

also declare your original productId

```
    private val billingManager by lazy { BillingManager(this) }
    private val productId:String = "Paste your original Product ID"
```  

#### Billing Initializaiton

Get debugging ids for testing using "getDebugProductIDList()" method

```
    if (BuildConfig.DEBUG) {
            billingManager.startConnection(billingManager.getDebugProductIDList()) { isConnectionEstablished, alreadyPurchased, message ->
                showMessage(message)
                if (alreadyPurchased) {
                    // Save settings for purchased product
                }
            }
        } else {
            billingManager.startConnection(listOf(productId)) { isConnectionEstablished, alreadyPurchased, message ->
                showMessage(message)
                if (alreadyPurchased) {
                    // Save settings for purchased product
                }
            }
        }

```
#### Billing State Observer

observe the states of establishing connections

```
State.billingState.observe(this) {
            Log.d("BillingManager", "initObserver: $it")
        }
```
#### Purchasing InApp

"this" parameter Must be a reference of an Activity

```
   billingManager.makePurchase(this) { isSuccess, message ->
            showMessage(message)
        }
```

#### Old Purchase

```
    if (BuildConfig.DEBUG) {
            billingManager.startOldPurchaseConnection(billingManager.getDebugProductIDList()) { isConnectionEstablished, alreadyPurchased, message ->
                if (alreadyPurchased) {
                    // Save settings for purchased product
                }
            }
        } else {
            billingManager.startOldPurchaseConnection(listOf(productId)) { isConnectionEstablished, alreadyPurchased, message ->
                if (alreadyPurchased) {
                    // Save settings for purchased product
                }
            }
        }

```

#### Note
Here is the list of debuging product ids, can be test every state of billing

```
"android.test.purchased"
"android.test.item_unavailable"
"android.test.refunded"
"android.test.canceled"
```

can be found here

```
billingManager.getDebugProductIDList()
billingManager.getDebugProductIDsList()
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

