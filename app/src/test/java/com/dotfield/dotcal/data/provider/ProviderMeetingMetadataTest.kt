package com.dotfield.dotcal.data.provider

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import android.provider.CalendarContract

class ProviderMeetingMetadataTest {
    @Test
    fun meetingMetadataRoundTripsJson() {
        val metadata = ProviderMeetingMetadata(
            organizer = "lead@example.com",
            accessLevel = 2,
            availability = 1,
            guestsCanModify = false,
            guestsCanInviteOthers = true,
            guestsCanSeeGuests = true,
            attendees = listOf(
                ProviderAttendee(
                    name = "Ana",
                    email = "ana@example.com",
                    status = 1,
                    type = 1,
                    relationship = 1,
                ),
                ProviderAttendee(
                    name = "Dev \"Ops\"",
                    email = "devops@example.com",
                    status = 2,
                    type = 2,
                    relationship = 2,
                ),
            ),
        )

        assertEquals(metadata, decodeProviderMeetingMetadata(encodeProviderMeetingMetadata(metadata)))
    }

    @Test
    fun blankMeetingMetadataEncodesAsNull() {
        assertNull(encodeProviderMeetingMetadata(ProviderMeetingMetadata()))
    }

    @Test
    fun invalidMeetingMetadataJsonReturnsNull() {
        assertNull(decodeProviderMeetingMetadata("not-json"))
    }

    @Test
    fun defaultProviderFieldsDoNotCountAsMeetingDetails() {
        val metadata = ProviderMeetingMetadata(
            organizer = "account@example.com",
            accessLevel = 1,
            availability = 1,
            guestsCanModify = true,
            guestsCanInviteOthers = true,
            guestsCanSeeGuests = true,
        )

        assertFalse(metadata.hasMeaningfulMeetingDetails())
    }

    @Test fun attendeesCountAsMeetingDetails() {
        assertTrue(
            ProviderMeetingMetadata(
                attendees = listOf(ProviderAttendee(email = "guest@example.com")),
            ).hasMeaningfulMeetingDetails(),
        )
    }

    @Test fun organizerOnlyAttendeeDoesNotCountAsMeetingDetails() {
        assertFalse(
            ProviderMeetingMetadata(
                attendees = listOf(
                    ProviderAttendee(
                        email = "owner@example.com",
                        relationship = CalendarContract.Attendees.RELATIONSHIP_ORGANIZER,
                    ),
                ),
            ).hasMeaningfulMeetingDetails(),
        )
    }
}
