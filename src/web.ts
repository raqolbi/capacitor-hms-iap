import { WebPlugin } from '@capacitor/core';

import type {
  AvailabilityResult,
  ConsumeResult,
  EnvReadyResult,
  HmsIapPlugin,
  OwnedPurchasesResult,
  Product,
  PurchaseResult,
  SandboxResult,
} from './definitions';

const MESSAGE = 'HMS IAP is only available on Android.';

/**
 * Web/iOS fallback: every method rejects with `UNAVAILABLE`,
 * except `isAvailable`, which resolves `{ available: false }`.
 */
export class HmsIapWeb extends WebPlugin implements HmsIapPlugin {
  async isAvailable(): Promise<AvailabilityResult> {
    return { available: false, reason: 'NOT_ANDROID' };
  }

  async isEnvReady(): Promise<EnvReadyResult> {
    throw this.unavailable(MESSAGE);
  }

  async isSandboxActivated(): Promise<SandboxResult> {
    throw this.unavailable(MESSAGE);
  }

  async getProducts(): Promise<{ products: Product[] }> {
    throw this.unavailable(MESSAGE);
  }

  async purchase(): Promise<PurchaseResult> {
    throw this.unavailable(MESSAGE);
  }

  async consumePurchase(): Promise<ConsumeResult> {
    throw this.unavailable(MESSAGE);
  }

  async getOwnedPurchases(): Promise<OwnedPurchasesResult> {
    throw this.unavailable(MESSAGE);
  }

  async getPurchaseHistory(): Promise<OwnedPurchasesResult> {
    throw this.unavailable(MESSAGE);
  }

  async openSubscriptionManagement(): Promise<void> {
    throw this.unavailable(MESSAGE);
  }

  async enablePendingPurchase(): Promise<void> {
    throw this.unavailable(MESSAGE);
  }

  async setLogging(): Promise<void> {
    throw this.unavailable(MESSAGE);
  }
}
