package com.dotfield.dotcal.data.holiday

import android.content.Context
import com.dotfield.dotcal.data.CalendarEvent
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

data class HolidayCountry(
    val code: String,
    val name: String,
)

class HolidayDataSource(private val context: Context) {
    fun loadBundledHolidays(countryCode: String, accountId: String): List<CalendarEvent> {
        val json = context.assets.open(HOLIDAY_ASSET).bufferedReader().use { it.readText() }
        val holidays = JSONObject(json).optJSONArray(countryCode) ?: return emptyList()
        val zoneId = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        return buildList {
            for (index in 0 until holidays.length()) {
                val item = holidays.getJSONObject(index)
                val date = LocalDate.parse(item.getString("date"))
                val startMs = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                add(
                    CalendarEvent(
                        id = "holiday-$countryCode-${item.getString("date")}",
                        accountId = accountId,
                        title = item.getString("name"),
                        description = "",
                        location = "",
                        startTimeMs = startMs,
                        endTimeMs = startMs,
                        timeZone = zoneId.id,
                        isAllDay = 1,
                        colorHex = null,
                        rrule = null,
                        exceptionDates = "[]",
                        source = HOLIDAY_SOURCE,
                        googleEventId = null,
                        googleCalendarId = null,
                        syncVersion = 0,
                        isTask = 0,
                        isCompleted = 0,
                        completedAtMs = null,
                        imageUris = "[]",
                        voiceNotePath = null,
                        createdAtMs = now,
                        updatedAtMs = now,
                    ),
                )
            }
        }
    }

    companion object {
        const val HOLIDAY_SOURCE = "HOLIDAY"
        private const val HOLIDAY_ASSET = "dotcal_holidays.json"

        val Countries = listOf(
            HolidayCountry("IN", "India"),
            HolidayCountry("DE", "Germany"),
            HolidayCountry("GB", "United Kingdom"),
            HolidayCountry("JP", "Japan"),
            HolidayCountry("IT", "Italy"),
            HolidayCountry("SA", "Saudi Arabia"),
            HolidayCountry("US", "United States"),
        )
    }
}
