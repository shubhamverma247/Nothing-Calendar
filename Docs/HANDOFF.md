# DotCal Handoff

Updated: 2026-06-25

## Purpose

Active compact handoff for DotCal (`com.dotfield.dotcal`). Full pre-compression archive: `Docs/HANDOFF.original.md`.

Keep this file short. Add only current decisions, completed steps, next-step facts, build/test result, and new gotchas. Do not paste long historical QA lists back in.

## Hard Rules

Use `$android-development` for future Android work. 
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

Latest commit: Task tab metadata icons styled (red, reduced gap, centered).

- 2026-06-30 (second pass): Task tab metadata icons improved. `TaskMetadata` icon color changed from `secondaryText` to `palette.accent` (red), icon-text gap reduced `3.dp → 2.dp`, icon size bumped `13.dp → 14.dp` for visual centering. versionCode bumped `3 → 4` in `app/build.gradle.kts`. Visual-only changes in `app/src/main/java/com/dotfield/dotcal/ui/DotCalApp.kt` (lines 5375-5395) and build config; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `gradlew --no-daemon --console=plain :app:assembleDebug` passed (2m 15s). APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29 version code bump: `versionCode = 2 → 3` in `app/build.gradle.kts` for Play Store internal testing builds. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK ready for internal testing track.
- 2026-06-27 widget-improvement branch: picker "Can't load widget" fix + dotted background:
  - Root cause of picker failure: `widget_preview_small.xml` used `<Space>`, which is not a RemoteViews-allowed view class and caused picker inflation to fail with "Can't load widget". Fixed by replacing `<Space>` with `<FrameLayout>`.
  - Added repeating dot texture to all three live Glance widgets. Texture is a 28×28 RGBA PNG tile (`drawable-nodpi/widget_dot_tile_dark.png` and `widget_dot_tile_light.png`) wrapped in a tiling `<bitmap tileMode="repeat">` drawable. Applied via `GlanceModifier.background(ImageProvider(resId))` — Glance routes this to `setViewBackgroundResource` which honors the drawable's tileMode (no stretching). Dark: white dots ~9-24/255 alpha. Light: black dots ~9-24/255 alpha.
  - Palette carries `dotTile: Int` (res id) so tile is theme-aware. System mode resolves tile based on current `UI_MODE_NIGHT` at widget-update time.
  - Picker preview backgrounds (`widget_preview_background.xml`) updated to layer-list with dot tile for both day (`drawable/`) and night (`drawable-night/`) variants so picker previews also show dot texture.
  - No data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK install skipped because `adb` not available in this shell; no manual UI QA run.

Latest commit before this branch: widget refinement commit after `340b036 Add home screen widgets`.

Latest committed behavior:
- 2026-06-24 widget branch visual redesign implemented for Glance 2x2/4x2/4x4 widgets:
  - Visual-only change in `DotCalWidgets.kt`, `DotCalGlanceTheme.kt`, widget colors, and widget picker preview resources.
  - No data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - Widgets use selected app accent color from `KEY_ACCENT_COLOR`; accent is used visually as outline/dot/text, not filled date circles/buttons.
  - Glance 1.1.1 has no usable `defaultWeight`/border modifier in this project; ring and pill outlines are composited from nested Glance boxes, and dashed dividers use repeated tiny Box segments.
  - No bundled font added; widget numerals use built-in Glance monospace because no licensed font asset was supplied.
  - Picker previews stay RemoteViews-safe with `LinearLayout`/`TextView` only; no `TableLayout`/`TableRow`.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed and APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24 widget branch image-match follow-up:
  - All Glance widgets now use a darker dotted Nothing-style background layer.
  - 4x4 widget outer padding increased, calendar grid moved lower, `JUNE` and `2026` split into different text colors, and weekday labels made brighter.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed and APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24 widget branch 4x4 reference spacing follow-up:
  - 4x4 header shifted slightly right, event count nudged right, and dotted background density expanded so dots cover the full widget surface.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; no phone connected at install check; no manual UI QA run.
- 2026-06-24 widget branch strict 4x4 reference alignment follow-up:
  - 4x4 `JUNE 2026` header moved slightly right, event count spacing moved farther right, and dotted background now starts at the rounded widget edge with denser full-surface coverage instead of a padded upper-left field.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24 widget branch 4x4 simple-background correction:
  - Removed the dotted background from live Glance widgets after launcher verification showed Glance/RemoteViews dot layers were unreliable.
  - 4x4 header now uses balanced left/right padding for `JUNE 2026` and event count, agenda event titles are larger, and the gap between event time and title is wider.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; live home-screen screenshot verified; no manual UI QA run.
- 2026-06-24 widget branch 4x4 spacing follow-up:
  - 4x4 content moved down by 8dp, dashed divider above agenda removed, event count target inset set to match the `JUNE 2026` 34dp edge inset, agenda title font increased, time/title gap widened, and `+N MORE` moved under the title column.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24 widget branch 4x4 event-count correction:
  - Corrected previous `N EVENTS` adjustment that moved the count too close to `JUNE 2026`; header spacer widened so count moves right again.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24 widget branch 4x2/4x4 spacing polish:
  - 4x4 `N EVENTS` moved slightly left from the previous build without changing font size, 4x2 date ring increased, ring numeral uses theme primary text (white in dark, black in light), vertical dashed divider height increased, and 4x2 date/divider/body gaps widened.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-24 widget branch 4x2 live polish follow-up:
  - 4x2 live Glance widget only: `+N` pill moved above the title lane, vertical dashed divider length increased to 102dp, 4x2 vertical padding tightened to 4dp, and vertical dash count fixed so the divider does not overdraw and clip inside its own height.
  - Widget picker preview XML was not changed in this follow-up.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched once to trigger widget refresh; direct `APPWIDGET_UPDATE` shell broadcast was blocked by Android permission; `screencap`/`screenrecord` hung on the device, so no screenshot artifact was captured.
- 2026-06-24 widget branch 4x2 divider visibility correction:
  - Confirmed live home widget id `209` uses `MediumDotCalWidgetReceiver`; picker XML not touched.
  - Root cause: increasing length alone did not read visually because the 4x2 divider used 2dp low-contrast `border` dashes, short 4dp dash segments, and a trailing spacer per dash in a constrained MIUI widget cell.
  - 4x2 live divider now uses 108dp length, no top/bottom row padding, brighter `secondary` dashes, 7dp dash segments with 4dp gaps, and no trailing spacer after the last dash.
  - `+N` pill moved further upward by expanding the event lane to 102dp and pushing the title column down to 16dp.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched once and returned home to trigger widget refresh.
- 2026-06-25 widget branch 4x2 divider-height root-cause fix:
  - Root cause: the 4x2 divider stopped responding to length increases because the Glance content canvas, not the divider, was the limit. `MediumDotCalWidget` declared `SizeMode.Responsive(setOf(DpSize(250.dp, 110.dp)))`, so Glance always composed the 4x2 inside a 110dp-tall box and the launcher upscaled that bitmap. The divider at 108dp already filled the 110dp canvas, so 102/108/anything-taller all clamped to the same rendered height.
  - Fix is visual-only: raised the Medium content canvas to `DpSize(250.dp, 140.dp)`, bumped `dotcal_widget_medium.xml` `minHeight` 110dp -> 140dp, and set the 4x2 vertical divider length to 124dp so it now has room to read taller.
  - No data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-25 widget branch 4x2 divider stale-parameter correction:
  - Root cause follow-up: the canvas/resource height fix existed, but `MediumWidget(...)` still passed `length = 56` to the live vertical `DashedDivider`, so prior height changes could not appear in the installed widget.
  - Corrected the live 4x2 divider call to `length = 124`; no preview XML or schema/package/deep-link/DB changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-25 widget branch 4x2 divider centering/dash correction:
  - Root cause follow-up: `length = 124` controlled the divider lane, but the painted dash pattern still used long `7dp` dashes and produced only ~117dp of visible marks starting at the top, so the height change read weakly on the launcher.
  - Changed live 4x2 divider to a 132dp centered lane with shorter 3dp dash marks and 4dp gaps; `DashedDivider(...)` now centers the painted vertical pattern inside its requested lane.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run.
- 2026-06-25 widget branch 4x2 divider real-device fix:
  - Self-run ADB screenshot showed only ~5 short dash marks rendering, despite 132dp requested height; actual root cause was Glance/RemoteViews/MIUI compressing the divider made from repeated `Box`/`Spacer` children.
  - Replaced the live 4x2 vertical divider with a single fixed vector drawable (`widget_medium_vertical_divider.xml`) rendered through Glance `Image`, 132dp tall with 19 short 3dp dashes. This makes height/centering deterministic on launcher.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched and home screenshot verified divider is visibly taller and centered. Direct `APPWIDGET_UPDATE` broadcast remains blocked by Android permission.
- 2026-06-25 widget branch 4x2 `+N` position correction:
  - Moved the live 4x2 `+N` pill upward by giving its Glance column an 86dp lane with bottom spacer, so it sits above the title instead of horizontally aligning with the title or dropping toward the detail/bottom area.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched, returned home, and screenshot verified `+N` is above the title.
- 2026-06-25 widget branch 4x2 final live spacing polish:
  - Raised the `+N` pill to align with the divider top, widened the event title lane so the current title wraps instead of clipping early, and reduced/thinned the live divider to a centered 108dp by 1dp image.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched, returned home, and ADB screenshot verified the divider is shorter/thinner/centered and the current title no longer ellipsizes.
- 2026-06-25 widget branch 2x2 live spacing polish:
  - Live 2x2 widget only: moved the red event dot to the far right of the header, moved the event section lower, reduced the title-to-time gap, and decreased the time/location font size.
  - No preview XML, data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched, returned home, and ADB screenshot verified the 2x2 widget rendered with the updated spacing.
- 2026-06-25 widget branch 2x2 dot/gap follow-up:
  - Live 2x2 widget only: moved the red dot slightly left from the far-right edge, increased the gap between the `JUN 25` date row and event section, and moved the dotted divider/event section lower together.
  - No preview XML, data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched, returned home, and ADB screenshot verified the updated 2x2 spacing.
- 2026-06-25 widget branch 2x2 live divider/detail follow-up:
  - Live 2x2 widget only: aligned the red dot within the `THURSDAY` header row, increased the `JUN 25` to divider gap, replaced the Glance box-based horizontal divider with a fixed vector image divider modeled after the 4x2 divider fix, reduced the divider-to-title gap, reduced the time/location label font to 9sp, and changed the time/location separator from hyphen to middle dot.
  - No widget picker preview XML, data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched and returned home, but ADB device disconnected during screenshot capture, so no screenshot verification was completed for this final pass.
- 2026-06-25 widget branch 2x2 divider dash-density follow-up:
  - Live 2x2 horizontal divider vector only: kept individual dash width at 2dp and reduced the gap between dashes to 3dp for a denser divider. Widget picker preview XML was not changed.
  - No data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; no phone connected in ADB, so APK was not installed and no screenshot verification was run.
- 2026-06-25 widget branch 2x2 divider tight-gap correction:
  - Live 2x2 horizontal divider vector only: corrected the dash spacing direction by keeping 2dp dash width and reducing the gap to 1dp, placing dash starts every 3dp so dashes are closer together. Widget picker preview XML was not changed.
  - No data repository, update trigger, deep-link route, package id, Room schema/table/column, or DB filename changes.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; app launched and returned home, but ADB disconnected during screenshot capture, so screenshot verification was not completed.
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
  - Added About rows: `Privacy Policy` opens an in-app WebView at `https://dotcal-website.netlify.app/privacy`, `Rate DotCal` opens the Play Store listing, and `Version` reads `BuildConfig.VERSION_NAME`.
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

Pro / Billing phase (Steps 1-10) COMPLETE in code and building. Next work is Play Console account setup by the developer:

- Play Console: fix Payments profile issue, create `dotcal_pro` product, add License Testing emails, set up Merchant Account. Then test full purchase flow end-to-end using License Tester credentials.

Phase 1 Step 2 (Print to PDF) was SKIPPED by explicit user decision — not pending, not planned. Do not resurrect it unless the user asks.

Keep existing app behavior source of truth. Future work must not change package, scheme, DB filename, schema columns, or 5-table count unless explicitly requested.

## Pro / Billing

Status: Steps 1-10 COMPLETE. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passes. No phone/manual UI QA run (per rules). Schema untouched — still 5 tables, no new columns.

Key architecture deviations from the step spec, and why:
- **No Hilt.** The spec said "Hilt singleton / DI module". This project has no Hilt — it uses manual DI. `ProManager` is created once in `DotCalApplication.onCreate` (`val proManager: ProManager by lazy { ProManager(this, repository) }`, `proManager.initialize()`), and reaches the UI through the existing manual `DotCalViewModel` factory (constructor param `val proManager: ProManager`). This matches how `DotCalRepository` is already wired. Do NOT add Hilt just for billing.
- **No separate Compose Navigation graph.** The app has no Nav graph — it is a single `DotCalApp` composable driving full-screen overlays with `AnimatedVisibility` + `BackHandler` (same pattern as Settings/EventDetail). Paywall and Date Calculator are overlays gated by `showPaywall` / `showDateCalculator` booleans, NOT nav routes. "Add route to nav graph" was satisfied by this overlay pattern.
- **UI lives in `DotCalApp.kt`.** All screen composables are `private` in `DotCalApp.kt` and share private tokens (`DotCalPalette`, `mono`, `noRippleClickable`, `AccentColor`). Paywall + Date Calculator screens are implemented in-file so they reuse those tokens (the "reuse existing components, no new variants" rule). Only the pure calc logic is a standalone file: `presentation/datecalculator/DateCalculatorViewModel.kt`. `presentation/paywall/*` was not created as separate files for this reason.
- **Fonts.** Uses the existing `mono` family (`FontFamily.SansSerif`) everywhere — no new font added, per user instruction.

Step-by-step:
1. **Billing permission + dep — COMPLETE.** `com.android.vending.BILLING` in `AndroidManifest.xml`. `billing-ktx 7.1.1` via `gradle/libs.versions.toml` (`billing = "7.1.1"`, `billing-ktx` lib) + `app/build.gradle.kts` (`implementation(libs.billing.ktx)`). Stayed on latest stable 7.x (8.x is a major bump; spec said no 6.x-or-below, 7.x preferred).
2. **DataStore key — COMPLETE.** `KEY_IS_PRO = booleanPreferencesKey("is_pro")` in `prefs/CalendarPreferences.kt`. Read/update via existing `calendarPreferencesDataStore` in `DotCalRepository` (`observeIsPro` flow, `readIsProOnce()` first(), `setIsPro(Boolean)` edit) — same style as other keys, writes on IO. Reuses the single existing DataStore instance; no second instance.
3. **ProManager — COMPLETE.** `data/billing/ProManager.kt`. `PRODUCT_ID_PRO = "dotcal_pro"`. `_isPro`/`isPro` StateFlow, `BillingConnectionState` (Connecting/Connected/Disconnected/Error), `PurchaseResult` (Success/Cancelled/Error), `productDetails` StateFlow (drives real price), `purchaseResultFlow`. Reads cached `KEY_IS_PRO` immediately on init for fast offline read, builds BillingClient with PurchasesUpdatedListener, startConnection with exponential-backoff retry (max 3) on SERVICE_DISCONNECTED, queryPurchasesAsync(INAPP) on connect, acknowledges unacked PURCHASED, trusts live query over cache. `launchPurchaseFlow(activity)` and `restorePurchases()` implemented. Never crashes — always falls back to last DataStore value; raw exceptions mapped to friendly messages.
4. **Paywall — COMPLETE.** In-file `PaywallScreen` overlay (slide-in, back dismisses). VM surface on `DotCalViewModel`: `productDetails`, `purchaseResult`, `purchasePro(activity)`, `restorePro(onResult)`. Layout: X close (no title), inline Canvas flat calendar illustration (no bitmap), "DotCal Pro" title (mono bold), 4 feature rows (Image Attachments / Voice Notes / Large Widget / Date Calculator), price row reads price from `ProductDetails` with `₹199` string-resource fallback marked estimate, "Buy Pro" full-width 0dp-corner accent button (reuses Save Event button style, shows progress, disabled when not Connected / in progress), "Restore Purchase" text button with snackbar. Success → "You're Pro!" then auto-dismiss ~1500ms; Cancelled → stay silent; Error → snackbar.
5. **Gate image attachments — COMPLETE.** In Add/Edit Event, `+ ADD IMAGE` tap checks `if (!isPro) { showPaywall = true; return }` before the Photo Picker. Existing max-5/thumbnail/URI logic unchanged.
6. **Gate voice notes — COMPLETE.** `TAP TO RECORD` tap checks `if (!isPro) { showPaywall = true; return }` before RECORD_AUDIO / MediaRecorder. Recording/playback/storage/delete unchanged.
7. **Gate Large widget — COMPLETE.** `widget/DotCalWidgets.kt`: `provideGlance` reads `KEY_IS_PRO` from `calendarPreferencesDataStore` once; Large renders `LargeWidgetLocked` (lock glyph, "DotCal Pro", "Unlock the Large widget in DotCal Pro", Unlock box) when not Pro, else normal `LargeWidget`. Locked state + Unlock both `actionStartActivity(openPaywallIntent(context))` → `dotcal://paywall`. Small/Medium untouched. `dotcal://paywall` deep link handled in `MainActivity` (`DotCalDeepLinkTarget(paywall = true)`) → `initialPaywall` → opens Paywall overlay.
8. **Date Calculator — COMPLETE.** `presentation/datecalculator/DateCalculatorViewModel.kt` (pure `java.time` math, no DB/network; Mode DAYS_BETWEEN | ADD_SUBTRACT, `CalculatorResult` sealed, derived-state recompute, working days = Mon-Fri, no holiday awareness for v1). In-file `DateCalculatorScreen` overlay: back + "Date Calculator" header (SettingsLargeHeader style), 2-option segmented control (reuses calendar segmented control), reuses existing date picker sheet, result cards (surface, 0dp, 1dp border). Settings entry with PRO badge (Step 9).
9. **Settings additions — COMPLETE.** New params threaded `SettingsPreview -> SettingsRoot`: `isPro`, `onDotCalPro`, `onRestorePurchase`, `onDateCalculator`. "DotCal Pro" row near top (`SettingsProRow`, star/crown accent icon, dynamic subtitle, tap → snackbar if Pro else Paywall). "Restore Purchase" row only when not Pro → `restorePro` + snackbar. "Date Calculator" row with PRO badge (`SettingsProBadgeRow`) → Paywall if not Pro else Date Calculator. Accent color selection left FREE.
10. **Splash black background — COMPLETE.** `windowSplashScreenBackground = #000000` in `res/values-v31/styles.xml` (both theme entries) and `res/values-night-v31/styles.xml`. Icon/tagline unchanged.

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
- Pro / Billing phase (Steps 1-10) COMPLETE — see the `Pro / Billing` section above. Phase 1 Step 2 (Print to PDF) was SKIPPED by explicit user decision. Next action is Play Console account setup (see `Current Next Step`).

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
- 2026-06-30: Bug-fix + UI pass (8 issues) plus Tasks empty-state redesign. (1) Bottom nav now hidden during onboarding and on Settings sub-screens — the floating `DotCalBottomNav` Box is gated by `!showOnboarding && (screenTab != ScreenTab.Settings || settingsScreen == SettingsScreen.Root)`. (2) Agenda empty state no longer renders today's `AgendaDateHeader` when there are no upcoming events; it shows only the `AgendaEndOfDayState` (height bumped `0.72f → 0.82f`). (3) Voice-note mic row no longer disappears after the user denies RECORD_AUDIO — the `when` block's `!permissionDenied` branch became `else`, and `EmptyVoiceNoteRow` now takes a `permissionDenied` flag that swaps the label to `MIC PERMISSION DENIED — TAP TO ENABLE` and routes the tap to the app's system settings page. (4) Bottom nav hidden on Settings sub-screens (same gate as #1). (5/9) Sub-settings typography/spacing increased to match the main Settings list: `HolidayCountryRow` 52→64dp/14→16sp, `GoogleAccountProviderRow` 52→64dp/14→16sp, `CalendarAccountToggleRow` 68→72dp/14→16sp primary + 11→12sp secondary, `ThemeOptionRow` 72→76dp/14→16sp label + 10→12sp subtitle, `SettingsSectionTitle` 13→14sp with more padding. (6) Privacy Policy is now a native Compose screen (removed the WebView) with the full 10-section content adapted from `DotCal-Site/privacy.html` plus a tappable contact card that opens an email intent to `dotfieldstudio@gmail.com`. (7) Added a `Send Feedback` row in Settings > About that opens a mail intent (`mailto:dotfieldstudio@gmail.com?subject=DotCal Feedback`). (8) Theme switching from the OS shortcut no longer recreates MainActivity / jumps to Calendar Month — added `android:configChanges="uiMode|keyboardHidden|keyboard|screenLayout|screenSize|smallestScreenSize|orientation"` to the `MainActivity` manifest entry. (Tasks) `TaskEmptyState` redesigned: large centered circular icon (red `+` for the All filter, muted check for other filters), bigger title + per-filter subtitle, and for the All filter the icon is tappable via `noRippleClickable` wired to `onAddClick` so tapping it opens the Add Task editor. Changes in `app/src/main/java/com/dotfield/dotcal/ui/DotCalApp.kt` and `app/src/main/AndroidManifest.xml`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: In-app update feature (Play Flexible) added. Mirrors the common "new version available — Update / Not now" dialog on launch plus a manual `Check for updates` row in Settings > About. (1) Added `com.google.android.play:app-update-ktx:2.1.0` via `gradle/libs.versions.toml` (`playAppUpdate` version + `play-app-update-ktx` library) and `app/build.gradle.kts` (`implementation(libs.play.app.update.ktx)`). (2) In `ui/DotCalApp.kt`: `AppUpdateManagerFactory.create(context)` held in `remember`; silent `checkForUpdates(false)` runs once per session via `LaunchedEffect`; if an update is available and FLEXIBLE is allowed, shows `UpdateAvailableDialog` ("Update available" / Update / Not now). Update tap calls `startUpdateFlowForResult(FLEXIBLE)` through a `StartIntentSenderForResult` launcher so download happens in the background. An `InstallStateUpdatedListener` + an `ON_RESUME` `LifecycleEventObserver` flip `updateDownloaded`, which shows `UpdateReadyDialog` ("Update ready" / Restart / Later); Restart calls `appUpdateManager.completeUpdate()`. (3) Settings > About gains a `Check for updates` row above Privacy Policy → manual `checkForUpdates(true)`; no update shows a `DotCal is up to date` Toast, check failure shows `Couldn't check for updates`. Both dialogs reuse the existing M3 `AlertDialog` pattern reading `palette.dialogSurface/primaryText/secondaryText/accent`, so they render correctly in both light and dark themes automatically. Threaded a new `onCheckForUpdates` param through `SettingsPreview` → `SettingsRoot`. Silent-fails when the build is not installed from Play Store (debug/sideload). No package id, deep link scheme, Room schema/table/column, or DB filename changes; no new permission (INTERNET already present). `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (7m). APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Agenda top gap fix, Year view scroll fix, Task filter segmented control revert, Tasks bottom padding bump. (1) `AgendaPreview` contentPadding top reduced `22dp → 8dp` so the first event/date-header appears at the same visual distance from the segmented control as Month/Week/Day/Year (all of which have 0dp extra top content). (2) `YearView` LazyVerticalGrid changed from `modifier.padding(8.dp)` to `contentPadding = PaddingValues(start=8.dp, top=8.dp, end=8.dp, bottom=96.dp)` so December and its mini-calendars scroll fully above the floating nav pill. (3) `TaskFilterSegmentedControl` reverted from `weight(1f)` equal-width tabs back to natural content width tabs with consistent `padding(horizontal=10.dp)` on each Box — fixes "Completed" label being squeezed/truncated. (4) Tasks LazyColumn `contentPadding bottom` bumped `90dp → 100dp` so the last task has a reliable scroll gap above the pill across devices. Settings nav bar `isNavigationBarContrastEnforced = false` fix remains in code (line 2100) from previous build — needs APK install to verify. Visual-only changes in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Segmented control equal gaps, agenda end-state, task tab redesign, bottom nav fixes. (1) `CalendarViewSegmentedControl` and `TaskFilterSegmentedControl`: added `weight(1f)` to each segment Box and removed dynamic `padding(horizontal = if (isSelected) 14.dp else 0.dp)` inner wrapper — all 5 calendar tabs and 4 task filter tabs now share equal width so gaps between them are identical regardless of which is selected. (2) Agenda `AgendaEndOfDayState` end-state `padding(top = 128.dp)` reduced to `32.dp` and wording changed from "No more events for this day" to "You're all caught up". (3) Task tab: date headers replaced with `AgendaDateHeader` (big day number, accent weekday/month, thin divider); a new `TaskNoDueDateHeader` composable handles no-due-date group; `TaskRow` redesigned with left 4dp accent strip via `drawBehind`, `RoundedCornerShape(16.dp)` card matching AgendaEventCard shape, circular checkbox (outline = pending, tinted fill + checkmark = done), title `SemiBold 18sp`, metadata `12sp secondaryText`. (4) Bottom nav pill: shadow changed from dark-mode `0.dp` / light-mode `6.dp` to always `6.dp` with `ambientColor`/`spotColor` overrides so dark theme gets a subtle white glow; `.noRippleClickable {}` added to pill Row to consume taps between icons and prevent passthrough to events under the nav bar. (5) `SystemBarColorSync` now sets `window.isNavigationBarContrastEnforced = false` on API 29+ so the system does not add an automatic translucent scrim behind the gesture handles — fixes Settings tab nav bar appearing opaque while Calendar/Tasks appeared transparent. Visual-only changes in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Task card bg matched to agenda card; Agenda date header redesigned; AgendaEventCard improved. Task card `taskCardColor()` changed from hardcoded `#0A0A0A`/`#FFFFFF` to `palette.dialogSurface`/`palette.eventCardSurface` — same values as `AgendaEventCard` so both tabs now use identical card backgrounds. Task card border changed from `palette.line` to `palette.eventCardBorder` for consistency. `AgendaEventCard` now uses a left 4dp accent strip drawn via `drawBehind` using `event.displayColor(palette)`, card shape changed to `16dp`, padding adjusted, title reduced to `18sp`/`22sp`, time reduced to `12sp`. Agenda date headers replaced with `AgendaDateHeader` composable: left-aligned big day number (`26sp bold`), weekday label in accent color + month in secondary (`10sp`), and a thin `0.5dp` divider line extending to the right — matches the premium editorial aesthetic. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom nav icon gap fix + task cards visibility fix. Root cause of clipping: BottomNavItem box was reduced to 22dp (smaller than icon canvases of 26/28/24dp), so `.clip(CircleShape)` cut the icons. Fixed by reverting box to 30dp and switching `Arrangement.SpaceEvenly` to `Arrangement.spacedBy(80.dp, Alignment.CenterHorizontally)` to explicitly widen the gap between icons (~80dp between items vs ~65dp before). Root cause of tasks hidden: Scaffold `bottomBar = Box(90dp)` + `containerColor = palette.topBarSurface` created a solid 90dp block below content that the floating pill sat on top of, hiding any tasks near the bottom. Fixed by setting Scaffold bottomBar to 0dp, containerColor to `palette.background`, and giving Tasks LazyColumn and Agenda LazyColumn `contentPadding bottom = 90dp` — same approach as the Settings overlay, so last item scrolls just above the pill top. Shadow reduction also applied: `16dp → 6dp`. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom nav shadow reduced, icon gap increased, Tasks bottom padding fixed. Shadow elevation reduced `16dp → 6dp` (light mode only; dark stays 0dp) so the pill shadow is less prominent. BottomNavItem box reduced `30dp → 22dp` so `SpaceEvenly` distributes more space between the 3 icons. Tasks LazyColumn `contentPadding bottom` reduced `96dp → 8dp` — the Scaffold already reserves `navBarHeight + 90dp` below the column so the 96dp was double-padding that pushed the last task ~100dp above the pill, creating a false empty gap. Now last task scrolls to ~12dp above the pill top, matching Settings behavior. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Settings scroll + floating nav fix. Root cause of "black border when scrolling Settings": DotCalBottomNav was inside Scaffold's `bottomBar` and the Settings overlay left a hardcoded `112dp` gap at the bottom which showed the dark Scaffold background between Settings content and the pill, making content appear hidden. Fix restructures z-order: Scaffold's `bottomBar` is now a transparent spacer (same height as DotCalBottomNav so Scaffold still reserves correct bottom padding for Calendar/Tasks content). The real DotCalBottomNav is placed in the outer Box AFTER the Settings overlay and BEFORE full-screen overlays (EventDetail, AddEvent, etc.) so it genuinely floats on top of Settings content but is covered by event/task detail screens. Settings AnimatedVisibility modifier changed from `.padding(bottom=112dp).navigationBarsPadding().background.statusBarsPadding` to `.background.statusBarsPadding.navigationBarsPadding` — background now fills full screen, no dark gap visible. SettingsRoot LazyColumn gains `contentPadding = PaddingValues(bottom=90dp)` so the last row scrolls fully above the floating pill. Bottom nav icon gap increased: inner pill horizontal padding reduced `18dp → 8dp` and BottomNavItem hit-area reduced `42dp → 38dp` giving ~53dp gap between icons (up from ~45dp). Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom-nav/tab-motion polish for Nothing Gallery-style feel, visual-only in `DotCalApp.kt`. Root cause was shared top chrome living outside the Calendar/Tasks `AnimatedContent`, so tab body moved while top sections felt static. Fix moves Calendar header/segmented control into `CalendarTabContainer`, gives Tasks matching top chrome with shared `CalendarActionBar`, keeps Calendar ↔ Tasks on short horizontal slide + fade, and reshapes bottom nav into centered pill with icon-only selected fill. Task add action moved from floating button to the shared top-right `+` in Tasks so the whole top section transitions together. No package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Settings/nav overlap follow-up. Increased Settings overlay bottom clearance (`78dp -> 112dp`) so content no longer hides under the taller bottom nav and the pill top is not clipped on Settings. Increased bottom nav pill height (`64dp -> 68dp`), radius (`32dp -> 34dp`), and reduced internal horizontal padding (`30dp -> 18dp`) to increase gaps between icons. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom-nav screenshot-match follow-up. Changed pill from fixed width to responsive `fillMaxWidth()` within 24dp side padding, increased pill height to `64dp`, rounded corners to `32dp`, and increased internal icon spacing to `30dp` so the bar reads wider/taller like the Nothing Gallery reference. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom-nav pill/icon-weight follow-up. Increased centered pill width again (`308dp -> 332dp`), made it slightly taller (`56dp -> 60dp`), increased internal horizontal padding (`22dp -> 26dp`), reduced nav item container size (`40dp -> 38dp`), and lightened all three custom nav icons by shrinking canvas/icon sizes and stroke widths for a tighter screenshot match. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom-nav width/icon-size follow-up. Increased centered pill width again (`284dp -> 308dp`), increased internal horizontal padding (`18dp -> 22dp`), and slightly reduced nav item tap/icon container size (`44dp -> 40dp`) for a more airy Nothing Gallery-style layout. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom-nav spacing follow-up. Increased centered pill width again (`252dp -> 284dp`), increased internal horizontal padding, and removed selected icon background fill so there is more visual gap around the active icon. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-29: Bottom-nav follow-up polish. Increased centered pill width (`196dp -> 252dp`), lifted it slightly with more bottom inset / less top inset, and removed the full-width opaque nav background so content no longer looks hidden behind the bar. Visual-only in `DotCalApp.kt`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-28 (branch `audit`): Tab/view-switching performance + smoothness pass, no behavior/UI semantics change. (1) `observeEventsForMonth` now loads a rolling prev/current/next-month window so Week/Day/3-day views straddling a month boundary always have neighbouring-month events and cross-month paging reuses loaded data instead of reloading. (2) Events are grouped by day once at the top level of `DotCalApp` (`remember(events)`) and shared into Month/Week/Day/ThreeDay/Year, so buckets persist across Calendar/Tasks/Settings tab switches and view switches instead of each view re-deriving them (also removes repeated `localDate()`/zone conversions per switch). (3) Calendar view switches use a 150ms `Crossfade` for a smoother transition. (4) The last-selected-date persistence write is debounced (400ms) so rapid Week/Day paging no longer commits to DataStore on every step. No package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no manual UI QA run. Higher-risk audit item P2 (keeping the Calendar subtree mounted while on Tasks via nav restructuring) intentionally NOT done — it conflicts with the established bottom-nav behavior; the cheaper buckets + top-level `eventsByDate` already remove most of the return cost.
- 2026-06-27: Follow-up root-cause fixes after device report. Agenda now loads upcoming agenda from today instead of the mutable selected calendar date, and `AgendaPreview` filters/group headers from today so selecting a future/past date in Calendar cannot hide other upcoming days. Fresh-install Light Mode on a dark phone was caused by restorable preferences/boot theme state, so app backup is disabled and no saved theme still resolves to System/splash system mode. Widget theme refresh now also enqueues widget updates when app observes system dark/light changes, while widget receivers still listen for configuration changes. No package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` is not available in this shell; no phone/manual UI QA run.
- 2026-06-27: Fixed first-launch/default theme and widget Add Event date/theme refresh issues. Fresh install now defaults to `System` theme and splash follows system when no boot theme exists; widget Add Event deep link includes today's date and Add Event route opens editor on that date instead of stale selected date; widget receivers enqueue refresh on `CONFIGURATION_CHANGED` so light/dark changes update Glance widgets. Preserved package id, deep link scheme, Room schema/table/column, and DB filename. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed. APK install skipped because `adb` was not available in this shell; no phone/manual UI QA run.
- 2026-06-27: Add/Edit Event start/end date wheel edge fix: shared `WheelColumn` now uses one-row vertical content padding and scrolls selected items directly, so the first allowed end date can center correctly. This fixes the case where Start `28 Jun 10:00` updates Ends display to `28 Jun 11:00`, but opening the Ends picker highlighted `29 Jun 11:00` because `minDate = startDate` made `28 Jun` the first wheel item. Applies to all shared date/time wheels using `WheelColumn`; no package id, deep link scheme, Room schema/table/column, or DB filename changes. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; APK installed on phone `4ab0d020`; no phone/manual UI QA run.
- 2026-06-28: Animation polish pass — all in `DotCalApp.kt`, no behavior/schema/package/deep-link changes. (1) All 10 full-screen `AnimatedVisibility` slide transitions now use `tween(220ms, FastOutSlowInEasing)` enter and `tween(200ms, FastOutSlowInEasing)` exit instead of the default 300ms spec — feels snappier. (2) Calendar ↔ Tasks tab switch wrapped in `AnimatedContent` with `fadeIn(140ms) togetherWith fadeOut(100ms)` so switching tabs fades smoothly instead of cutting instantly. (3) Bottom nav item tint animated with `animateColorAsState(200ms)` so selecting a tab smoothly transitions from secondary to accent color. (4) Task filter segmented control selected background animated with `animateColorAsState(180ms)` instead of instant snap. (5) `Modifier.animateItem()` added to Agenda event cards, Task rows, and EventListSheet rows so list items animate in/out when the list contents change. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed; `adb` not available for install; no manual UI QA run.

Phone/manual UI QA:
- Do not run unless user explicitly asks.
- If not run, add concise `What To Test Now` bullets for user.

## What To Test Now

- In-app update (needs a Play Store/Internal-testing build, NOT debug sideload): with an older version installed and a newer one on the track, opening the app should show the `Update available` dialog; `Update` downloads in background; when done, `Update ready` dialog → `Restart` applies it. Settings > About > `Check for updates`: on a current build shows `DotCal is up to date` toast. In light AND dark theme the dialogs should match the existing Delete-confirmation dialog styling (correct surface/text/accent colors).
- Bottom nav: centered floating pill, icon-only items, subtle selected fill, smoother tint shift.
- Tap `Calendar` <-> `Tasks`: top section should move with content; no frozen/static header feel.
- Tasks screen: top-right `+` should create task; old bottom-right floating add button should be gone.
- Widget picker (long-press home → Widgets → DotCal): all three sizes should show real previews, not "Can't load widget".
- Live 2x2, 4x2, 4x4 widgets on home screen: subtle repeating dot texture visible over dark/light background — fine white/black dots, not giant blobs.
- Switch phone light/dark mode while widgets are on screen: dot texture should match theme (white dots in dark, black dots in light).
- Fresh install while phone is in Dark Mode: app should open in dark theme by default.
- In Calendar, select any other date, then open Agenda: upcoming events from today onward should still show, grouped by event date.
- Widget Add Event after fresh install: editor date should be today, not June 19 or any prior saved date.
- Switch phone light/dark mode while app theme is System: widgets should refresh to matching light/dark appearance.
- Animation polish (2026-06-28): tap Calendar → Tasks → Calendar in bottom nav: should fade smoothly between tabs. Tap a bottom nav item: icon/label should animate from gray to accent color over ~200ms. Switch Task filter tabs (All/Today/Upcoming/Completed): selected background should fade in, not snap. Open Event Detail/Event Editor/Settings: slide-in should feel faster (~220ms) than before. Add tasks and complete/delete them: list items should animate out. Open a calendar date with events: event rows in the sheet should animate in.

### Pro / Billing (2026-07-02)

Step 1 (permission + dep):
- Upload the new APK to Play Console internal test track.
- Confirm Play Console now allows creating One-time products (the BILLING permission error should be resolved).

Step 4 (Paywall):
- Open Paywall from Settings > DotCal Pro row.
- Feature list shows 4 items correctly.
- "Buy Pro" button present (may be disabled/loading if the Play Console product isn't live yet — expected).
- X button dismisses Paywall.
- "Restore Purchase" shows correct snackbar.

Steps 5-6 (gates):
- Add/Edit Event: tap image + icon → Paywall opens.
- Add/Edit Event: tap voice-note mic → Paywall opens.
- After purchasing Pro via License Testing → image and voice note work normally, no Paywall.

Step 7 (Large widget):
- Add Large widget to home screen → shows locked state with "Unlock in DotCal Pro".
- Tapping "Unlock" opens the app to the Paywall.
- After purchasing Pro → Large widget shows normal month grid + events.
- Small and Medium widgets unchanged (still free).

Step 8 (Date Calculator):
- Settings shows "Date Calculator" row with PRO badge.
- Tapping without Pro → Paywall opens.
- After Pro → Date Calculator opens.
- "Days Between": enter two dates, verify total / working (Mon-Fri) / weekend counts are correct.
- "Add/Subtract": start date + N days → verify result date is correct.
- Any 7-day range should have exactly 2 weekends (holidays intentionally ignored in v1).

Step 10 (splash):
- Set system to Light theme, force-close, relaunch → splash background is BLACK, no harsh box around the icon.
- Dark theme splash looks identical to before.

Note: full purchase-flow verification is BLOCKED until the developer creates the `dotcal_pro` product + License Testers in Play Console. Until then, "Buy Pro" disabled/loading is the expected state.

## Resume Prompt

You are continuing development of DotCal (com.dotfield.dotcal).

Read Docs/HANDOFF.md first before doing anything. It is the source of truth. Pay special attention to the `Pro / Billing`, `Current Next Step`, and `Hard Rules` sections.

STRICT RULES:
1. Do NOT touch any already-working feature unless explicitly required.
2. Do NOT change the Room schema — exactly 5 tables (calendar_accounts, calendar_events, event_reminders, sync_metadata, deleted_event_log), no new columns.
3. Do NOT touch onboarding, calendar views, sync, holidays, tasks, or any completed feature.
4. Do NOT run phone/manual UI testing — only update HANDOFF.md.
5. Build after every step: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`. Fix failures before moving on.
6. This project has NO Hilt and NO Compose Nav graph — use manual DI (ProManager created in DotCalApplication, injected via the DotCalViewModel factory) and full-screen overlay screens gated by booleans (showPaywall / showDateCalculator). Do not introduce Hilt or a Nav graph.
7. Reuse existing UI components and the existing `mono` font — do not add new fonts or component variants. UI screens live in ui/DotCalApp.kt as private composables sharing private tokens.

STATUS: The Pro / Billing phase (Steps 1-10) is COMPLETE in code and the debug build passes. What remains is developer-side Play Console setup — NOT code:
- Fix Payments profile issue, create one-time product ID `dotcal_pro`, add License Testing emails, set up Merchant Account, then test the full purchase flow end-to-end with License Tester credentials.

Phase 1 Step 2 (Print to PDF) was SKIPPED by explicit user decision — do not resurrect it.

If asked to work on code next, confirm you have read Docs/HANDOFF.md, then wait for the specific task. Do not start changing things unprompted.

