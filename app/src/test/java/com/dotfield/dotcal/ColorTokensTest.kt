package com.dotfield.dotcal

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorTokensTest {
    @Test
    fun nothingRedHasOneSharedHexAndArgbToken() {
        assertEquals("#C8102E", NOTHING_RED_HEX)
        assertEquals(0xFFC8102EL, NOTHING_RED_ARGB)
    }
}
