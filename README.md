[![](https://jitpack.io/v/orbitalsonic/SonicInApp.svg)](https://jitpack.io/#orbitalsonic/SonicInApp)
# SonicInApp

SonicInApp is a [Google Play Billing](https://developer.android.com/google/play/billing/integrate) library that demonstrates InApp purchase in your Android application

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

### Step 2

Add SonicInApp dependencies in App level build.gradle.
```
    dependencies {
             implementation 'com.github.hypersoftdev:inappbilling:1.0.1'
    }
```  

### Step 3

Declare BillingManger Variable, "this" parameter Must be a reference of an Activity

also declare your original productId

```
    private val billingManager by lazy { BillingManager(this) }
    private val productId:String = "Paste your original Product ID"
```  

#### Billing Initializaiton

Get debugging ids for testing using "getDebugProductIDList()" method

```
 if (BuildConfig.DEBUG) {
            billingManager.startConnection(billingManager.getDebugProductIDList()){ isConnectionEstablished, message ->
                Log.d("BillingManager", message)
            }
        } else {
            billingManager.startConnection(listOf(productId)) { isConnectionEstablished, message ->
                Log.d("BillingManager", message)
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

```
billingManager.makePurchase { isSuccess, message ->
            Log.d("BillingManager", message)
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

