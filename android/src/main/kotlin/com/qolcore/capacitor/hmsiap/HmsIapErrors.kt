package com.qolcore.capacitor.hmsiap

import com.getcapacitor.JSObject
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.OrderStatusCode

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
    const val APP_NOT_CONFIGURED = "APP_NOT_CONFIGURED"
    const val IAP_NOT_ACTIVATED = "IAP_NOT_ACTIVATED"
    const val NOT_IMPLEMENTED = "NOT_IMPLEMENTED"
    const val UNKNOWN = "UNKNOWN"
}

/** A rejection ready to be passed to PluginCall.reject. Touches HMS classes. */
internal data class HmsIapFailure(val code: String, val message: String, val data: JSObject)

internal object HmsIapErrorMapper {

    /** Observed when agconnect-services.json is missing or the SHA-256 fingerprint is not registered. */
    private const val HMS_APP_AUTH_FAILED = 907135000

    /** Maps an HMS OrderStatusCode / returnCode to a plugin error code. */
    fun codeFor(returnCode: Int): String = when (returnCode) {
        OrderStatusCode.ORDER_STATE_CANCEL -> HmsIapErrors.USER_CANCELED
        OrderStatusCode.ORDER_HWID_NOT_LOGIN -> HmsIapErrors.NOT_SIGNED_IN
        OrderStatusCode.ORDER_PRODUCT_OWNED -> HmsIapErrors.ALREADY_OWNED
        OrderStatusCode.ORDER_PRODUCT_NOT_OWNED -> HmsIapErrors.NOT_OWNED
        OrderStatusCode.ORDER_PRODUCT_CONSUMED -> HmsIapErrors.ALREADY_CONSUMED
        OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> HmsIapErrors.REGION_NOT_SUPPORTED
        OrderStatusCode.ORDER_STATE_NET_ERROR -> HmsIapErrors.NETWORK_ERROR
        OrderStatusCode.ORDER_STATE_PARAM_ERROR -> HmsIapErrors.INVALID_ARGUMENT
        OrderStatusCode.ORDER_STATE_IAP_NOT_ACTIVATED -> HmsIapErrors.IAP_NOT_ACTIVATED
        // HMS framework (not OrderStatusCode): app ID / signing fingerprint check failed.
        HMS_APP_AUTH_FAILED -> HmsIapErrors.APP_NOT_CONFIGURED
        else -> HmsIapErrors.UNKNOWN
    }

    fun fromReturnCode(returnCode: Int, errMsg: String?): HmsIapFailure {
        val data = JSObject().apply {
            put("returnCode", returnCode)
            if (!errMsg.isNullOrEmpty()) put("errMsg", errMsg)
        }
        return HmsIapFailure(codeFor(returnCode), errMsg ?: "HMS IAP returned $returnCode", data)
    }

    fun fromException(e: Exception): HmsIapFailure {
        if (e is IapApiException) {
            val data = JSObject().apply {
                put("returnCode", e.statusCode)
                put("statusCode", e.status?.statusCode ?: e.statusCode)
                e.status?.statusMessage?.let { put("errMsg", it) }
            }
            val message = e.status?.statusMessage ?: e.message ?: "HMS IAP error ${e.statusCode}"
            return HmsIapFailure(codeFor(e.statusCode), message, data)
        }
        return HmsIapFailure(HmsIapErrors.UNKNOWN, e.message ?: e.toString(), JSObject())
    }
}
