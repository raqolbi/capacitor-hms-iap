# capacitor-hms-iap example app

Manual test app for running the plugin on a real device. Not published to npm.

## Prerequisites

- A Huawei device (or an Android device with HMS Core) connected with `adb`.
- An app in AppGallery Connect with In-App Purchases turned on and at least one active product, see [`../docs/APPGALLERY-SETUP.md`](../docs/APPGALLERY-SETUP.md) §2.
- The SHA-256 fingerprint of your **debug** key registered for that app:

  ```bash
  keytool -list -v -keystore ~/.android/debug.keystore -storepass android
  ```

- Your HUAWEI ID added as a sandbox tester (§4 of the setup guide).

## Configure

1. Download `agconnect-services.json` for the app and put it in `android/app/` (it is git-ignored).
2. The default application ID is `com.qolcore.capacitor.hmsiap.example`. To test against another AppGallery app, build with that app's package name:

   ```bash
   ./gradlew assembleDebug -PexampleAppId=com.example.yourapp
   ```

   The package name must match `agconnect-services.json`. Installing over another app with the same package name replaces it.

## Build and run

```bash
# in the plugin root
npm install && npm run build

# in example-app/
npm install
npm run build
npx cap sync android
cd android && ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Using the screen

| Control | What it does |
|---------|--------------|
| Product ID / Type | Product to query or buy (default `premium_lifetime`, non-consumable) |
| `isAvailable` … `getOwnedPurchases` | Call the plugin method; the result or error appears below |
| IAP public key | Paste the key from *Earn → In-App Purchases → Settings*. Kept only in memory, never saved |
| verify last result | Checks the signatures in the last `purchase` / `getOwnedPurchases` result |

Recommended order: `isEnvReady` → `isSandboxActivated` (must report `isSandboxUser: true`) → `getProducts` → `purchase` → `verify last result` → `getOwnedPurchases`.

Stop if `isSandboxActivated` does not confirm the sandbox: purchases would then be real.

## Troubleshooting

| Error | Usual cause |
|-------|-------------|
| `APP_NOT_CONFIGURED` (907135000) | `agconnect-services.json` missing or for another package, or debug SHA-256 not registered |
| `IAP_NOT_ACTIVATED` (60002) | In-App Purchases not turned on for this app yet, or still propagating |
| `getProducts` returns an empty list | Product ID differs or the product is not active |
| Gradle `NullPointerException` in the file-system watcher | Build with `-Dorg.gradle.vfs.watch=false` |
