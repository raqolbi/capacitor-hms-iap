/**
 * HMS product types. Values match the HMS `priceType` field.
 */
export enum ProductType {
  /** Consumed after use and can be bought again (priceType 0). */
  Consumable = 0,
  /** Bought once and never expires (priceType 1). */
  NonConsumable = 1,
  /** Auto-renewable subscription (priceType 2). */
  Subscription = 2,
}

/**
 * Signature algorithms supported by HMS IAP.
 */
export enum SignatureAlgorithm {
  /** HMS default. */
  Sha256WithRsa = 'SHA256WithRSA',
  /**
   * Stronger, but only honored after the PSS switch is on in AppGallery Connect
   * (Earn → In-App Purchases → Settings). Until then HMS returns `SHA256WithRSA`;
   * always verify with the algorithm reported in the result.
   */
  Sha256WithRsaPss = 'SHA256WithRSA/PSS',
}

/**
 * `purchaseState` field of `InAppPurchaseData`.
 * Values must be re-checked against the HMS SDK version in use before 1.0.
 */
export enum PurchaseState {
  Initialized = -1,
  Purchased = 0,
  Canceled = 1,
  Refunded = 2,
  Pending = 3,
}

/**
 * Stable error codes used in rejected promises (`error.code`).
 */
export type HmsIapErrorCode =
  | 'USER_CANCELED'
  | 'NOT_SIGNED_IN'
  | 'ALREADY_OWNED'
  | 'NOT_OWNED'
  | 'ALREADY_CONSUMED'
  | 'REGION_NOT_SUPPORTED'
  | 'NETWORK_ERROR'
  | 'INVALID_ARGUMENT'
  | 'PURCHASE_IN_PROGRESS'
  | 'NO_RESULT'
  | 'IAP_SDK_MISSING'
  | 'UNAVAILABLE'
  | 'APP_NOT_CONFIGURED'
  | 'IAP_NOT_ACTIVATED'
  | 'NOT_IMPLEMENTED'
  | 'UNKNOWN';

/**
 * Extra data attached to a rejected promise.
 */
export interface HmsIapErrorData {
  /** Original HMS `returnCode` / `OrderStatusCode`, when available. */
  returnCode?: number;
  /** Original HMS `Status.statusCode`, when available. */
  statusCode?: number;
  /** Original HMS error message, when available. */
  errMsg?: string;
}

/** `HMS_CORE_OUTDATED` is reserved; the Android implementation does not return it yet. */
export type AvailabilityReason = 'NOT_ANDROID' | 'HMS_CORE_MISSING' | 'HMS_CORE_OUTDATED' | 'IAP_SDK_MISSING';

export interface AvailabilityResult {
  available: boolean;
  /** Set when `available` is false. */
  reason?: AvailabilityReason;
}

export interface EnvReadyOptions {
  /**
   * Launch the HUAWEI ID sign-in screen when the user is not signed in.
   * @default true
   */
  resolveSignIn?: boolean;
  /**
   * Passed to `IapClient.isEnvReady(boolean)`.
   * @default false
   */
  supportAppTouch?: boolean;
}

export interface EnvReadyResult {
  returnCode: number;
  /** Country/region of the HUAWEI ID, when provided. */
  country?: string;
  carrierId?: string;
}

export interface SandboxResult {
  isSandboxUser: boolean;
  isSandboxApk: boolean;
  versionInApk?: string;
  versionFromMarket?: string;
}

export interface GetProductsOptions {
  /** Product IDs configured in AppGallery Connect. */
  productIds: string[];
  type: ProductType;
}

export interface Product {
  productId: string;
  type: ProductType;
  name: string;
  description: string;
  /** Localized, formatted price, e.g. "Rp 99.000". */
  price: string;
  priceMicros: number;
  /** ISO 4217 currency code. */
  currency: string;
  originalPrice?: string;
  originalPriceMicros?: number;
  /** 0 = valid. */
  status: number;
  /** Subscription only. ISO 8601 period, e.g. "P1M". */
  subscriptionPeriod?: string;
  subscriptionGroupId?: string;
  subscriptionGroupTitle?: string;
  subscriptionLevel?: number;
  freeTrialPeriod?: string;
  introductoryPrice?: string;
  introductoryPriceMicros?: number;
  introductoryPeriod?: string;
  introductoryPeriodCycles?: number;
  offerUsedStatus?: number;
}

export interface PurchaseOptions {
  productId: string;
  type: ProductType;
  /** Echoed back in the purchase data. */
  developerPayload?: string;
  /** JSON string passed to HMS as `reservedInfor`. */
  reservedInfo?: string;
  signatureAlgorithm?: SignatureAlgorithm;
}

export interface InAppPurchase {
  orderId: string;
  productId: string;
  purchaseToken: string;
  purchaseState: PurchaseState;
  /** Epoch milliseconds. */
  purchaseTime: number;
  kind: ProductType;
  packageName?: string;
  developerPayload?: string;
  consumptionState?: number;
  confirmed?: number;
  subscriptionId?: string;
  isSubscriptionValid?: boolean;
  autoRenewing?: boolean;
  /** Epoch milliseconds. */
  expirationDate?: number;
  /** Every field of the original JSON. */
  raw: Record<string, unknown>;
}

export interface PurchaseResult {
  /** Raw `InAppPurchaseData` JSON. Verify this string, not `purchase`. */
  purchaseData: string;
  signature: string;
  signatureAlgorithm: SignatureAlgorithm;
  /** Parsed copy of `purchaseData` for convenience. */
  purchase: InAppPurchase;
}

export interface ConsumeOptions {
  purchaseToken: string;
  developerChallenge?: string;
  signatureAlgorithm?: SignatureAlgorithm;
}

export interface ConsumeResult {
  consumeData: string;
  signature: string;
  signatureAlgorithm: SignatureAlgorithm;
}

export interface OwnedQuery {
  type: ProductType;
  /** Pass the token from the previous page to continue. */
  continuationToken?: string;
  signatureAlgorithm?: SignatureAlgorithm;
}

export interface OwnedPurchasesResult {
  purchases: PurchaseResult[];
  /** Purchases waiting for payment (pending purchase). */
  pendingPurchases: PurchaseResult[];
  /** Present when more pages are available. */
  continuationToken?: string;
}

export interface SubscriptionManagementOptions {
  /** Open the detail screen of one subscription instead of the list. */
  productId?: string;
}

export interface LoggingOptions {
  enabled: boolean;
}

export interface HmsIapPlugin {
  /**
   * Whether HMS IAP can be used on this device and build. Never rejects.
   */
  isAvailable(): Promise<AvailabilityResult>;

  /**
   * Checks HUAWEI ID sign-in and IAP availability for the user's region.
   */
  isEnvReady(options?: EnvReadyOptions): Promise<EnvReadyResult>;

  /**
   * Testing only: whether the current account and APK are in the sandbox.
   */
  isSandboxActivated(): Promise<SandboxResult>;

  /**
   * Localized product information configured in AppGallery Connect.
   */
  getProducts(options: GetProductsOptions): Promise<{ products: Product[] }>;

  /**
   * Launches the Huawei checkout screen.
   *
   * Resolves only after the user closes Huawei's "Payment result" screen, so keep
   * the UI in a processing state until then. Verify `purchaseData` + `signature`
   * before granting anything (docs/APPGALLERY-SETUP.md §6).
   */
  purchase(options: PurchaseOptions): Promise<PurchaseResult>;

  /**
   * Consumes a consumable so it can be bought again.
   */
  consumePurchase(options: ConsumeOptions): Promise<ConsumeResult>;

  /**
   * Owned items: unconsumed consumables, non-consumables, active subscriptions.
   */
  getOwnedPurchases(options: OwnedQuery): Promise<OwnedPurchasesResult>;

  /**
   * History: consumed consumables and all subscription receipts.
   */
  getPurchaseHistory(options: OwnedQuery): Promise<OwnedPurchasesResult>;

  /**
   * Opens Huawei's subscription management screen.
   */
  openSubscriptionManagement(options?: SubscriptionManagementOptions): Promise<void>;

  /**
   * Opt in to pending purchases.
   */
  enablePendingPurchase(): Promise<void>;

  /**
   * Turns verbose native logging on or off. Tokens and signatures are never logged.
   */
  setLogging(options: LoggingOptions): Promise<void>;
}
