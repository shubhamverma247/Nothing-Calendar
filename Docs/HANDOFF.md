# DotCal Handoff

Updated: 2026-06-20

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

Latest commit: `3b0f10f Add birthday calendar import`.

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

## Completed Roadmap Steps

1. Event Detail Screen: complete.
2. Image Attachments: complete.
3. Voice Notes: complete.
4. AlarmManager Reminders: complete.
5. Google Calendar Sync: complete as CalendarProvider-only.
6. Tasks Tab Complete: complete, with later task detail/edit/repeat refinements.
7. Birthday Calendar: complete.

## Current Next Step

Continuation Roadmap Step 8: Home Screen Widgets.

Keep existing app behavior source of truth. Widget work must not change package, scheme, DB filename, schema columns, or 5-table count.

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

8. Home Screen Widgets:
- Use Jetpack Glance only, not legacy `AppWidgetProvider`/`RemoteViews`.
- Widgets: small 2x2, medium 4x2, large 4x4.
- Add `WidgetDataRepository`, `WidgetUpdateWorker`, `DotCalGlanceTheme`.
- Update after event save/delete and sync completion.

9. Onboarding:
- Show once using `KEY_ONBOARDING_DONE`.
- 5 pages: Welcome, Calendar permission, Notifications, Contacts, Ready.
- App degrades gracefully when permissions skipped/denied.

10. Settings Missing Items:
- Reminders: `Default reminder` picker stored in `KEY_DEFAULT_REMINDER`.
- Additional: Birthday calendar, Sync enabled, Sync interval.
- About: Privacy Policy WebView, Rate DotCal Play Store link, Version from `BuildConfig.VERSION_NAME`.
- Do not rebuild Settings from scratch.

11. Release Rules:
- Add R8/ProGuard rules after all roadmap steps complete:

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
- Latest debug APK from optimization pass installed on phone `4ab0d020`.
- Confirm package `com.dotfield.dotcal`, label `DotCal`.
- Calendar: `Year`, `Month`, `Week`, `Day`, `Agenda` switch immediately and persist.
- Calendar: Week/Day event placement and Day task rows look identical to previous build.
- Add/Edit Event: full-screen slide, X/check top bar, start/end picker sheets, reminder/repeat neutral sheets.
- Event Detail: event taps open detail; edit pencil opens editor; delete confirms.
- Tasks: bottom nav opens Tasks; filters work; add/edit/detail/delete/complete behavior matches Task Detail rules above.
- Settings: theme, sync, calendar accounts, switches, and back behavior match current UI.
- Reminders: future event/task reminders fire; View/Snooze actions route correctly.

For Step 7 after implementation:
- Toggle Birthday Calendar on with contacts permission granted; birthdays import as read-only yearly all-day events.
- Toggle off; birthday events disappear.
- Deny contacts permission; toggle remains off and app stays usable.
- Tap birthday event; Event Detail opens without edit/delete actions.
- Verify no Room table/column/package/scheme/DB filename changes.

## Resume Prompt

Use caveman-ultra and `$android-development`. Work in `D:\Caveman\caveman\Nothing-Calendar`. Read `Docs/HANDOFF.md` first.

Continue DotCal (`com.dotfield.dotcal`). Preserve existing app behavior/UI when it conflicts with newer roadmap text. Preserve schema columns/current UI rules and exactly 5 Room tables: `calendar_accounts`, `calendar_events`, `event_reminders`, `sync_metadata`, `deleted_event_log`.

Do not change package name, deep link scheme, or DB filename. Do not run phone/manual UI QA unless explicitly asked. Keep `Docs/HANDOFF.md` updated after each completed step. Build after implementation steps with:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

Latest committed work: `3b0f10f Add birthday calendar import`. Current uncommitted work is a behavior-preserving optimization pass: lifecycle-aware DataStore collection, memoized Week/Day filters, and recurrence expansion moved off main dispatcher. Required debug build passed and APK was installed on connected phone `4ab0d020`.

Current next implementation step: Continuation Roadmap Step 8, Home Screen Widgets, but keep existing app behavior as source of truth where conflicts exist.
