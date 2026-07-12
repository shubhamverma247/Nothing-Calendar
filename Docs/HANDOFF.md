# DotCal Handoff

Updated: 2026-07-12

Source of truth for DotCal (`com.dotfield.dotcal`). Full archive/history lives in `Docs/HANDOFF.original.md`; keep this file short and current.

Feature batch specs live in `Docs/fable-suggested-feature.md`. For new feature work, read this handoff first, then read `Docs/fable-suggested-feature.md` and use it as the task spec unless the user overrides it.

## Hard Rules

- Use `$android-development` for Android work.
- Workdir: `D:\Caveman\caveman\Nothing-Calendar`.
- Branch: `pro-features`.
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

- For app feature work, read `Docs/fable-suggested-feature.md`; A2, A5, A1, A3, C2, B5, and B1 are complete. Suggested next implementation from the user's current ranked list is C5 Punch-Card Day Complete unless the user picks another feature. A4 Jump to Date and C4 Dead Time Finder remain available from the original suggested build order.
- Play/Internal-testing billing verification remains the next product check.
- Advanced Reminder Profiles remains NOT started; do not start without explicit confirmation.
- Offline OCR remains possible later; do not start unless user asks.
- Language picker scaffold/i18n remains TO BUILD; string extraction is a dedicated effort.

## What To Test Next

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
  - Open Calendar and tap the top-right three-dot menu. Expected: Search, Quick Add, Templates, Calendar Sets, Time Insights, Date Calculator, and Shift Patterns appear in the menu.
  - Tap Search. Expected: Search opens normally and does not require Pro.
  - As Pro, tap each Pro tool from Calendar three-dot menu. Expected: existing full-screen tool opens and back returns to Calendar.
  - As non-Pro, tap Quick Add/Templates/Calendar Sets/Time Insights/Date Calculator/Shift Patterns. Expected: existing Paywall opens.
  - Open Settings. Expected: direct feature rows for Date Calculator, Time Insights, Templates, Calendar Sets, and Shift Patterns are gone; settings remain focused on accounts/preferences/reminders/appearance/sync/data/about.
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

Continue DotCal development in `D:\Caveman\caveman\Nothing-Calendar` on branch `pro-features`.

First read `Docs/HANDOFF.md`; it is source of truth. Respect Hard Rules, schema lock, Pro/Billing status, and current next step.

For new feature work, also read `Docs/fable-suggested-feature.md`; it contains the current feature-batch instructions/specs. Suggested first feature is A2 Conflict Warning unless the user picks another item.

Latest status: `versionCode 9` / `versionName 1.1.3`; `:app:assembleDebug` passing. Latest debug APK installed successfully. Latest completed app-code changes include B5 Year view feedback correction: Settings/Event toggles now reuse the Year Heatmap switch style, and filled heatmap markers no longer cut date numbers. No manual phone UI QA run unless explicitly asked.

Strict: do not change Room schema, package id, deep links, DB filename, onboarding/calendar/sync/holidays/tasks unless required by the task. No Hilt, no Compose Nav graph. Build after app-code changes with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`, then install debug APK on the connected phone unless user says not to. Do not run manual phone UI QA unless explicitly asked. Do not start Advanced Reminder Profiles or Offline OCR without confirmation.
