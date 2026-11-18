import { HmsIap } from 'capacitor-hms-iap';

const output = document.getElementById('output');
const productId = () => document.getElementById('productId').value;
const type = () => Number(document.getElementById('productType').value);

const calls = {
  isAvailable: () => HmsIap.isAvailable(),
  isEnvReady: () => HmsIap.isEnvReady(),
  isSandboxActivated: () => HmsIap.isSandboxActivated(),
  getProducts: () => HmsIap.getProducts({ productIds: [productId()], type: type() }),
  purchase: () => HmsIap.purchase({ productId: productId(), type: type() }),
  getOwnedPurchases: () => HmsIap.getOwnedPurchases({ type: type() }),
};

window.run = async (name) => {
  output.textContent = `${name}…`;
  try {
    output.textContent = `${name} ✓\n` + JSON.stringify(await calls[name](), null, 2);
  } catch (e) {
    output.textContent = `${name} ✗\n` + JSON.stringify({ code: e.code, message: e.message, data: e.data }, null, 2);
  }
};
