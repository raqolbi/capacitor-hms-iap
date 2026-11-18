# capacitor-hms-iap

> **Status: 0.1.0 — early release, Android only.** Purchase, consume, cancel, signature check and restore are verified on a Huawei device in the sandbox. Subscriptions and purchase history come in later versions. See [Implementation status](#implementation-status).

A Capacitor plugin for **HUAWEI In-App Purchases (IAP)**: sell consumables, non-consumables and subscriptions in Android apps distributed through **Huawei AppGallery**.

There is no Capacitor-native HMS IAP plugin today. The official [`@hmscore/cordova-plugin-hms-iap`](https://github.com/HMS-Core/hms-cordova-plugin) works in Cordova, but relies on Cordova install hooks and Cordova-style activity results that Capacitor does not run. This plugin aims to be a small, Capacitor-first alternative.

---

## Goals

- **Capacitor-first.** Built from the official Capacitor plugin template; Huawei screens (sign-in, checkout) are launched with AndroidX Activity Result APIs, so results return to the same `PluginCall`. No Cordova hooks, no `config.xml`.
- **Small and predictable API.** Thin wrapper over `IapClient`; no hidden business logic, no server dependency.
- **All product types.** Consumables, non-consumables and auto-renewable subscriptions.
- **Raw data preserved.** Every purchase returns the original `inAppPurchaseData` string and its signature, so apps can verify locally or on their own server.
- **Store-flavor friendly.** The HMS IAP SDK is declared `compileOnly`, so apps that ship to several stores (for example AppGallery and Google Play) can include the SDK only in their Huawei build flavor.
- **Typed errors.** Stable string error codes plus the original HMS `returnCode`.

## Non-goals

- Cross-store abstraction (Google Play Billing, App Store). Use a separate plugin per store and wrap them in your app.
- Server-side receipt validation. Use [HMS-Core/hms-iap-serverdemo](https://github.com/HMS-Core/hms-iap-serverdemo) or a server library of your choice.
- iOS support (HMS IAP is Android-only).
- HarmonyOS NEXT (ArkTS) apps, which cannot run Capacitor/Android code.

---

## Quick start

```ts
import { HmsIap, ProductType } from 'capacitor-hms-iap';

// 1. Check that the user is signed in and IAP is available in their region.
const env = await HmsIap.isEnvReady();

// 2. Load localized product information.
const { products } = await HmsIap.getProducts({
  productIds: ['premium_lifetime'],
  type: ProductType.NonConsumable,
});

// 3. Launch the Huawei checkout.
const result = await HmsIap.purchase({
  productId: 'premium_lifetime',
  type: ProductType.NonConsumable,
});

// 4. Verify result.purchaseData + result.signature with your IAP public key
//    (where to get it and how: docs/APPGALLERY-SETUP.md §2.3 and §6),
//    then unlock the content.

// 5. Restore on reinstall or a new device.
const owned = await HmsIap.getOwnedPurchases({ type: ProductType.NonConsumable });
```

Full specification: [`docs/DESIGN.md`](./docs/DESIGN.md).

## Implementation status

| Method | Android | Verified on device |
|--------|---------|--------------------|
| `isAvailable` | ✅ | ✅ |
| `isEnvReady` | ✅ (with HUAWEI ID sign-in) | ✅ |
| `isSandboxActivated` | ✅ | ✅ |
| `getProducts` | ✅ | ✅ non-consumable |
| `purchase` | ✅ | ✅ non-consumable and consumable in sandbox; cancel → `USER_CANCELED` |
| `getOwnedPurchases` | ✅ (incl. pending, paging) | ✅ restore after clearing data and after reinstall |
| `consumePurchase` | ✅ | ✅ consumable consumed, no longer owned |
| `setLogging` | ✅ | — |
| `getPurchaseHistory` | ⏳ rejects `NOT_IMPLEMENTED` (0.2.0) | — |
| `openSubscriptionManagement`, `enablePendingPurchase` | ⏳ rejects `NOT_IMPLEMENTED` (0.3.0) | — |

Web and iOS: `isAvailable` resolves `{ available: false, reason: 'NOT_ANDROID' }`; everything else rejects with `UNAVAILABLE`.

## Errors

Rejections carry a stable string `code` (for example `USER_CANCELED`, `ALREADY_OWNED`, `IAP_NOT_ACTIVATED`, `APP_NOT_CONFIGURED`) plus the original HMS `returnCode` in `data`. Full table and causes: [`docs/DESIGN.md` §4](./docs/DESIGN.md#4-error-model). Setup-related errors and how to fix them: [`docs/APPGALLERY-SETUP.md`](./docs/APPGALLERY-SETUP.md).

## Verifying purchases

The plugin never verifies signatures and never needs your IAP public key, so it works the same in any framework. Where to find the key, where to store it (Next.js, SvelteKit, Vite, server) and a WebCrypto verification snippet: [`docs/APPGALLERY-SETUP.md` §6](./docs/APPGALLERY-SETUP.md#6-using-the-iap-public-key-in-your-app).

---

## Install

```bash
npm install capacitor-hms-iap
npx cap sync android
```

Installing straight from git is not supported, because `dist/` is not committed. For local development use `"capacitor-hms-iap": "file:../path/to/capacitor-hms-iap"` after `npm run build` in the plugin.

Then follow [`docs/APPGALLERY-SETUP.md`](./docs/APPGALLERY-SETUP.md): the app must add `com.huawei.hms:iap`, the AGConnect Gradle plugin and `agconnect-services.json`.

## Documents

| Document | Content |
|----------|---------|
| [`docs/DESIGN.md`](./docs/DESIGN.md) | API specification, data types, native mapping, error model, security notes |
| [`docs/APPGALLERY-SETUP.md`](./docs/APPGALLERY-SETUP.md) | Steps an app developer follows in AppGallery Connect and Gradle |
| [`docs/ROADMAP.md`](./docs/ROADMAP.md) | Milestones, test plan, release checklist |
| [`CONTRIBUTING.md`](./CONTRIBUTING.md) | Local setup, scripts, publishing |
| [`example-app/README.md`](./example-app/README.md) | Running the manual test app on a device |
| [`CHANGELOG.md`](./CHANGELOG.md) | Release notes |

---

## Requirements

| | Version |
|-|---------|
| Capacitor | 8.x (peer `>=8.0.0`; developed and tested with 8.5) |
| Android | Same minimum SDK as Capacitor 8 |
| HMS IAP SDK | `com.huawei.hms:iap` 6.x, added by the **app** (tested: `6.16.6.305`) |
| Device | HMS Core (APK) installed. Huawei devices ship with it; other Android devices are prompted to install it. Tested: Huawei JEF-NX9, Android 10 / EMUI 12, HMS Core 6.16.4 |
| Developer account | Verified HUAWEI Developers account with **Merchant Service** enabled |

---

## API reference

Generated from `src/definitions.ts` by `npm run docgen`.

<docgen-index>

* [`isAvailable()`](#isavailable)
* [`isEnvReady(...)`](#isenvready)
* [`isSandboxActivated()`](#issandboxactivated)
* [`getProducts(...)`](#getproducts)
* [`purchase(...)`](#purchase)
* [`consumePurchase(...)`](#consumepurchase)
* [`getOwnedPurchases(...)`](#getownedpurchases)
* [`getPurchaseHistory(...)`](#getpurchasehistory)
* [`openSubscriptionManagement(...)`](#opensubscriptionmanagement)
* [`enablePendingPurchase()`](#enablependingpurchase)
* [`setLogging(...)`](#setlogging)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### isAvailable()

```typescript
isAvailable() => Promise<AvailabilityResult>
```

Whether HMS IAP can be used on this device and build. Never rejects.

**Returns:** <code>Promise&lt;<a href="#availabilityresult">AvailabilityResult</a>&gt;</code>

--------------------


### isEnvReady(...)

```typescript
isEnvReady(options?: EnvReadyOptions) => Promise<EnvReadyResult>
```

Checks HUAWEI ID sign-in and IAP availability for the user's region.

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#envreadyoptions">EnvReadyOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#envreadyresult">EnvReadyResult</a>&gt;</code>

--------------------


### isSandboxActivated()

```typescript
isSandboxActivated() => Promise<SandboxResult>
```

Testing only: whether the current account and APK are in the sandbox.

**Returns:** <code>Promise&lt;<a href="#sandboxresult">SandboxResult</a>&gt;</code>

--------------------


### getProducts(...)

```typescript
getProducts(options: GetProductsOptions) => Promise<{ products: Product[]; }>
```

Localized product information configured in AppGallery Connect.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#getproductsoptions">GetProductsOptions</a></code> |

**Returns:** <code>Promise&lt;{ products: {}; }&gt;</code>

--------------------


### purchase(...)

```typescript
purchase(options: PurchaseOptions) => Promise<PurchaseResult>
```

Launches the Huawei checkout screen.

Resolves only after the user closes Huawei's "Payment result" screen, so keep
the UI in a processing state until then. Verify `purchaseData` + `signature`
before granting anything (docs/APPGALLERY-SETUP.md §6).

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#purchaseoptions">PurchaseOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#purchaseresult">PurchaseResult</a>&gt;</code>

--------------------


### consumePurchase(...)

```typescript
consumePurchase(options: ConsumeOptions) => Promise<ConsumeResult>
```

Consumes a consumable so it can be bought again.

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#consumeoptions">ConsumeOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#consumeresult">ConsumeResult</a>&gt;</code>

--------------------


### getOwnedPurchases(...)

```typescript
getOwnedPurchases(options: OwnedQuery) => Promise<OwnedPurchasesResult>
```

Owned items: unconsumed consumables, non-consumables, active subscriptions.

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code><a href="#ownedquery">OwnedQuery</a></code> |

**Returns:** <code>Promise&lt;<a href="#ownedpurchasesresult">OwnedPurchasesResult</a>&gt;</code>

--------------------


### getPurchaseHistory(...)

```typescript
getPurchaseHistory(options: OwnedQuery) => Promise<OwnedPurchasesResult>
```

History: consumed consumables and all subscription receipts.

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code><a href="#ownedquery">OwnedQuery</a></code> |

**Returns:** <code>Promise&lt;<a href="#ownedpurchasesresult">OwnedPurchasesResult</a>&gt;</code>

--------------------


### openSubscriptionManagement(...)

```typescript
openSubscriptionManagement(options?: SubscriptionManagementOptions) => Promise<void>
```

Opens Huawei's subscription management screen.

| Param         | Type                                                                                    |
| ------------- | --------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#subscriptionmanagementoptions">SubscriptionManagementOptions</a></code> |

--------------------


### enablePendingPurchase()

```typescript
enablePendingPurchase() => Promise<void>
```

Opt in to pending purchases.

--------------------


### setLogging(...)

```typescript
setLogging(options: LoggingOptions) => Promise<void>
```

Turns verbose native logging on or off. Tokens and signatures are never logged.

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#loggingoptions">LoggingOptions</a></code> |

--------------------


### Interfaces


#### AvailabilityResult

| Prop            | Type                                                              | Description                    |
| --------------- | ----------------------------------------------------------------- | ------------------------------ |
| **`available`** | <code>boolean</code>                                              |                                |
| **`reason`**    | <code><a href="#availabilityreason">AvailabilityReason</a></code> | Set when `available` is false. |


#### EnvReadyResult

| Prop             | Type                | Description                                     |
| ---------------- | ------------------- | ----------------------------------------------- |
| **`returnCode`** | <code>number</code> |                                                 |
| **`country`**    | <code>string</code> | Country/region of the HUAWEI ID, when provided. |
| **`carrierId`**  | <code>string</code> |                                                 |


#### EnvReadyOptions

| Prop                  | Type                 | Description                                                         |
| --------------------- | -------------------- | ------------------------------------------------------------------- |
| **`resolveSignIn`**   | <code>boolean</code> | Launch the HUAWEI ID sign-in screen when the user is not signed in. |
| **`supportAppTouch`** | <code>boolean</code> | Passed to `IapClient.isEnvReady(boolean)`.                          |


#### SandboxResult

| Prop                    | Type                 |
| ----------------------- | -------------------- |
| **`isSandboxUser`**     | <code>boolean</code> |
| **`isSandboxApk`**      | <code>boolean</code> |
| **`versionInApk`**      | <code>string</code>  |
| **`versionFromMarket`** | <code>string</code>  |


#### Product

| Prop                           | Type                                                | Description                                     |
| ------------------------------ | --------------------------------------------------- | ----------------------------------------------- |
| **`productId`**                | <code>string</code>                                 |                                                 |
| **`type`**                     | <code><a href="#producttype">ProductType</a></code> |                                                 |
| **`name`**                     | <code>string</code>                                 |                                                 |
| **`description`**              | <code>string</code>                                 |                                                 |
| **`price`**                    | <code>string</code>                                 | Localized, formatted price, e.g. "Rp 99.000".   |
| **`priceMicros`**              | <code>number</code>                                 |                                                 |
| **`currency`**                 | <code>string</code>                                 | ISO 4217 currency code.                         |
| **`originalPrice`**            | <code>string</code>                                 |                                                 |
| **`originalPriceMicros`**      | <code>number</code>                                 |                                                 |
| **`status`**                   | <code>number</code>                                 | 0 = valid.                                      |
| **`subscriptionPeriod`**       | <code>string</code>                                 | Subscription only. ISO 8601 period, e.g. "P1M". |
| **`subscriptionGroupId`**      | <code>string</code>                                 |                                                 |
| **`subscriptionGroupTitle`**   | <code>string</code>                                 |                                                 |
| **`subscriptionLevel`**        | <code>number</code>                                 |                                                 |
| **`freeTrialPeriod`**          | <code>string</code>                                 |                                                 |
| **`introductoryPrice`**        | <code>string</code>                                 |                                                 |
| **`introductoryPriceMicros`**  | <code>number</code>                                 |                                                 |
| **`introductoryPeriod`**       | <code>string</code>                                 |                                                 |
| **`introductoryPeriodCycles`** | <code>number</code>                                 |                                                 |
| **`offerUsedStatus`**          | <code>number</code>                                 |                                                 |


#### GetProductsOptions

| Prop             | Type                                                | Description                                                          |
| ---------------- | --------------------------------------------------- | -------------------------------------------------------------------- |
| **`productIds`** | <code>Array&lt;string&gt;</code>                    | <a href="#product">Product</a> IDs configured in AppGallery Connect. |
| **`type`**       | <code><a href="#producttype">ProductType</a></code> |                                                                      |


#### PurchaseResult

| Prop                     | Type                                                              | Description                                                       |
| ------------------------ | ----------------------------------------------------------------- | ----------------------------------------------------------------- |
| **`purchaseData`**       | <code>string</code>                                               | Raw `InAppPurchaseData` JSON. Verify this string, not `purchase`. |
| **`signature`**          | <code>string</code>                                               |                                                                   |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |                                                                   |
| **`purchase`**           | <code><a href="#inapppurchase">InAppPurchase</a></code>           | Parsed copy of `purchaseData` for convenience.                    |


#### InAppPurchase

| Prop                      | Type                                                    | Description                       |
| ------------------------- | ------------------------------------------------------- | --------------------------------- |
| **`orderId`**             | <code>string</code>                                     |                                   |
| **`productId`**           | <code>string</code>                                     |                                   |
| **`purchaseToken`**       | <code>string</code>                                     |                                   |
| **`purchaseState`**       | <code><a href="#purchasestate">PurchaseState</a></code> |                                   |
| **`purchaseTime`**        | <code>number</code>                                     | Epoch milliseconds.               |
| **`kind`**                | <code><a href="#producttype">ProductType</a></code>     |                                   |
| **`packageName`**         | <code>string</code>                                     |                                   |
| **`developerPayload`**    | <code>string</code>                                     |                                   |
| **`consumptionState`**    | <code>number</code>                                     |                                   |
| **`confirmed`**           | <code>number</code>                                     |                                   |
| **`subscriptionId`**      | <code>string</code>                                     |                                   |
| **`isSubscriptionValid`** | <code>boolean</code>                                    |                                   |
| **`autoRenewing`**        | <code>boolean</code>                                    |                                   |
| **`expirationDate`**      | <code>number</code>                                     | Epoch milliseconds.               |
| **`raw`**                 | <code>Record&lt;string, unknown&gt;</code>              | Every field of the original JSON. |


#### PurchaseOptions

| Prop                     | Type                                                              | Description                                   |
| ------------------------ | ----------------------------------------------------------------- | --------------------------------------------- |
| **`productId`**          | <code>string</code>                                               |                                               |
| **`type`**               | <code><a href="#producttype">ProductType</a></code>               |                                               |
| **`developerPayload`**   | <code>string</code>                                               | Echoed back in the purchase data.             |
| **`reservedInfo`**       | <code>string</code>                                               | JSON string passed to HMS as `reservedInfor`. |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |                                               |


#### ConsumeResult

| Prop                     | Type                                                              |
| ------------------------ | ----------------------------------------------------------------- |
| **`consumeData`**        | <code>string</code>                                               |
| **`signature`**          | <code>string</code>                                               |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |


#### ConsumeOptions

| Prop                     | Type                                                              |
| ------------------------ | ----------------------------------------------------------------- |
| **`purchaseToken`**      | <code>string</code>                                               |
| **`developerChallenge`** | <code>string</code>                                               |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |


#### OwnedPurchasesResult

| Prop                    | Type                                                                   | Description                                       |
| ----------------------- | ---------------------------------------------------------------------- | ------------------------------------------------- |
| **`purchases`**         | <code>Array&lt;<a href="#purchaseresult">PurchaseResult</a>&gt;</code> |                                                   |
| **`pendingPurchases`**  | <code>Array&lt;<a href="#purchaseresult">PurchaseResult</a>&gt;</code> | Purchases waiting for payment (pending purchase). |
| **`continuationToken`** | <code>string</code>                                                    | Present when more pages are available.            |


#### OwnedQuery

| Prop                     | Type                                                              | Description                                        |
| ------------------------ | ----------------------------------------------------------------- | -------------------------------------------------- |
| **`type`**               | <code><a href="#producttype">ProductType</a></code>               |                                                    |
| **`continuationToken`**  | <code>string</code>                                               | Pass the token from the previous page to continue. |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |                                                    |


#### SubscriptionManagementOptions

| Prop            | Type                | Description                                                     |
| --------------- | ------------------- | --------------------------------------------------------------- |
| **`productId`** | <code>string</code> | Open the detail screen of one subscription instead of the list. |


#### LoggingOptions

| Prop          | Type                 |
| ------------- | -------------------- |
| **`enabled`** | <code>boolean</code> |


### Type Aliases


#### AvailabilityReason

`HMS_CORE_OUTDATED` is reserved; the Android implementation does not return it yet.

<code>'NOT_ANDROID' | 'HMS_CORE_MISSING' | 'HMS_CORE_OUTDATED' | 'IAP_SDK_MISSING'</code>


### Enums


#### ProductType

| Members             | Value          | Description                                               |
| ------------------- | -------------- | --------------------------------------------------------- |
| **`Consumable`**    | <code>0</code> | Consumed after use and can be bought again (priceType 0). |
| **`NonConsumable`** | <code>1</code> | Bought once and never expires (priceType 1).              |
| **`Subscription`**  | <code>2</code> | Auto-renewable subscription (priceType 2).                |


#### SignatureAlgorithm

| Members                | Value                            | Description                                                                                                                                                                                                        |
| ---------------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`Sha256WithRsa`**    | <code>'SHA256WithRSA'</code>     | HMS default.                                                                                                                                                                                                       |
| **`Sha256WithRsaPss`** | <code>'SHA256WithRSA/PSS'</code> | Stronger, but only honored after the PSS switch is on in AppGallery Connect (Earn → In-App Purchases → Settings). Until then HMS returns `SHA256WithRSA`; always verify with the algorithm reported in the result. |


#### PurchaseState

| Members           | Value           |
| ----------------- | --------------- |
| **`Initialized`** | <code>-1</code> |
| **`Purchased`**   | <code>0</code>  |
| **`Canceled`**    | <code>1</code>  |
| **`Refunded`**    | <code>2</code>  |
| **`Pending`**     | <code>3</code>  |

</docgen-api>

---

## License

**MIT** — see [`LICENSE`](./LICENSE). The plugin is written from scratch against Huawei's public documentation; no code is taken from the Apache-2.0 HMS Cordova plugin.

HUAWEI, AppGallery and HMS are trademarks of Huawei Technologies Co., Ltd. This project is not affiliated with or endorsed by Huawei.
