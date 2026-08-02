package com.dotfield.dotcal.prefs

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `null blank and unknown tags fall back to System`() {
        assertEquals(AppLanguage.System, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.System, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("   "))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("xx"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("klingon"))
    }

    @Test
    fun `exact tags resolve to their language`() {
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en"))
        assertEquals(AppLanguage.Indonesian, AppLanguage.fromTag("id"))
        assertEquals(AppLanguage.Arabic, AppLanguage.fromTag("ar"))
        assertEquals(AppLanguage.Turkish, AppLanguage.fromTag("tr"))
        assertEquals(AppLanguage.Russian, AppLanguage.fromTag("ru"))
    }

    @Test
    fun `languages we no longer ship fall back to System`() {
        // Dropped after the first cut: hi, ja, ko, zh. A device left on one of these must land on
        // System (which resolves to English via resource fallback), never on a stale entry.
        assertEquals(AppLanguage.System, AppLanguage.fromTag("hi"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("ja"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("ko"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("zh"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("zh-Hans-CN"))
    }

    @Test
    fun `region and script subtags are dropped`() {
        assertEquals(AppLanguage.Portuguese, AppLanguage.fromTag("pt-BR"))
        assertEquals(AppLanguage.Portuguese, AppLanguage.fromTag("pt_PT"))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en-GB"))
        assertEquals(AppLanguage.Spanish, AppLanguage.fromTag("es-419"))
    }

    @Test
    fun `tags are matched case-insensitively and trimmed`() {
        assertEquals(AppLanguage.French, AppLanguage.fromTag("FR"))
        assertEquals(AppLanguage.German, AppLanguage.fromTag("  de  "))
        assertEquals(AppLanguage.Turkish, AppLanguage.fromTag("Tr-TR"))
    }

    @Test
    fun `System carries an empty tag and every other language carries a distinct one`() {
        assertEquals("", AppLanguage.System.tag)
        val tags = AppLanguage.entries.filter { it != AppLanguage.System }.map { it.tag }
        assertEquals(tags.size, tags.toSet().size)
        assertEquals(9, tags.size)
    }

    @Test
    fun `every shipped tag round-trips through fromTag`() {
        AppLanguage.entries.forEach { language ->
            assertEquals(language, AppLanguage.fromTag(language.tag))
        }
    }
}
