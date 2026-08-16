package com.dotfield.dotcal.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AvailabilityScrollbarTest {
    @Test
    fun scrollbarThumbIsHiddenForUnboundedHeight() {
        assertNull(availabilityScrollbarThumb(Dp.Infinity, scrollValue = 10, maxScroll = 100))
    }

    @Test
    fun scrollbarThumbIsHiddenWhenContentDoesNotScroll() {
        assertNull(availabilityScrollbarThumb(120.dp, scrollValue = 0, maxScroll = 0))
    }

    @Test
    fun scrollbarThumbUsesAvailableHeightWhenTrackIsShort() {
        val thumb = availabilityScrollbarThumb(24.dp, scrollValue = 10, maxScroll = 100)

        assertEquals(24.dp, thumb?.height)
        assertEquals(0.dp, thumb?.offset)
    }

    @Test
    fun scrollbarOffsetIsClampedToTrackTravel() {
        val thumb = availabilityScrollbarThumb(100.dp, scrollValue = 200, maxScroll = 100)

        assertEquals(32.dp, thumb?.height)
        assertEquals(68.dp, thumb?.offset)
    }
}
