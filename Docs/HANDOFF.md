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
- Custom Accent Colors (extra preset palette + custom hex picker; 5 base accents stay free).
- Calendar Import / Export (local `.ics` via SAF; no cloud/network).

Paywall:
- Route: `dotcal://paywall`.
- Opens from Settings > DotCal Pro, Pro-gated image/voice/date-calculator actions, Large widget locked state.
- Uses live `ProductDetails` price when available; fallback string currently INR 149.
- Restore Purchase lives in Paywall.

Known Pro fixes:
- 2026-07-02: Paywall purchase success auto-dismiss fixed. Root cause: success handler called `clearPurchaseResult()` before `delay(1500)`, causing `LaunchedEffect(purchaseResult)` to restart with `null` and cancel `onDismiss()`. Flow now shows `You're Pro!` for ~1.5s, closes Paywall, then clears purchase result. Version bumped to `versionCode 8`, `versionName 1.1.3`.
- 2026-07-02: Paywall crash fix changed app icon painter from adaptive `R.mipmap.ic_launcher` to bitmap `R.mipmap.ic_launcher_foreground`. Large locked widget Unlock radius changed `0dp -> 20dp`. Version bumped to `versionCode 7`, `versionName 1.1.2` so Play Internal Testing can accept fix over broken `1.1.1`.

## Latest Work

Branch `profeature` (WIP, not on main):
- ICS import/export Pro feature (local files only, no network — honors sync rule). New package `data/ics`: `IcsExporter.kt` (VEVENT + VTODO, RFC5545, line-folding, UID = event id, RRULE/EXDATE round-trip) and `IcsParser.kt` (unfold, VEVENT/VTODO subset, DTSTART/DTEND/DUE, TZID + all-day, EXDATE, STATUS; RRULE normalized to app's FREQ subset; unknown props ignored, no-crash). Pure Kotlin, no new dependency.
- `CalendarDao.getAllUserEventsForExport()` selects master rows excluding BIRTHDAY/HOLIDAY/GOOGLE. `DotCalRepository.exportIcs()/importIcs()/countExportableEvents()` + `IcsImportResult`. Import upserts by UID (match existing local id -> update, else insert), preserves images/voice/color/reminders on existing rows, validates timed span, reuses `LOCAL` source. No schema/column change.
- `DotCalViewModel.exportIcs{}` / `importIcs{}`. UI: SAF `CreateDocument("text/calendar")` + `OpenDocument()` launchers in `DotCalApp.kt`; new Settings "Data" section rows `Export Calendar` / `Import Calendar` via `SettingsImportExportRow` (Pro-gated -> Paywall for non-Pro, lock icon). Toast summaries.
- ICS reminders now round-trip via `VALARM` (`ACTION:DISPLAY`, relative `TRIGGER:-PT{minutes}M`). Import maps supported display alarms back to `event_reminders`, replaces existing reminders only when alarms are present, and schedules future reminders. Unsupported alarm types and positive after-start triggers are ignored.
- Paywall `PRO_FEATURES` gained "Import / Export".
- Verified: `:app:assembleDebug` passed (2m 27s), real `compileDebugKotlin`. No phone/manual QA.

- Custom accent + theme pack Pro feature. `AccentColor` in `ui/DotCalApp.kt` refactored from plain enum to a `sealed interface`: `Preset` enum (5 free + 8 Pro presets) and `Custom(hex)`. Storage in `KEY_ACCENT_COLOR` is backward compatible: preset enum name OR `#RRGGBB`. `fromStorage`/`normalizeHex` handle both; `storageValue` used everywhere `.name` was. `onColor` now auto-picks legible text via `luminanceApprox`.
- Theme settings screen: free swatches (unchanged), new "More Colors" Pro preset row (locked -> Paywall for non-Pro), new "Custom Color" row opening `CustomAccentPickerDialog` (hue/sat/brightness sliders via `detectTapGestures` + `detectHorizontalDragGestures`, live preview, hex text field). Non-Pro taps route to Paywall through existing `onDotCalPro`.
- Widget parser `widgetAccentColor` in `widget/DotCalGlanceTheme.kt` extended for new presets + `#hex`, red fallback.
- Paywall `PRO_FEATURES` gained "Custom Accent Colors".
- No Room/schema/DataStore-key changes. No version bump yet.
- New composables: `AccentColorSwatches` (now takes accent list + locked flag), `CustomAccentRow`, `CustomAccentPickerDialog`, `HueSlider`, `ValueSlider`, `SliderThumb`, `CalcSectionLabelSafe`. New imports: `detectTapGestures`, `ExperimentalLayoutApi`, `FlowRow`, `Lock` icon, `onSizeChanged`.
- Verified: `--rerun-tasks :app:compileDebugKotlin` passed (2m 28s), `:app:assembleDebug` passed (1m 15s). No phone/manual QA.

Branch `glyph-toy` (WIP, not on main):
- Nothing Glyph Matrix Toy: live countdown to next DotCal item on the rear Glyph Matrix (Phone 3 / 4a Pro). Differentiator generic calendar apps can't offer (native Glyph Progress reads only Google Calendar; DotCal is local-only so its events never reach it otherwise).
- SDK: local AAR `app/libs/glyph-matrix-sdk-2.0.aar` (Nothing `GlyphMatrix-Developer-Kit`, package `com.nothing.ketchum` / `com.nothing.thirdparty`). Wired via `implementation(files(...))` in `app/build.gradle.kts`. No Maven dep.
- SDK ships `minSdk 33`, app is `30`. Resolved with `<uses-sdk tools:overrideLibrary="com.nothing.thirdparty"/>` in manifest — NOT a minSdk bump. Safe because toy is bound only by Nothing OS toy manager (Android 14+); classes never load on other devices.
- New: `glyph/DotCalGlyphToyService.kt` (Android `Service`, action `com.nothing.glyph.TOY`, exported, app-level `NothingKey`, resource-backed meta-data name/image/summary/longpress). Device guard via `Common.is23112()/is25111p()`; all SDK calls `runCatching` silent-fail; inert on non-Nothing hardware. Render loop every 30s draws compact countdown text ("3d"/"5h"/"12m"/"now") via `GlyphMatrixObject`+`GlyphMatrixFrame`. Long-press `EVENT_CHANGE` cycles upcoming (Pro); `EVENT_AOD` re-renders.
- New: `DotCalRepository.getNextUpcomingList(includeTasks, nowMs, limit)` — one-shot snapshot reusing existing `expandRecurringEvents`/`observeUpcomingAgendaEvents`/`observeUpcomingTasks`. No schema/column/DataStore change.
- Freemium split (gated in code, not manifest): free = next EVENT countdown only; Pro (`repository.readIsPro()`) = tasks folded in + long-press cycle through next 5. Toy itself free = discovery hook for Nothing community. No new Paywall `PRO_FEATURES` entry.
- New permission `com.nothing.ketchum.permission.ENABLE` (Nothing custom; not a Google restricted permission; absent = inert on other devices). No Data Safety / Privacy Policy change (local-only, no network).
- Verified: `:app:assembleDebug` passed (4m 09s), real `compileDebugKotlin`. No phone/manual QA. Real Glyph render UNTESTED — needs a Nothing Phone 3 to confirm on-device behavior.

Latest local work:
- `versionCode = 8`, `versionName = "1.1.3"`.
- Paywall purchase success auto-dismiss fixed.
- Paywall adaptive-icon crash fixed.
- Large widget locked Unlock button rounded.
- Settings Pro feature tags fixed: Date Calculator and Import/Export now show `Pro feature` only for non-Pro users.
- ICS `VALARM` export/import added for event and task reminders.
- `Docs/HANDOFF.md` flattened/shortened.
- Date Calculator UI polish (`DateCalculatorScreen` in `ui/DotCalApp.kt`, no VM/logic change): both tabs now use uppercase section labels + bordered field groups. Days Between result now big accent hero number. Add/Subtract: cryptic square `+/−` toggle replaced by an Add/Subtract segmented control; day count is a `[−] [n] [+]` stepper (still typeable); result shows direction caption + big date. New private composables: `CalcSectionLabel`, `CalcFieldGroup`, `CalcResultHero`, `CalcDaysStepper`, `CalcStepperButton`. Added imports: `BasicTextField`, `SolidColor`.

Latest verification:
- 2026-07-02: Paywall purchase success auto-dismiss fix: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (2m 40s).
- 2026-07-02: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed after version bump (2m 35s).
- 2026-07-02: Date Calculator UI polish: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (2m 14s).
- 2026-07-03: Settings Pro feature tag visibility fix: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (2m 35s).
- 2026-07-03: ICS `VALARM` reminder round-trip: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (1m 15s).
- 2026-07-04: Glyph Toy discovery manifest fix (`NothingKey`, resource-backed toy name/summary, thumbnail): `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (2m 51s).
- 2026-07-04: Verified merged manifest matches official Nothing GlyphMatrix-Example structure exactly (manifest-level `com.nothing.ketchum.permission.ENABLE`, app-level `NothingKey=test`, exported service + `com.nothing.glyph.TOY` filter, resource-backed name/image/summary + `longpress=1`, merged minSdk 30 preserved). Swapped vector `glyph_toy_thumbnail.xml` for a raster PNG (144x144) to match official (toy manager may load image via BitmapFactory). `:app:assembleDebug` passed (42s), PNG confirmed packaged.
- 2026-07-04: ROOT CAUSE of "DotCal not in Glyph Toys list" found via on-device adb (Phone 3 model A024/Metroid, USB debugging). `adb shell cmd package query-services -a com.nothing.glyph.TOY` DID list `com.dotfield.dotcal.glyph.DotCalGlyphToyService` — so package-manager discovery, meta-data format (all resource-backed), non-empty `glyph_toy_name`="DotCal"/summary, and the PNG thumbnail were all correct and the app was NOT in stopped state. Pulled and `aapt2 dump`ed 3 shipping third-party toys (com.frank.magic8ball, com.lisra.matrixleveler, com.nothinglondon.compass). ALL THREE declare their toy service as a **foreground service**: `android:foregroundServiceType` (magic8ball `0x100`; matrixleveler + compass `0x40000000` = `specialUse`) plus `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE"/>` and a nested `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" .../>`. DotCal's toy service had NO `foregroundServiceType`. Also added `FOREGROUND_SERVICE_SPECIAL_USE` permission, `android:foregroundServiceType="specialUse"` on `DotCalGlyphToyService`, and the `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property to match all shipping third-party toys (`aapt2` confirms `foregroundServiceType=0x40000000` in the APK). These matched the app to the working reference but did NOT by themselves make it appear.
- 2026-07-04: **RESOLVED — DotCal now appears in the Glyph Toys picker on Phone 3 (confirmed by user screenshot: "DotCal — Shows a live countdown to your next DotCal event. (Community developed)").** Actual root cause: `NothingKey` is a **test key** (`android:value="test"`), and the Nothing Glyph Interface hides test-key toys from the picker unless developer debug mode is enabled. Fix was a device-side setting, not code: `adb shell settings put global nt_glyph_interface_debug_enable 1` (flag was `null`/off before). This opens a ~48h window during which test-key toys are listed. IMPORTANT PRODUCTION LIMITATION: before Play release the developer must apply to Nothing for a real production Glyph API key and swap it in for `NothingKey`; otherwise end users (without debug mode) will never see the toy. Next after user confirms on-device countdown render: implement remaining spec items (buildConfig/manifest-placeholder NothingKey + TODO, Settings Pro row deep-linking to the Glyph picker, Paywall `PRO_FEATURES` "Glyph Toy — Next Event Countdown", `GlyphSupport.isSupported()` util).
- 2026-07-04: Glyph Toy visual fix after on-device report: picker thumbnail changed from black/red DotCal art to a transparent, gray dot-matrix plate with a white calendar icon to match third-party Glyph Toys style. Matrix render changed from SDK text object at `setPosition(0, 0)` (showed only top-left white dots on hardware) to direct centered 25x25 brightness-frame rendering. Follow-up user screenshot showed visible four-side frame and oversized countdown, so the runtime frame was removed and countdown text is now fixed at compact 1x scale. `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (1m 20s). On-device render confirmation still needed after installing this build.
- 2026-07-04: Glyph render polish after on-device photo (pure-black background, text looked off-center). Two `DotCalGlyphToyService.kt` changes: (1) countdown bounding-box centering uses `(size - width + 1) / 2` int rounding (an earlier float `.toInt()` attempt was a no-op — same result as int divide — so reverted). (2) added `drawBackgroundGrid()` faint dot-matrix "plate" so the toy is never pure black: dim dots (`BG_DOT_BRIGHTNESS=400`) every `BG_DOT_SPACING=3` px, masked to the ROUND matrix (skip dots outside `radius = size/2` — corner LEDs don't exist on the circular hardware and would look clipped). `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed (1m 11s). On-device confirmation of dot brightness/centering still needed.
- 2026-07-04: Added Glyph progress ring. `drawProgressRing()` lights an outer-edge arc (`RING_BRIGHTNESS=1400`, between bg dots 400 and text 4095), clockwise from top via `atan2(dx,-dy)`, on a band at `radius = size/2 - RING_INSET(1.5)`, thickness `RING_BAND(0.7)`. Fill = `progressFraction()` = time-left / 24h window, clamped 0..1 (full when event far, drains as it nears, empty at/after start). `renderText`/`buildCountdownFrame` now take a `fraction` arg (`-1f` = no ring, used for the empty `--` state). `:app:assembleDebug` passed (1m 11s). On-device confirmation of ring brightness/sweep still needed.
- 2026-07-04: Glyph background now a FULL dim plate after on-device photo (user: other toys fill the whole round matrix; DotCal's plate looked sparse/center-only). `drawBackgroundGrid` changed from sparse dots (`BG_DOT_SPACING=3`, brightness 400) to every-LED fill (`BG_DOT_SPACING=1`, brightness `180`) inside the circular mask — a soft glowing disc filling the whole matrix, with ring (1400) and text (4095) still popping on top. `:app:assembleDebug` passed (1m 14s).
- 2026-07-04: REVERTED the hardware background plate AND the progress ring — on-device the dim gray wash "looked bad" and the user wants the rear render to be JUST the clean countdown. `buildCountdownFrame(text)` now only calls `drawCenteredText`; removed `drawBackgroundGrid`, `drawProgressRing`, `progressFraction`, the `fraction` plumbing on `renderText`, the `RING_*`/`RING_WINDOW_MS` consts, and the `kotlin.math.{PI,atan2,sqrt}` imports. Hardware render is now only countdown text (4095) on black.
- 2026-07-04: Picker thumbnail rebuilt to match other Glyph Toys (gray dot-matrix plate + white icon). New generator `scripts/gen_glyph_thumbnail.js` (pure Node, hand-rolled PNG encoder — no Python/PIL on this box) writes `app/src/main/res/drawable/glyph_toy_thumbnail.png` (144x144 RGBA, transparent bg so no black card): a round 25-dot plate of dark gray dots (`GRAY=45`) filling the circular mask, with a white (`255`) calendar icon on top (hangers + box border + solid header band + 3x2 date dots = 3 top, 3 bottom). Re-run with `node scripts/gen_glyph_thumbnail.js`. `:app:assembleDebug` passed (1m 7s), new PNG packaged. On-device confirmation of the clean hardware render + new picker thumbnail still needed.
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
