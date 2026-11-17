package com.qolcore.capacitor.hmsiap

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

/**
 * Capacitor bridge for HUAWEI In-App Purchases.
 * Public API: src/definitions.ts. Specification: docs/DESIGN.md.
 */
@CapacitorPlugin(name = "HmsIap")
class HmsIapPlugin : Plugin() {

    private lateinit var implementation: HmsIap

    override fun load() {
        implementation = HmsIap(context)
    }

    @PluginMethod
    fun isAvailable(call: PluginCall) {
        val result = JSObject()
        when {
            !implementation.isIapSdkPresent() -> {
                result.put("available", false)
                result.put("reason", "IAP_SDK_MISSING")
            }
            !implementation.isHmsCoreInstalled() -> {
                result.put("available", false)
                result.put("reason", "HMS_CORE_MISSING")
            }
            else -> result.put("available", true)
        }
        call.resolve(result)
    }

    @PluginMethod
    fun isEnvReady(call: PluginCall) {
        if (!requireSdk(call)) return
        notImplemented(call, "0.1.0")
    }

    @PluginMethod
    fun isSandboxActivated(call: PluginCall) {
        if (!requireSdk(call)) return
        notImplemented(call, "0.2.0")
    }

    @PluginMethod
    fun getProducts(call: PluginCall) {
        if (!requireSdk(call)) return
        val productIds = call.getArray("productIds")
        if (productIds == null || productIds.length() == 0) {
            call.reject("productIds must be a non-empty array", HmsIapErrors.INVALID_ARGUMENT)
            return
        }
        if (!requireProductType(call)) return
        notImplemented(call, "0.1.0")
    }

    @PluginMethod
    fun purchase(call: PluginCall) {
        if (!requireSdk(call)) return
        if (call.getString("productId").isNullOrBlank()) {
            call.reject("productId is required", HmsIapErrors.INVALID_ARGUMENT)
            return
        }
        if (!requireProductType(call)) return
        // TODO(0.1.0): createPurchaseIntent → startActivityForResult(call, intent, "onPurchaseResult")
        //  and an @ActivityCallback method. Reject PURCHASE_IN_PROGRESS if a checkout is open.
        notImplemented(call, "0.1.0")
    }

    @PluginMethod
    fun consumePurchase(call: PluginCall) {
        if (!requireSdk(call)) return
        if (call.getString("purchaseToken").isNullOrBlank()) {
            call.reject("purchaseToken is required", HmsIapErrors.INVALID_ARGUMENT)
            return
        }
        notImplemented(call, "0.2.0")
    }

    @PluginMethod
    fun getOwnedPurchases(call: PluginCall) {
        if (!requireSdk(call)) return
        if (!requireProductType(call)) return
        notImplemented(call, "0.1.0")
    }

    @PluginMethod
    fun getPurchaseHistory(call: PluginCall) {
        if (!requireSdk(call)) return
        if (!requireProductType(call)) return
        notImplemented(call, "0.2.0")
    }

    @PluginMethod
    fun openSubscriptionManagement(call: PluginCall) {
        if (!requireSdk(call)) return
        notImplemented(call, "0.3.0")
    }

    @PluginMethod
    fun enablePendingPurchase(call: PluginCall) {
        if (!requireSdk(call)) return
        notImplemented(call, "0.3.0")
    }

    @PluginMethod
    fun setLogging(call: PluginCall) {
        implementation.loggingEnabled = call.getBoolean("enabled", false) ?: false
        call.resolve()
    }

    // --- helpers -------------------------------------------------------------

    private fun requireSdk(call: PluginCall): Boolean {
        if (implementation.isIapSdkPresent()) return true
        call.reject(
            "com.huawei.hms:iap is not on the classpath of this build",
            HmsIapErrors.IAP_SDK_MISSING,
        )
        return false
    }

    /** Validates `type` (0, 1 or 2) on the call. */
    private fun requireProductType(call: PluginCall): Boolean {
        val type = call.getInt("type")
        if (type == null || type !in VALID_PRODUCT_TYPES) {
            call.reject("type must be 0 (Consumable), 1 (NonConsumable) or 2 (Subscription)", HmsIapErrors.INVALID_ARGUMENT)
            return false
        }
        return true
    }

    private fun notImplemented(call: PluginCall, version: String) {
        call.reject("Not implemented yet (planned for $version)", HmsIapErrors.NOT_IMPLEMENTED)
    }

    companion object {
        private val VALID_PRODUCT_TYPES = 0..2
    }
}
