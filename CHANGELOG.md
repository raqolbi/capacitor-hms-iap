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
- Error mapping from `OrderStatusCode`, plus `APP_NOT_CONFIGURED` for HMS `907135000`.
- Example app: buttons for each method, HMS IAP dependency, conditional AGConnect plugin, `allowBackup` merge fix, configurable `applicationId` (`-PexampleAppId`).

### Verified on device

- Huawei JEF-NX9, Android 10 / EMUI 12, HMS Core 6.16.4: `isAvailable`, argument validation, and `APP_NOT_CONFIGURED` path. Purchase flow pending AppGallery Connect setup.
