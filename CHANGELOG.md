# Changelog

All notable changes to this project are documented here. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Project scaffold from `@capacitor/create-plugin` 0.22.0 (Capacitor 8, Kotlin, Android only).
- Full TypeScript API in `src/definitions.ts`, following `docs/DESIGN.md`.
- Web fallback that rejects with `UNAVAILABLE`.
- Android: `isAvailable()` implemented (HMS IAP SDK and HMS Core detection); other methods validate arguments and reject with `NOT_IMPLEMENTED`.
- HMS IAP SDK declared as `compileOnly` (`hmsIapVersion` overridable, default `6.16.6.305`).
