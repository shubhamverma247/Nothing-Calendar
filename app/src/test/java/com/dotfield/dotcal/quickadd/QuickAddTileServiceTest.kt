package com.dotfield.dotcal.quickadd

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickAddTileServiceTest {
    @Test fun quickAddIntentUsesAppDeepLink() {
        assertEquals("dotcal://quick-add", QUICK_ADD_DEEP_LINK)
    }
}
