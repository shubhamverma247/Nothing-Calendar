package com.dotfield.dotcal.data.shifts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ShiftEventMetadataTest {

    @Test
    fun metadataRoundTripsThroughJson() {
        val metadata = ShiftEventMetadata(
            shiftTypeId = "night",
            shiftTypeName = "Night",
            colorHex = "#445566",
            date = LocalDate.of(2026, 8, 13),
            generatedBy = ShiftEventGeneratedBy.Pattern,
            patternId = "pattern-1",
        )

        assertEquals(metadata, parseShiftEventMetadata(metadata.encode()))
    }

    @Test
    fun invalidMetadataReturnsNull() {
        assertNull(parseShiftEventMetadata("{bad json"))
    }
}
