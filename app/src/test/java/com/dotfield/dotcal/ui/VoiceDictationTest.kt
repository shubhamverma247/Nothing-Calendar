package com.dotfield.dotcal.ui

import android.speech.SpeechRecognizer
import com.dotfield.dotcal.share.CardImageExporter
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceDictationTest {
    @Test fun mapsCancellationAndPermissionErrors() {
        assertEquals(VoiceDictationState.Cancelled, voiceDictationStateForError(SpeechRecognizer.ERROR_CLIENT))
        assertEquals(VoiceDictationState.PermissionDenied, voiceDictationStateForError(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
    }

    @Test fun mapsRecognizerFailures() {
        assertEquals(VoiceDictationState.Empty, voiceDictationStateForError(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals(com.dotfield.dotcal.R.string.quick_add_voice_empty, VoiceDictationState.Empty.stringRes())
    }

    @Test fun calendarExportSelectsRequestedLayout() {
        assertEquals("month", CardImageExporter.calendarViewLayout("Month"))
        assertEquals("week", CardImageExporter.calendarViewLayout("WEEK"))
        assertEquals("agenda", CardImageExporter.calendarViewLayout("Agenda"))
        assertEquals("agenda", CardImageExporter.calendarViewLayout("unknown"))
    }
    @Test fun monthGridStartsOnConfiguredWeekBoundary() {
        assertEquals(LocalDate.of(2026, 8, 31), CardImageExporter.monthGridStart(LocalDate.of(2026, 9, 1)))
        assertEquals(LocalDate.of(2026, 8, 30), CardImageExporter.monthGridStart(LocalDate.of(2026, 9, 1), DayOfWeek.SUNDAY))
    }
}
