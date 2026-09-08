# DotCal Handoff

Updated: 2026-09-08

Active resume document for `com.dotfield.dotcal`. Historical detail is preserved in
`Docs/HANDOFF.original.md`. Do not edit `Docs/HANDOFF - Copy.md` or user-owned
`Docs/FEEDBACK.md`.

## Worktree

- Branch: `feature-and-fixes`.
- Latest local commit: `3118237 fix(billing): preserve intro offer and share dates`.
- Current work is uncommitted. Do not commit, push, reset, clean, or switch branches unless explicitly asked.
- Preserve user-owned untracked files: QA screenshots, icon ZIPs, `.claude`, `Docs/logcat.txt`, and `tools/`.
- App version: `versionCode 42`, `versionName 1.5.0`.
- Package: `com.dotfield.dotcal`. Device: `000153573000720` when connected.

## Rules

- Read this file and use the Android development workflow before changing code.
- Use TDD for behavior changes: add/adjust a focused test, run it, implement, then run the relevant suite.
- Manual QA is post-22-August/new-feature scope only. Run one manual test at a time and state exact expected behavior before asking for the test.
- No install, commit, push, or branch operation unless requested or explicitly authorized by the current user message.
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
- Dynamic icon and crash-hardening changes remain local until explicitly approved for commit/push.

## QA baseline

- Previously passed manual QA includes reminders/full-screen access and snooze, widget theme/config/remove flows, provider availability/RDATE/meeting metadata/colors, calendar-move duplicate protection, shift-pattern export, auto-buffers/Find-a-Time, recurring occurrence sync, and Week/Day detail navigation.
- Pending manual QA: dynamic launcher icon behavior on small/large devices and external `.ics` open-with flow. Test only the requested new feature, one test at a time.

## Verification state

Passed after the latest code changes:

```text
:app:testDebugUnitTest
:app:assembleDebug
git diff --check
```

Audit follow-up focused tests and the full debug suite pass after the exception/logging hardening.

The current focused ICS intent test also passes:

```text
:app:testDebugUnitTest --tests com.dotfield.dotcal.MainActivityIntentTest
```

The latest combined run passed full `:app:testDebugUnitTest` and `:app:assembleDebug`. `:app:bundleRelease` previously passed before the ICS route edit; the latest run reached `lintVitalAnalyzeRelease` and produced no output for a bounded wait, matching the known lint/tooling stall, so it was stopped. Do not treat that tooling stall as a code failure; rerun the release bundle when release verification is required.

## Next safe steps

1. Inspect `git diff` and `git status`; leave unrelated user files untouched.
2. Run the full debug unit tests, debug APK build, release bundle, and diff check after any further edit.
3. If the user requests install, state the exact manual test and expected result first, then install the verified APK.
4. If the user requests release integration, commit/push/merge only the exact requested operation.
