# Design — capacitor-hms-iap

**Status:** Draft v0.1
**Last updated:** 2026-11-17

This document specifies the public API, native behavior and error model of the plugin. It is the source of truth for the first implementation.

---

## 1. Principles

1. **Thin wrapper.** One plugin method maps to one `IapClient` call (plus the checkout activity where required). No caching, no retry loops, no hidden state.
2. **Predictable promises.** Every method resolves once or rejects once. Cancellation is a rejection with a dedicated code, never a silent resolve.
3. **Keep the original data.** Purchase data is returned both raw (`purchaseData`, `signature`) for verification and parsed (`purchase`) for convenience. Apps must verify the raw values, not the parsed object.
4. **Friendly names, faithful values.** JavaScript names are idiomatic (`getProducts`, `type`), but no value is invented; numeric HMS fields are passed through.
5. **No secrets in the plugin.** The IAP public key and any server credentials stay in the app.

---

## 2. Public API

### 2.1 Enums

```ts
export enum ProductType {
  Consumable = 0,     // HMS priceType 0
  NonConsumable = 1,  // HMS priceType 1
  Subscription = 2,   // HMS priceType 2
}

export enum SignatureAlgorithm {
  Sha256WithRsa = 'SHA256WithRSA',          // HMS default
  Sha256WithRsaPss = 'SHA256WithRSA/PSS',   // recommended
}

export enum PurchaseState {
  Initialized = -1,
  Purchased = 0,
  Canceled = 1,
  Refunded = 2,
  Pending = 3,
}
```

> `PurchaseState` values follow the `purchaseState` field of `InAppPurchaseData`. Verify against the HMS documentation for the SDK version in use before the first release.

### 2.2 Plugin interface

```ts
export interface HmsIapPlugin {
  /** True when running on Android and HMS Core is installed and usable. Never rejects. */
  isAvailable(): Promise<{ available: boolean; reason?: AvailabilityReason }>;

  /**
   * Checks sign-in state and IAP availability for the user's region.
   * If the user is not signed in and `resolveSignIn` is true (default),
   * the Huawei sign-in screen is launched and the call resolves after it closes.
   */
  isEnvReady(options?: { resolveSignIn?: boolean; supportAppTouch?: boolean }): Promise<EnvReadyResult>;

  /** For testing only: whether the current account and APK are in the sandbox. */
  isSandboxActivated(): Promise<SandboxResult>;

  /** Localized product information configured in AppGallery Connect. */
  getProducts(options: { productIds: string[]; type: ProductType }): Promise<{ products: Product[] }>;

  /** Launches the Huawei checkout screen. */
  purchase(options: PurchaseOptions): Promise<PurchaseResult>;

  /** Consumes a consumable so it can be bought again. */
  consumePurchase(options: { purchaseToken: string; developerChallenge?: string;
                            signatureAlgorithm?: SignatureAlgorithm }): Promise<ConsumeResult>;

  /** Currently owned items: unconsumed consumables, non-consumables, active subscriptions. */
  getOwnedPurchases(options: OwnedQuery): Promise<OwnedPurchasesResult>;

  /** History: consumed consumables and all subscription receipts. */
  getPurchaseHistory(options: OwnedQuery): Promise<OwnedPurchasesResult>;

  /** Opens Huawei's subscription management (list or a single subscription). */
  openSubscriptionManagement(options?: { productId?: string }): Promise<void>;

  /** Opt in to pending purchases (pay later / offline payment methods). */
  enablePendingPurchase(): Promise<void>;

  /** Turns on verbose native logging (debug builds). */
  setLogging(options: { enabled: boolean }): Promise<void>;
}
```

### 2.3 Data types

```ts
export type AvailabilityReason =
  | 'NOT_ANDROID'
  | 'HMS_CORE_MISSING'
  | 'HMS_CORE_OUTDATED'
  | 'IAP_SDK_MISSING';       // app did not add com.huawei.hms:iap to this build

export interface EnvReadyResult {
  returnCode: number;
  country?: string;          // ISO country of the HUAWEI ID, when provided
  carrierId?: string;
}

export interface SandboxResult {
  isSandboxUser: boolean;
  isSandboxApk: boolean;
  versionInApk?: string;
  versionFromMarket?: string;
}

export interface Product {
  productId: string;
  type: ProductType;
  name: string;
  description: string;
  price: string;                 // formatted, e.g. "Rp 99.000"
  priceMicros: number;
  currency: string;              // ISO 4217
  originalPrice?: string;
  originalPriceMicros?: number;
  status: number;                // 0 = valid
  // Subscription-only fields
  subscriptionPeriod?: string;   // ISO 8601, e.g. "P1M"
  subscriptionGroupId?: string;
  subscriptionGroupTitle?: string;
  subscriptionLevel?: number;
  freeTrialPeriod?: string;
  introductoryPrice?: string;
  introductoryPriceMicros?: number;
  introductoryPeriod?: string;
  introductoryPeriodCycles?: number;
  offerUsedStatus?: number;
}

export interface PurchaseOptions {
  productId: string;
  type: ProductType;
  developerPayload?: string;     // echoed back in purchase data
  reservedInfo?: string;         // JSON string, passed as reservedInfor
  signatureAlgorithm?: SignatureAlgorithm;
}

export interface PurchaseResult {
  purchaseData: string;          // raw InAppPurchaseData JSON — verify this
  signature: string;
  signatureAlgorithm: SignatureAlgorithm;
  purchase: InAppPurchase;       // parsed copy for convenience
}

export interface InAppPurchase {
  orderId: string;
  productId: string;
  purchaseToken: string;
  purchaseState: PurchaseState;
  purchaseTime: number;          // epoch ms
  kind: ProductType;
  packageName?: string;
  developerPayload?: string;
  consumptionState?: number;
  confirmed?: number;
  // Subscription fields
  subscriptionId?: string;
  isSubscriptionValid?: boolean;
  autoRenewing?: boolean;
  expirationDate?: number;
  // Everything else from the original JSON
  raw: Record<string, unknown>;
}

export interface OwnedQuery {
  type: ProductType;
  continuationToken?: string;
  signatureAlgorithm?: SignatureAlgorithm;
}

export interface OwnedPurchasesResult {
  purchases: PurchaseResult[];          // same shape as purchase()
  pendingPurchases: PurchaseResult[];   // placedInapp* lists
  continuationToken?: string;           // call again while present
}

export interface ConsumeResult {
  consumeData: string;
  signature: string;
  signatureAlgorithm: SignatureAlgorithm;
}
```

---

## 3. Native mapping (Android)

| Plugin method | HMS call | Activity? |
|---------------|----------|-----------|
| `isAvailable` | `HuaweiApiAvailability.isHuaweiMobileServicesAvailable(context)` + class check for `com.huawei.hms.iap.Iap` | No |
| `isEnvReady` | `IapClient.isEnvReady(supportAppTouch)`; on `ORDER_HWID_NOT_LOGIN` with a resolution, `Status.startResolutionForResult` | Sign-in screen when needed |
| `isSandboxActivated` | `IapClient.isSandboxActivated(IsSandboxActivatedReq)` | No |
| `getProducts` | `IapClient.obtainProductInfo(ProductInfoReq{ priceType, productIds })` | No |
| `purchase` | `IapClient.createPurchaseIntent(PurchaseIntentReq)` → `Status.startResolutionForResult` → `IapClient.parsePurchaseResultInfoFromIntent` | Checkout screen |
| `consumePurchase` | `IapClient.consumeOwnedPurchase(ConsumeOwnedPurchaseReq)` | No |
| `getOwnedPurchases` | `IapClient.obtainOwnedPurchases(OwnedPurchasesReq)` | No |
| `getPurchaseHistory` | `IapClient.obtainOwnedPurchaseRecord(OwnedPurchasesReq)` | No |
| `openSubscriptionManagement` | `IapClient.startIapActivity(StartIapActivityReq)` → start returned intent | Huawei screen |
| `enablePendingPurchase` | `Iap.getIapClient(activity).enablePendingPurchase()` | No |

### 3.1 Activity results

- Capacitor's `startActivityForResult(call, intent, "callbackName")` and an `@ActivityCallback` method are used instead of overriding `onActivityResult`.
- HMS returns a `Status` with a `PendingIntent`. The plugin wraps it with `IntentSenderRequest` / the bridge's activity launcher so the result reaches the same `PluginCall`.
- Only one checkout may run at a time. A second `purchase()` while one is active rejects with `PURCHASE_IN_PROGRESS`.
- If the activity result intent is `null`, the call rejects with `NO_RESULT`.

### 3.2 Threading

All HMS `Task` listeners run on the main thread; results are resolved directly. No work is done on the WebView thread.

### 3.3 Dependencies

```gradle
// plugin/android/build.gradle
repositories {
    google()
    mavenCentral()
    maven { url 'https://developer.huawei.com/repo/' }
}
dependencies {
    compileOnly "com.huawei.hms:iap:${hmsIapVersion}"   // app decides where it is included
}
```

Because the SDK is `compileOnly`, the **app** must add `implementation "com.huawei.hms:iap:<version>"` (or a flavor-scoped variant such as `huaweiImplementation`). When the classes are missing at runtime, every method except `isAvailable` rejects with `IAP_SDK_MISSING`. This lets multi-store apps keep Huawei code out of other store builds.

### 3.4 Web and iOS

All methods reject with `UNAVAILABLE`, except `isAvailable`, which resolves `{ available: false, reason: 'NOT_ANDROID' }`.

---

## 4. Error model

Rejections use Capacitor's `PluginCall.reject(message, code, data)`:

```ts
interface HmsIapError {
  code: HmsIapErrorCode;     // stable string, documented below
  message: string;
  data?: { returnCode?: number; statusCode?: number; errMsg?: string };
}
```

| `code` | HMS source (`OrderStatusCode`) | Meaning |
|--------|-------------------------------|---------|
| `USER_CANCELED` | `ORDER_STATE_CANCEL` (60000) | User closed checkout or sign-in |
| `NOT_SIGNED_IN` | `ORDER_HWID_NOT_LOGIN` (60050) | No HUAWEI ID, and sign-in was not resolved |
| `ALREADY_OWNED` | `ORDER_PRODUCT_OWNED` (60051) | Non-consumable/consumable already owned — call `getOwnedPurchases` |
| `NOT_OWNED` | `ORDER_PRODUCT_NOT_OWNED` (60052) | Consume target not owned |
| `ALREADY_CONSUMED` | `ORDER_PRODUCT_CONSUMED` (60053) | |
| `REGION_NOT_SUPPORTED` | `ORDER_ACCOUNT_AREA_NOT_SUPPORTED` (60054) | IAP not available for the account's region |
| `NETWORK_ERROR` | `ORDER_STATE_NET_ERROR` (60005) | |
| `INVALID_ARGUMENT` | `ORDER_STATE_PARAM_ERROR` (60001) or plugin validation | |
| `PURCHASE_IN_PROGRESS` | plugin | Another checkout is open |
| `NO_RESULT` | plugin | Activity returned no data |
| `IAP_SDK_MISSING` | plugin | `com.huawei.hms:iap` not on the classpath |
| `UNAVAILABLE` | plugin | Not Android, or HMS Core missing |
| `APP_NOT_CONFIGURED` | HMS framework `907135000` | App ID or signing fingerprint check failed: `agconnect-services.json` missing, wrong package, or SHA-256 not registered in AppGallery Connect (observed on device, HMS Core 6.16.4) |
| `UNKNOWN` | anything else | Inspect `data.returnCode` |
| `NOT_IMPLEMENTED` | plugin | Temporary during 0.x: method exists but is not implemented yet |

> Numeric values must be checked against `OrderStatusCode` in the exact SDK version before release; the mapping is generated from the SDK constants, not hard-coded literals, in the implementation.

---

## 5. Security notes (for app developers)

- **Always verify** `purchaseData` against `signature` with the public key from AppGallery Connect (*Earn → In-App Purchases → Settings*). Verification can happen on-device (offline-friendly) or on a server (stronger).
- Prefer `SHA256WithRSA/PSS` and enable it in AppGallery Connect.
- Check `purchaseState === Purchased` and the expected `productId` / `packageName` before granting content.
- Grant consumables **before** consuming them, and consume only after the grant is persisted.
- For subscriptions or high-value content, validate on a server with the HMS IAP server API.
- The plugin never logs purchase tokens or signatures, even with logging enabled.

---

## 6. Example flows

### 6.1 One-time unlock (non-consumable)

```text
app start → getOwnedPurchases(NonConsumable) → verify → grant if found
"Buy" tapped → isEnvReady → getProducts → purchase
  ├─ success → verify → grant
  ├─ ALREADY_OWNED → getOwnedPurchases → verify → grant
  └─ USER_CANCELED → do nothing
"Restore" tapped → getOwnedPurchases → verify → grant
```

### 6.2 Consumable

```text
purchase → verify → grant & persist → consumePurchase
on start → getOwnedPurchases(Consumable) → grant & consume any leftovers
```

### 6.3 Subscription

```text
purchase → verify → grant until expirationDate
on start → getOwnedPurchases(Subscription) → keep only isSubscriptionValid
"Manage" → openSubscriptionManagement({ productId })
```

---

## 7. Open questions

| # | Question | Default until decided |
|---|----------|-----------------------|
| 1 | Ship an optional `verifySignature()` helper in JS (Web Crypto) for on-device verification? | Not in v0.1; document a snippet instead |
| 2 | Include the HMS SDK as `implementation` for apps that only target AppGallery? | No — keep `compileOnly`, document the one-line dependency |
| 3 | Emit an event when a pending purchase completes? | No — apps poll `getOwnedPurchases` on resume |
| 4 | Support `IsEnvReady` "app touch" (HUAWEI Vision / devices without HMS account UI)? | Option passed through, untested |
