# DotCal Handoff

Updated: 2026-06-18

## Product Target

Build DotCal (`com.dotfield.dotcal`) as a premium Android calendar. Quality bar: Proton Calendar + Fantastical. Visual system: black/white/red, mono typography, clean high-contrast surfaces, no brand references to companies not owned by the app.

## Source Prompt

Prompt came from `C:\Users\Admin\.codex\attachments\61ea4d34-0342-4103-9c6a-4302906da194\pasted-text.txt`.
Latest continuation prompt came from `C:\Users\Admin\.codex\attachments\7cb21fbc-f8f6-4294-a05e-8d242938d821\pasted-text.txt`.

Key requirements:
- Every future session working on this repo must use `$android-development`.
- Manual QA rule: do not run phone/manual UI testing unless the user explicitly asks. Build/install only when needed; otherwise update `What To Test Now` so the user can test manually.
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
- Recurring event expansion:
  - Month-scoped event loading now includes recurring master events that started before the visible month ends.
  - Daily, Weekly, and Monthly `rrule` values expand into visible display instances for Month, Week, Day, Agenda, and Year event markers.
  - Display instances use generated occurrence ids for Compose keys/layout while save/delete/reminder lookup routes back to the original event id.
  - No schema/table/column changes.
- Add/Edit Event picker polish:
  - Start and end time are no longer manually typed text fields.
  - Start and end time now show default-filled rows that open tap-to-pick bottom sheets.
  - Start and end now include dates; users can pick both date and time without typing.
  - Start and end now use one row each (`Starts`, `Ends`) instead of separate date/time labels.
  - Timed start/end rows display like `Wed, 17 Jun, 2026 9:00 pm`.
  - Start/end picker is a bottom sheet with scrollable date/hour/minute columns plus Cancel/OK actions.
  - All-day mode shows date-only start/end rows.
  - Timed events can end on a later date for overnight/multi-day events.
  - Reminder and Repeat changed from red-accent chip rows to Settings-style value rows that open neutral bottom-sheet pickers.
  - Selected picker rows use neutral text/check styling with no red background.
  - Delete event action no longer uses a red background.
- Verified debug build succeeds with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed latest debug APK on connected phone `4ab0d020`; package is `com.dotfield.dotcal`, launcher label is sourced from `@string/app_name` = `DotCal`.
- Verified debug build succeeds after Add/Edit expansion with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after dark/settings polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after settings arrow alignment fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after Add/Edit full-screen overlay with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after Add/Edit all-day switch sizing with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified visual QA screenshots on connected phone `4ab0d020` after Add/Edit all-day switch sizing.
- Verified debug build succeeds after Add/Edit recurrence/reminder/delete with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after recurring event expansion with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Verified debug build succeeds after Add/Edit picker polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit picker polish with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after Add/Edit date+time picker polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit date+time picker polish with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after single-row start/end wheel picker with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after single-row start/end wheel picker with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after Add/Edit picker center-wheel polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit picker center-wheel polish with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after Add/Edit picker font/cancel-button QA fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit picker font/cancel-button QA fix with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after Add/Edit picker cancel color/all-day correction with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit picker cancel color/all-day correction with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after Add/Edit picker dark dialog background fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit picker dark dialog background fix with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Verified debug build succeeds after Add/Edit picker cancel/circular time wheel fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after Add/Edit picker cancel/circular time wheel fix with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Add/Edit start/end date-time picker wheel polish:
  - Picker now renders exactly three visible rows per column: previous, centered selected, next.
  - Scrolling a date/hour/minute column snaps the nearest item into the center row.
  - Centered item becomes the selected value and uses larger semi-bold text.
  - Selected wheel text no longer uses blue; Dark theme uses white selected text, Light theme uses primary text for readability.
- Add/Edit picker manual QA fix:
  - Center selected wheel font reduced slightly from previous oversized size.
  - All-day picker behavior confirmed: All-day mode shows date-only picker, timed mode shows date/hour/minute.
  - Date/time picker dialog background follows the latest theme color codes.
  - Cancel and OK buttons follow the latest theme color codes.
  - Hour and minute picker columns are circular: after `23` comes `00`, and after `59` comes `00`; date column remains bounded.
- Theme color code alignment:
  - Dark theme uses Screen `#000000`, Dialog `#1E1E1E`, Cancel `#121212`, OK/accent `#FF3B30`, PrimaryText `#FFFFFF`, Secondary `#B3B3B3`, Disabled `#6E6E6E`.
  - Light theme uses Screen `#F7F7F7`, Dialog `#FFFFFF`, Cancel `#EFEFEF`, OK/accent `#FF3B30`, PrimaryText `#101010`, Secondary `#6B6B6B`, Disabled `#BDBDBD`.
  - Date/time, reminder, repeat, event-list sheets now use the dialog surface.
  - Date/time picker Cancel uses the cancel surface; OK uses `#FF3B30`.
- Verified debug build succeeds after theme color code alignment with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Installed debug APK after theme color code alignment with `adb install -r app\build\outputs\apk\debug\app-debug.apk`.
- Back gesture handling:
  - System back/gesture now closes Add/Edit overlay instead of exiting the app.
  - System back/gesture now returns Settings Theme detail to Settings root.
  - System back/gesture now closes Settings root back to the previous Calendar/Tasks screen.
  - System back/gesture now returns Tasks to Calendar instead of exiting the app.
  - Settings header back button uses the same behavior as system back.
- Verified debug build succeeds after back gesture handling with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Back gesture slide-out hardening:
  - Add/Edit screen now owns its own `BackHandler`, so a second back gesture during the slide-out animation is consumed and cannot exit the app.
  - Settings overlay now owns its own `BackHandler`, so Theme/root back behavior stays stable while the overlay is visible or animating.
- Verified debug build succeeds after back gesture slide-out hardening with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Add/Edit back gesture priority fix:
  - Add/Edit overlay now registers a dedicated highest-priority `BackHandler` after the animated overlay in composition.
  - Removed the editor-local `BackHandler` so one owner closes `addSheet` state.
  - This targets the manual repro where Android back gesture visually slides Add Event but leaves Add Event open, then the next back exits the app.
- Verified debug build succeeds after Add/Edit back gesture priority fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Theme startup flash fix:
  - Theme preference collection now starts as `null` instead of `System`, so the app no longer renders Light/System before stored Dark is loaded.
  - Added a black/dark boot palette used only until DataStore emits the saved theme.
  - This targets the manual repro where reopening in saved Dark briefly shows Light, then switches back to Dark.
- Verified debug build succeeds after theme startup flash fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Native launch flash hardening:
  - `Theme.DotCal` now sets native window background to black before Compose starts.
  - Native status/nav bars remain black and force-dark is disabled.
  - Added API 31 splash background/icon background as black to avoid a white splash frame before the Compose boot palette.
- Calendar header cleanup:
  - Removed secondary header labels below the main date label (`Local / Device calendar`, `Week`, `Day`, `Agenda`, `3 Days`, `Year`).
  - Increased main header label size across Month, Week, Day, Agenda, Three-day, and Year views.
- Verified debug build succeeds after native launch flash/header cleanup with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Theme startup flash final fix:
  - Added Light/Dark native theme variants: `Theme.DotCal.Light` and `Theme.DotCal.Dark`.
  - `MainActivity` reads saved `KEY_THEME_MODE` synchronously once before `super.onCreate`, mirrors it into `SharedPreferences`, and applies the matching native theme before Compose starts.
  - Compose theme collection starts from the mirrored boot theme, so saved Light no longer shows a black/dark content flash and saved Dark no longer shows a Light flash.
  - Theme changes immediately update the boot mirror before the DataStore write finishes.
- Verified debug build succeeds after final theme startup flash fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Theme startup flash race fix:
  - Removed blocking DataStore read before `setTheme`; it was delaying theme application and letting the manifest dark window show first.
  - `MainActivity` now applies the mirrored boot theme immediately, then syncs the mirror from DataStore after `super.onCreate`.
  - This targets the manual repro where saved Light still showed a black flash for about a second before switching to Light.
- Verified debug build succeeds after theme startup flash race fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Add/Edit recurring-series UX polish:
  - Editing a recurring event or generated recurring occurrence now shows `Changes apply to the whole series`.
  - Delete action changes from `Delete event` to `Delete series` for recurring events/occurrences.
  - Start/End/Reminder/Repeat row values now ellipsize instead of overflowing on narrow screens.
- Verified debug build succeeds after recurring-series Add/Edit polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Calendar view segmented control:
  - Removed the top calendar icon/dropdown view picker.
  - Added a pill segmented control between the top action bar and the calendar date/year header.
  - Segments are `Year`, `Month`, `Week`, `Day`, and `Agenda`; `Three-day` remains hidden and old stored values still map back to Month.
  - Selected segment uses a filled rounded chip; inactive segments use normal secondary text.
- Verified debug build succeeds after calendar segmented-control change with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Calendar segmented-control sizing polish:
  - Reduced segmented-control vertical height and padding so it no longer feels oversized.
  - Balanced first/last segment weights so `Year` left spacing and `Agenda` right spacing read more evenly.
- Verified debug build succeeds after segmented-control sizing polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Bottom navigation restore:
  - Removed the top three-dot overflow menu.
  - Added bottom navigation with `Calendar`, `Tasks`, and `Settings` tabs matching the dark rounded reference style.
  - Bottom nav uses custom calendar/check/settings icons; active tab uses red icon/text, inactive tabs use secondary gray.
  - `Settings` now opens from bottom nav as the same full-screen overlay; `Tasks` opens from bottom nav.
  - Calendar segmented control now uses `SpaceBetween` label layout so `Year` left text gap and `Agenda` right text gap are equal.
- Verified debug build succeeds after bottom navigation restore and segment gap fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Year view switch performance:
  - Replaced per-day mini-calendar Compose nodes in Year view with one Canvas-rendered mini grid per month.
  - This reduces the Year view from hundreds of tiny Text/Box composables to 12 month cells with lightweight canvas drawing.
  - Segmented control uses fixed-width labels so `Year` and `Agenda` edge gaps remain equal.
- Verified debug build succeeds after Year view Canvas optimization with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Light theme top/bottom nav color polish:
  - Light segmented control now uses white/off-white surface, `#E5E5E5` border, and `#EEEEEE` selected chip like the reference.
  - Light segmented inactive labels use dark neutral text instead of the prior softer gray.
  - Light bottom nav now uses white/off-white surface, `#E8E8E8` top border, red active icon/text, and `#5F6368` inactive icon/text.
- Verified debug build succeeds after light top/bottom nav color polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Segmented-control label clipping fix:
  - Removed the fixed-width label box that caused `Agenda` to render as `Agen...`.
  - Segment labels now wrap their content while `SpaceBetween` keeps the first and last text-edge gaps balanced.
- Verified debug build succeeds after segmented-control label clipping fix with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Light nav color/header placement polish:
  - Light top segmented control, top add bar, and bottom nav now use the same off-white nav surface (`#FAFAFA`) with light gray borders and selected chip color closer to the provided reference.
  - Note: this initially moved the segmented Year/Month/Week/Day/Agenda control too high; corrected later so only the date/year label moves to the top bar.
  - No schema/table/column changes.
- Verified debug build succeeds after light nav color/header placement polish with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Exact theme code and bottom-nav reference alignment:
  - Theme palette now follows the provided image codes: Dark Screen `#000000`, Dialog `#1E1E1E`, Cancel `#121212`, OK `#FF3B30`, Primary `#FFFFFF`, Secondary `#B3B3B3`, Disabled `#6E6E6E`; Light Screen `#F7F7F7`, Dialog `#FFFFFF`, Cancel `#EFEFEF`, OK `#FF3B30`, Primary `#101010`, Secondary `#6B6B6B`, Disabled `#BDBDBD`.
  - Light calendar/nav surfaces now use Screen `#F7F7F7` instead of the prior custom off-white nav color.
  - Segmented selected chip now uses Dialog `#1E1E1E` in Dark and Cancel `#EFEFEF` in Light.
  - Bottom nav now matches the image more closely: flat full-width bar, no rounded top/shadow, screen-color background, top divider, smaller icons/labels, red active item, secondary inactive items.
  - No schema/table/column changes.
- Verified debug build succeeds after exact theme/bottom-nav alignment with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Date/year header placement correction:
  - Moved the date/year label (`2026/6` or year-only in Year view) into the top action bar beside the add button.
  - Restored the segmented Year/Month/Week/Day/Agenda control below the top action bar.
  - Removed duplicate per-view date/year header rows from Month, Week, Day, Agenda, Three-day, and Year views.
  - No schema/table/column changes.
- Verified debug build succeeds after date/year header placement correction with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- Recurring event per-instance edit/delete:
  - Recurring occurrence editor now has an `Apply to` picker with `This event` and `Whole series`.
  - `This event` edit excludes the original occurrence via existing `calendar_events.exceptionDates` and creates a detached single local event with no `rrule`.
  - `This event` delete excludes only that occurrence via existing `calendar_events.exceptionDates`.
  - `Whole series` keeps existing master-series edit/delete behavior.
  - No schema/table/column changes; still exactly 5 Room tables.
- Verified debug build succeeds after recurring per-instance edit/delete with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.

Known gap:
- `calendar_events` CHECK (`endTimeMs >= startTimeMs`) is not enforced by Room annotation yet. Repository validates new local events by construction. Later add custom open helper/migration if strict SQLite CHECK required from v1.
- Phone manual QA is user-owned unless explicitly requested.
- Add/Edit recurrence/reminder/delete and recurring event expansion are ready for manual QA; user will test manually.
- Recurring detached occurrences are local-only and are not synced as Google exception instances yet.
- Current manual test list still contains stale references to older overflow/three-dot navigation. Current UI rule is bottom navigation for `Calendar`, `Tasks`, and `Settings`; no top three-dot overflow should be visible.

## Continuation Roadmap

Source: latest continuation prompt at `C:\Users\Admin\.codex\attachments\7cb21fbc-f8f6-4294-a05e-8d242938d821\pasted-text.txt`.

Strict rules:
- Do not touch already-working code unless a new feature requires it.
- Do not change Room schema: keep exactly 5 tables only: `calendar_accounts`, `calendar_events`, `event_reminders`, `sync_metadata`, `deleted_event_log`.
- Do not add columns to any table.
- Do not change package name `com.dotfield.dotcal`, deep link scheme `dotcal://`, or DB filename `dotcal.db`.
- Do not run phone/manual UI testing unless explicitly requested; update `What To Test Now` instead.
- After every completed implementation step, update this file.
- Build after every implementation step with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`; fix failures before moving on.

Build order:
1. Event Detail Screen
   - Full-screen `EventDetailScreen`, not a sheet.
   - Event taps from Month sheet, Week, Day, and Agenda navigate to details.
   - Top bar: back arrow, centered `EVENT DETAILS`, edit pencil.
   - Show accent strip, title, date/time or all-day chip, recurrence, reminders, location, calendar account, selectable description, images, and voice note.
   - Add deep link route `dotcal://event/{eventId}`.
   - Birthday/read-only events must hide edit in later birthday flow.
2. Image Attachments
   - Use existing `calendar_events.imageUris` JSON array column only.
   - Use Android Photo Picker via `ActivityResultContracts.PickMultipleVisualMedia`; no `READ_MEDIA_IMAGES`.
   - Max 5 images; persist URI read permission; show thumbnails and remove overlay.
3. Voice Notes
   - Use existing `calendar_events.voiceNotePath` column only.
   - Request `RECORD_AUDIO` at runtime on first mic tap; if denied, hide mic button with no blocking error.
   - Record AAC/MPEG_4 to `context.filesDir/voice_notes/{eventId}.m4a`, max 5 minutes.
   - Support playback, pause/resume, duration, and delete.
4. AlarmManager Reminders
   - Use existing `event_reminders` table only.
   - On event save, replace reminder rows and schedule future alarms.
   - Use exact alarms when allowed; fallback to `setWindow()` with 5-minute window on Android 12+ when exact alarms denied.
   - Notification channel `dotcal_reminders`; actions: `VIEW` deep link and `SNOOZE 10 MIN`.
   - Boot receiver reschedules future undelivered reminders.
5. Google Calendar Sync
   - Use CalendarProvider API only; no REST, OAuth, or network.
   - Use existing `sync_metadata` and `deleted_event_log`.
   - Local-only mode when `READ_CALENDAR` denied.
   - WorkManager periodic sync controlled by DataStore sync settings.
   - Settings rows: Sync Now, Last synced, Sync enabled, Sync interval.
6. Tasks Tab Complete
   - Store tasks in `calendar_events` where `isTask = 1`.
   - Filters: All, Today, Upcoming, Completed.
   - Group tasks by date; swipe left delete, swipe right complete.
   - Add Task bottom sheet saves task-only rows with optional date/time/reminder, no repeat/images/voice.
7. Birthday Calendar
   - Import birthdays from ContactsProvider into existing `calendar_events` as `source = BIRTHDAY`.
   - Create/use Birthdays account in existing `calendar_accounts`.
   - Settings toggle controlled by `KEY_BIRTHDAY_ENABLED`.
   - Birthday events are read-only and default to 1-day reminder.
8. Home Screen Widgets
   - Use Jetpack Glance only, not legacy `AppWidgetProvider`/`RemoteViews`.
   - Build small 2x2, medium 4x2, and large 4x4 widgets.
   - Add `WidgetDataRepository`, `WidgetUpdateWorker`, and `DotCalGlanceTheme`.
   - Update widgets after event save/delete and sync completion.
9. Onboarding Flow
   - Show once based on `KEY_ONBOARDING_DONE`.
   - 5-page pager: Welcome, Calendar permission, Notifications, Contacts, Ready.
   - App must degrade gracefully when permissions are skipped or denied.
10. Settings Missing Items
   - Calendars: Sync Now, Last synced.
   - Reminders: Default reminder picker saved to `KEY_DEFAULT_REMINDER`.
   - Additional: Birthday calendar, Sync enabled, Sync interval.
   - About: Privacy Policy WebView, Rate DotCal Play Store link, Version from `BuildConfig.VERSION_NAME`.
11. Release Rules After Steps
   - Add R8/ProGuard rules for Room, Hilt, Kotlin Serialization, Glance, and Coroutines after all roadmap steps are complete.

## Detailed Continuation Feature Spec

Source: latest continuation prompt. This section preserves the detailed future-feature requirements so future sessions stay in sync. Existing app behavior/UI remains source of truth when any item conflicts.

### Step 1: Event Detail Screen

Create a full-screen `EventDetailScreen`, not a sheet.

Requested navigation:
- Add deep link route `dotcal://event/{eventId}`.
- Back arrow returns to previous screen.
- Spec requests event row taps across Month sheet, Week, Day, and Agenda navigate to `EventDetailScreen`; if this conflicts with existing working edit behavior, preserve current app behavior unless user explicitly approves the navigation change.

Layout, top to bottom:
- Top bar:
  - Back arrow on left.
  - `EVENT DETAILS` centered title.
  - Pencil edit icon on right opens Add/Edit Event for this event.
- Color accent strip:
  - Full width.
  - 4dp height.
  - Color = `event.colorHex`; fallback account color; fallback `#FF0000`.
- Title:
  - `event.title`.
  - 24sp.
  - Primary text color.
- Date/time row:
  - Format `MON, 14 JAN 2026 - 09:00 - 10:00`.
  - If all-day, show `ALL DAY EVENT` chip instead of times.
- Recurrence row, only if `rrule` is not null:
  - `DAILY` -> `REPEATS DAILY`.
  - `WEEKLY` -> `REPEATS WEEKLY`.
  - `MONTHLY` -> `REPEATS MONTHLY`.
- Reminders section, only if reminders exist:
  - Query `event_reminders` for this `eventId`.
  - Each reminder text: `10 MINUTES BEFORE`.
- Location row, only if location is not empty:
  - Map pin icon.
  - Location text.
- Calendar account row:
  - Colored dot.
  - Account `displayName` from `calendar_accounts`.
- Description, only if not empty:
  - Full text.
  - Selectable.
- Images section, only if `imageUris` is not empty:
  - Horizontal thumbnail row.
  - Tap thumbnail -> full-screen zoomable image viewer.
  - Use Coil `AsyncImage`.
- Voice note section, only if `voiceNotePath` is not null:
  - Play/pause button.
  - Duration text.
  - Use `MediaPlayer`.

### Step 2: Image Attachments On Events

Add image attachment support to Add/Edit Event using existing `calendar_events.imageUris` JSON array column. No schema changes.

Implementation:
- Use `ActivityResultContracts.PickMultipleVisualMedia` Android Photo Picker.
- Do not request `READ_MEDIA_IMAGES`.
- Max 5 images per event.
- Store selected URIs as JSON array in `imageUris`.
- Take persistable URI permission:

```kotlin
context.contentResolver.takePersistableUriPermission(
    uri,
    Intent.FLAG_GRANT_READ_URI_PERMISSION
)
```

Add/Edit Event UI:
- Add `IMAGES` section below Description field.
- Show horizontal thumbnail row with Coil `AsyncImage`.
- Show `+ ADD IMAGE` chip when image count is below 5.
- Show `3/5` count badge when images exist.
- Tap thumbnail -> show remove `X` overlay.
- Tap `X` -> remove URI from list.

Save behavior:
- Serialize `imageUris` list to JSON string before saving to Room.
- Deserialize on load.

### Step 3: Voice Notes On Events

Add voice note support to Add/Edit Event using existing `calendar_events.voiceNotePath`. No schema changes.

Permission:
- `RECORD_AUDIO` already exists in manifest.
- Request at runtime on first mic tap.
- If denied, hide mic button and show no blocking error.

Recording:
- Use `MediaRecorder`.
- Output format: `MPEG_4`.
- Audio encoder: `AAC`.
- Save to `context.filesDir/voice_notes/{eventId}.m4a`.
- Max duration: 5 minutes, 300 seconds.
- Auto-stop at 5 minutes.

Add/Edit Event `VOICE NOTE` row below Images:
- State A, no recording:
  - Mic icon.
  - `TAP TO RECORD`.
  - Tap -> request `RECORD_AUDIO` if needed -> start recording.
- State B, recording:
  - Red pulsing dot.
  - Timer like `0:23`.
  - `STOP` button stops recording and saves file.
- State C, recording exists:
  - Play/pause button.
  - Duration text like `0:45`.
  - Delete `X` deletes file and clears `voiceNotePath`.

Playback:
- Use `MediaPlayer`.
- Show current position while playing.
- Pause/resume on button tap.

### Step 4: AlarmManager Reminders

Implement proper reminder scheduling with `AlarmManager` using existing `event_reminders`. No schema changes.

When event is saved:
1. Delete existing reminders for the event from `event_reminders`.
2. Insert new `EventReminder` rows.
3. Compute `triggerAtMs = startTimeMs - minutesBefore * 60_000`.
4. Skip triggers in the past.
5. Schedule `AlarmManager` for each future reminder.
6. Use unique `alarmRequestCode = (eventId.hashCode() * 31 + minutesBefore)`.

Android 12+ exact alarm behavior:
- If `Build.VERSION.SDK_INT >= 31`, check `AlarmManager.canScheduleExactAlarms()`.
- If exact alarms are denied, fallback to `setWindow()` with 5-minute window.
- Save exact-alarm-denied state to DataStore key `KEY_EXACT_ALARM_DENIED` if that key exists or is added without schema impact.
- Otherwise use `setExactAndAllowWhileIdle`.

Notification:
- Channel ID: `dotcal_reminders`.
- Channel name: `Event Reminders`.
- Importance: `IMPORTANCE_HIGH`.
- Create channel on app startup in `DotCalApplication`.
- Title: event title.
- Body:
  - `In X minutes` if no location.
  - `In X minutes - {location}` if location exists.
- Actions:
  - `VIEW` -> `PendingIntent` -> `dotcal://event/{eventId}`.
  - `SNOOZE 10 MIN` -> reschedule alarm 10 minutes later.
- After delivery, update `event_reminders.isDelivered = 1` for that reminder id.

Boot behavior:
- `BOOT_COMPLETED` receiver is already declared.
- On boot, query all `EventReminder` where `triggerAtMs > now` and `isDelivered = 0`.
- Reschedule all via `AlarmManager`.
- Run in `goAsync` coroutine.

### Step 5: Google Calendar Sync

Implement Google Calendar sync via CalendarProvider API only. No REST API, OAuth, network calls, schema changes, or new Room tables. Use existing `sync_metadata` and `deleted_event_log`.

`CalendarProviderDataSource` in `data/provider/`:
- `getDeviceCalendars(): List<CalendarAccountEntity>`
  - Query `CalendarContract.Calendars`.
  - Map to `CalendarAccountEntity` with `accountType = GOOGLE` or `DEVICE`.
  - Check `READ_CALENDAR` before querying.
  - Return empty list if permission denied or cursor null.
- `getEventsInRange(calendarId: Long, startMs: Long, endMs: Long): List<CalendarEventEntity>`
  - Query `CalendarContract.Events`.
  - Range: next 60 days from now.
  - Map all available columns to `CalendarEventEntity` with `source = GOOGLE`.
  - Handle null cursor gracefully.

`CalendarSyncRepository.sync()` logic:
1. Check `READ_CALENDAR`; return early if denied.
2. Query device calendars and upsert into `calendar_accounts`.
3. For each Google calendar:
   - Query provider events for next 60 days.
   - Query Room for existing `GOOGLE` source events in same range.
   - Build maps keyed by `googleEventId`.
   - Compute inserts, updates where `syncVersion` differs, and deletes.
   - Check inserts against `deleted_event_log`; skip tombstoned events.
   - Execute insert/update/delete in one transaction.
4. Clean `deleted_event_log` rows older than 30 days.
5. Update `sync_metadata` with `lastSyncMs`, `lastSyncStatus`, and counts.

`CalendarSyncWorker`:
- `CoroutineWorker` with Hilt injection if Hilt is already used; otherwise follow existing DI pattern.
- `doWork()` calls sync repository.
- On `SecurityException`, return `Result.failure()`.
- On other exception, retry up to 3 times then fail.
- Periodic work interval comes from `KEY_SYNC_INTERVAL_MINS`, default 15 minutes.
- Use `ExistingPeriodicWorkPolicy.KEEP`.
- Schedule on app startup when `KEY_SYNC_ENABLED = true`.

Permission handling:
- If `READ_CALENDAR` denied, show non-blocking Calendar banner: `Running in local mode - grant calendar access in Settings`.
- App remains fully usable in local-only mode.

Settings integration:
- `SYNC NOW` row enqueues one-time sync.
- Last synced text reads from `sync_metadata`, like `LAST SYNCED 5 MIN AGO`.
- Sync enabled toggle cancels/schedules WorkManager.
- Sync interval picker reschedules WorkManager.

### Step 6: Tasks Tab Complete

Replace placeholder Tasks screen with full implementation. Store tasks in existing `calendar_events` where `isTask = 1`. No schema changes.

Layout:
- Horizontal, no-wrap filter chip row:
  - `ALL`, `TODAY`, `UPCOMING`, `COMPLETED`.
  - Active chip: `#FF3B30` background and white text.
  - Inactive chip: transparent, 1dp border, secondary text.
  - Row scrolls horizontally if needed.
- `LazyColumn` grouped by date:
  - Date header: uppercase format like `MON, 14 JAN`.
  - Task row:
    - Square checkbox with 1dp border.
    - Title.
    - Completed title uses strikethrough and secondary color.
    - Optional time uses secondary color, 12sp.
    - Swipe left -> red background + trash icon -> delete.
    - Swipe right -> green background + check icon -> mark complete.
    - Use `SwipeToDismiss` or `AnchoredDraggable`.
- Empty states:
  - All: `NO TASKS YET` + `TAP + TO ADD ONE`.
  - Today: `NOTHING DUE TODAY`.
  - Upcoming: `ALL CLEAR`.
  - Completed: `NO COMPLETED TASKS`.
- FAB `+` opens Add Task `ModalBottomSheet`:
  - Required title field.
  - Date row opens date picker.
  - Optional time row opens time picker.
  - One optional reminder: None, 5m, 10m, 30m, 1 day.
  - `SAVE TASK` button.
  - Save with `isTask = true`, `isCompleted = false`.
  - No repeat field.
  - No calendar selector; use primary local account.
  - No voice note.
  - No images.

DAO queries needed:
- All: `WHERE isTask = 1`.
- Today: `WHERE isTask = 1 AND startTimeMs BETWEEN dayStart AND dayEnd`.
- Upcoming: `WHERE isTask = 1 AND isCompleted = 0 AND startTimeMs > now`.
- Completed: `WHERE isTask = 1 AND isCompleted = 1`.

### Step 7: Birthday Calendar

Import birthdays from device contacts as yearly recurring events using existing `calendar_events` with `source = BIRTHDAY`. No schema changes.

`ContactsProviderDataSource` in `data/provider/`:
- `getBirthdays(): List<CalendarEventEntity>`.
- Query `ContactsContract.Data` where:
  - `mimetype = CommonDataKinds.Event.CONTENT_ITEM_TYPE`.
  - `type = CommonDataKinds.Event.TYPE_BIRTHDAY`.
- For each birthday, create event:
  - Title: `{ContactName}'s Birthday`.
  - `isAllDay = 1`.
  - `rrule = FREQ=YEARLY`.
  - `source = BIRTHDAY`.
  - `isTask = 0`.
  - `colorHex = #FF3B30`.
  - `accountId = birthday calendar account id`.
- Check `READ_CONTACTS` before querying.
- Return empty list if denied.

Birthday calendar account:
- Create/use one `CalendarAccountEntity`:
  - `displayName = Birthdays`.
  - `accountType = DEVICE`.
  - `color = #FF3B30`.
  - `isVisible = 1`.

Settings integration:
- Birthday calendar toggle uses `KEY_BIRTHDAY_ENABLED`.
- When turned on:
  - Request `READ_CONTACTS` if not granted.
  - If granted, import birthdays and show count like `47 BIRTHDAYS IMPORTED`.
  - If denied, show rationale and keep toggle off.
- When turned off:
  - Delete all `BIRTHDAY` source events from `calendar_events`.
  - Show `BIRTHDAY CALENDAR DISABLED`.
- Re-import on launch if birthday calendar is enabled and contacts permission is granted.

Birthday behavior:
- Read-only: no edit, no delete from UI.
- Birthday tap can show Event Detail, but no edit icon in top bar.
- Default reminder: 1 day before, 1440 minutes.

### Step 8: Home Screen Widgets With Glance

Build 3 home screen widgets using Jetpack Glance 1.1+. Do not use legacy `AppWidgetProvider` or `RemoteViews`.

Dependencies, if not already present:
- `androidx.glance:glance-appwidget:1.1.0`.
- `androidx.glance:glance-material3:1.1.0`.

Small widget, 2x2, `widget/SmallCalendarWidget.kt`:
- Background: Glance theme surface, dark `#000000`.
- Day name: `MON`, 12sp, secondary.
- Date number: `14`, 40sp, primary; red `#FF3B30` if today.
- Event count: `3 EVENTS` or `NO EVENTS`, 11sp, secondary.
- Tap anywhere launches Day view: `dotcal://day/{todayMs}`.

Medium widget, 4x2, `widget/MediumCalendarWidget.kt`:
- Left column fixed 80dp:
  - Same content as small widget.
- Vertical divider: 1dp.
- Right column fills remaining:
  - Next 3 upcoming events.
  - Each row: time `09:00` 11sp secondary, 6dp colored dot, title 12sp primary one line ellipsis.
  - If no events: centered `NOTHING SCHEDULED` secondary.
- Tap event row -> `dotcal://event/{eventId}`.
- Tap left column -> `dotcal://day/{todayMs}`.

Large widget, 4x4, `widget/LargeCalendarWidget.kt`:
- Top half: mini month grid for current month:
  - 7 columns.
  - Day numbers only.
  - Today has red circle background.
  - Days with events show small red dot below number.
  - Tap launches Month view.
- Horizontal divider: 1dp.
- Bottom half: today's events list up to 5, same row style as medium.
- Tap event -> `dotcal://event/{eventId}`.

Widget data:
- Create `WidgetDataRepository`.
- `getTodayEvents()`:
  - Query non-task events for today.
  - Sort by `startTimeMs` ascending.
  - Limit 5.
- `getUpcomingEvents(limit: Int)`:
  - Query next events after now.
  - Limit by argument.

Widget updates:
- Create `WidgetUpdateWorker` with WorkManager.
- Periodic work every 15 minutes.
- Calls `GlanceAppWidgetManager.updateAll(context)`.
- Trigger immediate widget update after event create/edit/delete and after calendar sync completes.

Glance theme:
- Create `DotCalGlanceTheme`.
- Dark colors:
  - Background `#000000`.
  - Surface `#0A0A0A`.
  - Primary text `#FFFFFF`.
  - Secondary `#666666`.
  - Accent `#FF3B30`.
- Apply to all widgets.

### Step 9: Onboarding Flow

Show onboarding only once on first launch using `KEY_ONBOARDING_DONE`.

Flow:
- If false, show `OnboardingScreen` before main app.
- If true, go directly to main Calendar screen.
- Use 5-screen horizontal pager.

Screen 1, Welcome:
- `DotCal` large, bold, primary.
- `YOUR TIME. YOUR TERMS.` secondary below.
- `GET STARTED ->` red `#FF3B30` button at bottom.
- No skip.

Screen 2, Calendar Permission:
- Calendar icon, 64dp.
- Title: `Sync your calendars`.
- Body: `Connect your Google Calendar and device calendars. Your data never leaves your phone.`
- `ALLOW CALENDAR ACCESS` requests `READ_CALENDAR` and `WRITE_CALENDAR`.
- `SKIP FOR NOW` text button below.

Screen 3, Notifications:
- Bell icon, 64dp.
- Title: `Never miss an event`.
- Body: `Get reminders exactly when you need them.`
- `ALLOW NOTIFICATIONS` requests `POST_NOTIFICATIONS` on Android 13+.
- `SKIP FOR NOW` text button below.

Screen 4, Contacts:
- Person icon, 64dp.
- Title: `Birthday calendar`.
- Body: `Import birthdays from your contacts. Read-only access - we never modify your contacts.`
- `ALLOW CONTACTS` requests `READ_CONTACTS`.
- `SKIP FOR NOW` text button below.

Screen 5, Ready:
- Checkmark icon, 64dp, red `#FF3B30`.
- Title: `You're all set`.
- Body:
  - If `READ_CALENDAR` granted: `Your calendars are syncing`.
  - If denied: `Running in local mode`.
- `OPEN DOTCAL` red button:
  - Set `KEY_ONBOARDING_DONE = true`.
  - Navigate to main Calendar screen.

Progress dots:
- 5 dots at bottom of each screen.
- Active dot filled `#FF3B30`, 8dp.
- Inactive dot filled secondary, 6dp.

Permission handling:
- Prompt requested `rememberPermissionState` from Accompanist; use only if dependency already exists or adding it matches project direction.
- If permanently denied, show rationale text and no button.
- Skipping permission is valid; app degrades gracefully.

### Settings Missing Items

Add these rows to existing Settings. Do not rebuild Settings from scratch.

Calendars section:
- `SYNC NOW` row enqueues `CalendarSyncWorker`.
- `Last synced` subtitle reads from `sync_metadata`.

Reminders section:
- `Default reminder` row opens picker:
  - None.
  - 5 min.
  - 10 min.
  - 30 min.
  - 1 hour.
  - 1 day.
- Save to `KEY_DEFAULT_REMINDER`.
- Use value as preselected reminder in Add Event.

Additional section:
- `Birthday calendar` toggle -> `KEY_BIRTHDAY_ENABLED`; when turned on, request `READ_CONTACTS`.
- `Sync enabled` toggle -> `KEY_SYNC_ENABLED`; off cancels WorkManager sync, on schedules sync.
- `Sync interval` row opens picker:
  - Manual.
  - 15 min.
  - 30 min.
  - 1 hour.
- Save to `KEY_SYNC_INTERVAL_MINS`.

About section:
- `Privacy Policy` opens WebView: `https://dotfieldstudio.com/dotcal/privacy`.
- `Rate DotCal` opens Play Store: `https://play.google.com/store/apps/details?id=com.dotfield.dotcal`.
- `Version` reads from `BuildConfig.VERSION_NAME`.

### Architecture Rules For New Code

- All DB/IO operations run on `Dispatchers.IO`.
- CPU-bound recurrence expansion runs on `Dispatchers.Default`.
- ViewModels expose `StateFlow<UiState<T>>` where practical.
- `UiState` shape:
  - `Loading`.
  - `Success<T>(data)`.
  - `Error(message)`.
- Repository functions should return `Result<T>` for new feature boundaries where practical.
- Wrap exceptions and never expose raw exceptions to UI.
- Domain models should be `@Immutable` data classes.
- Prefer immutable UI collections. If project does not already use `ImmutableList<T>`, do not introduce broad dependency churn without need.
- Never hold `Context` in ViewModel.
- Never query DB on main thread.

### After All Roadmap Steps

Add R8/ProGuard rules to `proguard-rules.pro`:

```proguard
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Hilt
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-keep @kotlinx.serialization.Serializable class * { *; }

# Glance
-keep class androidx.glance.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
```

Conflict check:
- New prompt changes event-row behavior: existing event taps currently open Add/Edit directly in some surfaces; new rule routes taps to Event Detail first, with edit available from the detail pencil. Existing working app behavior wins unless user explicitly approves changing it.
- Handoff product target still mentions mono typography, but current implemented UI explicitly moved to system sans-serif. Preserve current UI rule unless user asks to revert typography.
- Older handoff source says Calendar sub-tabs Month/Week/Day/Agenda; current implemented UI and latest prompt use segmented `Year`, `Month`, `Week`, `Day`, `Agenda`. Preserve current segmented control and hidden Three-day behavior.
- Latest prompt says build after each implementation step; current user asked to update planning in one file and not change app code, so no build is required for this docs-only sync.

## Next Step

Next implementation step:
- Step 1 from Continuation Roadmap: build full-screen Event Detail Screen.
- Preserve schema columns/current UI rules and exactly 5 Room tables.
- Do not run phone/manual UI testing by default; update `What To Test Now` instead.
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
- Top three-dot menu should not be visible.
- Bottom navigation should show `Calendar`, `Tasks`, and `Settings`.
- Bottom nav active item should be red; inactive items should be gray.
- Tapping `Tasks` in bottom nav should open Tasks.
- Tapping `Settings` in bottom nav should open Settings full-screen overlay.
- From Tasks, Android back gesture should return to Calendar; Settings back should return to the previous screen.
- Settings > Theme opens nested picker.
- Theme picker can switch Light/Dark/System.
- Settings root should feel cleaner: sectioned rows, no inline theme buttons.
- Week tab shows date range, day strip, 24-hour grid, and current-time marker.
- Calendar top-right `+` opens Add Event.
- Calendar top-right should not show a calendar/view-picker icon.
- A segmented view control should appear between the top action bar and the date/year header.
- Segmented view control should show `Year`, `Month`, `Week`, `Day`, `Agenda`.
- Segmented view control should be compact vertically, not tall.
- `Year` left text gap and `Agenda` right text gap should be equal.
- In Light theme, segmented control should use Screen `#F7F7F7`, Disabled-border tint, and Cancel `#EFEFEF` selected chip like the reference.
- In Dark theme, segmented control should use Screen `#000000`, Disabled-border tint, and Dialog `#1E1E1E` selected chip.
- In Light theme, the top segmented control, top add bar, calendar surface, and bottom nav should use Screen `#F7F7F7`.
- The date/year label (`YYYY/M` or year-only in Year view) should sit in the top action bar beside the `+`.
- The segmented Year/Month/Week/Day/Agenda control should sit below the top action bar, not above it.
- `Agenda` should render fully, not as `Agen...`.
- Tapping each segment should switch active view immediately and persist selected view.
- UI should no longer show heavy borders around most elements.
- No dot pattern should be visible in background.
- Month view label should be left aligned as `YYYY/M`.
- Month view should show only one top label, no `Local / Device calendar` sublabel.
- Week/Day/Agenda/Three-day/Year should show only the primary date/year label and no secondary view-name sublabel.
- Top date/year labels should look larger than before.
- Month view should not show prev/next arrow buttons.
- Top bar should show icon-only `+`, not a filled button.
- Day view should show timeline + tasks section.
- Month/Week/Day/Agenda headers should all use `YYYY/M` format.
- Month/Week/Day headers should be left aligned and should not show arrow buttons.
- Agenda should group events by date and show `NO EVENTS` when empty.
- Week swipe left/right should change week.
- Segmented view control should not show Three-day option.
- Segment order should be Year, Month, Week, Day, Agenda.
- Selected segment should be a rounded filled chip like the reference.
- In Dark theme, top-right `+` should be white.
- In Dark theme, theme dropdown should use the same near-black background as the reference.
- Calendar screen should follow selected theme, not forced white.
- In Light theme Month view, top bar/header/week row/day grid should be white, with remaining background warm light gray.
- In Light theme Week/Day/Three-day/Year/Agenda, visible calendar/list surfaces should be white.
- In Dark theme Month view, top/calendar/day-grid background should be black.
- Segment control should sit above the primary date/year label, not inside a dropdown.
- Switching segmented views should feel immediate.
- Settings screen should be white in Light theme.
- Three-dot > Settings should slide a full-screen Settings page in from right to left.
- Settings top-left back arrow should slide Settings out from left to right and restore the previous screen.
- Open Settings from Calendar, back should return to Calendar; open Settings after Tasks, back should return to Tasks.
- Settings should match attached reference structure: Calendar title, Accounts/General/Reminders/Additional groups, toggles, chevrons.
- Settings top should show back arrow above large `Calendar`; after scrolling, sticky compact top bar should show same arrow and centered `Calendar`.
- Settings large header back arrow should align horizontally with option labels below.
- Dropdown labels should be normal weight.
- Text should no longer look monospace.
- Bottom nav should be visible and match the reference: flat full-width bar, no rounded top/shadow, top divider only.
- Bottom nav should use Screen color for the background, red active item, and Secondary color for inactive items in both themes.
- Month view should not show other-month day numbers.
- Week/Day hour grids should show visible block cells even when background is white.
- Week/Day grid background must remain white; only thin grid lines should be visible.
- Week view date labels must align with grid columns after the time gutter.
- Settings > Additional > Theme should open a dropdown with only Light/Dark/System and no icons.
- Theme dropdown row should show stacked up/down chevrons like reference, not a `v`.
- Year option should show 12 mini month calendars, 3 per row; header should show only year and swipe should change year.
- Switching from Month/Week/Day/Agenda to Year should feel immediate, without the earlier pause.
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
- Add a Daily repeating event; it should show on every date from the event start date in Month, Week, Day, and Agenda.
- Add a Weekly repeating event; it should show on the same weekday in later weeks.
- Add a Monthly repeating event; it should show on the same day number in later months, skipping months where that day does not exist.
- Tap a recurring instance; Edit should prefill title/time/repeat/reminder.
- In a recurring instance editor, `Apply to` should default to `This event`.
- For a recurring instance, choose `This event`, edit title/time, and save; only that occurrence should change and future occurrences should remain.
- For a recurring instance, choose `Whole series`, edit title/time, and save; the whole series should change.
- For a recurring instance, choose `This event`, tap `Delete event`; only that occurrence should disappear.
- For a recurring instance, choose `Whole series`, tap `Delete series`; the whole series should disappear.
- Tap `+`; Start and End should be filled by default and should open picker sheets, not keyboard/manual typing.
- Start and End should show full date + time in one row, like `Wed, 17 Jun, 2026 9:00 pm`.
- There should be no separate `Start time` or `End time` labels.
- Tap Starts/Ends; a bottom sheet should open with scrollable date/hour/minute columns and Cancel/OK.
- In Dark theme, date/time picker dialog should be `#1E1E1E`, Cancel `#121212`, OK `#FF3B30`, selected wheel text white.
- In Light theme, date/time picker dialog should be `#FFFFFF`, Cancel `#EFEFEF`, OK `#FF3B30`, selected wheel text `#101010`.
- From Add/Edit Event, Android back gesture should close editor and stay in app.
- From Add/Edit Event, swipe back twice quickly during the slide-out; app should not exit and should land on Calendar.
- From Add/Edit Event, swipe back once and wait for slide to finish; Add/Edit must not remain open.
- Repeat `+` -> back gesture five times; app must never exit and Add/Edit must close every time.
- Set theme to Dark, close app fully, reopen app; it should not flash Light before Dark appears.
- Set theme to Dark, force close from recents, reopen from launcher; native launch frame should stay black, no white/light flash.
- Set theme to Light, close app fully, reopen app; it should not flash black/dark content before Light appears.
- From Settings root, Android back gesture should close Settings and return to previous screen.
- From Settings > Theme, Android back gesture should return to Settings root, not exit app.
- From Tasks, Android back gesture should return to Calendar, not exit app.
- Toggle All-day; Start/End should show date-only rows.
- Create an overnight timed event by setting End date to tomorrow; it should save.
- Change Start; End should stay after Start automatically.
- Reminder row should open neutral picker sheet, with no red selected background.
- Repeat row should open neutral picker sheet, with no red selected background.
- Delete event button should not have a red background.
- Recurring event Edit should show `Changes apply to the whole series`.
- Recurring event Delete button should say `Delete series`.
- Long Start/End/Reminder/Repeat values should stay on one line and not overlap the chevron or title.
- Month cells stay readable; current day red fill still clear.

## Resume Prompt For New Chat

Use caveman-ultra and `$android-development`. Work in `D:\Caveman\caveman\Nothing-Calendar`. Read `Docs/HANDOFF.md` first.

Continue DotCal (`com.dotfield.dotcal`). Preserve existing app behavior/UI when it conflicts with newer roadmap text. Preserve schema columns/current UI rules and exactly 5 Room tables: `calendar_accounts`, `calendar_events`, `event_reminders`, `sync_metadata`, `deleted_event_log`.

Do not change package name, deep link scheme, or DB filename. Do not run phone/manual UI QA unless explicitly asked. Keep `Docs/HANDOFF.md` updated after each completed step. Build after implementation steps with:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

Current next implementation step: Continuation Roadmap Step 1, Event Detail Screen, but keep existing app behavior as source of truth where conflicts exist.
