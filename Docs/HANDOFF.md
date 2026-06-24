# DotCal Handoff

Updated: 2026-06-24

## Purpose

Active compact handoff for DotCal (`com.dotfield.dotcal`). Full pre-compression archive: `Docs/HANDOFF.original.md`.

Keep this file short. Add only current decisions, completed steps, next-step facts, build/test result, and new gotchas. Do not paste long historical QA lists back in.

## Hard Rules

- Use `$android-development` for future Android work.
- Work in `D:\Caveman\caveman\Nothing-Calendar`.
- Preserve existing app behavior/UI when newer roadmap text conflicts.
- Do not change package/application id: `com.dotfield.dotcal`.
- Do not change deep link scheme: `dotcal://`.
- Do not change Room DB filename: `dotcal.db`.
- Preserve schema columns and exactly 5 Room tables:
  - `calendar_accounts`
  - `calendar_events`
  - `event_reminders`
  - `sync_metadata`
  - `deleted_event_log`
- Do not add Room tables or columns.
- Do not run phone/manual UI QA unless user explicitly asks.
- After app-code implementation step, run:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

- Keep `Docs/HANDOFF.md` updated after each completed step.
- Notification action source labels stay Title Case across Calendar/Tasks, e.g. `View`, `View Task`, `Snooze 10 Min`. Do not reintroduce all-caps source strings.
- Reuse established behavior patterns for similar features unless user says otherwise. Examples: destructive actions need confirmation; full-screen surfaces close with existing slide transition.

## Schema Snapshot

`calendar_accounts` columns:
- `id`, `accountName`, `displayName`, `accountType`, `color`, `isVisible`, `isPrimary`, `sortOrder`.

`calendar_events` columns:
- `id`, `accountId`, `title`, `description`, `location`, `startTimeMs`, `endTimeMs`, `timeZone`, `isAllDay`, `colorHex`, `rrule`, `exceptionDates`, `source`, `googleEventId`, `googleCalendarId`, `syncVersion`, `isTask`, `isCompleted`, `completedAtMs`, `imageUris`, `voiceNotePath`, `createdAtMs`, `updatedAtMs`.

`event_reminders` columns:
- `id`, `eventId`, `minutesBefore`, `triggerAtMs`, `alarmRequestCode`, `isDelivered`.

Important:
- `calendar_events.accountId` FK cascades on `calendar_accounts.id`. Do not use Room `REPLACE` for accounts if it can delete child events.
- `event_reminders.eventId` FK cascades on `calendar_events.id`.
- `event_reminders.alarmRequestCode` unique.
- `calendar_events` CHECK (`endTimeMs >= startTimeMs`) not enforced by Room annotation yet; repository validates local events.

## Preferences

DataStore name: `calendar_preferences`.

Keys:
- `KEY_DEFAULT_VIEW`
- `KEY_WEEK_START`
- `KEY_DEFAULT_REMINDER`
- `KEY_DEFAULT_ALL_DAY_REMINDER_TIME`
- `KEY_SYNC_ENABLED`
- `KEY_SYNC_INTERVAL_MINS`
- `KEY_BIRTHDAY_ENABLED`
- `KEY_ONBOARDING_DONE`
- `KEY_LAST_SYNC_MS`
- `KEY_SHOW_DECLINED`
- `KEY_24_HOUR_FORMAT`
- `KEY_THEME_MODE`
- `KEY_LAST_SELECTED_DATE`

## Current App State

Product: premium black/white/red calendar app. App label: `DotCal`.

Current Phase 1:
- Step 1 Add Account button complete.
- Global Holidays complete by user override.
- Step 3 Extra Accent Themes complete on branch `feature/phase1-step3-accent-themes` by explicit user request before Step 2.
- Step 2 Print to PDF remains pending.
- Do not add Pro, billing, ads, cancel, or reschedule in this phase.

Work protocol:
- Before each feature, read relevant code and state exact files/functions to change, behavior impact, schema/package/scheme/DB impact, and what to test after build.
- Ask when needed; do not assume product decisions.
- Wait for user approval before code edits unless the user already explicitly approved that feature.
- Build after implementation with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
- If a phone is connected after build, always install `app/build/outputs/apk/debug/app-debug.apk`.
- Do not run manual UI QA unless user explicitly asks.

Main navigation:
- Bottom nav: `Calendar`, `Tasks`, `Settings`.
- Calendar segmented control: `Year`, `Month`, `Week`, `Day`, `Agenda`.
- Hidden/legacy Three-day behavior may still exist internally; UI should not expose it.
- Settings opens full-screen overlay with right-slide pattern; back returns to previous screen.
- Add/Edit Event opens full-screen overlay with close `X` left and save check right.
- Event Detail and Task Detail use full-screen right-slide detail pattern.

Theme/UI:
- Current UI uses system sans-serif, not old mono typography.
- Light palette: Screen `#F7F7F7`, Dialog `#FFFFFF`, Cancel `#EFEFEF`, OK/accent `#FF3B30`, Primary `#101010`, Secondary `#6B6B6B`, Disabled `#BDBDBD`.
- Dark palette: Screen `#000000`, Dialog `#1E1E1E`, Cancel `#121212`, OK/accent `#FF3B30`, Primary `#FFFFFF`, Secondary `#B3B3B3`, Disabled `#6E6E6E`.
- Light top/status/bottom bar surfaces are white where established; Dark uses black/near-black.
- Heavy borders and dot-pattern backgrounds were removed.
- Calendar top `+` is icon-only.
- Month date selection uses red outline circle for selected day; no gray selected fill.
- Calendar/list/settings/bottom-nav rows suppress default Android ripple where it clashes with DotCal visual style; keep visible state changes/haptics.
- Task filter segmented control suppresses default Android ripple; selected segment still uses existing theme-aware state fill/text.
- Calendar view switching optimized by precomputing event day/hour buckets for Week/Day/Three-day views instead of refiltering events inside each hour cell.
- Destructive actions are centered red text, not filled buttons.
- Delete Event/Task actions show confirmation dialogs.

Events:
- Month, Week, Day, Agenda, Year implemented.
- Recurrence uses existing `calendar_events.rrule`.
- Recurrence values: daily/weekly/monthly; visible instances expand in Month/Week/Day/Agenda/Year.
- Recurring instance edit/delete supports `This event` vs `Whole series` using existing columns.
- Event Detail shows accent strip, title, date/time/all-day, recurrence, reminders, location, account, selectable description, images, voice note.
- Event Detail edit pencil hidden for `source == "BIRTHDAY"`.
- Month date-sheet event rows open Event Detail first; edit available from Event Detail.
- Deep link `dotcal://event/{eventId}` opens Event Detail.
- Add/Edit images use existing `imageUris`; Android Photo Picker; no `READ_MEDIA_IMAGES`; max 5.
- Voice notes use existing `voiceNotePath`; files under app internal `voice_notes/{eventId}.m4a`; max 5 minutes.

Reminders:
- Uses existing `event_reminders`.
- `ReminderScheduler`, `ReminderReceiver`, `BootReceiver` implemented.
- Notification channel id: `dotcal_reminders`.
- Event reminder notifications route `View` to `dotcal://event/{eventId}` and support `Snooze 10 Min`.
- Task reminder notifications route `View Task` to `dotcal://task/{taskId}` and open Task Detail after switching to Tasks.
- Future reminders scheduled after save; old alarms canceled on edit/delete.
- Boot receiver reschedules future undelivered reminders.

Sync:
- CalendarProvider-only sync exists. No REST/OAuth/cloud/network sync.
- Reads local Android `CalendarProvider` only after device/cloud calendar already synced to phone.
- Uses existing `sync_metadata` and `deleted_event_log`.
- Settings and Calendar Accounts share one `Sync Now` row with last-sync subtitle.
- Direct sync runs in process; background periodic sync uses WorkManager.
- `calendar_accounts.isVisible` controls selected provider calendars.
- Provider sync preserves user-selected visibility and skips deselected provider calendars.
- Local mode stays usable when calendar permission denied.

Tasks:
- Tasks stored in `calendar_events` where `isTask = 1`.
- Filters: `All`, `Today`, `Upcoming`, `Completed`.
- Add/Edit Task uses bottom sheet; sheet opens expanded so save action visible.
- Task details follow Event Detail visual language: same toolbar/title/section/divider structure, no cards/icons/chips.
- Task Due shows date/time split.
- Reminder shows `None` when unset.
- Completed tasks hide `Mark Complete`.
- Bottom actions are centered red text-only actions; completed tasks show only `Delete Task`.
- Task repeat values store in existing `calendar_events.rrule`.
- Task reminders reuse `event_reminders`.

## Latest Committed Work

Latest commit: widget refinement commit after `340b036 Add home screen widgets`.

Latest committed behavior:
- Birthday calendar import is implemented.
- Contacts birthdays import as read-only yearly all-day events using existing rows/tables.
- Birthday account/reminders use existing `calendar_accounts`, `calendar_events`, and `event_reminders`.
- Task Detail visual language aligned with Event Details.
- Task Detail: same toolbar/title/section/divider structure, no cards/icons/chips.
- Due date/time split.
- Reminder shows `None` when unset.
- Completed tasks hide `Mark Complete`.
- Bottom actions are centered red text-only actions.
- 2026-06-20 optimization pass kept UI/functionality unchanged: lifecycle-aware DataStore collection, memoized Week/Day filters, and recurrence expansion moved off main dispatcher.
- 2026-06-20 Step 8 Home Screen Widgets implemented with Jetpack Glance only:
  - Added small 2x2, medium 4x2, and large 4x4 Glance widgets.
  - Added `WidgetDataRepository`, `WidgetUpdateWorker`, and `DotCalGlanceTheme`.
  - Widgets read visible calendar events only; tasks are intentionally excluded for separate future task widgets. No schema/table/column changes.
  - Widgets update after event save/delete, account visibility changes, theme changes, app launch, date/timezone change, boot, direct sync completion, and background sync completion.
  - Widget rows deep link to existing Event Detail using `dotcal://event/{id}`.
- 2026-06-20 widget visuals revised toward provided 2x2, 4x2, and 4x4 references: native-style shared corners, red date circle, event-only agenda, light/dark widget palette from app theme.
- 2026-06-20 widget density/click pass:
  - 2x2 removes DotCal title/full date/count/app-icon clutter; shows date circle, one event title/time.
  - 2x2 current display: smaller red date circle, compact date label, one-line event title, and time/location line.
  - 4x2 removes DotCal title, UPCOMING label, vertical divider, app icon, and counter; shows date circle plus compact date/title/time-location group.
  - 4x4 gives the mini month full width for better readability and lists up to 3 event agenda rows.
  - Widget row clicks route events to `dotcal://event/{id}`.
  - Widget date-circle clicks route to `dotcal://calendar/month`; 4x4 month-grid date clicks route to `dotcal://calendar/month?date=YYYY-MM-DD`.
  - 2x2 empty state routes to Add Event via `dotcal://event/new`.
  - Widget deep links use `MainActivity` singleTop route tokens so repeat taps on same widget target are handled and event-detail launches no longer flash Month first.
  - Widget palette uses resource-backed color providers for System mode so launcher can resolve day/night colors after phone theme changes. Dark widget bg currently `#1E1E1E` to match the native-looking reference; light bg `#F5F5F5`.
  - `WidgetMaintenanceReceiver` listens for `CONFIGURATION_CHANGED` so System-theme widgets refresh after phone light/dark theme changes.
- 2026-06-20 4x2 widget refinement pass keeps the existing minimal layout while tightening hierarchy: current date circle size retained, text block moved 8dp closer to the circle, uppercased AM/PM with bullet location separator, lighter time/location text, and centered `No Events` empty state.
- 2026-06-20 2x2 widget refinement pass keeps the existing layout/date circle/background/radius while moving content 8dp closer to the date circle, reducing the date label to 9sp, preserving bullet time/location format, and vertically centering the `No Events` empty state.
- 2026-06-20 shared widget corner radius reduced from 34dp to 28dp across 2x2, 4x2, and 4x4 to better match the native launcher widget reference.
- 2026-06-20 shared dark widget background lightened from `#181818` to `#1E1E1E` across resource-backed System mode and forced Dark mode.
- 2026-06-20 launcher icon replaced old `N` mark with DotCal calendar mark using black/white/red palette.
- 2026-06-24 App icon replacement complete using the provided `DotCal-FixedIcon-Final-v2.zip` Android adaptive icon resources as-is. Resources were placed under `app/src/main/res/mipmap-*` and `app/src/main/res/mipmap-anydpi-v26`; manifest icon refs now use `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`. Old `res/drawable/ic_launcher.xml` was removed. Play Store 512 image is at `Docs/play_store_512.png` for future Play Console upload. No Kotlin, Compose, ViewModel, DB, schema, package id, deep link scheme, or DB filename changes.
- 2026-06-24 Splash screen background fix complete. Android 12+ splash background and icon background are explicitly black in both `values-v31/styles.xml` and `values-night-v31/styles.xml`; splash icon unchanged. No Kotlin, Compose, ViewModel, DB, schema, package id, deep link scheme, or DB filename changes.
- 2026-06-20 4x4 widget final refinement keeps existing structure but removes the header icon, moves/centers month title with the grid, increases calendar cells slightly, uses a filled red current-day circle, shows subtle event dots, limits upcoming events to 3 with `+X more`, routes empty state to Add Event, and reduces shared widget radius to 24dp.
- 2026-06-20 4x4 widget follow-up refinement keeps header/month/current-day structure, increases calendar cell footprint again, keeps event dots under event dates, changes upcoming rows to stacked time/title/location, limits 4x4 upcoming events to 2 with `+X more`, and tightens bottom whitespace.
- 2026-06-20 4x2 widget final polish moves the date circle closer to the left edge, keeps the event block aligned/centered, allows the event title to wrap to 2 lines, keeps single-event focus, and slightly increases dark secondary-text contrast.
- 2026-06-20 widget picker previews now use themed realistic `previewLayout` resources for 2x2, 4x2, and 4x4 instead of icon-only previews; picker names are `Next Event (2x2)`, `Event Details (4x2)`, and `Calendar Dashboard (4x4)`.
- 2026-06-20 widget picker crash/load fix: 4x4 preview no longer uses `TableLayout/TableRow`; preview is RemoteViews-safe nested `LinearLayout`/`TextView` only.
- 2026-06-20 final widget picker polish removes size suffixes from picker names (`Next Event`, `Event Details`, `Calendar Dashboard`), tightens 2x2 preview top-left/date/content spacing with 2-line title support, and reduces 4x2 preview left padding.
- 2026-06-21 Step 9 Onboarding implemented:
  - Onboarding shows once from existing `KEY_ONBOARDING_DONE`.
  - 5 pages: Welcome, Calendar Access, Reminders, Birthdays, Ready.
  - Calendar, notification, and contacts permission requests are optional; skipping/denying keeps the app usable.
  - Deep links skip onboarding overlay for routed launches so widget/reminder/event/task routes still open directly.
  - No package, scheme, DB filename, Room table, or schema changes.
- 2026-06-21 onboarding launch flicker fix: app now waits for the first `KEY_ONBOARDING_DONE` DataStore value before revealing main Calendar UI, preventing Month view from flashing before onboarding on first launch.
- 2026-06-21 launch/splash theme fix: manifest launch theme now resolves Light in normal resources and Dark in night resources, including Android 12+ splash attrs, so the black app-icon splash follows system theme instead of always using black.
- 2026-06-21 onboarding premium redesign:
  - Reworked all 5 onboarding pages with a shared editorial mobile layout, larger 40-50% hero area, high-contrast labels/headings, 20dp+ button radius, and compact `N / 5` progress indicator.
  - Added custom Compose Canvas semi-3D illustrations for Welcome calendar, Calendar Access hub, Reminders bell, Birthdays card/timeline, and Ready success state.
  - Uses dedicated onboarding light/dark colors matching requested palette: light `#FAFAFA` / `#FFFFFF`; dark `#0B0B0D` / `#121216`; accent `#FF3B30`.
  - Illustrations use generic event/calendar/card shapes only; no third-party calendar brand logos.
  - No image assets, package, scheme, DB filename, Room table, or schema changes.
- 2026-06-21 onboarding icon/UI reference-match pass:
  - Added reference-style mini calendar, bell, check, and lock icons inside floating cards/badges.
  - Added side foliage/accent shapes around Welcome, Reminders, and Birthdays hero compositions.
  - Moved progress dots closer to the `N / 5` count to match the reference structure.
  - Build passed; latest APK not installed in this pass.
- 2026-06-21 onboarding reference correction pass:
  - Progress indicator now uses a left-aligned group with red current count and dots near the `N / 5` label instead of reading as centered.
  - Welcome hero calendar now has an angled side panel for a closer semi-3D reference shape.
  - Calendar Access central calendar has border and binding details closer to the reference.
  - Build passed; APK installed on phone `4ab0d020` after retry.
- 2026-06-21 onboarding visual redesign execution:
  - Added actual titles and subtitles inside all illustration floating cards on Screens 2 and 3 using Paint text drawing on Canvas.
  - Added the fourth connection path and the fourth mini-grid card on Screen 2 (Calendar Access) to match the reference.
  - Replaced the placeholder lines on the birthday card with actual text on Screen 4: "Alex Smith", "Birthday", and "May 20" (in red accent).
  - Redesigned the Screen 5 completion checkmark to be drawn as a single Path with smooth round caps and joins.
  - Updated Screen 5 celebratory particles to render 4-pointed stars (diamonds) and circles.
  - Redesigned the Screen 1 Welcome calendar object with curved rings looping over the top, stacked page layers, a non-overlapping grid layout preventing cells from overlapping, and organic fanning leaf clusters on both left and right.
  - Verified layout, typography, spacings, and build compiled successfully.
- 2026-06-21 onboarding screen 1 asset integration:
  - Replaced custom Welcome page drawing with the premium `screen1.png` asset.
  - Tuned the Welcome illustration layout (scaled by `1.72f` and offset vertically by `20.dp`).
  - Centered the onboarding progress dots at the bottom of the screen while keeping the page indicator text ("N / 5") left-aligned.
  - Inserted a `24.dp` top spacer below the header row to comfortably push the illustration and text labels downward.
  - Successfully compiled the APK and installed it directly on phone `4ab0d020`.
- 2026-06-21 onboarding screen assets integration and light/dark theme distinction:
  - Integrated screen assets for Light Mode (`screen1.png` to `screen4.png`) and Dark Mode (`dark1.png` to `dark4.png`), and `both5.png` for Screen 5 in both themes.
  - Set layout spacing with `1.72f` image scale, `4.dp` image y-offset, `12.dp` top spacer above image, and `64.dp` spacer below image for better layout balance.
  - Implemented missing settings helpers (`parseStoredTime`, `toHour12`, `toHour24`, `allDayReminderTimeLabel`) in `DotCalApp.kt` to fix unresolved settings compilation errors.
- 2026-06-21 Step 10 Settings Missing Items implemented:
  - Settings now has a functional `Default reminder` picker stored in existing `KEY_DEFAULT_REMINDER`; new Add Event uses it as the preselected reminder while existing event edits preserve their stored reminder/none state.
  - Reminder options are `None`, `5 minutes before`, `10 minutes before`, `30 minutes before`, `1 hour before`, and `1 day before`.
  - `Birthday calendar` now lives under Additional and still uses existing `KEY_BIRTHDAY_ENABLED` / contacts permission / birthday import behavior.
  - `Sync interval` now includes `Manual`, `15 min`, `30 min`, and `1 hour`; Manual cancels periodic WorkManager sync while leaving manual Sync Now available.
  - Added About rows: `Privacy Policy` opens an in-app WebView at `https://dotfieldstudio.com/dotcal/privacy`, `Rate DotCal` opens the Play Store listing, and `Version` reads `BuildConfig.VERSION_NAME`.
  - Enabled app `BuildConfig` and added `INTERNET` permission only for the in-app privacy WebView.
  - No package, deep link scheme, DB filename, Room table, column, or schema changes.
- 2026-06-21 Step 11 Release Rules implemented:
  - Added requested R8/ProGuard keep rules for Room, Hilt ViewModels, Kotlin Serialization annotations/classes, Glance, and coroutine dispatcher/exception handler names.
  - No package, deep link scheme, DB filename, Room table, column, schema, or UI behavior changes.
- 2026-06-21 Settings cleanup/all-day reminder picker follow-up:
  - Removed static Settings rows: `Time zone`, `Show week number`, and `Other calendars`.
  - Replaced static `Default all-day reminder time` row with a persisted picker stored in `KEY_DEFAULT_ALL_DAY_REMINDER_TIME`.
  - Picker uses three wheel rollers: hour, minute, and AM/PM. AM/PM is not a toggle.
  - No package, deep link scheme, DB filename, Room table, column, or schema changes.
- 2026-06-22 Settings week-start picker follow-up:
  - Removed the static `Reminders` Settings row while keeping functional reminder rows.
  - Replaced static `Start of the week` row with a persisted picker stored in existing `KEY_WEEK_START`.
  - Week start options are `Region default`, `Saturday`, `Sunday`, and `Monday`.
  - The selected week start applies to Month, Week, and Year calendar layouts.
  - No package, deep link scheme, DB filename, Room table, column, or schema changes.
- 2026-06-22 Global Holidays implemented after explicit roadmap override:
  - Bundled offline holiday asset at `app/src/main/assets/dotcal_holidays.json`.
  - Holiday data covers 2025-2031 for 7 countries: IN, DE, GB, JP, IT, SA, US.
  - Settings now opens a full-screen `Global Holidays` picker with reactive `SELECTED` and `AVAILABLE` sections.
  - Selected countries create deterministic `calendar_accounts` rows (`holiday-{code}`) and deterministic `calendar_events` rows (`holiday-{code}-{date}`).
  - Deselected countries delete only the account row; existing FK cascade removes holiday events.
  - Event Detail hides edit for `source == "HOLIDAY"` same as birthdays.
  - No package, deep link scheme, DB filename, Room table, column, network permission, Pro, billing, or ads changes.
- 2026-06-23 Global Holidays Settings UI correction:
  - Global Holidays sub-screen now uses the same Settings-style large scroll header and compact pinned header behavior as Calendar Accounts/Add Account.
  - Global Holidays now includes the same extra bottom scroll space as Add Account so the short 7-country list can actually scroll and trigger the compact top-center title.
  - Available country rows use a red plus icon instead of a chevron for add/select.
  - Selected country rows keep a close icon for removal.
  - Country row dividers are full-width instead of inset/half-width.
  - No package, deep link scheme, DB filename, Room table, column, or holiday data changes.

## Completed Roadmap Steps

1. Event Detail Screen: complete.
2. Image Attachments: complete.
3. Voice Notes: complete.
4. AlarmManager Reminders: complete.
5. Google Calendar Sync: complete as CalendarProvider-only.
6. Tasks Tab Complete: complete, with later task detail/edit/repeat refinements.
7. Birthday Calendar: complete.
8. Home Screen Widgets: complete.
9. Onboarding: complete.
10. Settings Missing Items: complete.
11. Release Rules: complete.

## Current Next Step

Phase 1 Step 2 pending: Print to PDF.

Keep existing app behavior source of truth. Future work must not change package, scheme, DB filename, schema columns, or 5-table count unless explicitly requested.

## Phase 1 - Easy Features

Rules:
- Build one step at a time in order.
- Do not start Step 3 until Step 2 builds and is marked complete.
- Do not add Pro/billing/ads/cancel/reschedule/global holidays here.
- Do not run phone/manual UI QA unless user explicitly asks.

1. Add Account button: complete.
2. Print to PDF: pending.
3. Extra Accent Themes: complete by explicit user request on separate branch before Step 2.

Implemented Step 1:
- Added `Add Account` button in Settings > Calendar Accounts below the connected account list.
- Button is centered, uses the app accent theme, and does not have its own top/bottom divider.
- Tap opens a nested `Add an account` Settings-style slide screen.
- The nested screen matches Settings scroll behavior: large `Add an account` title first, then centered compact header appears after a small scroll. It shows a `Google` row with Google logo icon and right chevron.
- Back from `Add an account` returns to Calendar Accounts.
- Tapping `Google` opens Android's direct Google add-account/sign-in flow through `AccountManager.addAccount("com.google")`; it does not show the intermediate account-picker dialog.
- If calendar permission is missing, tapping `Google` requests calendar permission first, then opens the direct Google add-account/sign-in flow after grant.
- Successful add-account result triggers existing `syncNow`; account list refreshes through existing account Flow.
- No new sync engine, account table, package, scheme, DB filename, Room table, column, or schema changes.

Pending Step 2: Print to PDF.
Goal:
- Export current calendar view as a simple readable PDF.
- Entry point is Calendar top action bar only; do not add print to Settings or event detail.

Implementation plan:
- Add `app/src/main/java/com/dotfield/dotcal/util/PdfExportUtil.kt`.
- Function:

```kotlin
suspend fun exportToPdf(
    context: Context,
    viewType: String,
    rangeStart: Long,
    rangeEnd: Long,
    events: List<CalendarEvent>,
): File
```

- Use `android.graphics.pdf.PdfDocument`.
- Use A4 size `595 x 842`.
- Run PDF drawing on `Dispatchers.Default`.
- Save to `context.getExternalFilesDir(null)/dotcal_export_{timestamp}.pdf`.
- Return generated `File`.
- Render black text on white page regardless of app theme.
- Month PDF: 7-column grid with day numbers and truncated event titles.
- Week/Day PDF: date-grouped vertical event list with time, title, optional location.
- Agenda PDF: date headers and event rows.
- Add multi-page support when content exceeds page height.
- Add Calendar top bar printer icon next to existing icon-only actions.
- Tap print icon opens a simple bottom sheet/dialog with `Print current view`.
- On print: use current selected calendar view/range and already-loaded events; do not duplicate data-loading or query DB from UI.
- Show loading while export runs.
- On success, share generated PDF using Android share sheet and FileProvider.
- On failure, show generic failure feedback using existing toast/snackbar pattern.
- Add FileProvider manifest entry and `res/xml/file_paths.xml` only if not already present.
- FileProvider scope must cover the external files directory used above.

Step 2 verification to add after build:
- Calendar top bar shows print icon.
- Tapping it shows `Print current view`.
- Generated PDF share sheet opens.
- PDF is readable and matches current Month/Week/Day/Agenda range data.
- No schema/package/scheme/DB/table/column changes.
- Known gap if still true: no custom date range selection; current view only.

Completed Step 3: Extra Accent Themes.
Goal:
- Add selectable accent color while preserving existing Light/Dark/System base themes.
- Existing users default to red and see no visual change until choosing another accent.

Data implementation plan:
- Add DataStore key `KEY_ACCENT_COLOR` in `CalendarPreferences`.
- Default value: `RED`.
- Add `AccentColor` enum in theme-related code:

```kotlin
enum class AccentColor(val hex: String, val label: String) {
    RED("#FF3B30", "Red"),
    BLUE("#0A84FF", "Blue"),
    GREEN("#30D158", "Green"),
    PURPLE("#BF5AF2", "Purple"),
    AMBER("#FF9F0A", "Amber"),
}
```

- Keep `KEY_THEME_MODE` unchanged.
- Accent preference is independent from Light/Dark/System mode.

Accent replacement plan:
- Search codebase for `#FF3B30`, `FF3B30`, and `Color(0xFFFF3B30)`.
- Replace brand/selection accent usage with current accent value.
- Do not change destructive/danger red if it is specifically used for delete/destructive actions.
- Must review at least:
  - today circle
  - selected date indicator
  - FAB/Add button
  - segmented selected state where accent-colored
  - bottom nav active state
  - all-day switch checked state
  - date/time picker OK/save accents
  - event default fallback color when `colorHex` is null
  - widget/app-theme accents only if tied to current app accent and safe to update
- Preserve Light/Dark backgrounds, surfaces, dialog colors, cancel color, text colors.

Settings UI plan:
- In Settings > Theme detail screen, add section `Accent Color` below Light/Dark/System picker.
- Show 5 circular 40dp swatches.
- Selected swatch shows checkmark or selected ring matching existing style.
- Tapping swatch saves DataStore immediately and applies accent instantly.

Startup plan:
- Mirror selected accent through existing boot/startup theme-preference path if needed.
- Avoid flash of red before selected accent appears on first Compose frame.
- Only extend existing boot theme mechanism; do not rewrite it.

Step 3 verification to add after build:
- Settings > Theme shows 5 accent swatches.
- Selecting accent immediately updates today circle, add button, active bottom nav, and all found brand accent usage.
- Light/Dark/System base themes otherwise unchanged.
- App relaunch preserves selected accent with no red startup flash.
- Existing default with no preference remains red.

Implemented Step 3:
- Branch: `feature/phase1-step3-accent-themes`.
- Added existing DataStore-backed `KEY_ACCENT_COLOR` with default `RED`.
- Added accent choices: Red `#FF3B30`, Blue `#0A84FF`, Green `#30D158`, Purple `#BF5AF2`, Amber `#FF9F0A`.
- Accent is independent from Light/Dark/System theme mode.
- Settings > Additional shows current theme plus accent label and 5 circular accent swatches. Existing Theme dropdown remains available.
- Theme detail screen also contains an `Accent Color` section for the same 5 swatches.
- 2026-06-23 follow-up refinement: Settings > Additional no longer shows accent swatches directly under Theme. It now shows one `Theme` row with `Light/Dark/System • Accent` value and chevron; tapping opens the Theme detail screen, where Light/Dark/System choices and `Accent Color` swatches live.
- 2026-06-23 follow-up refinement: Theme detail now uses the same Settings-style large header plus compact centered scroll header as Calendar Accounts/Add Account/Global Holidays. Add Account's Google row divider is full-width so it covers the icon area too.
- 2026-06-23 follow-up refinement: Theme detail and Add Account screen bottom spacers increased so both scroll on tall devices and can trigger compact centered headers.
- Selecting an accent writes DataStore immediately, mirrors the value to boot preferences to avoid a red startup flash, updates Compose palette, and enqueues widget refresh.
- Null event color fallback now uses the current accent in Month dots, Week all-day/timed blocks, and Day all-day/timed blocks while explicit event/provider/birthday colors remain untouched.
- Widgets read `KEY_ACCENT_COLOR` for their accent when refreshed.
- Light/Dark/System backgrounds, surfaces, dialog colors, text colors, package id, deep link scheme, DB filename, Room tables, columns, and schema are unchanged.
- No package/scheme/DB/table/schema changes; DataStore key only.

## Step 7 Spec: Birthday Calendar

Goal:
- Import birthdays from device contacts as yearly recurring read-only events using existing `calendar_events` rows with `source = BIRTHDAY`.
- No schema changes.

Implemented:
- Added `ContactsProviderDataSource` querying `ContactsContract.Data` for birthday events after `READ_CONTACTS`.
- Birthday imports use existing `calendar_accounts` account id `birthday-calendar`, display name `Birthdays`, type `DEVICE`, color `#FF3B30`.
- Birthday rows are stored in existing `calendar_events` with `source = BIRTHDAY`, `isAllDay = 1`, `rrule = FREQ=YEARLY`, `isTask = 0`.
- Added yearly recurrence expansion so birthdays render in Month/Week/Day/Agenda/Year using existing recurrence path.
- Birthday reminders use existing `event_reminders` with `1440` minutes before.
- Settings `Import contacts' birthdays` uses existing `KEY_BIRTHDAY_ENABLED`; enabling requests contacts permission, imports and toasts count; disabling deletes birthday events/reminders.
- App launch refreshes birthdays when enabled and contacts permission is granted.
- Birthday detail stays read-only through existing `source == "BIRTHDAY"` UI rule.

Data source:
- Add `ContactsProviderDataSource` in `data/provider/`.
- Function: `getBirthdays(): List<CalendarEvent>` or project-local equivalent.
- Query `ContactsContract.Data`.
- Filter:
  - `mimetype = CommonDataKinds.Event.CONTENT_ITEM_TYPE`
  - `type = CommonDataKinds.Event.TYPE_BIRTHDAY`
- Check `READ_CONTACTS` before query.
- Return empty list if permission denied or cursor null.
- Run contacts/provider IO on `Dispatchers.IO`.

Birthday event mapping:
- Title: `{ContactName}'s Birthday`.
- `isAllDay = 1`.
- `rrule = FREQ=YEARLY`.
- `source = BIRTHDAY`.
- `isTask = 0`.
- `colorHex = #FF3B30`.
- `accountId = birthday calendar account id`.
- Default reminder: 1 day before, `1440` minutes, stored via existing `event_reminders`.
- Use existing recurrence expansion/rendering so birthdays appear yearly in Month/Week/Day/Agenda/Year.

Birthday account:
- Create/use one existing-table `CalendarAccount`:
  - `displayName = Birthdays`
  - `accountType = DEVICE`
  - `color = #FF3B30`
  - `isVisible = 1`
- Do not use `REPLACE` in way that cascades/delete existing child birthday events unexpectedly.

Settings integration:
- Toggle uses existing `KEY_BIRTHDAY_ENABLED`.
- When turned on:
  - Request `READ_CONTACTS` if not granted.
  - If granted, import birthdays and show count like `47 Birthdays Imported` or existing app toast/copy style.
  - If denied, keep toggle off and show rationale/state using existing Settings patterns.
- When turned off:
  - Delete all `source = BIRTHDAY` events and related reminder rows via cascade/existing delete path.
  - Show disabled feedback using existing Settings patterns.
- Re-import on launch if birthday calendar enabled and contacts permission granted.

Birthday UI behavior:
- Birthday events are read-only.
- Tapping birthday event can show Event Detail.
- Event Detail must hide edit pencil for `source == "BIRTHDAY"`; this rule already exists in UI.
- No delete/edit actions from UI for birthdays unless user later asks.

## Remaining Roadmap

No continuation roadmap items remain. Steps 1-11 are implemented.

Completed Step 10 Settings Missing Items:
- `Default reminder` picker stored in `KEY_DEFAULT_REMINDER`.
- Additional section includes Birthday calendar, Sync enabled, Sync interval, and Sync Now.
- About section includes Privacy Policy WebView, Rate DotCal Play Store link, and Version from `BuildConfig.VERSION_NAME`.

Completed Step 11 Release Rules:
- R8/ProGuard keep rules added for Room, Hilt ViewModels, Kotlin Serialization, Glance, and coroutines.

## Global Holidays Feature

Status: COMPLETE after explicit roadmap override.

Generated bundled holiday counts:
- IN: 41
- DE: 63
- GB: 53
- JP: 127
- IT: 90
- SA: 28
- US: 82

Known gaps:
- Holiday data covers 2025-2031 only; regenerate `dotcal_holidays.json` for later years in a future update.
- Only 7 countries are supported in this pass.
- Same-date holidays are merged into one title because holiday event ids are deterministic by country/date.
- Python was unavailable in the local environment, so the asset was generated with transient workspace-local NPM tooling and the `date-holidays` package; no generator dependency or script ships with the app.

Next step:
- Return to Phase 1 roadmap: Step 2 Print to PDF pending. Do not start Step 3 until Step 2 builds and is marked complete.

## Architecture Rules

- UI -> ViewModel -> repository/data source; preserve existing local structure.
- No business logic in Composables.
- Use coroutines/Flow; no Rx/callback-first new code.
- DB/provider/file IO on `Dispatchers.IO`.
- CPU-heavy recurrence/sorting on `Dispatchers.Default`.
- ViewModel state via `StateFlow` where practical.
- Compose observes flows with lifecycle-aware collection.
- Keep dynamic lists lazy with stable keys.
- Do not hold `Context` in ViewModel.
- Wrap failures with `Result`/UI state where practical; no silent exception swallowing.

## Verification

After app-code change:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

Latest verification:
- 2026-06-23: Root Settings header cleanup: removed the back arrow from the root Settings large header and compact header now that bottom-nav tab switching stays visible; nested Settings screens still keep back arrows. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-23: Settings overlay bottom-nav visibility fix: Settings now reserves the bottom-nav area plus system navigation inset, so opening Settings keeps the bottom nav visible instead of covering it. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-23: Onboarding/button/navigation polish: primary onboarding buttons now use squarer 8dp corners, onboarding content uses navigation-bar padding and compact-height spacing so `Not Now` remains visible with 3-button navigation, bottom nav uses navigation-bar padding so it sits above system buttons, and Settings bottom-tab icon changed to a filled gear-style icon. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-23: Reverted uncommitted widget-related changes from this session (`DotCalRepository`, `DotCalWidgets`, `WidgetDataRepository`, `WidgetUpdateWorker`, `widget_preview_large.xml`, and untracked widget helper/assets). Kept non-widget `DotCalApp.kt` change and user reference PNGs. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed on rerun after first timeout; APK install skipped because no ADB device was connected.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after increasing Theme detail and Add Account scroll area for tall devices; no phone/manual UI QA run; APK not installed in this pass.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after increasing Add Account screen scroll area; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Theme detail Settings-style header and Add Account Google divider refinement; no phone/manual UI QA run.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after moving Accent Color swatches from Settings root into Theme detail screen; no phone/manual UI QA run.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Phase 1 Step 3 Extra Accent Themes on branch `feature/phase1-step3-accent-themes`; no phone/manual UI QA run.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after enabling Global Holidays scrolling on short lists; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Global Holidays compact-header/divider correction; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-23: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Global Holidays Settings UI correction; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Global Holidays implementation; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after changing Google row from account picker to direct add-account/sign-in flow; no phone/manual UI QA run.
- 2026-06-24: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after splash screen background fix; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after final launcher icon resource replacement; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after restoring Google row to open the Android account picker/sign-in flow; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after restoring Settings-style scroll/compact-header behavior on Add Account screen; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after adding Google logo icon to Add Account provider row; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Add Account header/direct-Google-sync update; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Add Account nested provider screen and back navigation correction; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Calendar Accounts Add Account button UI polish; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Phase 1 Step 1 Add Account button; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Settings week-start picker follow-up; no phone/manual UI QA run.
- 2026-06-22: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after merging latest `feature/onboarding-screen` into `main`; onboarding branch changes were prioritized in conflicts; no phone/manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Settings cleanup/all-day reminder picker follow-up; no phone/manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Step 11 Release Rules; no phone/manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after Step 10 Settings Missing Items; no phone/manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed before committing onboarding/splash/theme changes; no phone/manual UI QA run.
- 2026-06-21: APK installed on phone `4ab0d020` after onboarding reference correction retry with `adb install -r app\build\outputs\apk\debug\app-debug.apk`; no manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after onboarding reference correction pass.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after onboarding icon/UI reference-match pass; no phone/manual UI QA run.
- 2026-06-21: Premium onboarding redesign APK installed on phone `4ab0d020` with `adb install -r app\build\outputs\apk\debug\app-debug.apk`; no manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after premium onboarding redesign.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after launch/splash theme resource fix; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after onboarding launch flicker fix; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-21: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed for Step 9 Onboarding; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after final widget picker naming/spacing polish; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after replacing 4x4 picker preview table layout with RemoteViews-safe linear rows; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 4x2 widget polish and widget picker preview/name updates; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 4x4 calendar-size and stacked upcoming-events refinement; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 4x4 widget final refinement and shared radius reduction to 24dp; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after shared widget dark background lightening; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after shared widget corner-radius reduction; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 2x2 widget balance/readability refinement; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 4x2 widget gap/date-circle correction; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 4x2 widget readability/spacing refinement; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after widget dark background/corner-radius match pass; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after resource-backed widget day/night colors; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 2x2/4x2 layout and system-theme refresh pass; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after widget deep-link routing fix; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after 2x2 widget icon removal and empty-state Add Event route; no phone/manual UI QA run.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after widget visual/theme/data revision; no phone connected at install check.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed for Step 8 Home Screen Widgets.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after behavior-preserving optimization pass; APK installed on phone `4ab0d020`.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed for Step 7 Birthday Calendar.
- First build attempt timed out after 124s with no result; rerun with longer timeout passed.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after month selected-date/ripple polish; APK installed on phone `4ab0d020`.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after bottom nav ripple removal; APK installed on phone `4ab0d020`.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after tab/view switch smoothing; APK installed on phone `4ab0d020`.
- 2026-06-20: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after task filter segmented-control ripple removal; no phone connected at install check.
- No phone/manual UI QA run, per instruction.

Phone/manual UI QA:
- Do not run unless user explicitly asks.
- If not run, add concise `What To Test Now` bullets for user.

## What To Test Now

For current latest app state:
- Latest debug APK from splash screen background fix build is available at `app/build/outputs/apk/debug/app-debug.apk`; installed on phone `4ab0d020`; no manual UI QA run.
- System theme Light: fully close app, relaunch from launcher, confirm splash background is black, not white, with no harsh box around icon.
- System theme Dark: fully close app, relaunch from launcher, confirm splash background is still black and unchanged from prior dark behavior.
- Splash icon: confirm icon still displays correctly, colored or monochrome depending on Android `Themed icons`; only background behind it changed.
- Launcher icon: with Android `Themed icons` OFF, confirm colored DC calendar design appears: red header bar, red center dot, white D + red C.
- Launcher icon: with Android `Themed icons` ON, confirm white silhouette tinted with system accent color appears.
- Launch app and confirm no crash or missing resource error.
- Confirm package `com.dotfield.dotcal`, label `DotCal`.
- Settings > Additional: Theme row should show base theme and selected accent label, with no swatches directly under it.
- Tap Settings > Additional > Theme: Theme detail screen should open with large `Theme` header; after scrolling, compact centered `Theme` header should appear at the top.
- Theme detail screen: `Accent Color` should show Red, Blue, Green, Purple, and Amber circular swatches.
- Settings > Calendar Accounts > Add an account: Google row divider should span full row width, including under the Google icon area.
- Settings > Calendar Accounts > Add an account: screen should scroll even on tall devices and show the compact centered `Add an account` header after scrolling.
- Tap each swatch: today circle, selected indicators, add button, active bottom nav, switches, picker save/OK accents, and default event color fallback should update immediately.
- Relaunch app after selecting Blue/Green/Purple/Amber: selected accent should persist with no red startup flash.
- Light/Dark/System base theme backgrounds, surfaces, and text colors should remain unchanged.
- Widgets should use selected accent after a widget refresh trigger.
- First normal app launch: onboarding appears once with 5 pages: DotCal, Calendar Access, Reminders, Birthdays, Ready.
- First normal app launch: Calendar Month should not flash before onboarding appears.
- Onboarding visual QA: all 5 pages should use the same premium editorial layout, large semi-3D hero illustration, light/dark palette, red accent, `N / 5` progress, full-width rounded primary CTA, and secondary `Not Now` where applicable.
- Onboarding buttons: primary CTA should have squarer reference-style corners, not pill corners.
- Onboarding on phones with 3-button navigation: `Not Now` should stay visible above system buttons.
- Bottom nav on phones with 3-button navigation: Calendar/Tasks/Settings labels should sit above system buttons and remain fully visible.
- Open Settings: bottom nav should remain visible below the Settings overlay, and tapping `Calendar` or `Tasks` from there should still switch tabs normally.
- Root Settings screen: no top-left back arrow in either the large header or compact scrolled header.
- Nested Settings screens like `Theme`, `Calendar Accounts`, `Add an account`, `Global Holidays`, and `Privacy Policy` should still show back arrows.
- Bottom nav Settings icon should render as a filled gear-style icon.
- Onboarding icon QA: floating cards/badges should show reference-style mini calendar, bell, check, and lock icons; progress dots should sit near the `N / 5` count.
- Onboarding progress QA: progress should be left-aligned under the copy group, with current number in red and dots near the count, not centered.
- Onboarding illustrations: Calendar Access uses generic event/calendar cards only; no Google/Outlook/Yahoo/Apple or other third-party logos.
- Phone in light or dark theme: launch/splash screen should use black background behind app icon.
- Onboarding: allow or skip Calendar/Notifications/Contacts; denied/skipped permissions should not block local app use.
- Onboarding: after Skip or Start, relaunch should not show onboarding again.
- Onboarding/deep links: event/task/widget/reminder deep links should open their target directly instead of being covered by onboarding.
- Launcher widgets: add DotCal small 2x2, medium 4x2, and large 4x4 widgets.
- Widgets: visible-calendar events render; tasks do not render; hidden calendar events do not render.
- 2x2 widget: no app icon; red date circle, date label, one-line event title, time/location; empty state opens Add Event.
- 4x2 widget: no app icon; red date circle and date/title/time-location group; date circle opens Month.
- 4x2 widget: date circle sits closer to the left edge; title remains strongest and can wrap to 2 lines; empty state keeps the same left circle/content structure.
- Widget picker: 2x2, 4x2, and 4x4 previews show realistic themed content, not icon-only previews; picker names are `Next Event`, `Event Details`, and `Calendar Dashboard`.
- Widget picker: 4x4 preview uses RemoteViews-safe linear rows to avoid launcher `Can't load widget` preview failure.
- Widgets: change phone light/dark theme while app theme is System; widget colors should refresh.
- Widgets: 2x2, 4x2, and 4x4 share 24dp corners and match current reference structure: red date circle, event-only agenda/month dashboard, no app/calendar icon clutter.
- 4x4 widget: month title sits above centered weekday/grid layout; today uses filled red circle with white text; event days show subtle red dots; no-event state opens Add Event.
- 4x4 widget: upcoming section shows up to 2 stacked event rows as time, title, optional location; `+X more` opens Calendar.
- 4x4 widget: tapping month date opens Month with that date selected; tapping `+X more` opens Calendar.
- Widgets: tapping event rows opens Event Detail.
- Widgets: save/delete an event, toggle calendar account visibility, change app theme, and run Sync Now; widgets refresh.
- Calendar: `Year`, `Month`, `Week`, `Day`, `Agenda` switch immediately and persist.
- Calendar: Week/Day event placement and Day task rows look identical to previous build.
- Add/Edit Event: full-screen slide, X/check top bar, start/end picker sheets, reminder/repeat neutral sheets.
- Event Detail: event taps open detail; edit pencil opens editor; delete confirms.
- Tasks: bottom nav opens Tasks; filters work; add/edit/detail/delete/complete behavior matches Task Detail rules above.
- Settings: theme, sync, calendar accounts, switches, and back behavior match current UI.
- Settings > Reminders: `Default reminder` picker persists `None`, `5 min`, `10 min`, `30 min`, `1 hour`, and `1 day`; new Add Event opens with the selected default reminder.
- Settings > Reminders: `Default all-day reminder time` opens a three-wheel picker for hour, minute, and AM/PM; selected time persists after reopening Settings.
- Settings > General: `Start of the week` picker persists `Region default`, `Saturday`, `Sunday`, and `Monday`; Month, Week, and Year layouts reorder week starts accordingly.
- Settings > General: `Global Holidays` row shows `None selected` initially.
- Open Global Holidays screen: it uses the Settings-style large header/compact scroll header; all 7 countries appear under `AVAILABLE`, with no `SELECTED` section.
- Available country rows show a red plus icon, not a chevron.
- Tap India: it moves to `SELECTED` immediately and India holidays appear in Calendar views.
- Tap Germany: it also moves to `SELECTED`; India holidays remain untouched.
- Remove India from `SELECTED`: India holidays disappear; Germany holidays remain.
- Select all 7 countries: `AVAILABLE` section disappears and `SELECTED` shows all 7.
- Remove all countries: `SELECTED` disappears and `AVAILABLE` shows all 7 again.
- Settings subtitle updates as countries are added/removed: `1 country selected`, `3 countries selected`, `None selected`.
- Turn on airplane mode on a fresh install and select a country; holidays should still load from bundled assets.
- Tap a holiday event in any view: Event Detail opens with no edit icon.
- Close/reopen app with 2+ countries selected: both remain visible with no duplicate rows.
- Reopen Global Holidays after selecting India: India should already be under `SELECTED`, not selectable twice.
- Settings > General: `Time zone`, `Show week number`, and `Other calendars` should no longer appear.
- Settings > Additional: `Birthday calendar` toggle, `Sync enabled`, `Sync interval`, and `Sync Now` preserve existing behavior; `Manual` sync interval cancels periodic background sync but leaves `Sync Now` usable.
- Settings > Calendar Accounts: centered accent `Add Account` button appears below connected accounts without top/bottom list dividers; tapping it opens a nested `Add an account` screen.
- Settings > Calendar Accounts > Add an account: initially shows large `Add an account` title, then after a small scroll shows centered compact title; `Google` row has Google logo icon plus chevron; tapping `Google` opens Android's direct Google add-account/sign-in flow without the intermediate account-picker dialog.
- Settings > Calendar Accounts > Add an account: header/system back returns to Calendar Accounts.
- Settings > Calendar Accounts: completing Google add-account/sign-in triggers existing sync and refreshes the account list.
- Settings > Calendar Accounts > Add an account: if calendar permission is missing, tapping `Google` requests calendar permission first, then opens direct add-account/sign-in after grant.
- Settings > About: Privacy Policy opens in-app WebView, Rate DotCal opens Play Store, and Version shows `1.0.0` from `BuildConfig.VERSION_NAME`.
- Reminders: future event/task reminders fire; View/Snooze actions route correctly.

For Step 7 after implementation:
- Toggle Birthday Calendar on with contacts permission granted; birthdays import as read-only yearly all-day events.
- Toggle off; birthday events disappear.
- Deny contacts permission; toggle remains off and app stays usable.
- Tap birthday event; Event Detail opens without edit/delete actions.
- Verify no Room table/column/package/scheme/DB filename changes.

## Resume Prompt

Use caveman-ultra and `$android-development`. Work in `D:\Caveman\caveman\Nothing-Calendar`. Read `Docs/HANDOFF.md` first and follow it as source of truth.

Continue DotCal (`com.dotfield.dotcal`). Do not change Room schema/tables, package id, deep link scheme, or DB filename. Keep handoff updated, build after implementation, and install APK if phone is connected.

Next pending feature, only if user approves: Phase 1 Step 2 Print to PDF.
dont start work lmk when ready.