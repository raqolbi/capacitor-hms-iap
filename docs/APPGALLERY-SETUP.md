# AppGallery Setup Guide

**Status:** Steps in §2, §4 and §5 confirmed on a real account and a Huawei device (2026-11-18). Console labels may change; the paths below are what the console showed then.

This guide lists what an **app developer** must do outside of JavaScript before `capacitor-hms-iap` can sell anything.

---

## 1. HUAWEI Developers account

1. Register at HUAWEI Developers and complete **identity verification** (individual or enterprise).
2. Enable **Merchant Service**. Required information:
   - Bank account (country, bank name, branch, SWIFT code, account name and number)
   - Tax information (location, tax qualification, VAT number if any)
   - Contacts for operations, finance, legal and business
3. Read and accept the revenue-sharing terms shown when Merchant Service and IAP are activated.

## 2. App in AppGallery Connect

AppGallery Connect has two entry points. Most IAP settings live under **My projects**, not **My apps**.

| What | Where |
|------|-------|
| Signing fingerprint, `agconnect-services.json`, API switches | *My projects → your project → Project settings* |
| IAP switch, agreement, **public key** | *My projects → your project → Earn → In-App Purchases* |
| Products (ID, type, price) | *My apps → your app → Operate → Products → Product Management* |
| Sandbox testers | *Users and permissions → Sandbox testing* |

### 2.1 Create the app and register the signing key

1. Create a project and an Android app with your final **package name**.
2. Generate the SHA-256 fingerprint of **every** key you sign with (debug and release):

   ```bash
   keytool -list -v -keystore <your-keystore>.jks
   # debug key: keytool -list -v -keystore ~/.android/debug.keystore -storepass android
   ```

3. Add each fingerprint under *Project settings → General information → SHA-256 certificate fingerprint*.
4. Download **`agconnect-services.json`** from the same page.

### 2.2 Turn on In-App Purchases

1. *My projects* → select the project → make sure the right **app** is selected in the app picker at the top.
2. If **Earn → In-App Purchases** is not in the left menu, click **Customize menu bar** (bottom left) and **pin** *In-App Purchases* (under *Earn*). The menu is hidden by default in some accounts.
3. Open *Earn → In-App Purchases* and click **Settings**. The first time, sign the agreement.
4. Also check *Project settings → Manage APIs* and make sure **In-App Purchases** is on.
5. Do this **for each app** that sells products; the switch is per app.

> Pick **In-App Purchases** (HMS Core, Android). **IAP Kit** for HarmonyOS NEXT is a different product and does not apply to this plugin.

Until IAP is active for the app, `isSandboxActivated`, `getOwnedPurchases` and `purchase` fail with `IAP_NOT_ACTIVATED` (`60002`; the `hms_pay` log shows `createOrder fail, errCode 6, subErrCode 101`). `getProducts` may already work. After switching it on, allow a few minutes to propagate.

### 2.3 Copy the IAP public key

After the settings are saved, the *In-App Purchases → Settings* page shows a **public key**: a long base64 string starting with `MIIB…` (RSA, X.509 SubjectPublicKeyInfo). It is used to check that `purchaseData` really comes from Huawei. Where it goes in your app is described in §6.

Optional switches on the same page:

- **Use SHA256WithRSA/PSS signature algorithm.** Until it is on, HMS answers with `SHA256WithRSA` signatures even when you pass `signatureAlgorithm: 'SHA256WithRSA/PSS'` (observed 2026-11-18). Always verify with the algorithm **returned** in the result.
- **Key event notifications.** Only needed if you run a server.

### 2.4 Create products

*My apps → your app → Operate → Products → Product Management → Add product.*

- Type: **Non-consumable** for one-time unlocks, **Consumable** for repeatable items.
- **Product ID cannot be changed and cannot be reused after deletion.** Use the exact ID your code uses.
- Set language(s), name, description, sales countries/regions, and price, then **activate** the product. Inactive products are not returned by `getProducts`.

## 3. Gradle configuration (Capacitor app)

### 3.1 Root `android/build.gradle`

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://developer.huawei.com/repo/' }
    }
    dependencies {
        classpath 'com.huawei.agconnect:agcp:<version>'
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://developer.huawei.com/repo/' }
    }
}
```

### 3.2 App `android/app/build.gradle`

```gradle
dependencies {
    implementation 'com.huawei.hms:iap:<version>'
}
apply plugin: 'com.huawei.agconnect'
```

### 3.3 `agconnect-services.json`

Place it in `android/app/`. Without it (or without a registered SHA-256 fingerprint for the signing key in use, including the **debug** key), every IAP call fails with `APP_NOT_CONFIGURED` (HMS code `907135000`).

### 3.4 `allowBackup` conflict

The HMS IAP SDK manifest sets `android:allowBackup="false"`. If your app sets `allowBackup="true"`, the manifest merge fails. Keep your value with `tools:replace`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <application
        tools:replace="android:allowBackup"
        android:allowBackup="true"
        ...>
```

### 3.5 Multi-store apps (optional)

If the same app is also built for another store, scope Huawei pieces to a product flavor:

```gradle
android {
    flavorDimensions += 'store'
    productFlavors {
        huawei { dimension 'store' }
        play   { dimension 'store' }
    }
}
dependencies {
    huaweiImplementation 'com.huawei.hms:iap:<version>'
}
```

- Put `agconnect-services.json` in `android/app/src/huawei/`.
- Apply `com.huawei.agconnect` only when the Huawei variant needs it, for example:

  ```gradle
  // android/app/build.gradle (end of file)
  if (file('src/huawei/agconnect-services.json').exists()) {
      apply plugin: 'com.huawei.agconnect'
  }
  ```

- Validated so far in a production app (2026-11-18): flavors with the HMS dependency and AGConnect switched off produce an APK without any `com.huawei.*` classes. A Play build next to an active Huawei IAP build is **not yet validated**; that is milestone 0.4.0 (`ROADMAP.md`).
- In the other flavor, `HmsIap.isAvailable()` returns `IAP_SDK_MISSING`, so your app can pick a different billing provider.

## 4. Testing

1. Add tester HUAWEI IDs under *Users and permissions → Sandbox testing*. Changes take 30–60 minutes.
2. The APK under test must have a `versionCode` greater than 0 and, if a version was already released, greater than the released one.
3. Call `HmsIap.isSandboxActivated()` and continue only when it returns `isSandboxUser: true`. The checkout then shows a **"sandbox test"** notice and charges nothing.
4. If no sandbox notice appears, **real payments are made**. Stop and check steps 1–2.
5. The first purchase on a device shows the HUAWEI IAP user agreement. The user must accept it.
6. After paying, Huawei shows a **"Payment result"** screen. `purchase()` resolves only after the user closes it with **OK**, so keep your UI in a "processing" state until then.

Results observed on a Huawei device (Android 10, HMS Core 6.16.4) in the sandbox: purchase succeeds with an order ID starting `sandbox…`; buying the same non-consumable again → `ALREADY_OWNED` (`60051`); after clearing app data, `getOwnedPurchases` returns the purchase again.

> Sandbox availability for individual developers in some regions has varied over time. Confirm in AppGallery Connect before relying on it.

## 5. Release checklist

- [ ] Products are **valid** (activated) in Product Management
- [ ] Release build is signed with the key whose fingerprint is registered
- [ ] `agconnect-services.json` matches the release package name
- [ ] Signature verification uses the production public key
- [ ] Restore purchases is reachable from the UI
- [ ] Privacy policy mentions purchase data processed by Huawei

## 6. Using the IAP public key in your app

The plugin **does not** verify signatures and never reads the key, so it works the same in any framework (React, Next.js, Svelte, Vue, plain JS). The key name and where it is stored are up to your app; the verify function below takes it as a parameter. Verification is the app's job (or your server's), so the key belongs in **your app's configuration**, not in this plugin.

**Where to put it**

| App setup | Suggested place |
|-----------|-----------------|
| Vite / plain web build | `VITE_HMS_IAP_PUBLIC_KEY` in `.env` |
| SvelteKit (static adapter) | `PUBLIC_HMS_IAP_PUBLIC_KEY` in `.env`, read with `import { PUBLIC_HMS_IAP_PUBLIC_KEY } from '$env/static/public'` |
| Next.js static export | `NEXT_PUBLIC_HMS_IAP_PUBLIC_KEY` in `.env.production` or your build environment |
| Your own server | Server config / secret store; verify there instead of on the device |

- **One key per app.** Each AppGallery app has its own key; do not reuse a test app's key in production.
- The key is **public**, so it is not a secret and may ship inside the app. Still keep it out of shared example code and public repos that belong to another app, to avoid mixing keys up.
- Make the release build **fail** when the key is missing, so an app never ships without verification.

**How to verify (on-device, WebCrypto)**

```ts
const b64 = (s: string) => Uint8Array.from(atob(s), (c) => c.charCodeAt(0));

export async function verifyHuaweiPurchase(
  publicKeyB64: string,
  purchaseData: string,      // raw string from the plugin, not the parsed object
  signature: string,
  algorithm: 'SHA256WithRSA' | 'SHA256WithRSA/PSS',
): Promise<boolean> {
  const pss = algorithm === 'SHA256WithRSA/PSS';
  const key = await crypto.subtle.importKey(
    'spki',
    b64(publicKeyB64),
    { name: pss ? 'RSA-PSS' : 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['verify'],
  );
  const params = pss ? { name: 'RSA-PSS', saltLength: 32 } : { name: 'RSASSA-PKCS1-v1_5' };
  return crypto.subtle.verify(params, key, b64(signature), new TextEncoder().encode(purchaseData));
}
```

Then check the content: `productId` is yours, `packageName` is your app, `purchaseState === 0`. Grant only when **both** the signature and the content are valid.

- `SHA256WithRSA` (PKCS#1 v1.5) is confirmed with real sandbox data. The PSS salt length (`32` above) is **not yet confirmed**; test it with real data before relying on PSS.
- The example app (`example-app/`) has a public-key field and a **verify last result** button for manual checks.
