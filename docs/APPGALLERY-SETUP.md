# AppGallery Setup Guide

**Status:** Draft outline — steps will be confirmed during the first integration.

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

1. Create a project and an Android app with your final **package name**.
2. Generate the SHA-256 fingerprint of your signing key:

   ```bash
   keytool -list -v -keystore <your-keystore>.jks
   ```

3. Add the fingerprint under *Project settings → General information → SHA-256 certificate fingerprint*.
4. Under *Manage APIs*, turn on **In-App Purchases**.
5. Go to *Earn → In-App Purchases → Settings*, sign the agreement, and copy the **public key**.
6. (Recommended) Enable the `SHA256WithRSA/PSS` signature algorithm on the same page.
7. (Optional) Configure the key-event notification URL if you run a server.
8. Create your products under *Earn → Product Management* (product ID, type, price per country).
9. Download **`agconnect-services.json`**.

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
- Apply `com.huawei.agconnect` only when building Huawei variants. **The exact conditional setup will be validated and documented in a later version of this guide.**
- In the other flavor, `HmsIap.isAvailable()` returns `IAP_SDK_MISSING`, so your app can pick a different billing provider.

## 4. Testing

1. Add tester HUAWEI IDs under *Users and permissions → Sandbox testing*. Changes take 30–60 minutes.
2. The APK under test must have a `versionCode` greater than 0 and, if a version was already released, greater than the released one.
3. Call `HmsIap.isSandboxActivated()` to confirm. When the checkout shows a sandbox badge, no money is charged.
4. If no sandbox badge appears, **real payments are made**. Stop and check steps 1–2.

> Sandbox availability for individual developers in some regions has varied over time. Confirm in AppGallery Connect before relying on it.

## 5. Release checklist

- [ ] Products are **valid** (activated) in Product Management
- [ ] Release build is signed with the key whose fingerprint is registered
- [ ] `agconnect-services.json` matches the release package name
- [ ] Signature verification uses the production public key
- [ ] Restore purchases is reachable from the UI
- [ ] Privacy policy mentions purchase data processed by Huawei
