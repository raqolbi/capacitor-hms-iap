# capacitor-hms-iap

> **Status: early development (0.1.0-dev).** Implemented on Android: `isAvailable`, `isEnvReady`, `isSandboxActivated`, `getProducts`, `purchase`, `getOwnedPurchases`. Other methods reject with `NOT_IMPLEMENTED`. Not published to npm yet.

A Capacitor plugin for **HUAWEI In-App Purchases (IAP)**: sell consumables, non-consumables and subscriptions in Android apps distributed through **Huawei AppGallery**.

There is no Capacitor-native HMS IAP plugin today. The official [`@hmscore/cordova-plugin-hms-iap`](https://github.com/HMS-Core/hms-cordova-plugin) works in Cordova, but relies on Cordova install hooks and Cordova-style activity results that Capacitor does not run. This plugin aims to be a small, Capacitor-first alternative.

---

## Goals

- **Capacitor-first.** Built from the official Capacitor plugin template; uses `@ActivityCallback` for the checkout flow. No Cordova hooks, no `config.xml`.
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

## Planned API at a glance

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

// 4. Verify result.purchaseData + result.signature with your IAP public key,
//    then unlock the content.

// 5. Restore on reinstall or a new device.
const owned = await HmsIap.getOwnedPurchases({ type: ProductType.NonConsumable });
```

Full specification: [`docs/DESIGN.md`](./docs/DESIGN.md).

---

## Install (once published)

```bash
npm install capacitor-hms-iap
npx cap sync android
```

Then follow [`docs/APPGALLERY-SETUP.md`](./docs/APPGALLERY-SETUP.md): the app must add `com.huawei.hms:iap`, the AGConnect Gradle plugin and `agconnect-services.json`.

## Documents

| Document | Content |
|----------|---------|
| [`docs/DESIGN.md`](./docs/DESIGN.md) | API specification, data types, native mapping, error model, security notes |
| [`docs/APPGALLERY-SETUP.md`](./docs/APPGALLERY-SETUP.md) | Steps an app developer follows in AppGallery Connect and Gradle |
| [`docs/ROADMAP.md`](./docs/ROADMAP.md) | Milestones, test plan, release checklist |
| [`CONTRIBUTING.md`](./CONTRIBUTING.md) | Local setup, scripts, publishing |
| [`CHANGELOG.md`](./CHANGELOG.md) | Release notes |

---

## Requirements (planned)

| | Version |
|-|---------|
| Capacitor | 8.x (peer `>=8.0.0`; Capacitor 9 will be tested once it is stable) |
| Android | Same minimum SDK as Capacitor 8 |
| HMS IAP SDK | `com.huawei.hms:iap` 6.x (tested target: `6.16.6.305`) |
| Device | HMS Core (APK) installed. Huawei devices ship with it; other Android devices are prompted to install it |
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
isAvailable() => any
```

Whether HMS IAP can be used on this device and build. Never rejects.

**Returns:** <code>any</code>

--------------------


### isEnvReady(...)

```typescript
isEnvReady(options?: EnvReadyOptions | undefined) => any
```

Checks HUAWEI ID sign-in and IAP availability for the user's region.

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#envreadyoptions">EnvReadyOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### isSandboxActivated()

```typescript
isSandboxActivated() => any
```

Testing only: whether the current account and APK are in the sandbox.

**Returns:** <code>any</code>

--------------------


### getProducts(...)

```typescript
getProducts(options: GetProductsOptions) => any
```

Localized product information configured in AppGallery Connect.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#getproductsoptions">GetProductsOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### purchase(...)

```typescript
purchase(options: PurchaseOptions) => any
```

Launches the Huawei checkout screen.

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#purchaseoptions">PurchaseOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### consumePurchase(...)

```typescript
consumePurchase(options: ConsumeOptions) => any
```

Consumes a consumable so it can be bought again.

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#consumeoptions">ConsumeOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### getOwnedPurchases(...)

```typescript
getOwnedPurchases(options: OwnedQuery) => any
```

Owned items: unconsumed consumables, non-consumables, active subscriptions.

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code><a href="#ownedquery">OwnedQuery</a></code> |

**Returns:** <code>any</code>

--------------------


### getPurchaseHistory(...)

```typescript
getPurchaseHistory(options: OwnedQuery) => any
```

History: consumed consumables and all subscription receipts.

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code><a href="#ownedquery">OwnedQuery</a></code> |

**Returns:** <code>any</code>

--------------------


### openSubscriptionManagement(...)

```typescript
openSubscriptionManagement(options?: SubscriptionManagementOptions | undefined) => any
```

Opens Huawei's subscription management screen.

| Param         | Type                                                                                    |
| ------------- | --------------------------------------------------------------------------------------- |
| **`options`** | <code><a href="#subscriptionmanagementoptions">SubscriptionManagementOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### enablePendingPurchase()

```typescript
enablePendingPurchase() => any
```

Opt in to pending purchases.

**Returns:** <code>any</code>

--------------------


### setLogging(...)

```typescript
setLogging(options: LoggingOptions) => any
```

Turns verbose native logging on or off. Tokens and signatures are never logged.

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#loggingoptions">LoggingOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### AvailabilityResult

| Prop            | Type                                                              | Description                    |
| --------------- | ----------------------------------------------------------------- | ------------------------------ |
| **`available`** | <code>boolean</code>                                              |                                |
| **`reason`**    | <code><a href="#availabilityreason">AvailabilityReason</a></code> | Set when `available` is false. |


#### EnvReadyOptions

| Prop                  | Type                 | Description                                                         | Default            |
| --------------------- | -------------------- | ------------------------------------------------------------------- | ------------------ |
| **`resolveSignIn`**   | <code>boolean</code> | Launch the HUAWEI ID sign-in screen when the user is not signed in. | <code>true</code>  |
| **`supportAppTouch`** | <code>boolean</code> | Passed to `IapClient.isEnvReady(boolean)`.                          | <code>false</code> |


#### EnvReadyResult

| Prop             | Type                | Description                                     |
| ---------------- | ------------------- | ----------------------------------------------- |
| **`returnCode`** | <code>number</code> |                                                 |
| **`country`**    | <code>string</code> | Country/region of the HUAWEI ID, when provided. |
| **`carrierId`**  | <code>string</code> |                                                 |


#### SandboxResult

| Prop                    | Type                 |
| ----------------------- | -------------------- |
| **`isSandboxUser`**     | <code>boolean</code> |
| **`isSandboxApk`**      | <code>boolean</code> |
| **`versionInApk`**      | <code>string</code>  |
| **`versionFromMarket`** | <code>string</code>  |


#### GetProductsOptions

| Prop             | Type                                                | Description                                                          |
| ---------------- | --------------------------------------------------- | -------------------------------------------------------------------- |
| **`productIds`** | <code>{}</code>                                     | <a href="#product">Product</a> IDs configured in AppGallery Connect. |
| **`type`**       | <code><a href="#producttype">ProductType</a></code> |                                                                      |


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


#### PurchaseOptions

| Prop                     | Type                                                              | Description                                   |
| ------------------------ | ----------------------------------------------------------------- | --------------------------------------------- |
| **`productId`**          | <code>string</code>                                               |                                               |
| **`type`**               | <code><a href="#producttype">ProductType</a></code>               |                                               |
| **`developerPayload`**   | <code>string</code>                                               | Echoed back in the purchase data.             |
| **`reservedInfo`**       | <code>string</code>                                               | JSON string passed to HMS as `reservedInfor`. |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |                                               |


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


#### ConsumeOptions

| Prop                     | Type                                                              |
| ------------------------ | ----------------------------------------------------------------- |
| **`purchaseToken`**      | <code>string</code>                                               |
| **`developerChallenge`** | <code>string</code>                                               |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |


#### ConsumeResult

| Prop                     | Type                                                              |
| ------------------------ | ----------------------------------------------------------------- |
| **`consumeData`**        | <code>string</code>                                               |
| **`signature`**          | <code>string</code>                                               |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |


#### OwnedQuery

| Prop                     | Type                                                              | Description                                        |
| ------------------------ | ----------------------------------------------------------------- | -------------------------------------------------- |
| **`type`**               | <code><a href="#producttype">ProductType</a></code>               |                                                    |
| **`continuationToken`**  | <code>string</code>                                               | Pass the token from the previous page to continue. |
| **`signatureAlgorithm`** | <code><a href="#signaturealgorithm">SignatureAlgorithm</a></code> |                                                    |


#### OwnedPurchasesResult

| Prop                    | Type                | Description                                       |
| ----------------------- | ------------------- | ------------------------------------------------- |
| **`purchases`**         | <code>{}</code>     |                                                   |
| **`pendingPurchases`**  | <code>{}</code>     | Purchases waiting for payment (pending purchase). |
| **`continuationToken`** | <code>string</code> | Present when more pages are available.            |


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

<code>'NOT_ANDROID' | 'HMS_CORE_MISSING' | 'HMS_CORE_OUTDATED' | 'IAP_SDK_MISSING'</code>


### Enums


#### ProductType

| Members             | Value          | Description                                               |
| ------------------- | -------------- | --------------------------------------------------------- |
| **`Consumable`**    | <code>0</code> | Consumed after use and can be bought again (priceType 0). |
| **`NonConsumable`** | <code>1</code> | Bought once and never expires (priceType 1).              |
| **`Subscription`**  | <code>2</code> | Auto-renewable subscription (priceType 2).                |


#### SignatureAlgorithm

| Members                | Value                            | Description                                           |
| ---------------------- | -------------------------------- | ----------------------------------------------------- |
| **`Sha256WithRsa`**    | <code>'SHA256WithRSA'</code>     | HMS default.                                          |
| **`Sha256WithRsaPss`** | <code>'SHA256WithRSA/PSS'</code> | Recommended. Enable it in AppGallery Connect as well. |


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

**MIT** — see [`LICENSE`](./LICENSE). The plugin will be written from scratch against Huawei's public documentation. If any code is adapted from the Apache-2.0 HMS Cordova plugin, the license and notices will be updated accordingly.

HUAWEI, AppGallery and HMS are trademarks of Huawei Technologies Co., Ltd. This project is not affiliated with or endorsed by Huawei.
