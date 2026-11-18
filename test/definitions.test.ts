import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';

import { ProductType, PurchaseState, SignatureAlgorithm } from '../src/definitions';

const root = join(__dirname, '..');
const read = (path: string): string => readFileSync(join(root, path), 'utf8');

describe('public constants', () => {
  it('ProductType matches HMS priceType', () => {
    expect([ProductType.Consumable, ProductType.NonConsumable, ProductType.Subscription]).toEqual([0, 1, 2]);
  });

  it('PurchaseState matches HMS purchaseState', () => {
    expect(PurchaseState).toMatchObject({ Initialized: -1, Purchased: 0, Canceled: 1, Refunded: 2, Pending: 3 });
  });

  it('SignatureAlgorithm uses the exact HMS strings', () => {
    expect(SignatureAlgorithm.Sha256WithRsa).toBe('SHA256WithRSA');
    expect(SignatureAlgorithm.Sha256WithRsaPss).toBe('SHA256WithRSA/PSS');
  });
});

describe('error codes', () => {
  // The Kotlin constants and the TypeScript union must stay identical: JS code
  // branches on these strings.
  it('HmsIapErrorCode (TS) equals HmsIapErrors (Kotlin)', () => {
    const ts = read('src/definitions.ts');
    const union = ts.slice(
      ts.indexOf('export type HmsIapErrorCode'),
      ts.indexOf(';', ts.indexOf('export type HmsIapErrorCode')),
    );
    const tsCodes = [...union.matchAll(/'([A-Z_]+)'/g)].map((m) => m[1]).sort();

    const kt = read('android/src/main/kotlin/com/qolcore/capacitor/hmsiap/HmsIapErrors.kt');
    const ktCodes = [...kt.matchAll(/const val [A-Z_]+ = "([A-Z_]+)"/g)].map((m) => m[1]).sort();

    expect(tsCodes.length).toBeGreaterThan(10);
    expect(tsCodes).toEqual(ktCodes);
  });

  it('every error code is documented in DESIGN.md', () => {
    const design = read('docs/DESIGN.md');
    const kt = read('android/src/main/kotlin/com/qolcore/capacitor/hmsiap/HmsIapErrors.kt');
    for (const [, code] of kt.matchAll(/const val [A-Z_]+ = "([A-Z_]+)"/g)) {
      expect(design, code).toContain(`| \`${code}\` |`);
    }
  });
});
