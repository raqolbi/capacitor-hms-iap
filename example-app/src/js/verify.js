// On-device signature check for manual testing (DESIGN.md: "Always verify").
// The IAP public key comes from AppGallery Connect → Earn → In-App Purchases →
// Settings. It is pasted into the page at runtime and never committed.

const b64ToBytes = (b64) => Uint8Array.from(atob(b64.replace(/\s+/g, '')), (c) => c.charCodeAt(0));

function params(algorithm) {
  return algorithm === 'SHA256WithRSA/PSS'
    ? { importName: 'RSA-PSS', verify: { name: 'RSA-PSS', saltLength: 32 } }
    : { importName: 'RSASSA-PKCS1-v1_5', verify: { name: 'RSASSA-PKCS1-v1_5' } };
}

/** Verifies one raw purchaseData string against its base64 signature. */
export async function verifyPurchase(publicKeyB64, purchaseData, signature, algorithm) {
  const p = params(algorithm);
  const key = await crypto.subtle.importKey(
    'spki',
    b64ToBytes(publicKeyB64),
    { name: p.importName, hash: 'SHA-256' },
    false,
    ['verify'],
  );
  return crypto.subtle.verify(p.verify, key, b64ToBytes(signature), new TextEncoder().encode(purchaseData));
}

/** Collects every { purchaseData, signature, signatureAlgorithm } in a plugin result. */
export function signedItems(result) {
  if (!result) return [];
  if (result.purchaseData) return [result];
  return [...(result.purchases ?? []), ...(result.pendingPurchases ?? [])];
}
