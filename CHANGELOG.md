# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Project scaffold from `@capacitor/create-plugin` 0.22.0 (Capacitor 8, Kotlin, Android only).
- Full TypeScript API in `src/definitions.ts`, following `docs/DESIGN.md`.
- Web fallback that rejects with `UNAVAILABLE`.
- Android: `isAvailable()` implemented (HMS IAP SDK and HMS Core detection); other methods validate arguments and reject with `NOT_IMPLEMENTED`.
- HMS IAP SDK declared as `compileOnly` (`hmsIapVersion` overridable, default `6.16.6.305`).
- Android: `isEnvReady` (with HUAWEI ID sign-in resolution), `getProducts`, `purchase` (checkout via `StartIntentSenderForResult`), `getOwnedPurchases` (incl. pending purchases and paging).
- Error mapping from `OrderStatusCode`, plus `APP_NOT_CONFIGURED` for HMS `907135000` and `IAP_NOT_ACTIVATED` for `60002`.
- Android: `isSandboxActivated` (moved forward from 0.2.0 to check the sandbox before test purchases).
- Example app: buttons for each method, HMS IAP dependency, conditional AGConnect plugin, `allowBackup` merge fix, configurable `applicationId` (`-PexampleAppId`).

### Fixed

- Android: `purchase` (and HUAWEI ID sign-in) crashed the app with `NullPointerException: getResolution(...) must not be null` when HMS returned the screen as a plain `Intent` instead of a `PendingIntent` (HMS Core 6.16.4, IAP APK 6.26.8). Both forms are now launched (`StartIntentSenderForResult` or `StartActivityForResult`).

### Verified on device

- Huawei JEF-NX9, Android 10 / EMUI 12, HMS Core 6.16.4: `isAvailable`, argument validation, `APP_NOT_CONFIGURED`, `isEnvReady` success with a registered app, `IAP_NOT_ACTIVATED`. Purchase flow pending IAP activation in AppGallery Connect.
- IAP requests may wait while the screen is off.
- 2026-11-18: `getProducts` returns the configured non-consumable (price in IDR). `purchase` opens the HUAWEI IAP user agreement, then the server rejects order creation with `60002` (`createOrder fail, errCode 6, subErrCode 101` in the `hms_pay` log) while IAP is not yet active for the app in AppGallery Connect; `isSandboxActivated` and `getOwnedPurchases` return the same code.
