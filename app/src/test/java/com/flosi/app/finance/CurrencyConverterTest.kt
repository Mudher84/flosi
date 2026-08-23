package com.flosi.app.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyConverterTest {
    private val rates = setOf(
        CurrencyConverter.encodeRate("USD", "IQD", "1310")!!,
        CurrencyConverter.encodeRate("EUR", "USD", "1.10")!!
    )

    @Test fun directConversionUsesStoredRate() {assertEquals(131_000L, CurrencyConverter.convert(100L, "USD", "IQD", rates))}
    @Test fun reverseConversionUsesReciprocalRate() {assertEquals(100L, CurrencyConverter.convert(131_000L, "IQD", "USD", rates))}
    @Test fun twoHopConversionUsesBridgeCurrency() {assertEquals(144_100L, CurrencyConverter.convert(100L, "EUR", "IQD", rates))}
    @Test fun sameCurrencyDoesNotNeedRate() {assertEquals(500L, CurrencyConverter.convert(500L, "iqd", "IQD", emptySet()))}
    @Test fun missingRateFailsClosed() {assertNull(CurrencyConverter.convert(100L, "GBP", "IQD", rates))}

    @Test fun invalidRatesAreRejected() {
        assertNull(CurrencyConverter.encodeRate("USD", "USD", "1"))
        assertNull(CurrencyConverter.encodeRate("US", "IQD", "1"))
        assertNull(CurrencyConverter.encodeRate("USD", "I-Q", "1"))
        assertNull(CurrencyConverter.encodeRate("USD", "IQD", "0"))
        assertNull(CurrencyConverter.encodeRate("USD", "IQD", "abc"))
        assertNull(CurrencyConverter.encodeRate("USD", "IQD", "0.0000000000000000001"))
    }

    @Test fun twoHopChoiceIsDeterministicWhenSeveralBridgesExist() {
        val many=setOf(
            CurrencyConverter.encodeRate("USD","AAA","2")!!,
            CurrencyConverter.encodeRate("AAA","IQD","3")!!,
            CurrencyConverter.encodeRate("USD","ZZZ","4")!!,
            CurrencyConverter.encodeRate("ZZZ","IQD","10")!!
        )
        repeat(20){assertEquals(600L,CurrencyConverter.convert(100L,"USD","IQD",many))}
    }

    @Test fun overflowFailsClosedInsteadOfWrapping() {
        val huge=setOf(CurrencyConverter.encodeRate("USD","IQD","999999999999")!!)
        assertNull(CurrencyConverter.convert(Long.MAX_VALUE,"USD","IQD",huge))
    }
}
