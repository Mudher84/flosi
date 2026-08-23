package com.flosi.app.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlosiI18nTest {
    @Test
    fun allSupportedLocalesResolveCoreNavigationWithoutKeys() {
        val core = listOf("today","activity","people","me","add","back","save","cancel","accounts","security","language","income","expense","amount","account","category","reports","goals","budgets","commitments","invoices","settings","notifications","transfer","bank","wallet","cash")
        assertEquals(24, FlosiLocales.all.size)
        FlosiLocales.all.forEach { locale ->
            core.forEach { key ->
                val value = FlosiI18n.text(locale.code, key)
                assertTrue("blank $key for ${locale.code}", value.isNotBlank())
                assertFalse("raw key leaked: $key for ${locale.code}", value == key)
            }
        }
    }

    @Test
    fun rtlLocalesAreExplicitAndStable() {
        val rtl = FlosiLocales.all.filter { it.rtl }.map { it.code }.toSet()
        assertEquals(setOf("ar","fa","ur","he"), rtl)
    }

    @Test
    fun unsupportedLocaleFallsBackSafely() {
        assertEquals("Today", FlosiI18n.text("xx", "today"))
    }
}
