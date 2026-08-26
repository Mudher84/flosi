package com.flosi.app.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlosiI18nTest {
    @Test fun onlySixLaunchLocalesAreExposed() {
        assertEquals(setOf("ar","en","tr","fr","de","es"), FlosiLocales.codes)
        assertEquals(6, FlosiLocales.all.size)
    }

    @Test fun everyLaunchLocaleResolvesEveryCoreKey() {
        FlosiLocales.codes.forEach { language ->
            assertTrue("Missing $language keys: ${FlosiI18n.missingKeys(language)}", FlosiI18n.missingKeys(language).isEmpty())
            FlosiI18n.requiredKeys.forEach { key ->
                val value=FlosiI18n.text(language,key)
                assertTrue("blank $key for $language",value.isNotBlank())
                assertFalse("raw key leaked: $key for $language",value==key)
            }
        }
    }

    @Test fun everyLegacyPhraseIsTranslatedForEveryNonArabicLaunchLocale() {
        setOf("en","tr","fr","de","es").forEach { language ->
            assertTrue("Missing legacy $language: ${legacyMissingTranslations(language)}",legacyMissingTranslations(language).isEmpty())
        }
    }

    @Test fun arabicIsTheOnlyRtlLaunchLocale() {
        assertEquals(setOf("ar"),FlosiLocales.all.filter{it.rtl}.map{it.code}.toSet())
    }

    @Test fun unsupportedLocaleFallsBackToArabicSafely() {
        assertEquals("اليوم",FlosiI18n.text("xx","today"))
    }
}
