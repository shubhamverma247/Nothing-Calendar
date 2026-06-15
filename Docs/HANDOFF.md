# DotCal Handoff

Updated: 2026-06-15

## Product Target

Build DotCal (`com.dotfield.dotcal`) as a premium Android calendar. Quality bar: Proton Calendar + Fantastical. Visual system: black/white/red, mono typography, clean high-contrast surfaces, no brand references to companies not owned by the app.

## Source Prompt

Prompt came from `C:\Users\Admin\.codex\attachments\61ea4d34-0342-4103-9c6a-4302906da194\pasted-text.txt`.

Key requirements:
- Every future session working on this repo must use `$android-development`.
- Room DB with exactly 5 tables: `calendar_accounts`, `calendar_events`, `event_reminders`, `sync_metadata`, `deleted_event_log`.
- Preferences DataStore keys: default view, week start, default reminder, sync, birthday, onboarding, last sync, declined events, 24-hour format, theme mode.
- Features: Month, Week, Day, Agenda, Add/Edit Event, Event Detail, Reminders/Notifications, Glance widgets, Tasks, Settings, Onboarding.
- Bottom nav: Calendar / Tasks / Settings. Calendar sub-tabs: Month / Week / Day / Agenda.
- Manifest permissions declared for calendar, contacts, notifications, audio, boot, vibrate, exact alarms, foreground service, wake lock.

## Current State

Requested GitHub repo was empty, so scaffold created.

Implemented:
- Android Gradle project with wrapper copied from local Android project.
- App module: Kotlin, Compose, Room, DataStore, WorkManager, Glance dependencies.
- Package/application id: `com.dotfield.dotcal`.
- App label: `DotCal`.
- Manifest permissions and deep link scheme `dotcal://`.
- Room entities for all 5 required tables, with required columns, indexes, FKs, unique indexes.
- `DotCalDatabase`, `CalendarDao`, `DotCalRepository`.
- DataStore key declarations in `CalendarPreferences`.
- Main Compose shell:
  - Bottom nav.
  - Calendar sub-tabs.
  - Month view header/day row/42-cell grid.
  - Today red circle, selected outline, event dots max 3.
  - Horizontal swipe threshold for month nav.
  - Event list bottom sheet on day tap.
  - Simple Add Event sheet with title validation and 09:00 one-hour local event save.
- Placeholder Week/Day, minimal Agenda/Tasks/Settings previews.
- Settings theme switch: `Amoled`, `Dark`, `Light`.
- Removed top brand header text from app shell.
- Settings theme flow changed from inline buttons to nested picker:
  - Settings root has `Theme` row.
  - Tap `Theme` opens theme screen.
  - Select `Amoled`, `Dark`, or `Light`.
- Settings UI redesigned:
  - Cleaner `SETTINGS` header.
  - Sectioned rows for Display, Calendars, Reminders, About.
  - Theme detail screen has preview swatches and active indicator.
- Week View started:
  - Week header with previous/next arrows and date range.
  - 7-day strip with today/selected styling.
  - Optional all-day event strip.
  - 24-hour vertical grid.
  - Basic event blocks in matching hour cells.
  - Current-time marker for current day/hour.
  - Empty hour tap opens Add Event sheet for that date.
- Calendar top controls updated:
  - Add button moved from bottom FAB to top-right calendar action bar.
  - Calendar icon menu added next to Add button.
  - Icon menu switches calendar view.
  - Old visible Calendar tab row is no longer shown.
- Border-heavy UI removed:
  - Removed structural borders from app surfaces, calendar cells, settings rows, sheets, nav, and menus.
  - Replaced borders with filled surfaces, spacing, rounded chips, and subtle dot-grid background.
- Latest calendar header cleanup:
  - Removed dot-grid background.
  - Top calendar action bar has plain icon-only `+` and view-picker calendar icon.
  - Removed left title block from top action bar.
  - Month screen no longer shows left/right arrow buttons.
  - Month label moved left and uses compact format like `2026/6`.
- Day View added:
  - Single-day header with previous/next day.
  - All-day strip.
  - 24-hour vertical timeline.
  - Current-time marker.
  - Event blocks in start-hour rows.
  - Tasks section at bottom.
  - Empty hour tap opens Add Event sheet.
- Date label format normalized:
  - Month, Week, Day, and Agenda views now show top labels as `YYYY/M` (example `2026/6`).
- Header alignment normalized:
  - Week and Day headers now match Month: no previous/next arrows, left-aligned `YYYY/M`.
- Agenda feature improved:
  - Events grouped by date.
  - Date headers like `MON 14`.
  - Empty state shows `NO EVENTS`.
- View picker expanded:
  - Added `3 DAYS` option.
  - Added `YEAR` option.
- Swipe behavior:
  - Week view horizontal swipe changes previous/next week.
  - 3 Days view horizontal swipe changes previous/next 3-day range.
  - Year view horizontal swipe changes previous/next year.
- Year View added:
  - Header shows only year.
  - 12 mini month calendars in a year grid.
  - Selected month highlighted.
  - Days with known loaded events are accented red inside mini calendars.
- View picker polish:
  - Each dropdown option includes a custom calendar-style icon.
  - Labels use title case: `Year view`, `Month view`, `Week view`, `Day view`, `Three-day view`, `Agenda view`.
  - `Agenda view` is at the bottom.
  - Selected row uses theme cell background with red icon/text/check.
  - Normal rows use theme text color, no red mixed into inactive rows.
  - Top action bar shows only the selected view icon, not selected view text.
- Calendar views use the selected app theme palette.
- Bottom tab icons/text are red only when active; inactive tabs use normal secondary color.
- Visible copy moved toward title case (`Settings`, `Theme`, etc.) instead of all-uppercase.
- Year View updated:
  - Shows 3 mini month calendars per row.
  - Weekday labels are bold.
  - Real current month is marked red only in the current year.
  - Real current day is marked with a red circle.
  - Changing year no longer keeps the same selected month red as if it were current.
  - Tapping a month opens Month view for that month.
- Year mini-calendar polish:
  - Increased day row/circle sizing to prevent bottom clipping.
  - Current-day circle is centered around the date number.
  - Weekday labels use stronger weight.
  - Light theme weekday/month labels use dark text.
  - Dark/AMOLED weekday/month labels use white text.
- Clarification applied:
  - Mini-calendar weekday date numbers (Mon-Fri dates like 1,2,3,4,5,8,9) are bold.
  - Weekend date numbers remain normal weight.
  - All calendar grids now start on Sunday.
- New feature:
  - Theme selection now persists via Preferences DataStore `KEY_THEME_MODE`.
  - Selected calendar view now persists via Preferences DataStore `KEY_DEFAULT_VIEW`.
- Latest polish:
  - Removed blue selected dropdown styling.
  - Removed forced white calendar styling so theme mode applies correctly.
  - Month view no longer shows previous/next month date numbers.
  - Dropdown Week/Day/Agenda icons are custom drawn, not the disliked Material symbols.
  - Theme palettes include cell and dot colors, but dot-grid background is currently removed per request.
  - Top selected-view calendar icon is red.
  - Light theme month calendar/top surface is white while outer app background stays warm light gray.
  - Light theme Week/Day/Three-day/Year/Agenda calendar surfaces now use the same white calendar surface.
  - View dropdown background is forced white with black normal rows and red selected row.
  - Settings screens and event sheets use the white surface in Light theme.
  - Dropdown option labels are normal weight, not bold.
  - App text now uses system sans-serif instead of monospace to move closer to phone-style UI.
  - Three-day view option is hidden from dropdown for now; old saved Three-day preference maps back to Month.
  - Calendar view switching now updates local UI state immediately, then persists preference async.
  - Week/Day/Three-day hour cells show subtle visible blocks again on white surfaces.
  - Settings redesigned toward attached reference: `Calendar` title, grouped Accounts/General/Reminders/Additional sections, thin dividers, chevrons, right values, and switches.
  - Week/Day hour cells now keep white background and draw thin grid lines instead of gray-filled blocks.
  - Settings Theme row is an inline dropdown without icons.
  - Theme options are now only `Light`, `Dark`, and `System`; old `Amoled` storage falls back to `System`.
  - Week date header now reserves the same left time gutter as the grid, so date columns align with grid columns.
  - Settings dropdown affordance now uses a small stacked up/down chevron instead of a `v` text glyph.
  - Bottom nav is now a floating rounded white section with left/right/bottom margin and shadow.
  - Bottom nav uses white selected indicator in both Light and Dark, red only for active icon/text, gray for inactive icon/text.
- Latest requested UI change:
  - Dark theme calendar background is now black in Month view.
  - Calendar view dropdown follows theme surface, so it is no longer white in Dark theme.
  - Calendar view dropdown option icons were removed; selected view remains red icon only in top bar.
  - Calendar view dropdown and overflow dropdown are clipped with modest rounded corners.
  - Top-right three-dot overflow was added after the selected calendar icon.
  - Overflow menu contains `Settings` and `Tasks`.
  - Bottom nav bar was removed completely; Settings/Tasks are reached from top overflow, Calendar is reached by picking a calendar view.
- Package rename:
  - App id/namespace/package changed from `com.dotfield.ncalendar` to `com.dotfield.dotcal`.
  - Source package path moved to `app/src/main/java/com/dotfield/dotcal`.
  - Deep link scheme changed from `ncalendar://` to `dotcal://`.
  - Room DB filename changed from `ncalendar.db` to `dotcal.db`; schema tables/columns unchanged.
- Full internal rename:
  - Internal app classes/files now use `DotCal*` names: `DotCalApplication`, `DotCalApp`, `DotCalViewModel`, `DotCalTheme`, `DotCalDatabase`, `DotCalRepository`.
  - Manifest theme style is now `Theme.DotCal`.
  - Verified no remaining `NCalendar` or lowercase `ncalendar` references in app source/build files.
- Week View hardening:
  - Week timed events now position by start minute inside the hour row.
  - Week event block height now follows duration with a 15-minute minimum and one-day maximum.
  - Overlapping Week events are assigned side-by-side columns per overlapping cluster.
  - Tapping an empty Week hour prefills Add Event with that date and hour instead of fixed `09:00`.
  - Day and hidden Three-day hour taps also pass tapped hour into Add Event.
  - Add Event sheet displays the selected start time and saves local events at that selected time.
- Latest navigation polish:
  - Removed rounded clipping from top calendar view dropdown and three-dot overflow dropdown.
  - Settings now opens as a full-screen overlay from the right when selected from the three-dot overflow.
  - Settings root now has a top-left back arrow.
  - Tapping Settings back arrow slides the full-screen Settings overlay out to the right and returns to the previous Calendar or Tasks screen.
- Add/Edit Event expansion:
  - Add Event sheet was replaced with a shared Add/Edit Event editor.
  - Editor now supports title, location, description, all-day toggle, start time, end time, and reminder options (`None`, `5m`, `10m`, `30m`).
  - Tapping an event from the day event sheet opens Edit Event and updates the existing row.
  - Save path writes only existing schema columns plus existing `event_reminders`; no schema/table/column changes.
  - Event list in the day sheet now uses a keyed lazy list instead of direct dynamic rows.
- Latest dark/settings polish:
  - Dark theme calendar view dropdown, overflow dropdown, and theme dropdown use a near-black surface.
  - Dropdown selected options no longer use red text/background; selected check uses normal text color.
  - Dark theme top action icons are now white instead of red.
  - Settings root now starts with reference-style back arrow above large `Calendar` label.
  - Settings shows a sticky compact top bar while scrolling with the same back arrow and centered `Calendar` label.
  - Settings large header back arrow glyph is offset left so it aligns with settings option text.
  - Settings back arrow uses `Icons.AutoMirrored.Filled.ArrowBack`, avoiding deprecated icon API.
- Add/Edit navigation polish:
  - Tapping the top `+` now opens Add Event as a full-screen right-to-left overlay like Settings instead of a bottom sheet.
  - Event editing from a day event row uses the same full-screen editor overlay.
  - Add/Edit top bar has close `X` on the left and save check on the right.
  - All-day switch checked state now uses red track with white thumb instead of fully red.
- Latest Add/Edit polish:
  - Add/Edit Event all-day switch is visually smaller while preserving the red track and white thumb checked state.
- Visual QA:
  - Installed debug APK on connected phone `4ab0d020`.
  - Month dark theme pass: black calendar background, white top icons, current-day red circle readable.
  - Add Event overlay pass: full-screen editor opens from `+`, X/check top bar visible, all-day switch size looks smaller.
  - Add Event all-day checked pass: red track with white thumb remains correct after switch sizing change.
- Add/Edit Event recurrence/reminder/delete:
  - Editor now shows `Repeat` choices: None, Daily, Weekly, Monthly.
  - Repeat saves to existing `calendar_events.rrule`; no schema/table/column changes.
  - Edit Event now preselects the saved reminder from existing `event_reminders`.
  - Edit Event now shows a `Delete event` action that deletes the event and its reminders.
- Verified debug build succeeds with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed latest debug APK on connected phone `4ab0d020`; package is `com.dotfield.dotcal`, launcher label is sourced from `@string/app_name` = `DotCal`.
- Verified debug build succeeds after Add/Edit expansion with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after dark/settings polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after settings arrow alignment fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after Add/Edit full-screen overlay with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after Add/Edit all-day switch sizing with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified visual QA screenshots on connected phone `4ab0d020` after Add/Edit all-day switch sizing.
- Verified debug build succeeds after Add/Edit recurrence/reminder/delete with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.

Known gap:
- `calendar_events` CHECK (`endTimeMs >= startTimeMs`) is not enforced by Room annotation yet. Repository validates new local events by construction. Later add custom open helper/migration if strict SQLite CHECK required from v1.
- No visual QA screenshot pass yet; validate on connected phone.
- Recurrence currently stores `FREQ=DAILY/WEEKLY/MONTHLY` in `rrule`, but calendar views do not expand recurring instances yet.
- Add/Edit recurrence/reminder/delete is ready for manual QA; user will test manually.

## Next Step

Next should continue calendar polish:
- Polish Add/Edit Event UX: date controls, recurring instance expansion, and manual QA fixes.
- Use stored week start preference later instead of hardcoded Sunday, if user wants configurable week start.

## What To Test Now

Build:
```powershell
.\gradlew.bat :app:assembleDebug
```

Debug APK:
```text
app\build\outputs\apk\debug\app-debug.apk
```

Manual:
- Launch app.
- Confirm installed package is `com.dotfield.dotcal` and app label is `DotCal`.
- Confirm no old `com.dotfield.ncalendar` app remains on the phone.
- Month tab opens first.
- Swipe left/right changes month.
- Tap date: bottom sheet opens.
- Tap `+`, submit empty: `Title required`.
- Tap `+`: Add Event should slide in full-screen from right like Settings, not open as a bottom sheet.
- Add/Edit top bar should show close `X` on left and save check on right.
- Add title, location, description, start/end time, reminder, save: dot appears on that date and row appears in bottom sheet.
- Tap an existing event row from date sheet: Edit Event opens with title/location/description/time/all-day values.
- Edit title/time and save: existing row updates without creating a duplicate.
- Enter invalid time or end before start: validation shows `Use HH:mm and end after start`.
- Toggle all-day and save: event appears in all-day area for Week/Day.
- All-day switch checked state should be red track with white thumb, not fully red.
- Top three-dot menu opens Settings and Tasks.
- From Settings or Tasks, tapping the calendar icon and choosing any view returns to Calendar.
- Settings > Theme opens nested picker.
- Theme picker can switch Light/Dark/System.
- Settings root should feel cleaner: sectioned rows, no inline theme buttons.
- Week tab shows date range, day strip, 24-hour grid, and current-time marker.
- Calendar top-right `+` opens Add Event.
- Calendar top-right calendar icon opens view picker.
- Selecting Year/Month/Week/Day/Agenda switches active view.
- UI should no longer show heavy borders around most elements.
- No dot pattern should be visible in background.
- Month view label should be left aligned as `YYYY/M`.
- Month view should not show prev/next arrow buttons.
- Top bar should show icon-only `+`, not a filled button.
- Day view should show timeline + tasks section.
- Month/Week/Day/Agenda headers should all use `YYYY/M` format.
- Month/Week/Day headers should be left aligned and should not show arrow buttons.
- Agenda should group events by date and show `NO EVENTS` when empty.
- Week swipe left/right should change week.
- View picker should not show Three-day option.
- View picker order should be Year, Month, Week, Day, Agenda.
- View picker rows should show labels only, no option icons.
- Dropdown selected row should use red icon/text/check, not blue.
- Top action bar should show only selected view icon, not view text.
- Top selected-view icon should be red.
- In Dark theme, top-right `+`, selected calendar view icon, and overflow icon should be white.
- In Dark theme, calendar view dropdown, overflow dropdown, and theme dropdown should use the same near-black background as the reference.
- Dropdown selected rows should not use red text or red background.
- Calendar screen should follow selected theme, not forced white.
- In Light theme Month view, top bar/header/week row/day grid should be white, with remaining background warm light gray.
- In Light theme Week/Day/Three-day/Year/Agenda, visible calendar/list surfaces should be white.
- In Dark theme Month view, top/calendar/day-grid background should be black.
- View dropdown should be white in Light theme and dark/black in Dark theme.
- View dropdown should have square corners, not rounded corners.
- View dropdown should not show Three-day option.
- Switching dropdown views should feel immediate.
- Settings screen should be white in Light theme.
- Three-dot > Settings should slide a full-screen Settings page in from right to left.
- Settings top-left back arrow should slide Settings out from left to right and restore the previous screen.
- Open Settings from Calendar, back should return to Calendar; open Settings after Tasks, back should return to Tasks.
- Settings should match attached reference structure: Calendar title, Accounts/General/Reminders/Additional groups, toggles, chevrons.
- Settings top should show back arrow above large `Calendar`; after scrolling, sticky compact top bar should show same arrow and centered `Calendar`.
- Settings large header back arrow should align horizontally with option labels below.
- Dropdown labels should be normal weight.
- Text should no longer look monospace.
- Bottom nav should not be visible.
- Month view should not show other-month day numbers.
- Week/Day hour grids should show visible block cells even when background is white.
- Week/Day grid background must remain white; only thin grid lines should be visible.
- Week view date labels must align with grid columns after the time gutter.
- Settings > Additional > Theme should open a dropdown with only Light/Dark/System and no icons.
- Theme dropdown row should show stacked up/down chevrons like reference, not a `v`.
- Year option should show 12 mini month calendars, 3 per row; header should show only year and swipe should change year.
- In Year view, current month/day should be red only for the real current year/month/day.
- Year mini-calendar day numbers should not be cut off at bottom.
- Current-day red circle should be centered.
- Month/Week/Year mini calendars should start from Sunday.
- In Year mini calendars, weekday dates should be bold and weekend dates normal.
- Select a theme, close/reopen app; selected theme should persist.
- Select a calendar view, close/reopen app; selected view should persist.
- Add an event in Month, then open Week; event should appear in its start-hour cell.
- Tap empty Week hour cell; Add Event sheet opens for that date.
- Tap a Week hour like `14:00`; Add Event sheet should show that date and `14:00`.
- Save from tapped Week hour; event should appear in that hour, not `09:00`.
- Add/seed overlapping Week events; overlapping blocks should appear side-by-side, not stacked on top of each other.
- Add/seed a 30-minute and multi-hour event; Week block height should reflect duration.
- Month cells stay readable; current day red fill still clear.

## Resume Prompt For New Chat

Use caveman-ultra and `$android-development`. Work in `D:\Caveman\caveman\Nothing-Calendar`. Read `Docs/HANDOFF.md` first. Continue DotCal (`com.dotfield.dotcal`). Preserve schema columns/current UI rules. Keep HANDOFF updated after each completed step. Recent: dark dropdowns near-black/no red selected state; dark top icons white; Settings large/sticky headers done; Add/Edit Event is full-screen overlay with X/check top bar and red/white all-day switch. Next: visual QA, then Add/Edit recurrence/reminder-prefill/delete.
