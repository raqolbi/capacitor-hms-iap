package com.qolcore.capacitor.hmsiap

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.getcapacitor.JSObject
import com.getcapacitor.Logger
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
    private var client: HmsIapClient? = null

    /** Huawei screens (sign-in, checkout) are launched through this. */
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    /** Same, for screens that HMS returns as a plain Intent. */
    private var intentLauncher: ActivityResultLauncher<Intent>? = null
    private var pendingCall: PluginCall? = null
    private var pendingKind: PendingKind? = null

    private enum class PendingKind { SIGN_IN, PURCHASE }

    override fun load() {
        implementation = HmsIap(context)
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result -> onHuaweiActivityResult(result) }
        intentLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result -> onHuaweiActivityResult(result) }
    }

    // --- public methods ------------------------------------------------------

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
        val iap = requireClient(call) ?: return
        val resolveSignIn = call.getBoolean("resolveSignIn", true) ?: true
        val supportAppTouch = call.getBoolean("supportAppTouch", false) ?: false
        log("isEnvReady(resolveSignIn=$resolveSignIn, supportAppTouch=$supportAppTouch)")
        iap.isEnvReady(supportAppTouch, resolveSignIn) { step ->
            handleStep(call, step, PendingKind.SIGN_IN)
        }
    }

    @PluginMethod
    fun isSandboxActivated(call: PluginCall) {
        val iap = requireClient(call) ?: return
        iap.isSandboxActivated { step -> handleStep(call, step, null) }
    }

    @PluginMethod
    fun getProducts(call: PluginCall) {
        val iap = requireClient(call) ?: return
        val ids = call.getArray("productIds")?.toList<Any>()?.mapNotNull { it as? String }
        if (ids.isNullOrEmpty()) {
            call.reject("productIds must be a non-empty array of strings", HmsIapErrors.INVALID_ARGUMENT)
            return
        }
        val type = requireProductType(call) ?: return
        log("getProducts(type=$type, count=${ids.size})")
        iap.getProducts(ids, type) { step -> handleStep(call, step, null) }
    }

    @PluginMethod
    fun purchase(call: PluginCall) {
        val iap = requireClient(call) ?: return
        val productId = call.getString("productId")
        if (productId.isNullOrBlank()) {
            call.reject("productId is required", HmsIapErrors.INVALID_ARGUMENT)
            return
        }
        val type = requireProductType(call) ?: return
        if (pendingCall != null) {
            call.reject("Another Huawei screen is already open", HmsIapErrors.PURCHASE_IN_PROGRESS)
            return
        }
        log("purchase(productId=$productId, type=$type)")
        iap.createPurchaseIntent(
            productId,
            type,
            call.getString("developerPayload"),
            call.getString("reservedInfo"),
            call.getString("signatureAlgorithm"),
        ) { step -> handleStep(call, step, PendingKind.PURCHASE) }
    }

    @PluginMethod
    fun consumePurchase(call: PluginCall) {
        requireClient(call) ?: return
        if (call.getString("purchaseToken").isNullOrBlank()) {
            call.reject("purchaseToken is required", HmsIapErrors.INVALID_ARGUMENT)
            return
        }
        notImplemented(call, "0.2.0")
    }

    @PluginMethod
    fun getOwnedPurchases(call: PluginCall) {
        val iap = requireClient(call) ?: return
        val type = requireProductType(call) ?: return
        log("getOwnedPurchases(type=$type)")
        iap.getOwnedPurchases(
            type,
            call.getString("continuationToken"),
            call.getString("signatureAlgorithm"),
        ) { step -> handleStep(call, step, null) }
    }

    @PluginMethod
    fun getPurchaseHistory(call: PluginCall) {
        requireClient(call) ?: return
        requireProductType(call) ?: return
        notImplemented(call, "0.2.0")
    }

    @PluginMethod
    fun openSubscriptionManagement(call: PluginCall) {
        requireClient(call) ?: return
        notImplemented(call, "0.3.0")
    }

    @PluginMethod
    fun enablePendingPurchase(call: PluginCall) {
        requireClient(call) ?: return
        notImplemented(call, "0.3.0")
    }

    @PluginMethod
    fun setLogging(call: PluginCall) {
        implementation.loggingEnabled = call.getBoolean("enabled", false) ?: false
        call.resolve()
    }

    // --- Huawei activities ---------------------------------------------------

    private fun handleStep(call: PluginCall, step: HmsIapClient.Step, kind: PendingKind?) {
        when (step) {
            is HmsIapClient.Step.Done -> call.resolve(step.result)
            is HmsIapClient.Step.Failed -> reject(call, step.failure)
            is HmsIapClient.Step.NeedsActivity -> {
                if (kind == null) {
                    call.reject("Unexpected Huawei activity request", HmsIapErrors.UNKNOWN)
                } else {
                    launch(call, step, kind)
                }
            }
        }
    }

    private fun launch(call: PluginCall, screen: HmsIapClient.Step.NeedsActivity, kind: PendingKind) {
        if (pendingCall != null) {
            call.reject("Another Huawei screen is already open", HmsIapErrors.PURCHASE_IN_PROGRESS)
            return
        }
        val senderLauncher = launcher
        val plainLauncher = intentLauncher
        if (senderLauncher == null || plainLauncher == null) {
            call.reject("Activity launcher not ready", HmsIapErrors.UNKNOWN)
            return
        }
        val pending = screen.pendingIntent
        val intent = screen.intent
        if (pending == null && intent == null) {
            call.reject("Huawei screen has no intent", HmsIapErrors.UNKNOWN)
            return
        }
        pendingCall = call
        pendingKind = kind
        if (pending != null) {
            log("launching Huawei screen: $kind (PendingIntent)")
            senderLauncher.launch(IntentSenderRequest.Builder(pending.intentSender).build())
        } else if (intent != null) {
            log("launching Huawei screen: $kind (Intent)")
            plainLauncher.launch(intent)
        }
    }

    private fun onHuaweiActivityResult(result: ActivityResult) {
        val call = pendingCall ?: return
        val kind = pendingKind
        pendingCall = null
        pendingKind = null
        log("Huawei screen closed: $kind resultCode=${result.resultCode}")

        when (kind) {
            PendingKind.SIGN_IN -> {
                if (result.resultCode != Activity.RESULT_OK) {
                    call.reject("HUAWEI ID sign-in was not completed", HmsIapErrors.NOT_SIGNED_IN)
                    return
                }
                // Signed in: check again, without launching sign-in a second time.
                val iap = client ?: return call.reject("Client not ready", HmsIapErrors.UNKNOWN)
                iap.isEnvReady(
                    call.getBoolean("supportAppTouch", false) ?: false,
                    false,
                ) { step -> handleStep(call, step, null) }
            }
            PendingKind.PURCHASE -> {
                val data = result.data
                if (data == null) {
                    call.reject("Checkout returned no data", HmsIapErrors.NO_RESULT)
                    return
                }
                val iap = client ?: return call.reject("Client not ready", HmsIapErrors.UNKNOWN)
                handleStep(call, iap.parsePurchaseResult(data), null)
            }
            null -> call.reject("Unknown activity result", HmsIapErrors.UNKNOWN)
        }
    }

    // --- helpers -------------------------------------------------------------

    /** Returns the HMS client, or rejects the call when the SDK is missing. */
    private fun requireClient(call: PluginCall): HmsIapClient? {
        if (!implementation.isIapSdkPresent()) {
            call.reject(
                "com.huawei.hms:iap is not on the classpath of this build",
                HmsIapErrors.IAP_SDK_MISSING,
            )
            return null
        }
        return client ?: HmsIapClient(activity).also { client = it }
    }

    /** Validates `type` (0, 1 or 2) on the call. */
    private fun requireProductType(call: PluginCall): Int? {
        val type = call.getInt("type")
        if (type == null || type !in VALID_PRODUCT_TYPES) {
            call.reject(
                "type must be 0 (Consumable), 1 (NonConsumable) or 2 (Subscription)",
                HmsIapErrors.INVALID_ARGUMENT,
            )
            return null
        }
        return type
    }

    private fun reject(call: PluginCall, failure: HmsIapFailure) {
        log("rejected: ${failure.code} ${failure.data}")
        call.reject(failure.message, failure.code, null, failure.data)
    }

    private fun notImplemented(call: PluginCall, version: String) {
        call.reject("Not implemented yet (planned for $version)", HmsIapErrors.NOT_IMPLEMENTED)
    }

    private fun log(message: String) {
        if (implementation.loggingEnabled) Logger.debug(TAG, message)
    }

    companion object {
        private const val TAG = "HmsIap"
        private val VALID_PRODUCT_TYPES = 0..2
    }
}
