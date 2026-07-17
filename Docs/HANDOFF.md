# DotCal Handoff

Updated: 2026-07-17

Source of truth for DotCal (`com.dotfield.dotcal`). Full history: `Docs/HANDOFF.original.md`. Feature spec: `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`. Do not touch `Docs/HANDOFF - Copy.md`.

## Resume Prompt

Continue DotCal in `D:\Caveman\caveman\Nothing-Calendar` on branch `pro-features`. Read `Docs/HANDOFF.md` and `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`. QR Event Share and Availability Text Generator complete. Next: C4 Dead Time Finder using existing shared `FreeSlotEngine`. Keep Room at 5 tables; no package/deep-link/DB filename changes, Hilt, or Compose Nav. After app changes run required tests/build, then install debug APK when device connected. Report exact manual QA steps and expected results.

## Hard Rules

- Workdir: `D:\Caveman\caveman\Nothing-Calendar`
- Branch: `pro-features`
- Package/application id: `com.dotfield.dotcal`
- Deep link scheme: `dotcal://`
- Room DB: `dotcal.db`
- Room schema locked: exactly 5 tables:
  - `calendar_accounts`
  - `calendar_events`
  - `event_reminders`
  - `sync_metadata`
  - `deleted_event_log`
- No schema migrations, new tables, or new columns without explicit approval.
- Manual DI only. No Hilt/Koin. No Compose Nav graph.
- Preserve existing UI/behavior unless task requires change.
- Offline-first. CalendarProvider sync only; no REST/OAuth/cloud/analytics.
- Side data uses shared `dotcal_side_store.json`, not Room.
- Do not start release hygiene, Advanced Reminder Profiles, Offline OCR, or i18n unless requested or blocking.
- Do not run manual phone QA unless requested.
- Update this file after completed app work.

## Required Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug
```

After successful build, install when device connected:

```powershell
C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

Always report what to test, how to test, and expected result.

## Current State

- Version: `versionCode 10`, `versionName 1.1.3`
- Product: black/white/red offline Android calendar.
- Tabs: Calendar, Tasks, Settings.
- Views: Year, Month, Week, Day, Agenda. Keep hidden ThreeDay unexposed.
- Events/tasks stored in `calendar_events`; tasks use `isTask = 1`.
- Existing media fields: `imageUris`, `voiceNotePath`.
- Existing recurrence field: `rrule`.
- DataStore: `calendar_preferences`.
- Pro entitlement: `KEY_IS_PRO`.
- Billing product: `dotcal_pro`; one-time purchase; option `dotcal-pro-lifetime`; live INR 149.
- Paywall route: `dotcal://paywall`.
- Billing library: `billing-ktx` 7.1.1; do not downgrade below v6.
- Internal-testing billing verification still pending.

## Completed Roadmap

- A4 Jump to Date
- C5 Punch-Card Day Complete
- Smart Quick Add v2
- B2 Countdowns / D-Day
- B4 Bulk Edit / Multi-Select
- B3 Drag-and-Drop Reschedule + Resize
- QR Event Share
- Availability Text Generator

Earlier complete: A1/A2/A3/A5, C2, B1, B5, Search, Templates, Calendar Sets, Shift Patterns, Private Vault, App Lock, import/export, backup/restore, widgets, holidays, birthdays, reminders, billing.

## Latest Feature

Availability Text Generator complete locally on `pro-features`.

- Pro entry: Calendar overflow > Share availability.
- Free users: Paywall.
- Week day-header long-press: same flow, seeded from pressed date.
- Controls: Next 3 days / This week / Next week, custom From/To, working hours, 15/30/45/60-minute minimum, all-day policy, ghost busy/free policy.
- Output: compact 12/24-hour-aware text; Copy and system Share.
- UI polish: compact balanced-spacing layout, content-bounded bordered preview card, vertically unclipped From/To date cards, stepper-style working-hours controls instead of slider, navigation-bar-safe actions, and stable enabled state so Copy/Share do not blink during refresh.
- Active Calendar Set respected through visible-account queries.
- Private Vault events excluded.
- Recurring events expanded.
- Shared UI-free `FreeSlotEngine` handles overlap/adjacency merging, midnight clipping, all-day events, minimum gaps, and ghost policy.
- JVM tests cover overlap, adjacency, midnight, empty/full calendars, all-day, minimum duration, ghost policy, and text formatting.
- No Room/package/deep-link/DB/Hilt/Nav changes.
- Secondary filled actions now use one shared rule: light uses surface with red outline/text; dark uses dark surface with subtle grey outline and white text/icons.
- Required tests/build passed after latest UI polish.
- Latest debug APK installed on device `000153573000720` after latest UI polish.
- No manual phone QA run.

Quick Add UI polish:

- Continue action moved into the main content area instead of pinned to the bottom, avoiding home/gesture bar crowding.

QR Event Share complete in commit `4fdb4c4`.

- Free QR scanner icon immediately left of `+`.
- Event Detail > More > Share as QR.
- DotCal QR scans into existing ICS import preview.
- UI polish: Share QR card renders event title/date/location inside the white QR image under the barcode, Save image follows the same secondary action color rule as Availability Copy, bottom actions and import preview action are navigation-bar-safe, and bottom actions have no outer border.

General UI polish:

- Event Details title-to-time spacing tightened for a closer title/time hierarchy.
- Agenda date headers and event cards use tighter vertical spacing for a denser date/event hierarchy.

## Next Roadmap

1. C4 Dead Time Finder
2. C6 Ghost Events / Pencil-In
3. C3 On This Day
4. C1 Life-in-Dots
5. C7 Year Wrapped
6. Vault Decoy PIN

C4 must reuse `FreeSlotEngine`; no duplicate gap logic. Add “Share availability for this day” cross-link.

## Manual QA

Availability:

- Pro: Calendar > overflow > Share availability. Expected: config opens with live preview.
- Change presets, custom dates, hours, and minimum slot. Expected: preview updates; short gaps disappear.
- Test empty, overlapping, adjacent, all-day, and midnight-crossing events. Expected: correct compact free-time text.
- Apply Calendar Set hiding a calendar. Expected: hidden events stop blocking slots.
- Toggle all-day and ghost policies. Expected: slots update.
- Switch 12/24-hour setting. Expected: matching output.
- Copy and Share. Expected: clipboard/share text matches preview.
- Long-press Week day header. Expected: Pro opens seeded range; Free opens Paywall.

QR:

- Event Detail > More > Share as QR. Expected: QR opens.
- Scan from Calendar top bar. Expected: DotCal QR opens ICS import preview.

## Worktree Notes

- Current app changes are uncommitted.
- User-owned untracked files: `Docs/HANDOFF - Copy.md`, `build-b4.log`. Leave untouched.
