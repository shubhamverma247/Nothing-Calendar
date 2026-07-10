# DotCal Handoff

Updated: 2026-07-10

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

- For app feature work, read `Docs/fable-suggested-feature.md`; A2 and A5 are complete. Suggested next implementation is A4 Jump to Date unless the user picks another feature.
- Play/Internal-testing billing verification remains the next product check.
- Advanced Reminder Profiles remains NOT started; do not start without explicit confirmation.
- Offline OCR remains possible later; do not start unless user asks.
- Language picker scaffold/i18n remains TO BUILD; string extraction is a dedicated effort.

## What To Test Next

- Font picker: switch Ndot/NType/System and verify all top headings update, while body rows stay readable.
- Billing/internal test: Settings > DotCal Pro opens Paywall, live INR 149 appears, test purchase works, gates unlock, Restore works after fresh install.
- Quick regression: Calendar views, Event/Task detail/editor, Settings root/subscreens, Paywall, Search, Quick Add, Templates, Calendar Sets, Shift Patterns, Recently Deleted, Date Calculator.
- Shift Patterns: generated shifts span real duration in Week/Day, no duplicate regenerate, delete pattern removes generated events, bulk Month template stamping creates one-off events only.

## Resume Prompt

Continue DotCal development in `D:\Caveman\caveman\Nothing-Calendar` on branch `pro-features`.

First read `Docs/HANDOFF.md`; it is source of truth. Respect Hard Rules, schema lock, Pro/Billing status, and current next step.

For new feature work, also read `Docs/fable-suggested-feature.md`; it contains the current feature-batch instructions/specs. Suggested first feature is A2 Conflict Warning unless the user picks another item.

Latest status: `versionCode 9` / `versionName 1.1.3`; `:app:assembleDebug` passing. Latest app-code change fixed A5 follow-up issues: default view picker now switches/saves correctly and week numbers render as `W##` labels in Month/Week without hiding day cells. Debug APK installed successfully on device `4ab0d020`.

Strict: do not change Room schema, package id, deep links, DB filename, onboarding/calendar/sync/holidays/tasks unless required by the task. No Hilt, no Compose Nav graph. Build after app-code changes with `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`, then install debug APK on the connected phone unless user says not to. Do not run manual phone UI QA unless explicitly asked. Do not start Advanced Reminder Profiles or Offline OCR without confirmation.
