# DotCal Handoff

Updated: 2026-07-13

Source of truth for DotCal (`com.dotfield.dotcal`). Full archive/history lives in `Docs/HANDOFF.original.md`; keep this file short and current.

Feature batch specs now live in `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`, which supersedes previous feature lists including `Docs/fable-suggested-feature.md`. For new feature work, read this handoff first, then read the final-package file and use it as the task spec unless the user overrides it.

## Hard Rules

- Use `$android-development` for Android work.
- Workdir: `D:\Caveman\caveman\Nothing-Calendar`.
- Branch: `main`.
- Preserve existing behavior/UI unless user explicitly changes it.
- Do not change package/application id `com.dotfield.dotcal`, deep link scheme `dotcal://`, or Room DB filename `dotcal.db`.
- Do not change Room schema unless user explicitly approves. Schema must remain exactly 5 tables: `calendar_accounts`, `calendar_events`, `event_reminders`, `sync_metadata`, `deleted_event_log`.
- No Hilt, no Compose Nav graph. Keep manual DI + boolean full-screen overlays.
- Do not run phone/manual UI QA unless user asks.
- Always tell the user what to test, how to test it, and the expected result after each completed change/fix. Keep the same test notes in this handoff when updating completion status.
- After app-code changes run:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

- After successful debug builds, install the debug APK on the connected phone unless user explicitly says not to:

```powershell
C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

- Keep `Docs/HANDOFF.md` updated after completed steps.

## Storage / Architecture

- Room tables locked at 5; no new columns.
- `calendar_events` stores events and tasks (`isTask = 1` for tasks).
- Existing media fields only: `imageUris`, `voiceNotePath`.
- Existing recurrence field only: `rrule`; custom recurrence must serialize there.
- DataStore name: `calendar_preferences`.
- Important DataStore keys include `KEY_APP_FONT`, `KEY_ACCENT_COLOR`, `KEY_IS_PRO`, widget settings, app lock PIN keys, and private vault IDs.
- Sync is CalendarProvider-only. No REST/OAuth/cloud/network sync.
- Local/export/backup/import features must stay offline-first.

## Current App State

- Product: premium black/white/red Android calendar app. App label: `DotCal`.
- Main tabs: `Calendar`, `Tasks`, `Settings`.
- Calendar views exposed: `Year`, `Month`, `Week`, `Day`, `Agenda`; hidden `ThreeDay` may exist internally but do not expose.
- Settings, Event Detail, Task Detail, Add/Edit Event, Paywall, and Pro tools use full-screen/right-slide or existing bottom-sheet patterns.
- `mono = FontFamily.SansSerif`; old app-wide mono typography is not desired.
- Font picker scope: top headings only use `LocalHeadingFont.current`; body rows/cards/controls remain `mono` or system for readability.
- Base accent colors stay free: Red, Blue, Green, Purple, Amber.
- Destructive actions need confirmation and use centered red text, not filled buttons.
- Notification action labels stay Title Case: `View`, `View Task`, `Snooze 10 Min`.

## Built / Active Features

- Events: Month/Week/Day/Agenda/Year, detail/edit, recurring instance edit/delete, deep links.
- Tasks: stored in `calendar_events`; filters All/Today/Upcoming/Completed; task detail/editor; task reminders; Task Time Blocking.
- Reminders: scheduler/receiver/boot reschedule/channel/snooze.
- Media: Pro-gated images and voice notes using existing fields.
- Birthdays: contacts import to existing tables, read-only.
- Global Holidays: bundled 2025-2031 data for IN/DE/GB/JP/IT/SA/US.
- Widgets: Glance widgets plus Widget Pack/config, Pro-gated large/config options.
- Onboarding: 5 pages; optional permissions; deep links skip onboarding.
- In-app updates: Play Flexible update check.
- Import/Export and Backup/Restore are FREE offline data-portability features.
- App Lock + Private Vault built.
- Global Search built.
- Quick Add built.
- Event/Task Templates built.
- Advanced Recurrence built.
- Recently Deleted built as file snapshots, not `deleted_event_log`.
- Calendar Sets / Focus Profiles built and committed/pushed earlier.
- Shift Pattern Builder + Bulk Apply built locally on `pro-features`.
- Font picker built and current heading coverage fixed.
- A2 Conflict Warning built and locally verified.
- A5 Scheduling Defaults + ISO Week Numbers built and locally verified.
- A1 Duplicate Event / Copy to Date built and locally verified by debug build/install.
- A3 Share Event built and locally verified by debug build/install.
- A4 Jump to Date built and locally verified by debug build/install.
- C5 Punch-Card Day Complete built and locally verified by debug build/install.
- Smart Quick Add v2 built and locally verified by debug build/install.
- B2 Countdowns / D-Day built and locally verified by debug build/install.
- C2 Day Density Forecast Strip built and locally verified by debug build/install.
- B5 Year-in-Pixels Heatmap built and locally verified by debug build/install.
- B1 Time Insights built and locally verified by debug build/install.

## Pro / Billing

- Billing code complete. Product active in Play Console; Google payment/merchant review clear per user on 2026-07-09.
- Product ID: `dotcal_pro`.
- Purchase option ID: `dotcal-pro-lifetime`.
- Type: one-time purchase / Buy.
- Live price: INR 149.
- Dependency: Google Play Billing Library `billing-ktx` v7.1.1; do not downgrade below v6.
- Permission: `com.android.vending.BILLING`.
- Paywall route: `dotcal://paywall`.
- Entitlement persists in `KEY_IS_PRO`; widgets refresh after purchase/restore.
- Next Play action: internal-testing billing verification with a tester account in `pro-tester`.

## Latest Work

- 2026-07-14: B2 countdown widget clipping fix completed on `main`. Root cause was vertical crowding inside the 2x2 Glance countdown widget after the new pinned D-Day layout: outer padding, 7-row dot digits, two text labels, and a 2-line title could overrun the available height and cut the number in half on-device. Tightened the countdown widget stack by reducing outer padding, shrinking dot cells/gaps, giving the digit grid a fixed 40dp slot, and reducing title/footer sizing so the pinned countdown number stays fully visible while preserving `D-DAY`, title, and `DAYS UNTIL`. No Room schema/package/deep-link/DB/billing/sync/onboarding changes. `:app:testDebugUnitTest` and `:app:assembleDebug` passed, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-13: B2 Countdowns / D-Day completed on `main`. Event Detail > More now supports `Pin as Countdown`, `Remove Countdown`, and `Share Countdown Image` for writable events. Countdown pins persist in the shared side-store namespace `countdown_pins` keyed by master event ID; no Room schema/package/deep-link/DB changes. Free users can keep exactly one active countdown; trying to pin a second opens a bottom sheet with `Unlock Unlimited` and `Swap to this countdown`, while Pro keeps unlimited pins. Pinned Event Detail shows a premium D-Day card with large dot-matrix day digits and a share action. Added a reusable `CardImageExporter` for countdown PNG sharing through the existing FileProvider cache path, intended for B2/C1/C7 reuse. The existing countdown widget now prefers the next active pinned countdown and renders the day count with dot rows; without pins it falls back to the existing next-event behavior. Added JVM tests for day-count math across DST, all-day-style dates, and same-day timed events. No Room schema/package/deep-link/DB/billing/sync/onboarding changes. `:app:testDebugUnitTest` and `:app:assembleDebug` passed, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-13: Smart Quick Add v2 completed on `main`. The existing Quick Add flow now uses an expanded pure-Kotlin on-device parser for English + Hinglish natural-language drafts, covering relative dates (`today`, `tomorrow`, `kal`, `parso`, `in 3 days`), weekdays (`next monday`, `agle somvar`), absolute dates (`14 march`, `march 14`, `14/3`, `14-03-2026`), times/ranges (`1pm`, `13:00`, `sham 5 baje`, `2-4pm`, `2pm to 4pm`), durations (`for 2 hours`, `2 ghante`), all-day date-only drafts, recurrence (`daily`, `roz`, `weekly`, `monthly`, `every/har <weekday>`, `every mon wed fri`), title cleanup, and past-time rollover. Quick Add remains Free and still pre-fills the existing event editor/save flow; no parallel creation path was added, so defaults, conflict warnings, reminders, accounts, and calendar-set behavior remain on the existing path. Preview now renders a dot-matrix chip row for Title/Date/Time/Repeats; tapping a chip continues into the existing editor for manual override. Added JVM parser tests with 30+ assertions covering the spec, Hinglish inputs, rollover, and graceful degradation. No Room schema/package/deep-link/DB/billing/sync/onboarding changes. `:app:testDebugUnitTest` and `:app:assembleDebug` passed, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-13: C5 Punch-Card feedback fix completed. Punch is no longer a tap-to-toggle control, so an accidental second tap after `1-day streak` does not clear the punch. Day view now shows the punch-card UI as a centered slim strip below the day date header instead of inside the previous/next navigation row. Tapping an unpunched day punches it; tapping an already-punched day leaves it punched; long-pressing a punched day clears it. No Room schema/package/deep-link/DB/billing/sync/onboarding changes. `:app:testDebugUnitTest` and `:app:assembleDebug` passed, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-13: C5 Punch-Card Day Complete completed on `main`. Added a reusable shared side-store at app-files `dotcal_side_store.json` with namespace/key/value JSON storage, in-memory cache, mutex protection, and suspend read/write/remove APIs. Punch-card state uses namespace `punchcard` keyed by ISO date string, with no Room schema/package/deep-link/DB/billing/sync/onboarding changes and no Pro gate. Day view header now shows a compact dot-matrix punch stamp; tapping it toggles the selected day, plays haptic feedback, fills the 5x5 dot stamp with an accent animation, persists the punch, and shows the computed consecutive streak label such as `6-day streak`. Streak math is pure Kotlin and covered across month boundaries; side-store namespace round-trip/remove behavior is covered by JVM tests. `:app:testDebugUnitTest` passed, `:app:assembleDebug` passed, and debug APK installed successfully. No manual phone UI QA run.
- 2026-07-13: A4 Jump to Date completed on `main`. Calendar overflow now includes free `Go to date`; long-pressing the calendar top title/month header opens the same picker, while short-tapping the title still jumps to Today. Day view's center date header also short-taps to Today and long-presses to the picker. The picker is a DotCal bottom sheet with a first-day-of-week row, date wheel, `Today` shortcut, and `Jump` action. Jumping preserves the current calendar view, updates the selected date/current month/year through existing ViewModel state, and briefly accent-highlights the target date cell/header with a 500ms fade. No Pro gate, Room schema, package, deep-link, DB, billing, onboarding, sync, task, or storage changes. `:app:assembleDebug` passed with only existing deprecated API warnings, debug APK installed successfully on device `4ab0d020`. No manual phone UI QA run.
- 2026-07-13: Roadmap source updated. User added `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`; it supersedes previous feature lists after keep/remove review. Locked build order is now: A4 Jump to Date, C5 Punch-Card Day Complete, Smart Quick Add v2, B2 Countdowns / D-Day, B4 Bulk Edit / Multi-Select, B3 Drag-and-Drop Reschedule + Resize, QR Event Share, Availability Text Generator, C4 Dead Time Finder, C6 Ghost Events / Pencil-In, C3 On This Day, C1 Life-in-Dots, C7 Year Wrapped, Vault Decoy PIN. Tier changes: B2 is Free 1 / Pro unlimited; C3 is Free. New shared utilities expected by upcoming work: side-store utility, FreeSlotEngine, and CardImageExporter. No app code changed; no build/install needed.
- 2026-07-12: Settings option-sheet style alignment completed on `main`. Shared Settings option sheets for Default view, Start of the week, Default reminder, Default event duration, and Sync interval now reuse the Font picker dialog style: no drag handle, skip-partial bottom sheet, 22dp side padding, bold 22sp heading, 10dp rounded option rows, cancel-surface unselected rows, accent-tinted selected row, border treatment, and 22dp check icon. Selection callbacks/persistence unchanged. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 1m 38s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Tasks segmented perceived-gap correction completed on `main`. Root cause was label-width perception: equal-width cells made short `All` leave a large visible text gap while long `Completed` crowded the right edge, even when cell gaps were mathematically equal. Tasks filter now measures each pill by its content width plus fixed padding, then computes one equal gap used before `All`, between every pill, and after `Completed`. No Room schema/package/deep-link/DB/sync/onboarding/settings behavior changes. `:app:assembleDebug` passed in 1m 16s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Tasks segmented edge-gap correction completed on `main`. Replaced the Tasks segmented-control Row/DP-width layout with a custom pixel layout so the left edge gap, all three internal gaps, and the right edge gap are exactly the same pixel value. Any leftover pixels from screen-width division are distributed into segment widths only, not into gaps. No Room schema/package/deep-link/DB/sync/onboarding/settings behavior changes. `:app:assembleDebug` passed in 1m 17s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Tasks segmented spacing correction completed on `main`. Tasks filter segmented control now uses a fixed 4dp gap and computes each of the four segment widths from the available inner width, avoiding weight/rounding differences that made All / Today / Upcoming / Completed gaps look uneven. No Room schema/package/deep-link/DB/sync/onboarding/settings behavior changes. `:app:assembleDebug` passed in 38s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Settings picker bottom-sheet redesign completed on `main`. Replaced Settings `DropdownMenu` pickers with shared bottom-sheet option dialogs for Default view, Start of the week, Default reminder, Default event duration, and Sync interval. Existing values, callbacks, persistence behavior, selected-check state, and row labels are preserved; Font and all-day reminder time already used bottom sheets and were left intact. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 1m 41s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Settings/tasks redesign feedback fixes completed on `main`. Tasks filter segmented control now uses explicit equal-width segments with equal 4dp gaps so All / Today / Upcoming / Completed spacing is consistent and labels stay compact. Settings sub-screens for Calendar Preferences, Reminder Defaults, Appearance, Widgets, Sync, Data & Restore, and App Lock & Private Vault now use the same grouped rounded panel language as the redesigned Settings root instead of the older divider-only layout. The root `App Lock & Private Vault` row no longer shows duplicate `Pro` text for non-Pro users, and Settings root/tool icon cells no longer draw the dark-theme outline border beside icons. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 2m 4s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Settings root Nothing OS-style redesign follow-up completed on `main`. Removed the top `DOTCAL / SETTINGS` micro-label, top-right dot matrix, and free-plan/account summary per user feedback. Icon cells now use transparent/subtle bordered treatment instead of filled dark blocks, and the DotCal Pro card now uses the app surface in dark theme instead of an inverted white card. Grouped rounded panels, icon-led rows, two-card Tools section, real Material `Widgets` icon, existing row destinations, Pro gating, Paywall behavior, Settings sub-screen overlays, data actions, and Time Insights/Date Calculator root placement are preserved. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 2m 6s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Settings root Nothing OS-style redesign completed on `main`. Settings root now uses a `DOTCAL / SETTINGS` hero with a dot-matrix accent, grouped rounded panels, icon-led rows, a two-card Tools section for Time Insights and Date Calculator, and a stronger DotCal Pro card. The Widgets row uses the real Material `Widgets` icon from the existing material-icons-extended dependency. Existing row destinations, Pro gating, Paywall behavior, Settings sub-screen overlays, data actions, and Time Insights/Date Calculator root placement are preserved. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 2m 16s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Settings root tool placement correction completed on `main`. Time Insights and Date Calculator moved from Settings > Calendar Preferences > Tools to the Settings root under a `Tools` section, preserving existing Pro gating and full-screen overlays. Calendar Preferences returns to calendar-only preferences. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 1m 51s, debug APK installed successfully on device `58fb7faf`. No manual phone UI QA run.
- 2026-07-12: Calendar overflow / Settings tool placement / Tasks segmented follow-up completed on `main`. Calendar three-dot menu is now mixed free + Pro: Search and New Event are free entries, while Quick Add, Templates, Calendar Sets, and Shift Patterns remain Pro-labeled. Time Insights and Date Calculator were removed from Calendar overflow and moved into Settings > Calendar Preferences > Tools with existing Pro gating. Calendar overflow Pro label is now plain small text, not a pill. Tasks filter segmented control was tightened so `Completed` fits without ellipsis by sharing segment width, reducing internal padding, and using 13sp labels. No Room schema/package/deep-link/DB/sync/onboarding changes. `:app:assembleDebug` passed in 2m 39s with only an existing deprecated credential API warning. Debug APK installed successfully on device `58fb7faf`. No manual phone UI QA run.
- 2026-07-12: Calendar Pro labeling + Paywall UI redesign completed on `main`. Calendar tab three-dot menu now shows compact `Pro` badges on paid tool entries: Quick Add, Templates, Calendar Sets, Time Insights, Date Calculator, and Shift Patterns; Search remains unbadged/free. Paywall was redesigned with a clear `DotCal Pro` top bar, premium hero card, lifetime price row, benefit cards, scrollable Pro feature list, and a pinned bottom purchase area so the primary `Unlock Pro - {price}` CTA plus Restore Purchase remain visible. Billing flow, product ID, ProManager gating, schema/package/deep-link/DB/sync/onboarding behavior unchanged. `:app:assembleDebug` passed in 2m 44s. Debug APK install attempted, but ADB reported `no devices/emulators found`; no APK installed and no manual phone UI QA run.
- 2026-07-12: Settings sub-screen cleanup completed. Settings root now shows category rows instead of dense inline controls: Calendar Accounts, Calendar Preferences, Reminder Defaults, Appearance, Widgets, App Lock & Private Vault, Sync, Data & Restore, About, and DotCal Pro. Moved Week start / Default view / Week numbers / Birthday calendar / Global Holidays into Calendar Preferences; moved Default reminder / Default event duration / Default all-day reminder time into Reminder Defaults; moved Font into Appearance with Theme/Accent; moved Transparent Widgets / Widget Dot Texture into Widgets; moved Export / Import / Backup / Restore / Recently Deleted into Data & Restore. Existing callbacks, Pro gates, offline data actions, and full-screen/right-slide settings pattern preserved. No Room schema/package/deep-link/DB/onboarding/sync-provider changes. `:app:assembleDebug` passed in 1m 55s, debug APK installed successfully on device `4ab0d020` after adb reconnect recovered the device from `offline`. No manual phone UI QA run.
- 2026-07-12: Settings/Search/Shift feedback fixes completed. Search filter dropdowns no longer show a literal `v`; they now use the standard down-arrow icon. Shift Patterns is now included in the root back-handler stack, so Android back gesture closes Shift Patterns and returns to Calendar instead of exiting the app. Settings root now has a single `Sync` row showing `Off` or the sync interval; the old root-level `Sync enabled`, `Sync interval`, and `Sync now` rows moved into a dedicated full-screen Sync settings page. No Room schema/package/deep-link/DB/onboarding/sync-provider changes. `:app:assembleDebug` passed in 2m 29s, debug APK installed successfully on device `4ab0d020`. No manual phone UI QA run.
- 2026-07-12: Settings cleanup/tool relocation completed. Calendar tab three-dot menu now owns calendar/action tools: Search, Quick Add, Templates, Calendar Sets, Time Insights, Date Calculator, and Shift Patterns. Settings no longer shows direct rows for Date Calculator, Time Insights, Templates, Calendar Sets, or Shift Patterns; Settings remains focused on accounts, calendar preferences, reminders, appearance/widgets, sync, privacy/data, billing, and about. Existing Pro gates/full-screen overlays are reused: non-Pro tool taps open Paywall, Pro tools open their existing screens, and Search stays free. No Room schema/package/deep-link/DB/onboarding/sync/holiday/task changes. `:app:assembleDebug` passed in 2m 34s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-12: Settings cleanup plan recorded before implementation. User wants feature launchers moved out of the crowded Settings tab where reasonable. Plan: Calendar tab three-dot menu should own calendar/action tools: Quick Add, Templates, Search, Go/Jump to date, Calendar Sets / Focus Profiles, Time Insights, Date Calculator, and Shift Patterns. Settings should remain mostly configuration/data/billing: DotCal Pro, Restore purchase, Theme/Accent/Font, Default view, Week numbers, Default event duration, Calendar accounts, Notifications/Sync, App Lock & Private Vault, Import/Export, Backup/Restore, and Recently Deleted under Privacy/Data. Implementation should preserve existing full-screen overlay/Paywall behavior and avoid Room schema/package/deep-link/DB changes. Build/install after code changes.
- 2026-07-12: B1 Time Insights completed. Settings > Additional now has a Pro-gated Time Insights row. Pro users open a full-screen Time Insights overlay with This week / This month / Custom range chips, custom date pickers, total scheduled hours, timed event count, busiest day, task completion rate, per-calendar colored hour bars, and a 7-column weekday load chart. Stats are computed in-memory from existing `calendar_events` and `calendar_accounts`; hour totals exclude all-day events, tasks, and BIRTHDAY-source rows, while task completion rate uses tasks in the selected range. Non-Pro users open the existing Paywall. No Room schema/package/deep-link/DB/onboarding/sync/holiday/task changes. `:app:assembleDebug` passed in 2m 43s after cleanup, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-10: B5 Year view feedback correction completed. Shared DotCal toggle now preserves the Year Heatmap Material switch style and reuses it in Settings toggles and the Event Editor All-day control, matching the user's requested direction. Filled heatmap markers for 1/2/3+ event days were tightened further so red fill no longer cuts into the date number; 0-event faint outline behavior remains unchanged. No Room schema/package/deep-link/DB/onboarding/sync/holiday/task changes. `:app:assembleDebug` passed in 1m 36s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-10: B5 Year view device-feedback fix completed. Year mini-month cells no longer draw previous/next-month dates inside each month. Heatmap circles were resized so faint 0-event outlines and 1/2/3+ red fills sit behind the date without cutting the date number; later correction tightened filled markers further and made the Year Heatmap switch style the shared toggle style for Settings/Event controls. No Room schema/package/deep-link/DB/onboarding/sync/holiday/task changes. `:app:assembleDebug` passed in 2m 52s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-10: B5 Year-in-Pixels Heatmap completed and C2 visibility follow-up fixed. Year view now has a Pro-gated Heatmap toggle above the year grid. Toggle state persists in DataStore via `KEY_YEAR_HEATMAP`. Heatmap density uses a new year-scoped repository/ViewModel stream so all visible-year events are counted, including expanded recurring occurrences, without changing Room schema. Density excludes all-day events, tasks, and BIRTHDAY-source rows; 0-day cells render faint outlines while 1/2/3+ counts render increasing accent intensity. Non-Pro toggle opens Paywall. C2 Agenda density strip is now pinned above the Agenda list instead of being part of the auto-scrolled list, fixing the case where it appeared missing after Agenda scrolled to the selected date. No Room schema/package/deep-link/DB/onboarding/sync/holiday/task changes. `:app:assembleDebug` passed in 3m 14s, debug APK installed successfully on device `4ab0d020`. No manual phone UI QA run.
- 2026-07-10: C2 Day Density Forecast Strip completed. Agenda now renders a slim next-7-days density strip above the agenda list. Each day is labeled by weekday initial and uses four dot intensity/size levels based on total scheduled timed event minutes, excluding all-day events, tasks, and BIRTHDAY-source rows. Tapping a dot selects that date and scrolls Agenda to the matching date header when present. Computation derives from the existing expanded agenda stream in `DotCalViewModel`; no storage, DataStore key, Room schema, package/deep-link, sync, holiday, onboarding, or task changes. `:app:assembleDebug` passed in 2m 42s. Debug APK install succeeded per latest resume status. No manual phone UI QA run.
- 2026-07-10: Task Detail action layout and Event Edit back behavior follow-up completed after device feedback. Task Detail top-right Edit text was replaced with a three-dot More button. More opens a compact bottom sheet with Edit, Add to Calendar, and Move/Restore Private Vault as applicable. Task Detail body now only shows Mark Complete/Reopen Task and Delete Task. Event Detail -> More -> Edit no longer closes Event Detail before opening the editor, so Android back gesture from the event editor returns to Event Detail instead of Month view; saving from that path refreshes the detail row. No Room schema/package/deep-link/DB changes. `:app:assembleDebug` passed in 1m 32s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-10: Event Detail action layout polish completed after device feedback. Top-right Edit text was replaced with a three-dot More button. More opens a compact bottom sheet with Edit, Share, Duplicate, Copy to date, and Move/Restore Private Vault actions as applicable. Event Detail body now only shows the destructive Delete Event action for writable events, fixing the clipped `Copy to date...` label and the oversized two-option Share sheet by reusing a compact action-sheet layout. No Room schema/package/deep-link/DB changes. `:app:assembleDebug` passed in 2m 13s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-10: A3 Share Event completed. Event Detail now shows Share for all events. Share opens a bottom sheet with Share as text and Share as .ics. Text payload includes title, date/time respecting `KEY_24_HOUR_FORMAT`, location, and notes. `.ics` payload reuses `IcsExporter` for a single event with reminders and `rrule`, writes only to cache through a new `FileProvider`, shares mime `text/calendar`, and excludes images/voice notes. No Room schema/package/deep-link/DB changes. `:app:assembleDebug` passed in 2m 35s, debug APK installed successfully. No manual phone UI QA run.
- 2026-07-10: A1 Duplicate Event / Copy to Date completed. Event Detail now shows Duplicate and Copy to date actions for writable non-birthday/non-holiday events. Duplicate opens the existing event editor with a new draft id, copied title/account/duration/rrule/color/description/location and existing reminders; media attachments are intentionally not copied. Copy to date opens the existing date picker first, then opens the same draft editor on the chosen date while preserving duration. Repository event saves now accept an optional reminder list for duplicate/import-style full reminder preservation while keeping the existing single-reminder editor behavior. Duplicates save through the existing local insert path with no reused event id or Google id. No Room schema/package/deep-link/DB changes. `:app:assembleDebug` passed in 3m 56s, debug APK installed successfully.
- 2026-07-10: A5 device-feedback follow-up completed. Default view startup now seeds from a lightweight boot preference mirror so a saved Week default opens directly in Week instead of flashing Month first, and default-view changes update both boot prefs and DataStore. Month view week-number labels now share each date row's computed height, keeping `W##` horizontally centered with the date cells instead of floating above/missing the month row. No Room schema/package/deep-link/DB changes. `:app:assembleDebug` passed in 38s, debug APK installed successfully on device `4ab0d020`.
- 2026-07-10: A5 Scheduling Defaults + ISO Week Numbers completed. Added DataStore keys `KEY_DEFAULT_EVENT_DURATION` and `KEY_SHOW_WEEK_NUMBERS`; Settings now exposes Default view, Week numbers, and Default event duration. New event editor end time uses the selected default duration (15/30/60/90/120 minutes, default 60). Month and Week views render ISO-8601 week numbers when enabled while preserving `KEY_WEEK_START` row layout. No Room schema/package/deep-link/DB changes. `:app:assembleDebug` passed in 2m 33s, debug APK installed successfully on device `4ab0d020`.
- 2026-07-10: A5 follow-up bugfix completed after device feedback. Default view picker now updates the active calendar tab immediately and follows persisted DataStore value on startup. Week number labels now render as visible `W##` values in a fixed 36dp leading column so Month day cells remain visible; Week view time/all-day columns align to the same width when week numbers are enabled. `:app:assembleDebug` passed in 1m 56s, debug APK installed successfully on device `4ab0d020`.
- 2026-07-10: A2 Conflict Warning reviewed as implemented. It uses a 300ms debounced ViewModel query for visible timed non-completed non-birthday events, expands recurring events, excludes the edited event, shows inline max-3 warnings plus `+N more`, and respects `KEY_24_HOUR_FORMAT`. Cosmetic note remains: time range uses hyphen instead of en dash. Debug build and install had passed before A5.
- 2026-07-09: Added `Docs/fable-suggested-feature.md` as companion feature-spec source for the next DotCal feature batch. Ranked implementation preference: A2 Conflict Warning, A5 Scheduling Defaults + ISO Week Numbers, A1 Duplicate / Copy to Date, A3 Share Event, C2 Day Density Forecast Strip, B5 Year-in-Pixels Heatmap, B1 Time Insights, C5 Punch-Card Day Complete, C4 Dead Time Finder, B2 Countdowns, C3 On This Day, C1 Life-in-Dots, C7 Year Wrapped, B4 Bulk Edit, C6 Ghost Events, B3 Drag-and-Drop Reschedule + Resize.
- 2026-07-09: Font picker heading coverage fixed. Updated Event/Task detail headers and hero titles, Event/Task editor headers, date/time choice sheet headers, repeat/custom-repeat sheet headers, shared choice sheet headers, and Agenda date headers to use `LocalHeadingFont.current`. Body text, rows, numeric controls, and empty states remain `mono`/system per scope. `:app:assembleDebug` passed in 2m 41s, debug APK installed successfully.
- 2026-07-09: Font picker port built. Resources: `res/font/ndot.ttf`, `res/font/ntype82.otf`; model `AppFont`; root `LocalHeadingFont`; Settings > Additional > Font bottom sheet. NType82 intentionally kept per user decision.
- 2026-07-09: Refactor Steps 1-8 completed. UI split into `CalendarViews.kt`, `DotCalTheme.kt`, `AppChrome.kt`, `EventScreens.kt`, `TaskScreens.kt`, `SettingsScreens.kt`, `ProFeatureScreens.kt`, `OnboardingScreens.kt`, `AgendaScreens.kt`, `DialogScreens.kt`, `UiModels.kt`, and `UiHelpers.kt`. `DotCalApp.kt` now mostly wires root state/launchers/overlays. Behavior intended unchanged.
- 2026-07-09: Settings Theme value polish and mojibake sweep completed. Debug builds passed and installed.
- 2026-07-09: Search filter UI compacted to three dropdown selectors.

Current dirty files may include earlier Pro/UI polish and release assets. Do not revert unrelated user/local changes. At last check there was also an untracked `Docs/HANDOFF - Copy.md`; leave it alone unless user asks.

## Current Next Step

- Current roadmap state: A2, A5, A1, A3, A4, C2, B5, B1, C5, Smart Quick Add v2, and B2 Countdowns / D-Day are complete. Next implementation is B4 Bulk Edit / Multi-Select unless the user picks another feature. Then continue in this order: B3 Drag-and-Drop Reschedule + Resize, QR Event Share, Availability Text Generator, C4 Dead Time Finder, C6 Ghost Events / Pencil-In, C3 On This Day, C1 Life-in-Dots, C7 Year Wrapped, Vault Decoy PIN.

- Play/Internal-testing billing verification remains the next product check.
- Advanced Reminder Profiles remains NOT started; do not start without explicit confirmation.
- Offline OCR remains possible later; do not start unless user asks.
- Language picker scaffold/i18n remains TO BUILD; string extraction is a dedicated effort.

## What To Test Next

- B2 Countdowns / D-Day:
  - Open any writable future event > More > Pin as Countdown. Expected: the event detail shows a Countdown card with large dot-matrix day digits and `Share as image`.
  - Tap More again. Expected: actions include `Remove Countdown` and `Share Countdown Image`.
  - Tap `Share Countdown Image`. Expected: Android share sheet opens with a PNG card showing the day count, event title, and DotCal wordmark.
  - Add the DotCal Countdown widget after pinning. Expected: the widget prefers the pinned event, shows `D-DAY`, dot-matrix day digits, the title, and `DAYS UNTIL`; the number is fully visible and not cut in half. With no pins it falls back to the existing next-event countdown.
  - As a free user with one countdown already pinned, try to pin a second event. Expected: bottom sheet says `1 countdown active`, offers `Unlock Unlimited`, and offers `Swap to this countdown`.
  - Tap `Swap to this countdown`. Expected: the old countdown unpins, the new event becomes the active countdown, and the widget updates.
  - As Pro, pin multiple writable future events. Expected: no free-limit sheet appears and pins persist after app restart.
  - Tap `Remove Countdown`. Expected: the detail Countdown card disappears and the widget uses the next remaining pinned countdown or falls back to next event.

- Smart Quick Add v2:
  - Open Calendar > three-dot menu > Quick Add. Expected: a single free-text input appears with examples below it.
  - Type `Lunch with Rahul kal 1pm`. Expected: preview chips show title `Lunch with Rahul`, tomorrow's date, `1:00 PM-2:00 PM`, and no repeat; Continue opens the existing event editor pre-filled with those values.
  - Type `kal sham 5 baje dentist`. Expected: title is `dentist`, date is tomorrow, and time is 5:00 PM.
  - Type `har somvar gym 7am` or `every mon wed fri training 7am`. Expected: preview shows the parsed date/time and a weekly repeat label; Continue opens the normal editor with recurrence pre-filled.
  - Type `mummy ka birthday 14 march`. Expected: preview shows the date as an all-day draft and title stays `mummy ka birthday`.
  - Type `deep work 9am for 2 hours` or `study 2pm to 4pm`. Expected: editor opens with the correct start/end range.
  - Type `random text`. Expected: title is preserved, the flow degrades to the legacy default draft instead of crashing or blocking save.
- C5 Punch-Card Day Complete:
  - Open Calendar > Day view. Expected: a centered slim punch-card strip appears below the day date header, not inside the previous/next arrow row.
  - Tap the punch strip. Expected: the stamp fills with animated accent dots, haptic feedback fires, and the label changes from `Complete day` to `1-day streak`.
  - Tap the punched strip again. Expected: it stays punched and still shows `1-day streak`; a normal second tap does not clear it.
  - Move to the next day and tap the punch stamp. Expected: the label shows `2-day streak`; continue across a month boundary and the streak continues.
  - Return to a punched day or restart the app. Expected: the stamp remains filled because state persists in the shared side-store.
  - Long-press a filled punch strip. Expected: the day unpunches, the stamp returns to the quiet dotted state, and streak count recalculates.
- A4 Jump to Date:
  - Open Calendar in Month view, tap the top date title. Expected: selected date jumps to today, current month updates, and today's cell briefly accent-highlights.
  - Long-press the top date title/month header. Expected: `Go to date` bottom sheet opens with a date wheel, first-day-of-week row matching Settings, `Today` shortcut, and `Jump` button.
  - Pick a date in another month and tap `Jump`. Expected: the app stays in Month view, moves to that month/date, and the target date cell flashes with the 500ms accent highlight.
  - Open Calendar > three-dot menu > Go to date. Expected: the same free picker opens; no Paywall appears.
  - Repeat from Week, Day, Agenda, and Year. Expected: the current view is preserved, selected date/year updates, and visible target date/header highlights where that view has a date cell/header.
  - In Day view, short-tap the center date header. Expected: jumps to today. Long-press the same header. Expected: opens the Go to date picker.
- Settings option-sheet style alignment:
  - Open Settings > Appearance > Font. Expected: existing Font picker style remains unchanged.
  - Open Settings > Calendar Preferences > Start of the week and Default view. Expected: sheets visually match Font picker style: no handle, same padding, bold title, bordered rounded rows, accent selected row/check.
  - Open Settings > Reminder Defaults > Default reminder and Default event duration. Expected: same Font picker-style sheet and existing value updates still work.
  - Open Settings > Sync > Sync interval. Expected: same Font picker-style sheet and sync interval behavior unchanged.
- Tasks segmented perceived-gap correction:
  - Open Tasks tab. Expected: visible gap before `All`, between `All`/`Today`, `Today`/`Upcoming`, `Upcoming`/`Completed`, and after `Completed` looks equal by label/pill edges.
  - Switch filters. Expected: selected pill wraps its label cleanly, gaps stay equal, and `Completed` no longer crowds the right edge.
- Tasks segmented edge-gap correction:
  - Open Tasks tab. Expected: gap before `All`, gaps between all buttons, and gap after `Completed` are visually identical.
  - Switch between filters including `Completed`. Expected: selected pill moves but outer edge gaps and internal gaps do not change.
- Tasks segmented spacing correction:
  - Open Tasks tab. Expected: All / Today / Upcoming / Completed segments have identical visible 4dp gaps between each button.
  - Switch between all four filters. Expected: selected background moves without changing segment widths or gaps, and `Completed` stays fully visible.
- Settings picker bottom-sheet redesign:
  - Open Settings > Calendar Preferences, tap Start of the week. Expected: bottom sheet opens with week options, selected option highlighted with a check; choosing one updates the row and closes the sheet.
  - Open Settings > Calendar Preferences, tap Default view. Expected: bottom sheet opens for calendar view choices and persists selection as before.
  - Open Settings > Reminder Defaults, tap Default reminder and Default event duration. Expected: each opens a bottom sheet, selected item shows a check, and picked value updates immediately.
  - Open Settings > Sync, tap Sync interval. Expected: bottom sheet opens with Off / 15 / 30 / 60 minute choices and existing sync behavior remains unchanged.
  - Use Android back or outside tap while a picker sheet is open. Expected: sheet dismisses and stays on the current Settings sub-screen.
- Settings/tasks redesign feedback fixes:
  - Open Tasks tab. Expected: All / Today / Upcoming / Completed segments have equal gaps and `Completed` does not truncate.
  - Open Settings root in dark theme. Expected: option row icons have no visible border box around them; locked Pro icon tint remains red/subtle.
  - As non-Pro, check Settings root `App Lock & Private Vault`. Expected: only one small `Pro` label appears, not both a value and trailing label.
  - Open Settings > Calendar Preferences, Reminder Defaults, Appearance, Widgets, Sync, Data & Restore, and App Lock & Private Vault. Expected: each sub-screen uses rounded grouped panels matching Settings root rather than old loose divider rows.
  - Use toolbar back and Android back from those sub-screens. Expected: returns to Settings root with existing behavior.
- Settings root Nothing OS-style redesign:
  - Open Settings root. Expected: top shows only the large `Settings` title; no `DOTCAL / SETTINGS` label, no plan/account summary, and no top-right dot matrix.
  - Scroll Settings root. Expected: Accounts, Settings, Tools, About, and DotCal Pro appear as rounded grouped surfaces, not loose divider-only rows.
  - Check Settings rows. Expected: rows have left icons in subtle bordered cells without filled black blocks, readable title/value text, and existing destinations still open: Calendar Preferences, Reminder Defaults, Appearance, Widgets, App Lock & Private Vault, Sync, and Data & Restore.
  - Check Tools. Expected: Time Insights and Date Calculator appear as two compact cards under Tools; non-Pro taps open Paywall, Pro taps open the existing tool screens.
  - Check DotCal Pro card. Expected: card uses the app surface in dark theme instead of a bright white card, opens the existing Paywall/Pro flow, shows active/unlocked copy for Pro users, and does not affect billing behavior.
  - Check light and dark themes. Expected: grouped panels, icon cells, text, and Pro card remain readable with no clipped row text.
- Calendar overflow / Settings tools / Tasks segmented follow-up:
  - Open Calendar > three-dot menu. Expected: Search and New Event show as free entries; Quick Add, Templates, Calendar Sets, and Shift Patterns show a small plain `Pro` text label, not a pill.
  - In the same menu, confirm Time Insights and Date Calculator are no longer listed. Expected: menu is not mostly Pro-only.
  - Open Settings root. Expected: Tools section contains Time Insights and Date Calculator with existing Pro behavior.
  - Open Settings > Calendar Preferences. Expected: no Tools section; only calendar preferences remain.
  - As non-Pro, tap Time Insights or Date Calculator from Settings root. Expected: Paywall opens.
  - As Pro, tap Time Insights or Date Calculator from Settings root. Expected: the existing full-screen tool opens.
  - Open Tasks tab. Expected: segmented filters All / Today / Upcoming / Completed fit without `...`.
- Calendar Pro labels + Paywall redesign:
  - Open Calendar and tap the top-right three-dot menu. Expected: Quick Add, Templates, Calendar Sets, and Shift Patterns each show small plain `Pro` text; Search and New Event have no Pro label.
  - As non-Pro, tap any Pro-badged menu item. Expected: Paywall opens using the existing overlay/gating behavior.
  - On Paywall, check the first screen and bottom area. Expected: `Unlock Pro - {price}` button is visible in a pinned bottom purchase area, with Restore Purchase below it; the feature list scrolls without hiding the pay button.
  - Disconnect billing/network or before product details load. Expected: CTA shows a disabled connecting state until billing connects; no crash.
- Settings sub-screen cleanup:
  - Open Settings root. Expected: root shows category rows, not inline controls for Week start, Default view, reminders, widgets, or data import/export.
  - Tap Calendar Preferences. Expected: Start of the week, Default view, Week numbers, Birthday calendar, and Global Holidays appear; toolbar back and Android back return to Settings root.
  - Tap Reminder Defaults. Expected: Default reminder, Default event duration, and Default all-day reminder time appear and existing pickers still work.
  - Tap Appearance. Expected: Font row appears with Theme and Accent controls; changing font/theme/accent still updates the app.
  - Tap Widgets. Expected: Transparent Widgets and Widget Dot Texture rows appear with existing Pro behavior.
  - Tap Data & Restore. Expected: Export Calendar, Import Calendar, Back Up Data, Restore Data, and Recently Deleted appear; free data actions still launch their existing pickers/screens.
- Settings/Search/Shift feedback fixes:
  - Open Calendar > three-dot menu > Search. Expected: filter dropdowns show a normal down-arrow icon, not a literal `v`, and each filter still opens/selects correctly.
  - Open Calendar > three-dot menu > Shift Patterns, then use Android back gesture. Expected: Shift Patterns closes and returns to Calendar; the app does not exit.
  - Open Settings. Expected: root shows one `Sync` row only; root no longer shows separate `Sync enabled`, `Sync interval`, or `Sync now` rows.
  - Tap Settings > Sync. Expected: full-screen Sync page opens with Sync enabled toggle, Sync interval picker, and Sync now/status row.
  - From Settings > Sync, use the toolbar back and Android back gesture. Expected: both return to Settings root.
- Settings cleanup/tool relocation:
  - Open Calendar and tap the top-right three-dot menu. Expected: Search, New Event, Quick Add, Templates, Calendar Sets, and Shift Patterns appear in the menu.
  - Tap Search. Expected: Search opens normally and does not require Pro.
  - As Pro, tap each Pro tool from Calendar three-dot menu. Expected: existing full-screen tool opens and back returns to Calendar.
  - As non-Pro, tap Quick Add/Templates/Calendar Sets/Shift Patterns. Expected: existing Paywall opens.
  - Open Settings > Calendar Preferences. Expected: Time Insights and Date Calculator live under Tools; settings remain mostly focused on accounts/preferences/reminders/appearance/sync/data/about.
- B1 Time Insights:
  - As Pro, open Settings > Additional > Time Insights. Expected: a full-screen Time Insights screen slides in with This week / This month / Custom controls and summary cards.
  - Switch between This week and This month. Expected: scheduled hours, event count, busiest day, calendar bars, and weekday chart update for the selected range.
  - Tap Custom, choose From and To dates. Expected: date picker opens for each row; stats recompute for the chosen inclusive range.
  - Compare timed events, all-day events, tasks, and birthdays. Expected: scheduled-hour totals exclude all-day events, tasks, and BIRTHDAY-source events; task completion card counts tasks separately.
  - As non-Pro, tap Settings > Additional > Time Insights. Expected: Paywall opens and the insights screen does not unlock.
- Last completed Year view feedback correction:
  - Open Calendar > Year and compare the Heatmap toggle with Settings toggles and Event Editor > All-day. Expected: Settings and All-day now use the Year Heatmap switch style, not the other way around.
  - In Calendar > Year with Heatmap on, check dates with 1, 2, and 3+ timed events. Expected: red fill is visible but smaller/tighter; date numbers are not cut.
  - Check a 0-event day. Expected: faint outline remains unchanged and readable.
- Last completed Year view feedback fix:
  - Open Calendar > Year, turn Heatmap on. Expected: Heatmap toggle height/shape matches Settings toggles; Event Editor > All-day uses the same toggle style too.
  - Check any month grid in Year view. Expected: only that month's dates are shown; previous/next-month dates are blank.
  - Check dates with 0, 1, 2, and 3+ timed events. Expected: 0 events shows a faint outline; 1 is light red; 2 is medium red; 3+ is solid red; date numbers remain readable and are not cut by the circles.
  - Tap a month in Year view. Expected: existing Year navigation still opens that month; heatmap drawing does not block taps.
- C2 Day Density Forecast Strip fix:
  - With your events on dates 11 through 16, open Calendar > Agenda. Expected: the 7-dot strip is always visible directly under the top calendar controls, not hidden by Agenda auto-scroll.
  - Tap each dot in the strip. Expected: selected date changes and Agenda scrolls to that date's event header when that date has events.
  - Compare days with more timed events against fewer/empty days. Expected: dots get larger/darker with more scheduled timed minutes; all-day events, tasks, and birthdays do not increase dot intensity.
- B5 Year-in-Pixels Heatmap:
  - Open Calendar > Year. Expected: a Heatmap row appears above the year grid with a 0/1/2/3+ legend and a toggle.
  - As Pro, turn Heatmap on. Expected: year days show faint outlines for 0 timed events and stronger red dots for 1, 2, and 3+ timed events.
  - Check your 11 through 16 event dates in Year view. Expected: those dates are visibly highlighted; dates with more timed events are stronger.
  - Tap a highlighted month/day area as before. Expected: Year navigation behavior still opens Month view; heatmap does not block existing selection.
  - Restart app or leave/reopen Year view. Expected: Heatmap toggle state stays saved.
  - As non-Pro, tap Heatmap toggle. Expected: Paywall opens and Heatmap does not unlock.
- Last completed detail action polish:
  - Event Detail edit back stack: open an event, tap top-right More, tap Edit, then use Android back gesture. Expected: the app returns to Event Detail, not Month view; saving from this path refreshes the same detail screen.
  - Event Detail More menu: open a writable event, tap top-right More. Expected: bottom sheet shows Edit, Share, Duplicate, Copy to date, and Move/Restore Private Vault when applicable; Event Detail body only shows Delete Event.
  - Task Detail More menu: open a task, tap top-right More. Expected: bottom sheet shows Edit, Add to Calendar, and Move/Restore Private Vault when applicable; Task Detail body only shows Mark Complete/Reopen Task and Delete Task.
- C2 Day Density Forecast Strip: open Agenda with timed events across the next 7 days. Expected: a slim 7-day dot strip appears above the agenda list, dot intensity/size reflects scheduled timed event load, all-day/tasks/birthdays do not increase density, and tapping a dot selects that date and scrolls to its agenda header when present.
- Font picker: switch Ndot/NType/System and verify all top headings update, while body rows stay readable.
- Billing/internal test: Settings > DotCal Pro opens Paywall, live INR 149 appears, test purchase works, gates unlock, Restore works after fresh install.
- Quick regression: Calendar views, Event/Task detail/editor, Settings root/subscreens, Paywall, Search, Quick Add, Templates, Calendar Sets, Shift Patterns, Recently Deleted, Date Calculator.
- Shift Patterns: generated shifts span real duration in Week/Day, no duplicate regenerate, delete pattern removes generated events, bulk Month template stamping creates one-off events only.

## Resume Prompt

Continue DotCal development in `D:\Caveman\caveman\Nothing-Calendar` on branch `main`.

First read `Docs/HANDOFF.md`; it is source of truth. Respect Hard Rules, schema lock, Pro/Billing status, and current next step.

For new feature work, also read `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`; it supersedes previous feature lists and contains the locked 14-feature roadmap. Next feature is B2 Countdowns / D-Day unless the user picks another item.

Latest status: `versionCode 9` / `versionName 1.1.3`; `:app:testDebugUnitTest` and `:app:assembleDebug` passing. Latest debug APK installed successfully. Latest completed app-code change is Smart Quick Add v2. Quick Add now has an expanded pure-Kotlin English + Hinglish parser and dot-matrix preview chips, still pre-filling the existing event editor/save flow. No manual phone UI QA run unless explicitly asked.

Strict: do not change Room schema, package id, deep links, DB filename, onboarding/calendar/sync/holidays/tasks unless required by the task. No Hilt, no Compose Nav graph. Build after app-code changes with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`, then install debug APK on the connected phone unless user says not to. Do not run manual phone UI QA unless explicitly asked. Do not start Advanced Reminder Profiles or Offline OCR without confirmation.
