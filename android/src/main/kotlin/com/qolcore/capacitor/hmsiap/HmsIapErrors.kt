package com.qolcore.capacitor.hmsiap

/**
 * Stable error codes returned to JavaScript as `error.code`.
 * Keep in sync with `HmsIapErrorCode` in src/definitions.ts.
 */
object HmsIapErrors {
    const val USER_CANCELED = "USER_CANCELED"
    const val NOT_SIGNED_IN = "NOT_SIGNED_IN"
    const val ALREADY_OWNED = "ALREADY_OWNED"
    const val NOT_OWNED = "NOT_OWNED"
    const val ALREADY_CONSUMED = "ALREADY_CONSUMED"
    const val REGION_NOT_SUPPORTED = "REGION_NOT_SUPPORTED"
    const val NETWORK_ERROR = "NETWORK_ERROR"
    const val INVALID_ARGUMENT = "INVALID_ARGUMENT"
    const val PURCHASE_IN_PROGRESS = "PURCHASE_IN_PROGRESS"
    const val NO_RESULT = "NO_RESULT"
    const val IAP_SDK_MISSING = "IAP_SDK_MISSING"
    const val UNAVAILABLE = "UNAVAILABLE"
    const val NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
    const val UNKNOWN = "UNKNOWN"

    // TODO(0.1.0): map com.huawei.hms.iap.entity.OrderStatusCode constants
    //  (read from the SDK, not hard-coded) to the codes above. See docs/DESIGN.md §4.
}
