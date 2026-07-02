# DotCal Handoff

Updated: 2026-07-02

Source of truth for DotCal (`com.dotfield.dotcal`). Full history/archive lives in `Docs/HANDOFF.original.md`. Keep this file short: current rules, invariants, active status, latest work, next tests only.

## Hard Rules

- Use `$android-development` for Android work.
- Workdir: `D:\Caveman\caveman\Nothing-Calendar`.
- Preserve existing app behavior/UI unless user explicitly changes it.
- Do not change package/application id: `com.dotfield.dotcal`.
- Do not change deep link scheme: `dotcal://`.
- Do not change Room DB filename: `dotcal.db`.
- Do not change Room schema: exactly 5 tables, no new columns unless user explicitly approves:
  - `calendar_accounts`
  - `calendar_events`
  - `event_reminders`
  - `sync_metadata`
  - `deleted_event_log`
- Do not run phone/manual UI QA unless user explicitly asks.
- After app-code changes run:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

- Keep `Docs/HANDOFF.md` updated after completed steps.
- Notification action labels stay Title Case: `View`, `View Task`, `Snooze 10 Min`.
- Destructive actions need confirmation. Full-screen surfaces use existing right-slide transition.
- Project has NO Hilt and NO Compose Nav graph. Keep manual DI + boolean full-screen overlays (`showPaywall`, `showDateCalculator`).

## Schema / Storage

Room:
- `calendar_accounts`: `id`, `accountName`, `displayName`, `accountType`, `color`, `isVisible`, `isPrimary`, `sortOrder`.
- `calendar_events`: `id`, `accountId`, `title`, `description`, `location`, `startTimeMs`, `endTimeMs`, `timeZone`, `isAllDay`, `colorHex`, `rrule`, `exceptionDates`, `source`, `googleEventId`, `googleCalendarId`, `syncVersion`, `isTask`, `isCompleted`, `completedAtMs`, `imageUris`, `voiceNotePath`, `createdAtMs`, `updatedAtMs`.
- `event_reminders`: `id`, `eventId`, `minutesBefore`, `triggerAtMs`, `alarmRequestCode`, `isDelivered`.

DB gotchas:
- `calendar_events.accountId` FK cascades on `calendar_accounts.id`; do not use Room `REPLACE` for accounts if it can delete child events.
- `event_reminders.eventId` FK cascades on `calendar_events.id`.
- `event_reminders.alarmRequestCode` unique.
- Repository validates local event end >= start.

DataStore name: `calendar_preferences`.
Keys: `KEY_DEFAULT_VIEW`, `KEY_WEEK_START`, `KEY_DEFAULT_REMINDER`, `KEY_DEFAULT_ALL_DAY_REMINDER_TIME`, `KEY_SYNC_ENABLED`, `KEY_SYNC_INTERVAL_MINS`, `KEY_BIRTHDAY_ENABLED`, `KEY_ONBOARDING_DONE`, `KEY_LAST_SYNC_MS`, `KEY_SHOW_DECLINED`, `KEY_24_HOUR_FORMAT`, `KEY_THEME_MODE`, `KEY_ACCENT_COLOR`, `KEY_LAST_SELECTED_DATE`, `KEY_IS_PRO`.

## Current App State

Product: premium black/white/red calendar app. App label: `DotCal`.

Main nav:
- Bottom nav: `Calendar`, `Tasks`, `Settings`.
- Calendar views: `Year`, `Month`, `Week`, `Day`, `Agenda`. Hidden/legacy `ThreeDay` may exist internally; do not expose.
- Settings, Event Detail, Task Detail, Add/Edit Event use full-screen right-slide surfaces.

Theme/UI:
- System sans-serif (`mono = FontFamily.SansSerif`), not old mono typography.
- Light palette: Screen `#F7F7F7`, Dialog `#FFFFFF`, Cancel `#EFEFEF`, Accent `#FF3B30`, Primary `#101010`, Secondary `#6B6B6B`, Disabled `#BDBDBD`.
- Dark palette: Screen `#000000`, Dialog `#1E1E1E`, Cancel `#121212`, Accent `#FF3B30`, Primary `#FFFFFF`, Secondary `#B3B3B3`, Disabled `#6E6E6E`.
- Accent choices: Red `#FF3B30`, Blue `#0A84FF`, Green `#30D158`, Purple `#BF5AF2`, Amber `#FF9F0A`.
- Calendar/list/settings/bottom-nav rows suppress default ripple where it clashes with DotCal style; keep visible state/haptics.
- Destructive actions: centered red text, not filled buttons.

Implemented features:
- Events: Month/Week/Day/Agenda/Year, recurrence daily/weekly/monthly, recurring instance edit/delete (`This event` vs `Whole series`), Event Detail, deep link `dotcal://event/{eventId}`.
- Media: images via existing `imageUris`, Android Photo Picker, max 5; voice notes via existing `voiceNotePath`, internal `voice_notes/{eventId}.m4a`, max 5 min.
- Reminders: `ReminderScheduler`, `ReminderReceiver`, `BootReceiver`, channel `dotcal_reminders`, event/task notification deep links, snooze 10 min, boot reschedule.
- Sync: CalendarProvider-only. No REST/OAuth/cloud/network sync. Uses existing `sync_metadata` + `deleted_event_log`. Local mode works without calendar permission.
- Tasks: stored in `calendar_events` (`isTask = 1`), filters `All/Today/Upcoming/Completed`, bottom-sheet editor, full-screen detail, task reminders reuse `event_reminders`.
- Birthday calendar: contacts import into existing tables, `source = BIRTHDAY`, yearly all-day recurrence, read-only detail/edit hidden.
- Global Holidays: bundled 2025-2031 data only for IN/DE/GB/JP/IT/SA/US.
- Widgets: Glance 2x2/4x2/4x4. Small/Medium free. Large is Pro-gated. Widget previews must stay RemoteViews-safe (`LinearLayout`/`TextView`/safe views only; no `Space`, `TableLayout`, `TableRow`).
- Onboarding: 5 pages; permissions optional; deep links skip onboarding.
- In-app updates: Play Flexible update check on launch + Settings > About > Check for updates.
- Splash: Android 12+ splash background black in light and dark.

Skipped:
- Phase 1 Step 2 Print to PDF was explicitly skipped. Do not resurrect unless user asks.

## Pro / Billing

Status: complete in code. Billing product active in Play Console; full purchase-flow verification blocked by Google-side Payments/Merchant review.

Important files:
- `app/src/main/java/com/dotfield/dotcal/data/billing/ProManager.kt`
- `app/src/main/java/com/dotfield/dotcal/prefs/CalendarPreferences.kt`
- `app/src/main/java/com/dotfield/dotcal/DotCalApplication.kt`
- `app/src/main/java/com/dotfield/dotcal/ui/DotCalApp.kt`
- `app/src/main/java/com/dotfield/dotcal/ui/DotCalViewModel.kt`
- `app/src/main/java/com/dotfield/dotcal/presentation/datecalculator/DateCalculatorViewModel.kt`
- `app/src/main/java/com/dotfield/dotcal/widget/DotCalWidgets.kt`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`

Billing facts:
- Dependency: Google Play Billing Library `billing-ktx` v7.1.1. Do not downgrade below v6.
- Permission: `com.android.vending.BILLING`.
- Product ID: `dotcal_pro`.
- Purchase option ID: `dotcal-pro-lifetime`.
- Type: one-time purchase / Buy.
- Live price: INR 149 (auto-converted elsewhere).
- License Testing: `pro-tester` list, response `RESPOND_NORMALLY`.
- Still confirm actual test Gmail is in `pro-tester` and logged in on test device.
- Entitlement persisted in `KEY_IS_PRO`; widgets refresh after purchase/restore.

Pro gates:
- Image Attachments.
- Voice Notes.
- Large Widget.
- Date Calculator.

Paywall:
- Route: `dotcal://paywall`.
- Opens from Settings > DotCal Pro, Pro-gated image/voice/date-calculator actions, Large widget locked state.
- Uses live `ProductDetails` price when available; fallback string currently INR 149.
- Restore Purchase lives in Paywall.

Known Pro fixes:
- 2026-07-02: Paywall purchase success auto-dismiss fixed. Root cause: success handler called `clearPurchaseResult()` before `delay(1500)`, causing `LaunchedEffect(purchaseResult)` to restart with `null` and cancel `onDismiss()`. Flow now shows `You're Pro!` for ~1.5s, closes Paywall, then clears purchase result. Version bumped to `versionCode 8`, `versionName 1.1.3`.
- 2026-07-02: Paywall crash fix changed app icon painter from adaptive `R.mipmap.ic_launcher` to bitmap `R.mipmap.ic_launcher_foreground`. Large locked widget Unlock radius changed `0dp -> 20dp`. Version bumped to `versionCode 7`, `versionName 1.1.2` so Play Internal Testing can accept fix over broken `1.1.1`.

## Latest Work

Latest local work:
- `versionCode = 8`, `versionName = "1.1.3"`.
- Paywall purchase success auto-dismiss fixed.
- Paywall adaptive-icon crash fixed.
- Large widget locked Unlock button rounded.
- `Docs/HANDOFF.md` flattened/shortened.
- Date Calculator UI polish (`DateCalculatorScreen` in `ui/DotCalApp.kt`, no VM/logic change): both tabs now use uppercase section labels + bordered field groups. Days Between result now big accent hero number. Add/Subtract: cryptic square `+/−` toggle replaced by an Add/Subtract segmented control; day count is a `[−] [n] [+]` stepper (still typeable); result shows direction caption + big date. New private composables: `CalcSectionLabel`, `CalcFieldGroup`, `CalcResultHero`, `CalcDaysStepper`, `CalcStepperButton`. Added imports: `BasicTextField`, `SolidColor`.

Latest verification:
- 2026-07-02: Paywall purchase success auto-dismiss fix: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (2m 40s).
- 2026-07-02: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after version bump (2m 35s).
- 2026-07-02: Date Calculator UI polish: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (2m 14s).
- No phone/manual UI QA run.

Current dirty files may include earlier Pro/UI polish and release assets. Do not revert unrelated user/local changes.

## Current Next Step

Upload `1.1.3` / `versionCode 8` to Play Internal Testing.

Then wait on Google:
1. Payments profile verification.
2. Merchant Account review.

After Google clears:
1. Confirm test Gmail is in `pro-tester`.
2. Log into that Google account on test device/emulator.
3. Open Settings > DotCal Pro. Paywall should open and show live INR 149 from `ProductDetails`. If fallback appears, debug product-detail loading first.
4. Tap Buy Pro. Confirm Play test purchase sheet appears as test order, no real charge.
5. Confirm purchase. Verify image/voice gates removed, Large widget shows content, Date Calculator opens, Settings says Pro active.
6. Test Restore Purchase on fresh install.
7. Complete Content Rating + Data Safety, then Open Testing.

## What To Test Now

Play/Internal-testing build only, not debug sideload:
- Settings > DotCal Pro opens Paywall; app must not close.
- Pro-gated image/voice/date-calculator taps open Paywall; app must not close.
- Paywall X dismisses; Restore Purchase shows correct toast/snackbar.
- Large widget locked state shows rounded Unlock button; tapping opens Paywall.
- After Pro purchase/restore: image + voice work, Date Calculator opens, Large widget unlocks.
- In-app update dialog still works when older installed build sees newer track build.

## Resume Prompt

You are continuing development of DotCal (`com.dotfield.dotcal`).

Read `Docs/HANDOFF.md` first. It is source of truth. Respect Hard Rules, Pro/Billing status, schema invariants, and Current Next Step.

STRICT:
1. Do not touch working features unless required.
2. Do not change Room schema: exactly 5 tables, no new columns.
3. Do not touch onboarding/calendar views/sync/holidays/tasks unless task requires it.
4. Do not run phone/manual UI QA unless user asks.
5. Build after app-code change with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`.
6. No Hilt, no Compose Nav graph. Use existing manual DI + overlays.
7. Reuse existing UI components and `mono`; no new fonts/component variants unless needed.
