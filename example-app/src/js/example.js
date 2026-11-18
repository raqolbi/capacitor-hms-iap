import { HmsIap } from 'capacitor-hms-iap';

import { signedItems, verifyPurchase } from './verify.js';

const output = document.getElementById('output');
const productId = () => document.getElementById('productId').value;
const type = () => Number(document.getElementById('productType').value);
const publicKey = () => document.getElementById('publicKey').value.trim();
let lastResult = null;

const calls = {
  isAvailable: () => HmsIap.isAvailable(),
  isEnvReady: () => HmsIap.isEnvReady(),
  isSandboxActivated: () => HmsIap.isSandboxActivated(),
  getProducts: () => HmsIap.getProducts({ productIds: [productId()], type: type() }),
  purchase: () => HmsIap.purchase({ productId: productId(), type: type() }),
  getOwnedPurchases: () => HmsIap.getOwnedPurchases({ type: type() }),
  // Consumes the first purchase in the last result (purchase or getOwnedPurchases).
  consumePurchase: () => {
    const item = signedItems(lastResult)[0];
    if (!item) throw new Error('Run purchase or getOwnedPurchases (Consumable) first.');
    return HmsIap.consumePurchase({ purchaseToken: item.purchase.purchaseToken });
  },
};

window.run = async (name) => {
  output.textContent = `${name}…`;
  try {
    lastResult = await calls[name]();
    output.textContent = `${name} ✓\n` + JSON.stringify(lastResult, null, 2);
  } catch (e) {
    output.textContent = `${name} ✗\n` + JSON.stringify({ code: e.code, message: e.message, data: e.data }, null, 2);
  }
};

/** Verifies the signatures in the last purchase / getOwnedPurchases result. */
window.verifyLast = async () => {
  const items = signedItems(lastResult);
  if (!publicKey()) return (output.textContent = 'verify ✗\nPaste the IAP public key first.');
  if (items.length === 0) return (output.textContent = 'verify ✗\nNo signed purchase in the last result.');
  try {
    const checks = await Promise.all(
      items.map(async (it) => ({
        algorithm: it.signatureAlgorithm,
        valid: await verifyPurchase(publicKey(), it.purchaseData, it.signature, it.signatureAlgorithm),
      })),
    );
    output.textContent = 'verify ✓\n' + JSON.stringify(checks, null, 2);
  } catch (e) {
    output.textContent = `verify ✗\n${e.message}`;
  }
};
