import { describe, expect, it } from 'vitest';

import { HmsIapWeb } from '../src/web';

/** Web and iOS fallback (docs/DESIGN.md §3.5). */
describe('HmsIapWeb', () => {
  const web = new HmsIapWeb();

  it('isAvailable resolves NOT_ANDROID instead of rejecting', async () => {
    await expect(web.isAvailable()).resolves.toEqual({ available: false, reason: 'NOT_ANDROID' });
  });

  const rejecting: [string, () => Promise<unknown>][] = [
    ['isEnvReady', () => web.isEnvReady()],
    ['isSandboxActivated', () => web.isSandboxActivated()],
    ['getProducts', () => web.getProducts()],
    ['purchase', () => web.purchase()],
    ['consumePurchase', () => web.consumePurchase()],
    ['getOwnedPurchases', () => web.getOwnedPurchases()],
    ['getPurchaseHistory', () => web.getPurchaseHistory()],
    ['openSubscriptionManagement', () => web.openSubscriptionManagement()],
    ['enablePendingPurchase', () => web.enablePendingPurchase()],
    ['setLogging', () => web.setLogging()],
  ];

  it.each(rejecting)('%s rejects with UNAVAILABLE', async (_name, call) => {
    await expect(call()).rejects.toMatchObject({ code: 'UNAVAILABLE' });
  });
});
