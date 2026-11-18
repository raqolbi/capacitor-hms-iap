# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.1.0] - 2026-11-18

First public release. Android only; HMS IAP SDK `6.16.6.305`, Capacitor 8.

### Added

- Android: `isAvailable` (HMS IAP SDK and HMS Core detection), `isEnvReady` (with HUAWEI ID sign-in), `isSandboxActivated`, `getProducts`, `purchase`, `getOwnedPurchases` (incl. pending purchases and paging), `consumePurchase`, `setLogging`.
- `getPurchaseHistory`, `openSubscriptionManagement` and `enablePendingPurchase` are in the API but reject with `NOT_IMPLEMENTED` until 0.2.0 / 0.3.0.
- Web/iOS fallback: `isAvailable` resolves `{ available: false, reason: 'NOT_ANDROID' }`, everything else rejects with `UNAVAILABLE`.
- Stable string error codes mapped from `OrderStatusCode`, plus `APP_NOT_CONFIGURED` (HMS `907135000`) and `IAP_NOT_ACTIVATED` (`60002`).
- HMS IAP SDK declared as `compileOnly` (`hmsIapVersion` overridable), so multi-store apps can scope it to a Huawei flavor.
- Docs: AppGallery Connect setup confirmed on a real account, IAP public key usage and a WebCrypto verification snippet.
- Unit tests: Vitest (web fallback, constants, error-code parity between TypeScript, Kotlin and docs) and JUnit (return-code mapping, purchase/consume/product mapping).
- Example app: a button per method, public-key field with on-device signature check, conditional AGConnect plugin, configurable `applicationId` (`-PexampleAppId`).

### Fixed

- `purchase` and HUAWEI ID sign-in crashed the app with `NullPointerException: getResolution(...) must not be null` when HMS returned the screen as a plain `Intent` instead of a `PendingIntent` (HMS Core 6.16.4, IAP APK 6.26.8). Both forms are now launched.
- `purchase` crashed the app with `IllegalStateException: Attempting to launch an unregistered ActivityResultLauncher` when the user left the app (Back) while HMS was still preparing the checkout. The call now rejects with `USER_CANCELED`.

### Verified on device

Huawei JEF-NX9, Android 10 / EMUI 12, HMS Core 6.16.4, sandbox tester account:

- `isEnvReady`, `isSandboxActivated` (`isSandboxUser: true`), `getProducts` for a non-consumable and a consumable (price in IDR).
- Non-consumable `purchase` in the sandbox (IDR 0 charged). The promise resolves only after the user closes Huawei's "Payment result" screen with OK.
- `purchaseData` + `signature` (`SHA256WithRSA`) verify with the IAP public key via WebCrypto `RSASSA-PKCS1-v1_5`; tampered data fails.
- Buying an owned item again → `ALREADY_OWNED` (`60051`).
- Restore with `getOwnedPurchases` after clearing app data and after a full uninstall/reinstall.
- Consumable: `purchase` → `consumePurchase` (`consumptionState: 1`) → no longer owned.
- Closing the checkout with Back → `USER_CANCELED` (`60000`), nothing bought.
- Before IAP is enabled for the app in AppGallery Connect: `IAP_NOT_ACTIVATED` (`60002`), while `getProducts` may already work.
- Requesting `SHA256WithRSA/PSS` still returns `SHA256WithRSA` while the PSS switch in AppGallery Connect is off.
- IAP requests may wait while the screen is off.

[Unreleased]: https://github.com/raqolbi/capacitor-hms-iap/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/raqolbi/capacitor-hms-iap/releases/tag/v0.1.0
