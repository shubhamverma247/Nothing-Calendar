# DotCal Handoff

Updated: 2026-09-30

Active resume document for `com.dotfield.dotcal`. Historical detail is preserved in
`Docs/HANDOFF.original.md`. Do not edit `Docs/HANDOFF - Copy.md` or user-owned
`Docs/FEEDBACK.md`.

## Worktree

- Branch: `main`, tracking `origin/main`.
- Latest pushed commit: `afeadb1 feat: polish planning and widget workflows`.
- Worktree was clean at `afeadb1` before this handoff edit; only `Docs/HANDOFF.md` should be modified by this documentation update. Do not commit, push, reset, clean, or switch branches unless explicitly asked.
- Preserve user-owned untracked files: QA screenshots, icon ZIPs, `.claude`, `Docs/logcat.txt`, and `tools/`.
- App version: `versionCode 47`, `versionName 1.8`.
- Package: `com.dotfield.dotcal`. Device: `000153573000720` when connected.

## Rules

- Read this file and use the Android development workflow before changing code.
- Before making any change, explain clearly what will be changed and wait for the user's explicit approval; continue implementation only after approval.
- Use TDD for behavior changes: add/adjust a focused test, run it, implement, then run the relevant suite.
- Manual QA is post-22-August/new-feature scope only. Run one manual test at a time and state exact expected behavior before asking for the test.
- The user performs manual device QA. Do not tap, swipe, launch screens, or capture screenshots for QA unless explicitly asked. Provide one test with expected behavior, then wait for the result. Continue automated tests, builds, and APK installation as required.
- After Android changes, build and install the verified APK. State the exact manual QA test and expected behavior before each test.
- Do not commit, push, reset, clean, or switch branches unless requested or explicitly authorized by the current user message.
- Keep fixes minimal; do not rewrite unrelated code.

## Current implementation

### App invariants

- Kotlin + Jetpack Compose; `compileSdk 36`, `minSdk 30`, `targetSdk 36`.
- Offline-first calendar. CalendarProvider is only for Google/system calendar integration; no backend/cloud dependency without explicit approval.
- Package, `dotcal://` deep-link scheme, Room database filename, and billing product IDs are compatibility surfaces; do not change casually.
- Tabs are Calendar, Tasks, Settings. Public views are Year, Month, Week, Day, and Agenda; keep hidden ThreeDay unexposed.
- Events/tasks live in `calendar_events`; tasks use `isTask = 1`. Preferences use `calendar_preferences`; non-relational side data uses `dotcal_side_store.json`.
- Preserve explicit Free/Pro behavior: Free remains a complete calendar; Pro unlocks smart planning, power features, advanced widgets, and privacy controls.

### Billing contract

- Lifetime Pro: `dotcal_pro` (`INAPP`).
- Primary subscription: `dotcal_pro_subscription` (`SUBS`); legacy fallback: `dotcal_pro_sub`.
- Subscription base plans: `monthly`, `yearly`.
- Lifetime entitlement wins forever; otherwise an active subscription grants Pro. Google Play manages subscription cancellation.

### Daily launcher icon

- `MainActivity` is the target of 31 `activity-alias` launcher entries; exactly one alias is enabled for the local day-of-month.
- Startup, resume, boot, date/time, timezone, configuration, and package-replacement paths refresh the alias.
- Normal and monochrome resources exist for days `01`-`31` in all launcher densities, with adaptive XML and legacy fallbacks.
- Daily assets use `Docs/base-new-normal.zip` and `Docs/base-new-monochrome.zip`.
- Fixed fallback `24` assets use the supplied `Docs/defaul-new-normal.zip` and `Docs/default-new-monochorme.zip` (plus existing IconKitchen exports where present).
- `tools/generate_launcher_icons.ps1` validates deterministic resource counts and adaptive references.
- Debug two-minute date rotation was removed. Release and debug use the real local date.

### Daily launcher icon feedback and placement constraint

- Feedback received: when the date changes, some launchers make DotCal leave its user-created folder and reappear in a default position.
- Root cause confirmed in the current implementation: the launcher component identity changes from one `activity-alias` (`LauncherDayNN`) to another at midnight. The launcher can treat this as remove-old-icon plus add-new-icon, so home-screen placement is not preserved.
- Impact: folder users can need to place DotCal back every day; standalone home-screen users can see the icon move or refresh; app-drawer-only users may notice little or no placement impact.
- Google Calendar uses launcher-side special handling in supported launchers: the launcher keeps one component identity, invalidates its cached icon on date changes, and selects one of 31 date resources. This is not a general app API that DotCal can enable for every launcher. See Android Launcher `IconProvider`: <https://android.googlesource.com/platform/frameworks/libs/systemui/+/refs/heads/master/iconloaderlib/src/com/android/launcher3/icons/IconProvider.java>.
- `PackageManager.DONT_KILL_APP` and atomic component-state updates cannot guarantee folder preservation because they do not keep the same launcher identity.
- Pinned shortcut was reviewed as an alternative but rejected as the default fix: it creates a separate home-screen shortcut, requires first-time launcher/user approval, and does not transparently convert existing app icons.
- Practical mitigation implemented: Settings → Calendar Preferences now has a `Daily date icon` toggle, defaulting to ON. ON keeps current daily date behavior and its launcher-placement risk; OFF stops daily alias switching and leaves the stable fixed `24` icon, so users can arrange DotCal once. The toggle also cancels/restores the midnight refresh alarm.
- This is a user-controlled mitigation, not a universal launcher fix: users who want the date on the icon can keep ON, while folder-placement-sensitive users can choose OFF. Launcher placement still requires manual QA on Nothing Launcher.
- Any future launcher-icon change must test both standalone and folder placement on Nothing Launcher, with one manual QA test at a time. Do not create another fix solely from this feedback without a reproducible, launcher-specific approach.

### Import/open behavior

- Existing Settings import uses Android `OpenDocument`, reads ICS on IO, parses it, shows `IcsImportPreviewScreen`, and imports only after confirmation.
- MainActivity now accepts `ACTION_VIEW` for `text/calendar`, `text/x-vcalendar`, and `application/ics`.
- External `.ics` URI is read on IO and routed through the same preview/import flow. It never auto-imports.
- `singleTop` plus intent parsing supports both cold launch and a new file opened while DotCal is already running.
- Focused intent tests are in `MainActivityIntentTest`.

### Crash hardening

- Preferences DataStore has corruption recovery and missing-file fallback.
- Room list Flows retry locked-database reads; persistent locks are logged, the last emitted value is retained, and an empty list is emitted only when no value exists.
- Week/Day timeline scroll containers avoid vertical scrolling plus unbounded `fillMaxSize()` measurement.
- Widget maintenance uses `goAsync()` and `Dispatchers.IO` for icon/widget refresh work.
- Widget maintenance catches recoverable `Exception` only; fatal JVM errors are not swallowed. External ICS read failures log a safe diagnostic tag and still show the existing generic toast.
- Recovery, database-lock, and receiver-action tests pass.

### Release/tooling constraints

- AGP `9.0.1`, Gradle `9.1.0`, built-in Kotlin `2.2.10`, KSP `2.2.10-2.0.2`, and Room `2.8.4` are intentional.
- `android.disallowKotlinSourceSets=false` remains as a temporary KSP compatibility flag.
- Release R8 keeps the QR share screen/singletons and ML Kit manifest registrar constructors; the temporary 64-bit-only ABI restriction was removed.
- Exact-alarm declarations, Android 14 full-screen reminder access, receiver `goAsync()`, and notification DataStore IO handling were hardened in the production audit.

### Other recent product changes

- Month-view long-press bulk actions apply navigation-bar insets and keep selection/template controls visible; template content remains scrollable.
- Billing code keeps one-time and subscription product sources separate; do not merge product types.
- Quick Add voice dictation preserves typed text; Quick Settings/launcher shortcuts reuse `dotcal://quick-add`.
- Calendar/Week/Agenda share cards, directional Week/Day transitions, Play review gating, and reminder snooze cleanup are implemented locally.
- Sync & Widget Health Center is implemented in Settings with calendar permission, sync, and widget refresh status plus manual recovery actions.
- Reminder Center is implemented with grouped pending reminders and inline snooze, dismiss, and cancel actions.
- Event Readiness Checklist is implemented in Event Detail with add, edit, delete, and complete actions. Data uses the existing side store; Room schema is unchanged. Reminder notifications include the remaining preparation-item summary.
- `Find Time for This` first slice is implemented on Share Availability: future-only duration-specific suggestions, active-slot trimming, and refresh on resume. It is not the complete roadmap workflow yet.
- Event editor start-time changes preserve duration unless the end time was manually edited.
- Deferred Phase 2 polish: add Settings -> Calendar Preferences -> Time format with
  `System default`, `12-hour`, and `24-hour`. Do this as a separate broad consistency pass
  because it must affect calendar rows, editors, reminders, widgets, notifications, share text,
  and time pickers together.

## Authoritative active product roadmap

This is the only active roadmap in this handoff. It supersedes the old flat roadmap lists.
`Docs/DotCal-Feature-Roadmap.md`, `Docs/fable-suggested-feature.md`,
`Docs/DotCal — FINAL PACKAGE 14 Feature.txt`, and historical roadmap sections in
`Docs/HANDOFF.original.md` are reference material only; do not select new work from them
without updating this section first.

The current Play listing and implementation history are the shipped baseline. Do not list
Quick Add 3.0, voice input, launcher/Quick Settings shortcuts, Auto-Buffers, the base
Find-a-Time flow, Countdowns, QR Event Share, Availability Text, Dead Time Finder,
Pencil-In Events, On This Day, drag/resize, bulk edit, widgets, or image/shift-plan sharing
as new features. The items below are the next active feature proposals and advancements.

| # | Feature | Access | Type | Priority |
|---|---|---|---|---|
| 1 | Sync & Widget Health Center | Free | Implemented; future diagnostics polish only | Complete |
| 2 | Reminder Center + Reminder Readiness Check | Free basic; Pro batch actions and saved snooze presets | Center complete; readiness check and Pro actions pending | P1 |
| 3 | Find Time for This | Pro advanced matching; preserve current Find-a-Time | First slice complete; full task/event workflow is next | P1 next |
| 4 | Saved Smart Views | Free basic filters; Pro saved combinations | Search/filter advancement | P1 |
| 5 | Evening Task Review | Free manual review; Pro scheduling suggestions | Task-planning advancement | P1 |
| 6 | Event Readiness Checklist | Free basic checklist; Pro reusable setups and rules | Basic checklist complete; reusable setups/rules pending | P1 advancement |
| 7 | Repair My Day | Pro | Rescheduling advancement | P1 |
| 8 | Linked Event Kits | Pro; preserve current templates | Template advancement | P1 |
| 9 | Advanced Widget Profiles + 14-day view | Free core readability; Pro advanced profiles and filters | Widget advancement | P1 |
| 10 | Advanced Availability Rules | Pro; preserve current availability sharing | Availability advancement | P1 |
| 11 | Calendar Health + Usable-Time Insights | Free basic overview; Pro trends and filters | Insights advancement | P1 |
| 12 | Offline Common-Time QR | Free short two-person comparison; Pro longer ranges and advanced rules | Distinctive offline workflow | P1 |
| 13 | Shift-aware Usable Time + Routines | Free basic boundaries; Pro shift-relative rules | Shift-planning advancement | P2 |
| 14 | Pencil-In Plan Comparison | Pro; preserve current Pencil-In Events | Tentative-planning advancement | P2 |
| 15 | Schedule Change Detector | Pro prototype | New high-risk workflow | P2 |

### Research-reconciled feature decisions

The 2026-09-19 competitor review did not replace the roadmap above. It clarified which
existing DotCal capabilities are worth advancing and the order in which they should be built.

Existing-feature advancements:

1. Complete `Find Time for This`: task/event entry, duration derivation, deadline or date-range
   constraints, preferred hours, ranked slots, and preview/confirm behavior.
2. Build Saved Smart Views on the existing Search filters and Calendar Sets. Support useful
   combinations such as Work this week, Pending tasks, and Upcoming appointments; later allow
   compatible saved views to drive widget content.
3. Advance Event Readiness with reusable setups and rules for repeated event types while
   preserving occurrence-specific completion state.
4. Add Reminder Readiness Check for notification permission, exact-alarm access, relevant
   channel state, and a test reminder. Add Pro batch actions and saved snooze presets later.
5. Advance availability with weekday-specific hours, multiple daily windows, minimum notice,
   before/after buffers, and reusable rule presets.
6. Link advanced widget profiles to Saved Smart Views and reusable Work/Personal calendar
   combinations. `Next14Days` already exists in widget configuration; treat broader 14-day
   exposure as an advancement, not a wholly new capability.

New workflows worth building:

1. Evening Task Review: process unfinished tasks through Complete, Tomorrow, Choose date,
   Unscheduled/Inbox, Delete, Skip, and Undo. Do not blindly carry recurring tasks forward.
2. Repair My Day: preview a revised plan for selected flexible tasks after disruption. Never
   move fixed/shared/provider events silently; confirmation and undo are required.
3. Linked Event Kits: create related preparation, main-event, and follow-up items from one kit;
   preview linked changes when the anchor event moves.
4. Offline Common-Time QR: exchange privacy-preserving availability snapshots and calculate
   common slots locally. Prototype before committing because both participants need compatible
   data and snapshots can become stale.
5. Shift-aware Usable Time + Routines: support routines relative to shift start/end and off-days.
   Validate with shift users before expanding beyond a prototype.

Lower-priority ideas from the review: cloud AI assistants, public booking infrastructure,
weather subscriptions, and screenshot-to-events import. These add backend, accuracy, privacy,
or maintenance cost before the higher-value local workflows above are complete.

### User feedback audit: calendar reliability and defaults

The 2026-09-28 Victor feedback contains four valid core-calendar improvements. These are Free
reliability/usability work, not Pro gates. All four are implemented in the current working tree.

1. **Default View persistence (P1 bug) - complete.** Calendar tab changes now update only
   current UI state. Settings -> Default view is the path that persists
   `CalendarPreferences.KEY_DEFAULT_VIEW` and the boot/startup mirror, so temporary navigation
   no longer overwrites the startup default.
2. **Colors in the month-day event sheet (P1 polish) - complete.** `EventListSheet` now shows
   the established calendar color marker for event rows without redesigning the sheet.
3. **Read-only/subscribed Google calendars (P1 sync compatibility) - complete.** Calendar
   accounts retain provider access level, readable subscribed calendars remain visible, and
   non-writable/read-only calendars are excluded from create/move destinations. Existing events
   from those calendars remain viewable while edits that CalendarProvider would reject are not
   presented as writable destinations. Offline-first behavior is preserved; no backend or
   Google API dependency was added.
4. **Default event calendar (P1 preference) - complete.** Settings -> Calendar Preferences has
   a DataStore-backed Default event calendar row using writable/assignable calendars only. New
   events preselect the resolved default; edits retain the event's existing calendar. If the
   saved calendar is missing or read-only, DotCal falls back deterministically to another
   writable calendar or DotCal Local. Changing the setting does not move existing events.

The separate widget feedback requesting Monday-first widgets is complete in `afeadb1`.
Compact and large month widgets now resolve `KEY_WEEK_START`, rotate weekday labels, align the
month grid, and refresh after the preference changes. Focused widget tests, full debug unit
tests, debug assembly, lint, and `git diff --check` passed before that commit.

### Shift Pro advancement audit

Shift is already a Pro feature. Existing behavior includes shift types with minute-precise
start/duration, all-day and reminder options, reusable rotation patterns, quick shift add,
range generation, and shift-plan sharing. Keep the entire advanced Shift lane under the
existing Pro entitlement; do not create another Shift paywall tier.

Recommended implementation order:

1. **Generation reliability and preview.** Make regeneration range-safe, show exactly which
   generated events will be created, updated, skipped, or removed, and require confirmation.
   Current `DotCalRepository.applyShiftPattern` can remove overlapping prior generation records
   for the same pattern when regenerating a smaller range; fix this before adding more pattern
   power. Preserve manually edited/cancelled generated occurrences and provide undo where
   existing events change.
2. **Pattern management.** Add edit, duplicate, archive/delete confirmation, and practical
   presets such as rotating days/nights and four-on/four-off. Keep custom rotations available
   and preview the resulting cycle before save/generation.
3. **Richer shift types.** Expose per-type reminders clearly, then consider breaks and explicit
   overnight/rest-boundary handling. Night shifts must remain one logical shift across midnight.
4. **Shift-aware planning.** Use shift start/end and off-days as constraints for usable-time
   suggestions and routines. Suggestions remain local, previewable, and explicitly confirmed;
   never move provider-backed, shared, recurring, or all-day events silently.
5. **Reports later.** Pay, overtime, allowances, and work-hour reports are valuable competitor
   benchmarks but are lower priority because regional rules and payroll expectations expand the
   product surface. Do not begin these without a separate product decision.

Competitor patterns used for this audit: Supershift for long-range rotations/share/widgets,
MyShiftPlanner for presets and saved pattern editing, and WorkShift for profile and
pay/overtime concepts. These are benchmarks, not requirements to clone.

### Roadmap constraints

- Keep Free as a complete, useful calendar. Pro should unlock automation, deeper planning,
  reusable power-user controls, and advanced insights—not basic calendar reliability.
- Preserve offline-first behavior, local processing, no backend/cloud dependency, current Room
  schema, existing DataStore/side-store patterns, billing IDs, lifetime-Pro entitlement,
  and existing sync/export behavior.
- Every scheduling action must be user-confirmed, previewable, undoable where it changes
  existing events, and must not silently move fixed/shared/provider events.
- Treat “unique” as a DotCal-specific combination or positioning opportunity, not a worldwide
  uniqueness claim. Prototype P2 items before committing to implementation.
- Before implementation, reconcile each item against the current code and add focused tests;
  this roadmap is not itself an implementation approval.

### Research basis

- BusyCal benchmarks Smart Filters, saved calendar sets, event suggestions, and snooze management:
  <https://www.busymac.com/docs/busycal/70612-smart-filters/>
  <https://www.busymac.com/docs/busycal/event-suggestions/>
- Fantastical benchmarks automatic calendar sets, multiple timezones, and multiple scheduling
  durations: <https://flexibits.com/fantastical/help/calendar-sets>
  and <https://flexibits.com/blog/2026/07/new-feature-roundup-you-asked-we-built/>
- Reclaim benchmarks habits, auto-rescheduling, buffers, and time analytics:
  <https://reclaim.ai/features/habits>
- Fantastical Openings benchmarks duration choices, day-specific availability windows,
  lead time, and before/after buffers:
  <https://flexibits.com/account/help/openings>
- Morgen benchmarks task-aware scheduling with a visible preview and explicit approval:
  <https://www.morgen.so/guides/plan-your-day-using-the-ai-planner>
- Structured Replan benchmarks guided processing of unfinished tasks with reschedule,
  inbox, complete, delete, skip, and undo actions:
  <https://help.structured.app/en/articles/4511874>
- User-feedback themes include sync reliability, widget freshness, reminder reliability,
  recurrence handling, and automatic schedule shifting:
  <https://digibites.zendesk.com/hc/en-us/articles/200243176-Widgets-not-updating-task-killer-issue>
  and <https://apps.apple.com/us/app/structured-daily-planner-todo/id1499198946?see-all=reviews>

## QA baseline

- Reminder UI revision: Settings has one `Reminders` entry. `Defaults & alerts` opens
  existing defaults; back returns to Reminders. The pending list uses DotCal typography,
  grouped reminder cards, localized alert times, and visible inline Snooze, Dismiss, and
  Cancel actions.
- Reminders uses the existing large-to-centered compact Settings header on scroll.
  Device QA of this revision is pending. Reminder Readiness Check is still unimplemented.
- Manual QA passed for Event Readiness Checklist, `Find Time for This` suggestions,
  readiness sheet polish, and event-editor duration adjustment.

- Previously passed manual QA includes reminders/full-screen access and snooze, widget theme/config/remove flows, provider availability/RDATE/meeting metadata/colors, calendar-move duplicate protection, shift-pattern export, auto-buffers/Find-a-Time, recurring occurrence sync, and Week/Day detail navigation.
- Pending manual QA: dynamic launcher icon behavior on small/large devices and external `.ics` open-with flow. Test only the requested new feature, one test at a time.

## Verification state

Passed for pushed commit `afeadb1`:

```text
:app:testDebugUnitTest
:app:assembleDebug
:app:lintDebug
git diff --check
```

Reminder Center lifecycle tests pass, including deterministic dismiss, cancel, snooze,
and task/event action behavior. The full debug unit-test suite and debug APK build pass.
Lint passes without errors. A release bundle passed on the prior baseline through R8 and
lint-vital, but `bundleRelease` was not rerun for `afeadb1`.
The Room Reminder Center instrumentation test compiles, but connected execution is pending:
the connected device disappeared before test execution and ADB currently reports no devices.

Focused tests also pass:

```text
:app:testDebugUnitTest --tests com.dotfield.dotcal.MainActivityIntentTest
:app:testDebugUnitTest --tests com.dotfield.dotcal.data.ReminderCenterLifecycleTest
:app:testDebugUnitTest --tests com.dotfield.dotcal.data.scheduling.FindTimeForThisMatcherTest
:app:testDebugUnitTest --tests com.dotfield.dotcal.ui.EventLocationNavigationTest
:app:testDebugUnitTest --tests com.dotfield.dotcal.widget.WidgetCalendarWeekStartTest
:app:connectedDebugAndroidTest (not completed: no connected device)
```

Passed for current uncommitted Victor feedback working tree:

```text
.\gradlew.bat testDebugUnitTest --tests com.dotfield.dotcal.ui.UiHelpersTest
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat lintDebug
git diff --check
```

`git diff --check` reports only CRLF normalization warnings. Debug APK install was not run
because no device was attached when checked with the Android SDK `adb.exe`.

## Next safe steps

1. Inspect `git diff` and `git status`; leave unrelated user files untouched.
2. Run the full debug unit tests, debug APK build, release bundle, and diff check after any further edit.
3. After Android changes, state the exact manual test and expected result first, then install the verified APK.
4. If the user requests release integration, commit/push/merge only the exact requested operation.

## Next selected work

Victor feedback work is implemented and verified in the current working tree. Next scoped work
is Shift Pro generation reliability: range-safe regeneration plus an affected-event preview,
not reports/pay calculations.

## Resume prompt for next session

Continue DotCal development from `D:\Caveman\caveman\Nothing-Calendar`.

Before work:

- Read `Docs/HANDOFF.md` and all applicable `AGENTS.md` instructions.
- Inspect git status and recent commits. Preserve all user-owned tracked and untracked work.
- Do not reset, clean, force-push, switch branches, commit, or push unless explicitly requested. Install verified APK after Android changes.
- Do not change app icon assets or revisit daily launcher icon behavior unless explicitly requested; current folder-placement feedback is documented above and has no verified app-only fix.

Current pushed baseline is `main` at `afeadb1`, version `1.8` (`versionCode 47`). Continue the
audited feedback work one item at a time:

1. Victor feedback points are complete in the current dirty working tree: Default View
   persistence, month-sheet colors, read-only/subscribed CalendarProvider access handling, and
   Default Event Calendar.
2. If a device is connected, install the verified debug APK. Current automated verification has
   already passed for the feedback work.
3. Keep these four reliability/default features Free. Preserve offline-first behavior and do
   not add a backend or Google API dependency.
4. Continue Shift Pro with range-safe regeneration and a preview of
   created/updated/skipped/removed events. Preserve manual edits and never silently move or
   delete provider-backed, shared, recurring, or all-day events.
5. Reconcile each slice against current code before editing, follow focused TDD, and audit all
   changes before any explicitly requested commit.
