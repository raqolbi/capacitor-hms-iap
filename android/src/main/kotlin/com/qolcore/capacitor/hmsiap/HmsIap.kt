package com.qolcore.capacitor.hmsiap

import android.content.Context
import android.content.pm.PackageManager

/**
 * Native implementation, kept separate from the Capacitor bridge class.
 * HMS SDK classes must only be touched after [isIapSdkPresent] returns true,
 * because the SDK is a compileOnly dependency.
 */
class HmsIap(private val context: Context) {

    var loggingEnabled = false

    /** True when com.huawei.hms:iap is on the app's runtime classpath. */
    fun isIapSdkPresent(): Boolean =
        try {
            Class.forName(IAP_ENTRY_CLASS)
            true
        } catch (_: ClassNotFoundException) {
            false
        }

    /** True when HMS Core (APK) is installed on the device. */
    fun isHmsCoreInstalled(): Boolean =
        try {
            context.packageManager.getPackageInfo(HMS_CORE_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    // TODO(0.1.0): isEnvReady, obtainProductInfo, createPurchaseIntent,
    //  parsePurchaseResultInfoFromIntent, obtainOwnedPurchases. See docs/DESIGN.md §3.
    // TODO(0.2.0): consumeOwnedPurchase, obtainOwnedPurchaseRecord, isSandboxActivated.
    // TODO(0.3.0): startIapActivity, enablePendingPurchase.

    companion object {
        const val IAP_ENTRY_CLASS = "com.huawei.hms.iap.Iap"
        const val HMS_CORE_PACKAGE = "com.huawei.hwid"
    }
}
