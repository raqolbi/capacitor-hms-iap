package com.qolcore.capacitor.hmsiap

import com.huawei.hms.iap.IapApiException
import com.huawei.hms.support.api.client.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Error codes are part of the public API (docs/DESIGN.md §4). The numeric HMS
 * values are written as literals here on purpose: if a future SDK changes an
 * OrderStatusCode constant, these tests fail instead of silently remapping.
 */
class HmsIapErrorMapperTest {

    @Test
    fun mapsEveryDocumentedReturnCode() {
        val expected = mapOf(
            60000 to "USER_CANCELED",
            60001 to "INVALID_ARGUMENT",
            60002 to "IAP_NOT_ACTIVATED",
            60005 to "NETWORK_ERROR",
            60050 to "NOT_SIGNED_IN",
            60051 to "ALREADY_OWNED",
            60052 to "NOT_OWNED",
            60053 to "ALREADY_CONSUMED",
            60054 to "REGION_NOT_SUPPORTED",
            907135000 to "APP_NOT_CONFIGURED",
        )
        expected.forEach { (returnCode, code) ->
            assertEquals("returnCode $returnCode", code, HmsIapErrorMapper.codeFor(returnCode))
        }
    }

    @Test
    fun unknownReturnCodeMapsToUnknown() {
        assertEquals(HmsIapErrors.UNKNOWN, HmsIapErrorMapper.codeFor(12345))
        assertEquals(HmsIapErrors.UNKNOWN, HmsIapErrorMapper.codeFor(-1))
    }

    @Test
    fun fromReturnCodeKeepsOriginalCodeAndMessage() {
        val failure = HmsIapErrorMapper.fromReturnCode(60051, "already owned error")
        assertEquals("ALREADY_OWNED", failure.code)
        assertEquals("already owned error", failure.message)
        assertEquals(60051, failure.data.getInt("returnCode"))
        assertEquals("already owned error", failure.data.getString("errMsg"))
    }

    @Test
    fun fromReturnCodeWithoutMessageHasReadableFallback() {
        val failure = HmsIapErrorMapper.fromReturnCode(60005, null)
        assertEquals("NETWORK_ERROR", failure.code)
        assertEquals("HMS IAP returned 60005", failure.message)
        assertFalse(failure.data.has("errMsg"))
    }

    @Test
    fun fromIapApiExceptionUsesStatusCodeAndMessage() {
        val e = IapApiException(Status(60002, "iap not activated"))
        val failure = HmsIapErrorMapper.fromException(e)
        assertEquals("IAP_NOT_ACTIVATED", failure.code)
        assertEquals("iap not activated", failure.message)
        assertEquals(60002, failure.data.getInt("returnCode"))
        assertEquals(60002, failure.data.getInt("statusCode"))
        assertEquals("iap not activated", failure.data.getString("errMsg"))
    }

    @Test
    fun otherExceptionsMapToUnknown() {
        val failure = HmsIapErrorMapper.fromException(IllegalStateException("boom"))
        assertEquals(HmsIapErrors.UNKNOWN, failure.code)
        assertEquals("boom", failure.message)
    }
}
