# DotCal Handoff

Updated: 2026-08-25

Source of truth for DotCal (`com.dotfield.dotcal`). Full old history lives in
`Docs/HANDOFF.original.md`. Do not touch `Docs/HANDOFF - Copy.md` or user-owned
`Docs/FEEDBACK.md`.

## Current Worktree

- Branch: `main`.
- All work happens on `main`. Do not create or switch branches.
- Do not commit or push unless the user explicitly asks.
- Latest pushed commit before current local widget work: `2b61b79 feat(widgets): start unified widget configuration`.
- Local release target: `versionCode 35`, `versionName 1.3.1`.
- Expected untracked user file: `Docs/FEEDBACK.md`; leave it untouched.

## Hard Rules

- Preserve app identity: black/white/red, minimal, offline-first Android calendar.
- CalendarProvider sync only for Google/system calendar integration; no backend/cloud/account
  dependency unless the user explicitly approves it.
- Room schema currently stays constrained to 5 entities/tables. Prefer side-store data in
  `dotcal_side_store.json` when a feature does not need relational querying.
- Do not change package name, deep-link scheme, database filename, or billing product ids casually.
- Keep Pro/free behavior explicit. Free should remain a complete calendar; Pro should unlock smart
  planning, power features, advanced widgets, and privacy controls.
- Do not start release hygiene, full i18n, AGP upgrade, backend sync, or manual phone QA unless
  requested or blocking.
- Update this handoff after completed app work.

## Current State

- Android: Kotlin + Compose, `compileSdk 36`, `minSdk 30`, `targetSdk 36`.
- Billing: `billing-ktx 8.0.0`; do not downgrade below v8.
- Version: `versionCode 35`, `versionName 1.3.1`.
- Release build has `isMinifyEnabled=true`, `isShrinkResources=true`, and
  `proguard-android-optimize.txt`.
- Tabs: Calendar, Tasks, Settings.
- Views: Year, Month, Week, Day, Agenda. Keep hidden ThreeDay unexposed.
- Events/tasks stored in `calendar_events`; tasks use `isTask = 1`.
- Existing media fields: `imageUris`, `voiceNotePath`; PDFs and side data are tracked outside Room.
- Existing recurrence field: `rrule`; recurrence exceptions use side-store/provider metadata.
- DataStore: `calendar_preferences`.
- Pro entitlement: `KEY_IS_PRO`.
- Paywall route: `dotcal://paywall`.

## Billing Products

- Lifetime Pro: `dotcal_pro` as `INAPP` one-time product.
- Subscription Pro: `dotcal_pro_subscription` as primary `SUBS` product.
- Legacy/fallback subscription id still queried: `dotcal_pro_sub`.
- Base plan ids mapped by code: `monthly`, `yearly`.
- Entitlement rule: lifetime purchase wins forever; otherwise active subscription grants Pro;
  otherwise Free.
- App cannot cancel a user's Google Play subscription if they later buy Lifetime. Show/manage via
  Google Play subscription management when relevant.

## Recently Completed

- Calendar sync handles reminders both ways:
  - DotCal -> Google writes `CalendarContract.Reminders`.
  - Google -> DotCal imports provider reminder minutes.
  - Old local alarms are cancelled and future imported reminders are scheduled.
- Calendar sync handles recurrence exceptions:
  - Google `EXDATE` imports into DotCal `exceptionDates`.
  - DotCal skipped/deleted single occurrence pushes provider `EXDATE`.
  - DotCal edited single occurrence excludes original and creates a standalone provider event.
  - Google modified recurring occurrences import using `ORIGINAL_ID` / `ORIGINAL_INSTANCE_TIME`.
  - Modified Google occurrence rows import as standalone non-recurring DotCal rows.
- Added transient provider exception metadata on `CalendarEvent` using `@Ignore`.
- Added provider reminder/EXDATE helper tests.
- Calendar overflow menu customization is implemented:
  - Route: `Settings > Calendar Preferences > Calendar menu`.
  - Storage: `hidden_calendar_menu_actions` in DataStore.
  - Configurable actions: Search, New Event, Add Shift, Go to date, Quick Add, Share availability,
    Templates, Calendar Sets, Shift Patterns.
- PDF event attachments are implemented:
  - Up to 5 PDFs per event, 20 MB per PDF.
  - Stored app-private through side-store metadata.
  - Backup/restore includes PDFs with limits.
  - Adding PDFs is Pro-gated; viewing/opening/removing existing PDFs remains available.
- Shift Worker Convenience Pack partial:
  - Quick Shift Add from Calendar surfaces is built and Pro-gated.
  - Shift plan sharing supports image, PDF, ICS, and DotCal QR for compact ranges.
  - QR sharing capped at 14 shifts for reliability.
- Widget/month polish completed:
  - Medium agenda widget date clarity fixed.
  - Transparent widget opacity slider added.
  - Month chips, bottom nav accessibility, and week-number alignment polished.
- Widget product decision after competitor/code review:
  - Core widget sizes, including large/4x4 calendar or agenda utility, should be Free because Free
    must feel like a complete offline calendar.
  - Keep advanced widget controls Pro: per-widget calendar selection/filtering, transparent
    background, opacity, dot texture/style controls, advanced 14-day behavior, privacy masking,
    presets if they become substantial, and smart planning widgets.
  - Existing code currently Pro-gates large widget rendering and per-widget calendar selection; remove
    the large-widget gate when widget work resumes, but preserve customization/filter gates.
- Reminder product decision after Business Calendar 2 Pro check:
  - Business Calendar 2 Pro advertises repeating alarms and individual ringtones for different
    calendars as premium reminder features.
  - DotCal should keep basic reminders Free.
  - DotCal Pro can unlock advanced local reminder controls: per-calendar reminder sound/custom ring,
    repeating alarm, custom snooze presets, vibration pattern, and stronger notification behavior
    options.
  - Keep this offline/local. Respect Android notification-channel limits when designing per-calendar
    sounds; avoid cloud notification infrastructure.
- CalendarProvider sync hardening mini-pass completed:
  - Provider `AVAILABILITY_FREE` now imports as DotCal ghost/non-blocking side-store state so
    Find-a-Time / Free Time logic can avoid treating provider-free events as blockers.
  - DotCal ghost/Pencil-In provider-backed saves now export `AVAILABILITY_FREE`; normal events export
    `AVAILABILITY_BUSY`.
  - Provider status now imports/exports through `CalendarContract.Events.STATUS`; synced status is
    preserved in side-store for provider-backed edits.
  - Provider `RDATE` is audited/preserved through side-store so edits do not casually erase provider
    recurrence extras, but DotCal still does not generate local RDATE rules.
  - Provider-cancelled recurring instances now become parent exceptions only; they no longer import
    as standalone visible events.
  - Calendar-move duplicate guard deletes stale local provider rows with the same `googleEventId`
    after the event appears under its new provider calendar.
  - Added DST/local-time EXDATE regression coverage.
  - No Room schema change; side data uses `dotcal_side_store.json`.
- Language picker exists, but most app UI strings are still hardcoded English.
- Unified configurable widget system work started:
  - Added unified per-widget config model for Calendar, Schedule, Today, Tasks, Countdown, and Quick
    Actions.
  - Existing widget receivers/classes are preserved for backward compatibility; placed widgets
    migrate legacy per-widget calendar state into `KEY_WIDGET_INSTANCE_CONFIG`.
  - Widget config model exists for migration/defaults, but widget picker providers no longer launch
    `WidgetConfigActivity`; all `android:configure` entries were removed from widget provider XML
    after user review.
  - Added basic config-aware rendering for Today, Tasks, Quick Actions, Schedule, Calendar, and
    Countdown categories.
  - Added Next 14 Days as a widget time range and kept core large widget rendering Free.
  - Added pure responsive size-classifier foundation for later Glance size-aware rendering.
  - Polished real 14-day Schedule presentation:
    - `2x2` stays single-event density.
    - `4x2` now uses one dominant event plus compact upcoming rows.
    - `4x4` Schedule has a dedicated `SCHEDULE / NEXT 14 DAYS` agenda renderer with capped rows
      and `+X MORE`.
  - Month-grid `4x4` calendar agenda is capped short so the calendar grid remains readable.
  - Added Quick Actions deep links and config options for Quick Add, Add Task, and Search:
    - `dotcal://quick-add`
    - `dotcal://task/new`
    - `dotcal://search`
  - Heavy preview/dropdown widget config UI was removed after phone/user review, and add-widget now
    skips the configuration screen entirely.
  - Widget picker now has purpose-specific labels/descriptions instead of generic `Configurable`:
    - `DC 1x1 Date`: date tile.
    - `DC 2x2 Event`: today, next event, countdown, tasks, or quick action.
    - `DC 4x2 Agenda`: wide agenda/today/tasks/countdown/action.
    - `DC 4x4 Month`: month grid/schedule/today/tasks/countdown/action.
    - Existing legacy picker entries are restored, so picker count is 7 total with 1x1:
      `DC Count`, `DC Agenda`, and `DC Month`.
  - Added picker entry:
    - `DateOnlyDotCalWidgetReceiver` / `DateOnlyDotCalWidget`, provider `@xml/dotcal_widget_date_only`.
  - Removed dedicated `DC 14 Day` picker entry/provider per user request.
  - Fixed 2x2 Month chevron month navigation:
    - Root cause: label/grid rendered from `data` captured in `provideGlance`, so glance-state
      offset changes recomposed palette but not the visible month until a full reload.
    - Fix: compact month derives `monthLabel`/`days` in composition from
      `currentDotCalWidgetSettings().monthOffset`; grid is drawn as a bitmap; header uses a
      non-overlapping Row; offset clamped to ±12 months (saturates silently at the limits).
  - Verification so far:
    `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest --tests com.dotfield.dotcal.widget.WidgetInstanceConfigTest --tests com.dotfield.dotcal.widget.WidgetResponsiveSizeTest`
    passed, and
    `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug`
    passed.
  - Latest widget verification:
    `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest --tests com.dotfield.dotcal.widget.WidgetInstanceConfigTest :app:assembleDebug`
    passed.
    `git diff --check` passed with CRLF warnings only.
  - Latest local install succeeded on device `4ab0d020`:
    `C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk`.

## Verification Baseline

Use focused checks first, then broad checks when risk warrants it.

```powershell
.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug
.\gradlew.bat --no-daemon --console=plain :app:lintDebug
git diff --check
```

Install only when requested or needed:

```powershell
C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

Known install caveat: if Android reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the installed app was
signed with a different key. Do not uninstall without explicit user approval because that deletes
local app data.

## Manual QA Focus

After next debug install, prioritize:

- Open phone widget picker.
  - Expected: 7 entries visible: `DC 1x1 Date`, `DC 2x2 Event`, `DC 4x2 Agenda`,
    `DC 4x4 Month`, `DC Count`, `DC Agenda`, and `DC Month`.
- Add `DC 1x1 Date`.
  - Expected: compact date tile; no generic configurable label.
- Add any widget.
  - Expected: no widget configuration screen opens; widget is placed directly.
- Add `2x2` Schedule widget.
  - Expected: one strongest upcoming event only; no cramped list.
- Add `4x2` Schedule widget with `Next 14 Days`.
  - Expected: date badge at left; first event dominant; up to two compact rows; no clipping.
- Add `4x4` Schedule widget with `Next 14 Days`.
  - Expected: `SCHEDULE / NEXT 14 DAYS` header; up to four rows; `+X MORE` when needed.
- Add `4x4` Calendar widget.
  - Expected: month grid readable; short agenda only; no overflow below grid.
- Configure Quick Actions for Quick Add, Add Task, and Search.
  - Expected: each tap opens the matching app screen, not a fallback calendar screen.
- DotCal reminder -> Google reminder.
- Google reminder -> DotCal reminder.
- DotCal delete one recurring occurrence -> Google hides that occurrence.
- Google delete one recurring occurrence -> DotCal hides that occurrence.
- DotCal edit one recurring occurrence -> Google shows original excluded plus standalone edited event.
- Google edit/move one recurring occurrence -> DotCal imports standalone edited occurrence and hides original.
- Repeated sync does not create duplicate detached occurrences.
- Swipe Week and Day views repeatedly on the clipping-prone device.
- Create/sync a Google all-day event spanning multiple days; Month/Day/Week show every visible date.
- Sync Google events with custom calendar colors; DotCal should not fall back to red when provider
  event color is missing.
- Restore purchase on an account with old lifetime purchase.

## Planned: CalendarProvider Sync Hardening

Do one focused sync hardening pass before or alongside Find-a-Time if the user wants sync confidence
first. Keep it CalendarProvider-only; do not add Google Calendar API/backend/account dependency.

Worth adding:

- Provider availability/free-busy mapping. DONE in first mini-pass.
  - Imported provider `AVAILABILITY_FREE` as non-blocking ghost side-store state for Find-a-Time /
    Free Time logic.
  - Exported DotCal ghost/Pencil-In provider-backed events as `AVAILABILITY_FREE`; normal events as
    `AVAILABILITY_BUSY`.
- Recurrence "this and following" edits.
  - Implement clean series split: old parent ends before selected occurrence; new parent starts at
    selected occurrence.
  - Avoid creating many per-instance exceptions for a future range.
- RDATE support. PRESERVE-ONLY in mini-pass.
  - Import/export provider RDATE string through side-store for provider-backed edits.
  - Keep full local RDATE expansion/generation for a later recurrence-model pass.
- Provider event status. DONE for preservation.
  - Preserve confirmed/tentative/cancelled status through side-store/provider writes.
  - Tentative remains separate from DotCal Pencil-In/Ghost.
- Cancelled-instance QA/retention. CODED + TESTED for import behavior.
  - Provider-cancelled recurring instances remain parent exceptions and do not import as standalone
    visible events.
  - Manual repeated-sync QA still useful on device/provider.
- Timezone/DST recurrence tests. STARTED.
  - Added local-time EXDATE parsing test across DST boundary.
  - Add broader recurrence expansion tests when touching recurrence engine.
- Calendar-move duplicate guard. DONE in sync layer.
  - Moving a provider event between calendars deletes stale local duplicate rows with the same
    provider event id.

Lower priority unless user demand appears:

- EXRULE import support.
- Read-only attendee/organizer/RSVP display.
- Provider privacy/visibility import for future privacy masking.

## Completed Roadmap Snapshot

- Year / Month / Week / Day / Agenda views.
- Events, tasks, recurrence, reminders, holidays, birthdays.
- Search, Templates, Calendar Sets.
- Smart Quick Add v2.
- Countdowns / D-Day.
- Bulk Edit / Multi-Select.
- Drag-and-drop reschedule and resize.
- QR Event Share.
- Availability Text Generator.
- Dead Time Finder.
- Ghost Events / Pencil-In.
- On This Day.
- Shift Patterns and partial Shift Worker Convenience Pack.
- Private Vault, App Lock.
- Import/export, backup/restore.
- Widgets and widget opacity.
- PDF event attachments.
- Google Calendar provider sync with reminders and recurrence exceptions.
- Billing: Lifetime, Monthly, Yearly Pro.

## Product Direction

Position DotCal as:

> The calendar that helps you plan your time privately.

Product pillars:

- Minimal UI.
- Offline-first.
- Privacy-first.
- Smart time planning.
- QR/offline sharing.
- Local intelligence.

Free must stay a complete calendar. Pro should unlock planning leverage, advanced controls, privacy
depth, and power-user workflows.

## Next Roadmap (Phase-wise, merged)

Decision sources: `D:\chrome downloads\DotCal_Product_Roadmap_Handoff(1).md`, competitor review
(Business Calendar 2, DigiCal, aCalendar+, Fantastical, Motion/Reclaim, Any.do, TimeTree, Proton,
Apple/Google Calendar 2026 state), and the 2026-08-25 new-feature research pass. Items marked NEW
were not in any prior DotCal roadmap. Each phase reuses the previous phase's infrastructure to keep
cost low and updates regular. The old flat list and the "Planned: Product Roadmap Free/Pro" section
below are absorbed into these phases.

### Phase 0 — Finish Current Work

- Unified Configurable Widget System completion + pending theme-toggle manual QA (MIUI Autostart +
  battery no-restrictions verify on device `4ab0d020`).

### Phase 1 — Free Quick Wins (one small release each; retention/acquisition)

1. Rich notification actions: Snooze presets (5/15/30 min), Complete task, Open — directly from
   reminder notifications. No storage change.
2. Voice dictation into Smart Quick Add: mic button -> on-device `SpeechRecognizer` ->
   existing NL parser. No new parser work.
3. Quick Settings Tile + app long-press shortcut for Quick Add.
4. Export view as image: share Month/Week/Agenda snapshot as a dot-matrix branded card. Free basic;
   Pro unlocks custom ranges/styling. Reuses the shared CardImageExporter approach.

### Phase 2 — Reminder / Scheduling Core (Pro value build-up)

5. Advanced Reminders / Ringtone Controls (Pro):
   - Custom tune picker per event / per calendar: system notification sounds or user audio file
     (MP3/OGG/WAV) from device storage.
   - Per-calendar default tunes (Work = one tone, Personal = another).
   - Repeating alarm until dismissed; custom snooze presets; vibration patterns; stronger
     notification behavior (full-screen intent, high priority).
   - Android caveat: notification-channel sound is hard to change after channel creation — design
     per-calendar channel ids deliberately before implementation. Keep it local/offline only.
6. Auto-Buffers (NEW): global rule to pad X minutes before/after every meeting; surfaced in conflict
   warnings and later Find-a-Time. DataStore rule + FreeSlotEngine reuse. Pre-work for Find-a-Time.
7. Find-a-Time (existing plan): Free gets basic slot suggestions + working hours; Pro gets preferred
   days/times, Calendar Sets, minimum gap, ghost policy, multiple candidate slots, best-slot ranking.

### Phase 3 — Flagship Differentiators

8. Smart Quick Add 3.0 MERGED with Conversational Edit Commands (NEW): extend the local parser from
   create-only to edit/move/delete/query ("move my 2pm to kal", "add 30 min prep before it",
   "delete tomorrow's gym"). Hinglish support is a differentiator no global competitor offers.
   Marketing hero feature.
9. SMS-to-Event Parser (NEW): on-device regex reads booking/train/movie/appointment SMS and creates
   Ghost Event drafts with one-tap pencil-in. Fully offline, Hinglish SMS aware, zero cloud.
   Requires explicit user opt-in (notification/SMS access) given privacy posture — discuss tiering
   before build.

### Phase 4 — Insights Cluster (FreeSlotEngine family; cheap together)

10. Calendar Health (existing plan): local weekly/monthly analytics; never leaves the phone.
11. Task Auto-Scheduling (NEW, Motion-lite): pull unscheduled tasks into Dead-Time slots via
    FreeSlotEngine; one-tap confirm. Uses existing `isTask = 1` rows; no schema change.
12. Protect Free Time + Free Time Map (existing plans).

### Phase 5 — Sharing & Niche Domination

13. Template Assistant (existing plan): Free limited templates; Pro unlimited + suggestions +
    create-from-event.
14. Availability QR Card (NEW): encode Availability Text Generator output as QR; scanning shows the
    slots without app/internet. Extends the offline-Calendly concept using existing QR infra.
15. Shift-Swap QR (NEW): two shift workers exchange shifts via QR; accepting updates both calendars.
    Exact fit for the shift-worker niche; BC2/Supershift do not have this.

### Phase 6 — Power / Privacy Depth

16. Manual Travel Blocks + Custom Conference Links (existing plan).
17. Expanded Attachments (existing plan): DOC/DOCX/XLS/XLSX/TXT/ZIP, higher limits, still
    app-private. No Drive/OneDrive sync.
18. Scheduled Encrypted Auto-Backup (NEW): nightly/weekly AES-encrypted backup to a user-chosen SAF
    folder. Automates existing backup/restore; deepens "private by default"; no cloud.
19. Vault Decoy PIN (existing plan) + App Lock PIN hardening (PBKDF2 migration + failed-attempt
    backoff).

### Phase 7 — Delight / Seasonal (timing-sensitive last)

20. Ghost Week Planner (NEW): next-week dry run — drag tentative events in as ghosts, confirm-all in
    one tap. Evolution of Ghost Events; nobody ships a week-rehearsal view.
21. Dual-Timezone Mode (NEW): second timezone column/labels in Day/Week + event timezone override.
22. Geofence Reminders (NEW): arrive/leave location reminders via Play Services geofencing, offline.
    Adds location permission — needs explicit user approval before build because it touches the
    privacy posture.
23. Life-in-Dots (existing plan) with share-as-image export.
24. Year Wrapped (existing plan) — December launch with free teaser card conversion moment.

### Explicitly Rejected by Research (do not build)

- Weather-in-calendar (DigiCal/BC2 sell it): requires INTERNET permission; breaks offline-first
  identity and Play listing claims. Skip unless an opt-in network module is ever approved.
- Scheduling/booking links (Calendly-style): needs backend; already in Backlog Boundaries.
- AI chatbot / cloud LLM anything: conversational edits give the same wow locally.

## Planned: Product Roadmap Free/Pro (absorbed into phase-wise roadmap above)

1. **Unified Configurable Widget System + 14-Day Widget** - highest priority.
   - Free: all six core widget categories (Calendar, Schedule, Today, Tasks, Countdown, Quick
     Actions), multiple independent widget instances, supported 1x1 through large sizes, calendar
     selection/basic filters, Minimal/Compact/Detailed layouts, basic appearance, opacity, tap
     actions, and a useful 14-day/two-week view.
   - Pro later: Free Time intelligence, Focus Window, advanced 14-day density/privacy/shift modes,
     advanced smart rules, advanced privacy masking, and other genuinely smart widget behavior.
   - Architecture: reuse current Glance/widget providers and configuration flow; do not create a
     parallel widget system or dozens of launcher-facing widget types. Preserve existing widgets,
     per-widget calendar selection where present, opacity behavior, and migrate legacy widget state
     into a unified per-instance config when possible. No Room schema change expected.
2. **Advanced Reminders / Ringtone Controls**.
   - Free: basic event/task reminders stay useful.
   - Pro: per-calendar custom reminder sound/ring, repeating alarms, custom snooze presets,
     vibration pattern, and stronger notification behavior options.
   - Architecture: local settings in DataStore/side-store first. Do not add backend/cloud push.
   - Android caveat: notification-channel sound behavior can be hard to change after channel
     creation, so design channel ids/settings deliberately before implementation.
3. **Find-a-Time**.
   - Free: basic slot suggestions and working-hour constraints.
   - Pro: preferred days/time of day, Calendar Sets, minimum gap, ghost-event policy, multiple
     candidate slots, and best-slot ranking.
   - Architecture: reuse existing Availability / FreeSlot / Dead Time infrastructure. No backend and
     no Room schema change expected.
4. **Smart Quick Add 3.0**.
   - Free: basic English natural-language event creation.
   - Pro: advanced recurrence, calendar selection, task creation, countdown, Pencil-In, and template
     detection.
   - Constraint: deterministic/local parser first. Do not market multilingual parsing until shipped.
5. **Calendar Health**.
   - Pro: local weekly/monthly analytics such as scheduled hours, free hours, meetings, back-to-back
     blocks, overloaded days, busiest day, and comparison with previous week.
   - Privacy copy: calendar analytics never leave the phone.
6. **Template Assistant**.
   - Free: limited templates.
   - Pro: unlimited templates, smart suggestions, and create-template-from-event workflow.
7. **Protect Free Time**.
   - Pro: find daily/weekly focus-time blocks and create Focus Time events.
   - Reuse free-slot logic.
8. **Free Time Map**.
   - Free: optional basic weekly free-time total.
   - Pro: per-day free-time map with tappable blocks and filters.
9. **Manual Travel Blocks + Custom Conference Links**.
   - Free: custom meeting URL and manual travel blocks such as 15/30/45/60 minutes.
   - Pro later: provider link generation and smart maps/location-based travel estimate only if the
     technical/privacy cost is acceptable.
10. **Expanded Attachments**.
   - Free: keep basic image/PDF usage useful.
   - Pro: DOC/DOCX/XLS/XLSX/TXT/ZIP and higher limits, still local/app-private.
   - Do not add Drive/OneDrive attachment sync now.
11. **Life-in-Dots, Year Wrapped, Vault Decoy PIN**.
   - Still worth doing, but after the planning features above because they are retention/delight or
     privacy-hardening features, not core planning differentiation.

Tiering rule:

- If a feature is needed for DotCal to feel like a good calendar, put a useful basic version in Free.
- If a feature saves planning effort, analyzes time, automates choices, expands limits, customizes
  widgets deeply, or protects sensitive data, put the advanced version in Pro.
- Keep all analysis local unless the user explicitly approves a cloud-backed feature later.

## Backlog Boundaries

Do not build now:

- Live shared calendars between DotCal users.
- Booking marketplace or payments.
- Payroll/overtime module.
- Generic AI chatbot.
- Mandatory cloud account.
- Huge theme marketplace.
- Google Drive / OneDrive attachment sync.
- Backend encrypted sync unless explicitly approved.

Worth later if demand repeats:

- Wear OS.
- Web/Desktop companion.
- Optional encrypted cross-device sync.
- Time-zone planner.
- More complete shift pack, without turning DotCal into a shift-only app.

## Existing Known Gaps

- Full UI string extraction and translation: language picker exists, but most visible text remains
  hardcoded English.
- App Lock PIN hardening: current PIN storage needs a backward-compatible migration to a slow hash
  such as PBKDF2 plus failed-attempt backoff/lockout UI.
- `DotCalApp.kt` remains large and should eventually be split into smaller route/state coordinators.
- Internal-testing billing verification still pending.
- Play "optimized resource shrinking" warning requires AGP 9.0+; defer until dependency upgrade and
  release smoke testing.
