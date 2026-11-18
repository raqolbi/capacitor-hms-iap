# Roadmap

**Status:** 0.1.0 released (2026-11-18). Next: 0.2.0.
**Last updated:** 2026-11-18

---

## Milestones

| Version | Scope | Exit criteria |
|---------|-------|---------------|
| **0.1.0** ✅ released 2026-11-18 | `isAvailable`, `isEnvReady`, `isSandboxActivated`, `getProducts`, `purchase`, `getOwnedPurchases`, `consumePurchase`, `setLogging`; web stub; error mapping | One non-consumable bought, restored after reinstall, and canceled cleanly on a real Huawei device — all met |
| **0.2.0** | `getPurchaseHistory` (`consumePurchase` shipped early in 0.1.0) | Consumable buy → grant → consume → buy again; history lists the consumed item |
| **0.3.0** | Subscriptions: subscription fields, `openSubscriptionManagement`, `enablePendingPurchase` | Subscribe, renew in sandbox, cancel, expire |
| **0.4.0** | Multi-store flavor guide validated (`compileOnly` + `huaweiImplementation` + conditional `agcp`); example app with two flavors | Play flavor builds without any `com.huawei.*` classes |
| **1.0.0** | API freeze, full docs, npm publish, CI | Used in at least one production app on AppGallery |

## Repository layout

```text
capacitor-hms-iap/
├── android/                    # Kotlin/Java plugin source
│   └── src/main/kotlin/com/qolcore/capacitor/hmsiap/   # HmsIapPlugin, HmsIapClient, mapper, errors
├── src/
│   ├── definitions.ts          # public TypeScript API (from DESIGN.md §2)
│   ├── index.ts
│   └── web.ts                  # UNAVAILABLE stub
├── example-app/                # minimal Capacitor app for manual testing
├── docs/
│   ├── DESIGN.md
│   ├── APPGALLERY-SETUP.md
│   └── ROADMAP.md
├── CHANGELOG.md
├── LICENSE
└── README.md
```

## Test plan

| Layer | How |
|-------|-----|
| TypeScript | Vitest (`npm test`): web stub, public constants, TS ↔ Kotlin error-code parity, error codes documented in DESIGN.md |
| Native mapping | JUnit (`cd android && ./gradlew test`): HMS return code → error code (literal values), purchase JSON parsing incl. a real sandbox payload, product mapping |
| Integration | Manual matrix on real devices with the example app |

### Device matrix (minimum)

| Device | Purpose |
|--------|---------|
| Huawei phone with EMUI/HarmonyOS 4.x (Android-based), HMS Core current | Primary target |
| Non-Huawei Android phone with HMS Core installed from AppGallery | Non-Huawei users |
| Non-Huawei Android phone **without** HMS Core | `isAvailable` → `HMS_CORE_MISSING` |

### Scenarios

- Not signed in → sign-in screen → continue purchase
- Cancel at checkout
- Successful purchase → kill app before verifying → restore on next start
- Buy a non-consumable twice → `ALREADY_OWNED`
- Airplane mode during `getProducts` and `purchase`
- Account region without IAP → `REGION_NOT_SUPPORTED`
- Refund from Huawei → `purchaseState` no longer `Purchased` on next query

## Release checklist (per version)

- [ ] `CHANGELOG.md` updated
- [ ] HMS SDK version in docs matches `build.gradle`
- [ ] Error-code table regenerated from SDK constants
- [ ] Example app builds for both flavors
- [ ] `npm pack` contents checked (no keystores, no `agconnect-services.json`)
- [ ] Git tag + npm publish
