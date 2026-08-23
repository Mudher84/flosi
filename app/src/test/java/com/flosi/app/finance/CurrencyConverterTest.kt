package com.flosi.app.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyConverterTest {
    private val rates = setOf(
        CurrencyConverter.encodeRate("USD", "IQD", "1310")!!,
        CurrencyConverter.encodeRate("EUR", "USD", "1.10")!!
    )

    @Test
    fun directConversionUsesStoredRate() {
        assertEquals(131_000L, CurrencyConverter.convert(100L, "USD", "IQD", rates))
    }

    @Test
    fun reverseConversionUsesReciprocalRate() {
        assertEquals(100L, CurrencyConverter.convert(131_000L, "IQD", "USD", rates))
    }

    @Test
    fun twoHopConversionUsesBridgeCurrency() {
        assertEquals(144_100L, CurrencyConverter.convert(100L, "EUR", "IQD", rates))
    }

    @Test
    fun sameCurrencyDoesNotNeedRate() {
        assertEquals(500L, CurrencyConverter.convert(500L, "iqd", "IQD", emptySet()))
    }

    @Test
    fun missingRateFailsClosed() {
        assertNull(CurrencyConverter.convert(100L, "GBP", "IQD", rates))
    }

    @Test
    fun invalidRatesAreRejected() {
        assertNull(CurrencyConverter.encodeRate("USD", "USD", "1"))
        assertNull(CurrencyConverter.encodeRate("USD", "IQD", "0"))
        assertNull(CurrencyConverter.encodeRate("USD", "IQD", "abc"))
    }
}
