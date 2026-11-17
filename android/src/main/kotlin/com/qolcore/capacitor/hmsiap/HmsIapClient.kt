package com.qolcore.capacitor.hmsiap

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import com.getcapacitor.JSObject
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.IsEnvReadyResult
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesReq
import com.huawei.hms.iap.entity.ProductInfoReq
import com.huawei.hms.iap.entity.PurchaseIntentReq

/**
 * Thin wrapper around [IapClient]. Only created after the plugin has checked
 * that the HMS IAP SDK is on the classpath (the SDK is compileOnly).
 *
 * Results are delivered through callbacks on the main thread.
 */
internal class HmsIapClient(activity: Activity) {

    private val client: IapClient = Iap.getIapClient(activity)

    /** Outcome of a call that may need a Huawei activity (sign-in or checkout). */
    sealed class Step {
        data class Done(val result: JSObject) : Step()
        data class NeedsActivity(val intent: PendingIntent) : Step()
        data class Failed(val failure: HmsIapFailure) : Step()
    }

    fun isEnvReady(supportAppTouch: Boolean, resolveSignIn: Boolean, callback: (Step) -> Unit) {
        val task = if (supportAppTouch) client.isEnvReady(true) else client.isEnvReady()
        task.addOnSuccessListener { result: IsEnvReadyResult ->
            callback(Step.Done(JSObject().apply {
                put("returnCode", result.returnCode)
                result.country?.let { put("country", it) }
                result.carrierId?.let { put("carrierId", it) }
            }))
        }.addOnFailureListener { e ->
            if (resolveSignIn && e is IapApiException &&
                e.statusCode == OrderStatusCode.ORDER_HWID_NOT_LOGIN &&
                e.status?.hasResolution() == true
            ) {
                callback(Step.NeedsActivity(e.status.resolution))
            } else {
                callback(Step.Failed(HmsIapErrorMapper.fromException(e)))
            }
        }
    }

    fun getProducts(productIds: List<String>, type: Int, callback: (Step) -> Unit) {
        val req = ProductInfoReq().apply {
            priceType = type
            this.productIds = productIds
        }
        client.obtainProductInfo(req).addOnSuccessListener { result ->
            val products = com.getcapacitor.JSArray()
            result.productInfoList.orEmpty().forEach { products.put(HmsIapMapper.product(it)) }
            callback(Step.Done(JSObject().apply { put("products", products) }))
        }.addOnFailureListener { e ->
            callback(Step.Failed(HmsIapErrorMapper.fromException(e)))
        }
    }

    fun createPurchaseIntent(
        productId: String,
        type: Int,
        developerPayload: String?,
        reservedInfo: String?,
        signatureAlgorithm: String?,
        callback: (Step) -> Unit,
    ) {
        val req = PurchaseIntentReq().apply {
            this.productId = productId
            priceType = type
            developerPayload?.let { this.developerPayload = it }
            reservedInfo?.let { reservedInfor = it }
            signatureAlgorithm?.let { this.signatureAlgorithm = it }
        }
        client.createPurchaseIntent(req).addOnSuccessListener { result ->
            val status = result.status
            if (status != null && status.hasResolution()) {
                callback(Step.NeedsActivity(status.resolution))
            } else {
                callback(Step.Failed(HmsIapErrorMapper.fromReturnCode(result.returnCode, result.errMsg)))
            }
        }.addOnFailureListener { e ->
            callback(Step.Failed(HmsIapErrorMapper.fromException(e)))
        }
    }

    /** Parses the checkout activity result. */
    fun parsePurchaseResult(data: Intent): Step {
        val info = client.parsePurchaseResultInfoFromIntent(data)
        return when (info.returnCode) {
            OrderStatusCode.ORDER_STATE_SUCCESS -> Step.Done(
                HmsIapMapper.purchaseResult(
                    info.inAppPurchaseData,
                    info.inAppDataSignature,
                    info.signatureAlgorithm,
                ),
            )
            else -> Step.Failed(HmsIapErrorMapper.fromReturnCode(info.returnCode, info.errMsg))
        }
    }

    fun getOwnedPurchases(
        type: Int,
        continuationToken: String?,
        signatureAlgorithm: String?,
        callback: (Step) -> Unit,
    ) {
        val req = OwnedPurchasesReq().apply {
            priceType = type
            continuationToken?.let { this.continuationToken = it }
            signatureAlgorithm?.let { this.signatureAlgorithm = it }
        }
        client.obtainOwnedPurchases(req).addOnSuccessListener { result ->
            val algorithm = result.signatureAlgorithm
            callback(Step.Done(JSObject().apply {
                put("purchases", HmsIapMapper.purchaseList(result.inAppPurchaseDataList, result.inAppSignature, algorithm))
                put(
                    "pendingPurchases",
                    HmsIapMapper.purchaseList(result.placedInappPurchaseDataList, result.placedInappSignatureList, algorithm),
                )
                result.continuationToken?.takeIf { it.isNotEmpty() }?.let { put("continuationToken", it) }
            }))
        }.addOnFailureListener { e ->
            callback(Step.Failed(HmsIapErrorMapper.fromException(e)))
        }
    }
}
