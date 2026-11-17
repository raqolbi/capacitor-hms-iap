package com.qolcore.capacitor.hmsiap

import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.huawei.hms.iap.entity.ProductInfo
import org.json.JSONObject

/** Converts HMS entities and raw purchase JSON into objects for JavaScript. */
internal object HmsIapMapper {

    fun product(p: ProductInfo): JSObject = JSObject().apply {
        put("productId", p.productId)
        put("type", p.priceType)
        put("name", p.productName)
        put("description", p.productDesc)
        put("price", p.price)
        put("priceMicros", p.microsPrice)
        put("currency", p.currency)
        putIfNotNull("originalPrice", p.originalLocalPrice)
        put("originalPriceMicros", p.originalMicroPrice)
        put("status", p.status)
        putIfNotNull("subscriptionPeriod", p.subPeriod)
        putIfNotNull("subscriptionGroupId", p.subGroupId)
        putIfNotNull("subscriptionGroupTitle", p.subGroupTitle)
        if (p.priceType == 2) put("subscriptionLevel", p.subProductLevel)
        putIfNotNull("freeTrialPeriod", p.subFreeTrialPeriod)
        putIfNotNull("introductoryPrice", p.subSpecialPrice)
        if (!p.subSpecialPrice.isNullOrEmpty()) {
            put("introductoryPriceMicros", p.subSpecialPriceMicros)
            putIfNotNull("introductoryPeriod", p.subSpecialPeriod)
            put("introductoryPeriodCycles", p.subSpecialPeriodCycles)
        }
        if (p.priceType == 2) put("offerUsedStatus", p.offerUsedStatus)
    }

    /** Builds a PurchaseResult: raw data + signature + parsed copy. */
    fun purchaseResult(purchaseData: String, signature: String?, algorithm: String?): JSObject =
        JSObject().apply {
            put("purchaseData", purchaseData)
            put("signature", signature ?: "")
            put("signatureAlgorithm", algorithm ?: DEFAULT_ALGORITHM)
            put("purchase", parsePurchase(purchaseData))
        }

    fun purchaseList(data: List<String>?, signatures: List<String>?, algorithm: String?): JSArray {
        val array = JSArray()
        data.orEmpty().forEachIndexed { i, json ->
            array.put(purchaseResult(json, signatures?.getOrNull(i), algorithm))
        }
        return array
    }

    fun parsePurchase(json: String): JSObject {
        val raw = try {
            JSONObject(json)
        } catch (_: Exception) {
            JSONObject()
        }
        return JSObject().apply {
            put("orderId", raw.optString("orderId"))
            put("productId", raw.optString("productId"))
            put("purchaseToken", raw.optString("purchaseToken"))
            put("purchaseState", raw.optInt("purchaseState", -1))
            put("purchaseTime", raw.optLong("purchaseTime"))
            put("kind", raw.optInt("kind", -1))
            copyIfPresent(raw, "packageName")
            copyIfPresent(raw, "developerPayload")
            copyIfPresent(raw, "consumptionState")
            copyIfPresent(raw, "confirmed")
            copyIfPresent(raw, "subscriptionId")
            if (raw.has("subIsvalid")) put("isSubscriptionValid", raw.optBoolean("subIsvalid"))
            copyIfPresent(raw, "autoRenewing")
            copyIfPresent(raw, "expirationDate")
            put("raw", JSObject.fromJSONObject(raw))
        }
    }

    private fun JSObject.putIfNotNull(key: String, value: String?) {
        if (!value.isNullOrEmpty()) put(key, value)
    }

    private fun JSObject.copyIfPresent(raw: JSONObject, key: String) {
        if (raw.has(key)) put(key, raw.get(key))
    }

    const val DEFAULT_ALGORITHM = "SHA256WithRSA"
}
