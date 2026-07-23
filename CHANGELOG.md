# Changelog

## 4.0.0

Breaking rewrite of the library's internal architecture and public API.

### Changed
- Dropped the internal `data/domain/presentation` clean-architecture split (the layering added no real value at this library's size — see the migration notes for details) in favor of `api` (`BillingManager`), `internal/{connection,products,purchases,mapper,cache}`, and `model` packages.
- Public API moved from callback listeners (`BillingConnectionListener`, `BillingProductDetailsListener`, `BillingPurchaseHistoryListener`, `BillingPurchaseListener`) to Kotlin `suspend fun`s and `Flow` (`connectionState: StateFlow<ConnectionState>`, `purchaseUpdates: SharedFlow<PurchaseOutcome>`).
- Public models moved from `com.hypersoft.billing.data.entities.*` / `com.hypersoft.billing.presentation.enums.ProductType` to `com.hypersoft.billing.model.*`.
- Play Billing Library bumped from `billing-ktx:7.1.1` to `billing-ktx:9.1.0`; `enableAutoServiceReconnection()` is now enabled.
- Removed the unused `androidx.lifecycle:lifecycle-livedata-ktx` dependency; added an explicit `kotlinx-coroutines-android` dependency (previously only pulled in transitively).
- Query failures are now `Result<T>` with a `BillingException(responseCode, message)` instead of a plain error string.
- `BillingManager` no longer accepts a caller-supplied `CoroutineScope`. It now owns its own internal scope and is meant to be created once as an app-lifetime singleton; added `close()` to tear down the scope and the Play Billing connection (`BillingClient.endConnection()` was previously never called at all).
- `queryProductDetails()`/`queryPurchaseHistory()` replaced by `refreshProducts()`/`refreshPurchases()` plus two new observable `StateFlow`s — `productsState`/`purchasesState` (`UiState<T>`: `Loading`/`Success`/`Error`) — that refresh automatically after every successful `connect()` and, for purchases, after every purchase settles. Screens can now just collect state instead of orchestrating fetch calls themselves.
- `BillingClient` is now built from `context.applicationContext` instead of whatever `Context` was passed in, avoiding an accidental `Activity` leak now that `BillingManager` is meant to be long-lived.

### Fixed
- `updateSubs()` no longer sends a hardcoded `"old_purchase_token"` string to `SubscriptionUpdateParams.setOldPurchaseToken()` — it now uses the actual token of the subscription being replaced. Subscription upgrades/downgrades were broken prior to this fix.
- Removed duplicated pricing/plan-title mapping logic (previously implemented independently in three different places) in favor of a single `ProductMapper`/`PurchaseMapper`.
- Fixed silently dropped purchase acknowledgements: previously, if the app passed a short-lived `viewModelScope`/`lifecycleScope` into `BillingManager` and that scope was cancelled before Play's `PurchasesUpdatedListener` fired (e.g. the screen that started the purchase was destroyed), the purchase would never be acknowledged/consumed — a paid purchase that silently never grants entitlement and gets auto-refunded by Play after 3 days. `BillingManager` no longer accepts an external scope, so this can't happen.
- `ProductDetail` gained an `offerId` field. Previously, multiple concurrent offers on the same base plan (e.g. a default price + a time-limited "Christmas Sale" offer) collided in the product cache under the same `(productId, planId)` key, silently dropping all but one. `purchaseSubs()`/`updateSubs()`/`getProductDetail()` gained a matching optional `offerId` parameter to target a specific offer.

### Build
- Converted `build.gradle`/`settings.gradle` (Groovy) to `build.gradle.kts`/`settings.gradle.kts` (Kotlin DSL); dependency versions now live in `gradle/libs.versions.toml`.
- `compileSdk`/`targetSdk` bumped to 37 (Android 17); `minSdk` stays 23.
- AGP 9.2.1 → 9.3.0; Gradle 9.5.1 → 9.6.1.
- Switched to AGP 9's built-in Kotlin support (pinned to Kotlin 2.3.20 via a `kotlin-gradle-plugin` classpath override in the root build script) instead of applying the `org.jetbrains.kotlin.android` plugin, which AGP 9 no longer accepts.
- Dependency bumps: `androidx.core:core-ktx` 1.18.0 → 1.19.0, `androidx.lifecycle:lifecycle-runtime-ktx` 2.11.0 (replacing the unused `lifecycle-livedata-ktx`), `kotlinx-coroutines-android` 1.11.0. `androidx.appcompat`, `material`, and `androidx.constraintlayout` were already pinned to their latest stable versions.

## 3.1.3 and earlier

See git history.
