import { registerPlugin } from '@capacitor/core';

import type { HmsIapPlugin } from './definitions';

const HmsIap = registerPlugin<HmsIapPlugin>('HmsIap', {
  web: () => import('./web').then((m) => new m.HmsIapWeb()),
});

export * from './definitions';
export { HmsIap };
