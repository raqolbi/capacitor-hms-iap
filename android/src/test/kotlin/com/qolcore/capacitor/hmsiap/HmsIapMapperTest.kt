package com.qolcore.capacitor.hmsiap

import com.huawei.hms.iap.entity.ProductInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HmsIapMapperTest {

    /** Shape of a real sandbox InAppPurchaseData (2026-11-18), token shortened. */
    private val sandboxPurchase = """
        {"autoRenewing":false,"orderId":"20261118120654051a6c71f215.119853273",
         "packageName":"com.qolcore.capacitor.hmsiap.example","applicationId":119853273,
         "kind":1,"productId":"premium_lifetime","productName":"Premium Lifetime",
         "purchaseTime":1794974891695,"purchaseState":0,"purchaseToken":"000001a1.119853273",
         "consumptionState":0,"confirmed":1,"purchaseType":0,"currency":"IDR","price":274100,
         "country":"ID","payOrderId":"sandboxDAb7","payType":"65","sdkChannel":"1"}
    """.trimIndent()

    @Test
    fun parsesSandboxNonConsumable() {
        val p = HmsIapMapper.parsePurchase(sandboxPurchase)
        assertEquals("20261118120654051a6c71f215.119853273", p.getString("orderId"))
        assertEquals("premium_lifetime", p.getString("productId"))
        assertEquals("000001a1.119853273", p.getString("purchaseToken"))
        assertEquals(0, p.getInt("purchaseState"))
        assertEquals(1794974891695L, p.getLong("purchaseTime"))
        assertEquals(1, p.getInt("kind"))
        assertEquals("com.qolcore.capacitor.hmsiap.example", p.getString("packageName"))
        assertEquals(0, p.getInt("consumptionState"))
        assertEquals(1, p.getInt("confirmed"))
        assertFalse(p.getBoolean("autoRenewing"))
        assertFalse(p.has("isSubscriptionValid"))
        // Every original field stays available under `raw`.
        val raw = p.getJSONObject("raw")
        assertEquals("sandboxDAb7", raw.getString("payOrderId"))
        assertEquals(0, raw.getInt("purchaseType"))
    }

    @Test
    fun subscriptionValidityIsRenamed() {
        val p = HmsIapMapper.parsePurchase(
            """{"productId":"sub_monthly","kind":2,"purchaseState":0,"subIsvalid":true,"expirationDate":1800000000000}""",
        )
        assertTrue(p.getBoolean("isSubscriptionValid"))
        assertEquals(1800000000000L, p.getLong("expirationDate"))
    }

    @Test
    fun invalidJsonGivesSafeDefaults() {
        val p = HmsIapMapper.parsePurchase("not json")
        assertEquals("", p.getString("productId"))
        assertEquals(-1, p.getInt("purchaseState"))
        assertEquals(-1, p.getInt("kind"))
        assertEquals(0, p.getJSONObject("raw").length())
    }

    @Test
    fun purchaseResultKeepsRawDataAndDefaultsAlgorithm() {
        val r = HmsIapMapper.purchaseResult(sandboxPurchase, "c2ln", null)
        assertEquals(sandboxPurchase, r.getString("purchaseData"))
        assertEquals("c2ln", r.getString("signature"))
        assertEquals("SHA256WithRSA", r.getString("signatureAlgorithm"))
        assertEquals("premium_lifetime", r.getJSONObject("purchase").getString("productId"))
    }

    @Test
    fun purchaseListPairsDataWithSignaturesByIndex() {
        val list = HmsIapMapper.purchaseList(
            listOf("""{"productId":"a"}""", """{"productId":"b"}"""),
            listOf("sigA"),
            "SHA256WithRSA/PSS",
        )
        assertEquals(2, list.length())
        assertEquals("sigA", list.getJSONObject(0).getString("signature"))
        assertEquals("", list.getJSONObject(1).getString("signature"))
        assertEquals("SHA256WithRSA/PSS", list.getJSONObject(1).getString("signatureAlgorithm"))
        assertEquals(0, HmsIapMapper.purchaseList(null, null, null).length())
    }

    @Test
    fun mapsNonConsumableProductWithoutSubscriptionFields() {
        val info = ProductInfo().apply {
            productId = "premium_lifetime"
            priceType = 1
            productName = "Premium Lifetime"
            productDesc = "Unlocks premium features forever."
            price = "IDR 2741.00"
            microsPrice = 2741000000L
            currency = "IDR"
            originalLocalPrice = "IDR 2741.00"
            originalMicroPrice = 2741000000L
            status = 0
        }
        val p = HmsIapMapper.product(info)
        assertEquals("premium_lifetime", p.getString("productId"))
        assertEquals(1, p.getInt("type"))
        assertEquals("IDR 2741.00", p.getString("price"))
        assertEquals(2741000000L, p.getLong("priceMicros"))
        assertEquals("IDR", p.getString("currency"))
        assertEquals(0, p.getInt("status"))
        assertFalse(p.has("subscriptionPeriod"))
        assertFalse(p.has("subscriptionLevel"))
        assertFalse(p.has("introductoryPrice"))
    }

    @Test
    fun mapsSubscriptionFieldsOnlyForSubscriptions() {
        val info = ProductInfo().apply {
            productId = "sub_monthly"
            priceType = 2
            productName = "Monthly"
            productDesc = "Monthly plan"
            price = "IDR 10000.00"
            microsPrice = 10000000000L
            currency = "IDR"
            status = 0
            subPeriod = "P1M"
            subGroupId = "g1"
            subGroupTitle = "Plans"
            subProductLevel = 1
            subSpecialPrice = "IDR 5000.00"
            subSpecialPriceMicros = 5000000000L
            subSpecialPeriod = "P1M"
            subSpecialPeriodCycles = 1
            offerUsedStatus = 0
        }
        val p = HmsIapMapper.product(info)
        assertEquals("P1M", p.getString("subscriptionPeriod"))
        assertEquals(1, p.getInt("subscriptionLevel"))
        assertEquals("IDR 5000.00", p.getString("introductoryPrice"))
        assertEquals(5000000000L, p.getLong("introductoryPriceMicros"))
        assertEquals(1, p.getInt("introductoryPeriodCycles"))
        assertEquals(0, p.getInt("offerUsedStatus"))
    }
}
