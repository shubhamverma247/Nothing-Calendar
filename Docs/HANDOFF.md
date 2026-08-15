# DotCal Handoff

Updated: 2026-08-15

Source of truth for DotCal (`com.dotfield.dotcal`). Full history: `Docs/HANDOFF.original.md`. Feature spec: `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`. Do not touch `Docs/HANDOFF - Copy.md`.

## Latest Continuation

**Calendar overflow menu customization is implemented and verified on `main`.** Users can now open
`Settings > Calendar Preferences > Calendar menu` and show/hide Calendar tab overflow actions with a
Reset action. Hidden actions persist through DataStore stable ids; default behavior remains all
actions visible. The fixed top-bar `+` button and hardware-gated QR scanner icon are unchanged.
`versionCode` is bumped to 22 for the next Play upload; `versionName` remains 1.3.0.
Verification passed: `git diff --check`, `:app:testDebugUnitTest`, `:app:assembleDebug`,
`:app:lintDebug`, and `:app:installDebug`.

Manual QA passed for the previous polish batch:

- `Settings > Privacy Policy` opens in-app and scrolls correctly.
- Discord opens as an external invite.
- Slovak/system locale Year view weekday labels align correctly, including Friday 14 under `pi`.
- Billing sheet/redeem flow is visible through Google Play purchase flow.

Latest Play release notes draft:

```text
<en-US>
What’s new in DotCal 1.3.0

• PDF attachments for events
• Customizable Calendar overflow menu
• Smoother shift scheduling
• Improved month calendar navigation
• Better overflow counts and out-of-month day visibility
• More polished widgets
• Clearer shift markers and actions
• Better calendar localization for month, week, and year labels

We’ve also made several fixes for a smoother calendar experience.
</en-US>
```

Next release audit follow-ups:

- App Lock PIN hardening: `AppPrivacyManager.kt` currently stores a 4-8 digit PIN with salted
  single-pass SHA-256 and no persisted retry throttling. Fix in the next release with a
  backward-compatible migration to a slow hash such as PBKDF2, plus failed-attempt backoff/lockout
  UI. Risk: existing SHA-256 PINs must continue to unlock and migrate safely, and QA must cover set
  PIN, unlock, wrong attempts, cooldown, change PIN, remove PIN, and app relock.
- DotCalApp decomposition: `DotCalApp.kt` is roughly 2,800 lines with very high cognitive
  complexity. In the next release, split route/state orchestration into smaller screen coordinators
  and move settings, calendar, and event flows into scoped composables. Goal is to reduce future
  regression risk and broad recomposition side effects; this should be a refactor-only pass with
  focused navigation/app-lock/paywall/manual QA.

**Shift Worker Convenience Pack Phase 1 first slice is implemented and Phase 2 first share slice is
now built.** `Docs/FEEDBACK.md` is still user-owned untracked and must stay untouched.

- Phase 1 Add Shift remains Pro-gated from Calendar overflow. It opens a bottom sheet with date,
  saved shift type, and the revised single-row calendar picker dialog. Off/no-output shift types are
  hidden. Saving creates normal `CalendarEvent` rows through `saveLocalEvent`; Room still has exactly
  5 entities/tables and no schema migration.
- Add Shift edge-case polish: selected calendar id now falls back to the visible selected account if
  accounts refresh while the sheet is open, avoiding stale account ids.
- Phase 2 first slice: saved shift patterns now have a share action. The share dialog lets the user
  choose range start/days and export a virtual shift plan as image, PDF, ICS, or DotCal QR.
- Shift plan sharing does not write events to Room. It builds virtual `CalendarEvent` rows from the
  saved pattern/types, skips Off days, preserves same-day timed shifts, overnight shifts, and all-day
  exclusive-end semantics, then reuses existing ICS and DotCal QR import-preview infrastructure.
- DotCal QR sharing is capped at 14 shifts for reliability; longer ranges tell the user to use ICS,
  PDF, or image.
- Shift Patterns screen UI polish is now built: top summary surface, aligned section action pills,
  refined shift type/pattern cards, proper calendar/share/delete pattern action icons, and a cleaner
  Build Pattern dialog with shift-type buttons, cycle preview chips, and aligned Remove/Clear
  actions.
- Shift plan image/PDF/QR export polish is now built: image footer has extra bottom breathing room
  so the DotCal label no longer collides with the final shift row, share artifacts do not show the
  added DotCal icon/mark, PDF header text is width-bounded, PDF event rows scale down on page width
  so shift times stay fully visible, and the QR share card has a cleaner branded frame/instruction
  treatment.
- Notification small icon was updated from the generic calendar glyph to a white-only DotCal
  calendar/`DC` monogram silhouette in `res/drawable/ic_notification.xml`. Reminder notifications
  and their `View` / `Snooze 10 Min` actions still reference the same `R.drawable.ic_notification`
  resource; launcher icons were not used directly.
- New uncommitted Month view polish after `e0f6eef`: tapping a date now shows split action buttons
  in the event list sheet (`Add Event` / `Add Shift`), with Add Shift reusing the existing Pro-gated
  Quick Shift sheet for the selected date. The Add Shift button uses an accent-tinted outlined
  treatment so it stays visible in light theme and shows a `Pro` badge for free users. Long-press
  bulk selection controls now reserve space above the floating bottom nav so Apply/Clear do not hide
  behind it.
- New files added in this continuation:
  - `app/src/main/java/com/dotfield/dotcal/data/shifts/ShiftPlanShare.kt`
  - `app/src/main/java/com/dotfield/dotcal/share/ShiftPlanShareExporter.kt`
  - `app/src/test/java/com/dotfield/dotcal/data/shifts/ShiftPlanShareTest.kt`
- Verification after this continuation:
  - `.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin` returned `BUILD SUCCESSFUL`.
  - `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest --tests com.dotfield.dotcal.data.shifts.ShiftEventDraftTest --tests com.dotfield.dotcal.data.shifts.ShiftPlanShareTest` returned `BUILD SUCCESSFUL`.
  - `.\gradlew.bat --no-daemon --console=plain :app:lintDebug` returned `BUILD SUCCESSFUL`.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` returned `BUILD SUCCESSFUL`.
  - `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest` returned `BUILD SUCCESSFUL`.
  - After notification icon polish, `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`
    returned `BUILD SUCCESSFUL`.
  - After Shift Patterns UI polish, `.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin`
    returned `BUILD SUCCESSFUL`; `.\gradlew.bat --no-daemon --console=plain :app:lintDebug :app:assembleDebug`
    returned `BUILD SUCCESSFUL`.
  - After final shift share export polish, `.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`
    returned `BUILD SUCCESSFUL`.
  - After Month view Add Shift/safe-area polish, `.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin`
    returned `BUILD SUCCESSFUL`; `.\gradlew.bat --no-daemon --console=plain :app:lintDebug :app:assembleDebug`
    returned `BUILD SUCCESSFUL`.
  - Debug APK installed successfully on device `4ab0d020` using
    `C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk`.
  - `git diff --check` passed with CRLF warnings only.

**Month View + Bottom Nav UX polish is complete through Batch 3 partial, and priority widget date
clarity + opacity polish is now built/installed for review.** Full detail in
`## Planned: Month View + Bottom Nav UX`.

- **Batch 1A (Month grid) — DONE, approved on device.** `+N` overflow indicator as string resource
  `month_day_more_count` (base + all 8 locales, key parity 720/755), dimmed out-of-month day numbers,
  and the `+N` vertical alignment fix (`includeFontPadding = false` + `LineHeightStyle` trim).
- **Batch 1B (bottom nav, `AppChrome.kt`) — DONE, approved on device.** Six items: 48dp touch target,
  `weight(1f)` spacing, 26dp/1.85dp icon normalisation, `LocalView` + `VIRTUAL_KEY` haptics,
  hand-drawn Canvas gear, dead `selectedFill` deleted.
- **Batch 2 (persistent drag panel) — BUILT, REJECTED, REVERTED.** Do not rebuild. See
  `### Batch 2 — REJECTED AND REVERTED`.
- **Batch 2 (redefined) — event title chips in month cells. DONE, approved on device `4ab0d020`.**
  Tall rectangular cells (no `DayCell` `aspectRatio(1f)`), 3 flat tinted
  title chips + `+N more` per cell, **no chip borders**, tighter day-row spacing after user
  feedback, no inline events list below the grid. Day taps still open `EventListSheet`; chips are
  visual-only. All-day events use the same chip treatment.
- **Batch 3 partial polish — DONE and installed on `4ab0d020`.** Added TalkBack/accessibility
  semantics for Month day cells and bottom nav tabs, and removed dead `MonthView` params
  `onJumpToday` / `onJumpPickerRequest`. Visual Batch 3 polish (density tint, all-day marker, month
  transition animation, conditional 6th row) is still optional and not started.
- **Priority widget polish — DONE and installed on `4ab0d020`.** Medium agenda widget date block now
  uses today's date number with today's weekday, so it no longer mixes next-event date with today's
  weekday. Added a Pro-gated `Settings > Widgets > Widget Opacity` slider backed by
  `widget_opacity_percent` (default 35%) and applied it to transparent widget surfaces. Chip taps in
  Month view were deliberately left unchanged: chips remain visual-only and day tap behavior still
  opens `EventListSheet`.
- **Audit fixes after widget/month polish - DONE and installed on `4ab0d020`.** Opacity slider now
  uses local draft state while dragging and commits DataStore/widget refresh on tap, accessibility
  set-progress, or drag finish. Month day TalkBack labels and bottom nav selected/not-selected state
  are localized through string/plural resources. Month visible title chips adapt to 2 when week
  numbers or tight row height would overcrowd the cell, otherwise 3. Existing users who already had
  transparent widgets stay fully transparent after upgrade when no opacity value exists; newly
  enabling transparency writes the 35% default.
- **Week-number alignment follow-up - DONE, approved after tuning, and installed on `4ab0d020`.**
  Month week-number column now shares the same reserved width as the month header/body grid, and
  body week-number labels are top-aligned to the day-number row instead of sitting low in each tall
  event-chip cell.

The current month/bottom-nav/widget polish batch is being committed to `main` and pushed to
`origin/main` at the user's request. `Docs/FEEDBACK.md` remains user-owned untracked and must stay
untouched.

Priority widget feedback from a Spanish user is now implemented and pending user visual approval:

- Medium agenda widget left badge now always shows `data.todayLabel`, matching `todayDayAbbrev()`.
  Future event dates stay in the event rows. Previous screenshot mixed a next-event badge with
  today's weekday, while the visible events were on
  `MIE, AGO 12`; this is the case now fixed.
- Transparent widgets now have a gradual opacity control in Settings. When `Transparent Widgets` is
  off, the opacity row remains visible but disabled with explanatory copy; when on, the slider writes
  `CalendarPreferences.KEY_WIDGET_OPACITY_PERCENT` and refreshes widgets immediately.
- Follow-up polish: opacity slider no longer renders as a lone round thumb. It now has a visible
  pill rail, accent-filled progress segment, and muted disabled state.
- Audit follow-up: slider drag no longer writes preferences for every movement, slider exposes
  progress semantics, accessibility strings are localized, and legacy transparent-widget installs
  keep their old fully-transparent look until the user changes opacity.
- Month week-number alignment follow-up: week-number column width now matches header/body grid math,
  and week-number labels align to the day-number row in tall month cells.
- Verification:
  `.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin` returned `BUILD SUCCESSFUL`;
  `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug` returned
  `BUILD SUCCESSFUL`; `.\gradlew.bat --no-daemon --console=plain :app:lintDebug` returned
  `BUILD SUCCESSFUL`; `git diff --check` passed with CRLF warnings only; debug APK installed
  successfully on device `4ab0d020`.

User feedback from Gary: bought Pro soon after installing and asked for file attachments in addition
to images, especially venue tickets supplied as PDFs.

- Status: implemented for PDF attachments.
- Current scope: Event editor and Event detail support up to 5 PDFs per event, 20 MB per PDF.
- Storage: selected PDFs are copied into app-private storage and tracked through side-store metadata;
  Room remains unchanged.
- Access model: adding PDFs is Pro-gated; viewing/opening/removing already-attached PDFs remains
  available if entitlement is later lost.
- Backup/restore includes PDF attachments with enforced size/count limits.
- Boundary: generic documents such as DOCX/XLSX/TXT and Google Calendar / Drive attachment sync are
  not implemented. DotCal remains offline-first and CalendarProvider-based.

User feedback referencing SuperShift calendar: user likes easier work-shift entry, sharing plans
with other DotCal users, and a two-week widget. Treat this as shift-worker convenience, **not** a
DotCal rebrand into a shift-only app.

- Recommendation: add as a later Pro-focused roadmap pack because DotCal already has Shift Patterns
  and this improves value for nurses, retail staff, drivers, freelancers, trainers, and other users
  with irregular schedules.
- Product positioning: DotCal remains a general offline calendar. The feature name should be
  something like `Shift Worker Convenience Pack`, not `SuperShift clone`.
- Best first scope: quick shift add from Calendar, share shift plan as image/PDF/ICS plus DotCal QR
  for small ranges, and a compact two-week widget. These fit offline-first and require no backend.
- Growth-loop note: sharing shift plans should be first-class because users can ask coworkers/family
  to install DotCal to scan/import a plan. Single shifts can reuse the existing Event Detail >
  `Share as QR` flow; multi-shift plans should get a dedicated `Share shift plan as DotCal QR`
  option with range/event-count limits so QR payloads stay reliable.
- Avoid for now: live shared calendars between DotCal users, payroll/overtime reports, multi-job
  management as a first-class model, and shift alarms. Those make DotCal feel like a niche shift app
  and/or require permissions/backend/product complexity.

Calendar overflow menu customization:

- User asked about making the Calendar tab three-dot menu customizable so users decide which actions
  appear there.
- Recommendation: add as a medium-easy polish feature, especially before/alongside shift features
  because new shift actions will otherwise make the overflow menu feel crowded.
- First version should be show/hide only, not drag reorder. Keep all current items visible by default,
  add `Reset to default`, and either prevent hiding the last visible action or leave a stable
  `Customize menu` entry reachable from Settings.
- Preserve existing Pro/free behavior, camera-less QR hiding, subtitles, and `Pro` badges.

Billing state: DotCal now supports one-time Lifetime plus subscription plans through Google Play
Billing.

- Lifetime product: `dotcal_pro` as an INAPP one-time product.
- Subscription products: `dotcal_pro_subscription` and legacy/fallback `dotcal_pro_sub` as SUBS.
  Base plans are mapped by id: `monthly` -> Monthly, `yearly` -> Yearly.
- Paywall shows Yearly, Monthly, and Lifetime offers from Play Billing product details. Prices and
  offer tokens are dynamic.
- `ProManager` queries product details and purchases for both `BillingClient.ProductType.INAPP` and
  `BillingClient.ProductType.SUBS`, restores both, and treats either lifetime purchase or active
  subscription as Pro.
- Entitlement priority: lifetime purchase wins forever; otherwise active subscription grants Pro;
  otherwise free.
- If a user buys lifetime while subscribed, DotCal unlocks lifetime but cannot cancel the Google Play
  subscription from app-only code. The app shows `Manage subscription` when an active subscription is
  detected; backend cancellation remains out of scope for the offline-first/app-only model.

Month view + bottom nav UX research pass complete on `main` (**read-only — no code changed**):

- Competitive research plus a full read of `MonthView`, `DayCell`, `EventListSheet` and
  `DotCalBottomNav`. Findings and a phased work order are in
  `## Planned: Month View + Bottom Nav UX`.
- Nothing was implemented. The plan is deliberately split into three batches so it does **not** ship
  in one pass — batch 2 changes state ownership and needs its own branch.
- Branch audit: all five non-`main` local branches are **fully merged** into `main` (ahead-count 0).
- The `versionCode` 17 -> 18 bump landed separately as `6793737` and is pushed.

Play device-reach compatibility fix complete on `main`:

- `android.hardware.camera` and `android.hardware.microphone` are now explicitly declared
  `required="false"` alongside the existing `android.hardware.camera.any` optional declaration.
- Runtime guards were added for optional hardware:
  - Calendar QR scan top-bar action is hidden when `PackageManager.FEATURE_CAMERA_ANY` is absent.
  - QR scanner host only renders when camera hardware is present, and `QrEventScannerScreen` has a
    defensive hardware guard.
  - Event editor hides the voice-note recording control when `FEATURE_MICROPHONE` is absent.
  - Existing voice-note playback remains available on mic-less devices when a voice note already
    exists.
- Verification:
  `.\gradlew.bat --no-daemon --console=plain --rerun-tasks :app:testDebugUnitTest :app:assembleDebug`
  returned `BUILD SUCCESSFUL`.
- `aapt2 dump badging app/build/outputs/apk/debug/app-debug.apk` now reports:
  - `uses-feature-not-required: name='android.hardware.camera'`
  - `uses-feature-not-required: name='android.hardware.camera.any'`
  - `uses-feature-not-required: name='android.hardware.microphone'`
  - no implied camera or microphone feature remains.
- `versionCode` was bumped to 18 for the next Play upload.
- Debug APK was installed successfully on device `4ab0d020` after first installing the existing
  package for user 0 with `cmd package install-existing --user 0 com.dotfield.dotcal`.

Google Calendar outbound sync issue confirmed and fixed on `feature/google-calendar-outbound-sync`:

- Inbound provider sync already worked: events added on Google Calendar desktop are imported from
  Android CalendarProvider into DotCal during sync.
- Outbound provider sync was missing: DotCal event saves only wrote Room rows, so events created in
  DotCal under a Google/provider calendar did not appear in Google Calendar.
- `CalendarProviderDataSource` now supports `WRITE_CALENDAR` save/delete for provider events and
  exposes provider account-id parsing.
- `DotCalRepository.saveLocalEvent` now writes provider-backed events to CalendarProvider first,
  stores the returned provider identity/version in Room, moves events cleanly between local/provider
  accounts, and deletes provider events when a provider-backed event is deleted or moved local.
- Verification:
  `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug`
  returned `BUILD SUCCESSFUL`.
- No device was attached during this pass, so debug APK was not installed.

Locale fill pass is complete mechanically:

- `values-de`, `values-fr`, `values-pt`, `values-ru`, `values-tr`, `values-in`, and `values-ar`
  now mirror `values-es` key-for-key: 699 keys each, exact canonical order, no extras, no missing
  keys.
- The stale German `<!--CHUNK-->` marker was removed. Existing German translations were preserved
  where present; remaining entries were filled in the same pass.
- Russian plurals use `one` / `few` / `many` / `other`; Arabic plurals use
  `zero` / `one` / `two` / `few` / `many` / `other`; Turkish and Indonesian use the same text across
  singular/plural categories.
- Remaining locale copy was cleaned after the value-based pass. A mixed-English scan over common UI
  words now reports 0 hits for German, French, Portuguese, Russian, Turkish, Indonesian, and Arabic.
  Native-language copy review is still recommended before treating these as final marketing-quality
  translations.
- Audits run after the locale fill:
  - format specifiers: `OK - specifiers match across 8 locale(s)`
  - dead keys: `0`
  - key diff against `values-es`: all 7 generated locales at 699 keys, no missing/extra/order drift
  - mixed-English scan across common UI words: 0 hits in all 7 generated locales
  - non-translatable/base-missing audit still reports the expected 47 base-only keys: 35
    `translatable="false"` plus the 12 app/widget/Glyph keys that intentionally stay absent.
- RTL static scan found no `layoutDirection`, `Alignment.Start`, `Arrangement.Start`, or
  `TextAlign.Start` assumptions in app source. Matches were only draw-coordinate math, chevron icons,
  absolute file paths, and content descriptions.
- Verification passed again after the final locale cleanup:
  `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug`
  returned `BUILD SUCCESSFUL`.
- Debug APK was later installed successfully on device `4ab0d020`.
- Billing/promo-code check: billing uses Google Play Billing Library 8.0.0 with both lifetime INAPP
  and subscription SUBS products.
  - One-time/lifetime promo codes for `dotcal_pro` should redeem through Google Play or the Play
    purchase sheet.
  - Subscription promo codes apply to the selected monthly/yearly subscription offer inside the Play
    purchase sheet. One-time-use subscription codes can also be redeemed through Google Play;
    custom subscription codes are app-flow only.
  - If a code is redeemed outside the app while DotCal is already open, reopen DotCal or tap Restore
    Purchase to refresh entitlement.
- Product offer support is wired:
  - `ProManager` reads `oneTimePurchaseOfferDetailsList` for lifetime offers and
    `subscriptionOfferDetails` for subscription base plans/offers.
  - Paywall shows eligible offer price/options and passes the selected `offerToken` into
    `BillingFlowParams.ProductDetailsParams.setOfferToken(...)`.
  - App-level Pro price display prefers eligible Play Billing offers and falls back to
    `oneTimePurchaseOfferDetails?.formattedPrice` for lifetime.
- Add/Edit Event now has a free Color row/sheet. Preset colors write `EventEditorData.colorHex`;
  `Use calendar color` writes `null`. Existing Agenda bulk color behavior is unchanged.
- Audit fixes after implementation:
  - `saveLocalEvent` and detached recurrence saves now preserve explicit color clears instead of
    treating `null` as "keep old color".
  - App/widget/Glyph strings are present in every shipped locale, so locale key parity is now
    complete without base-only translation misses.
  - French, Portuguese, and Russian unit-label plurals suppress `ImpliedQuantity` only where the
    number is rendered separately by the UI; conflict plurals now include the count argument.
- Verification after discount-offer + event-color work:
  - `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest` returned `BUILD SUCCESSFUL`.
  - `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` returned `BUILD SUCCESSFUL`.
  - `.\gradlew.bat --no-daemon --console=plain :app:lintDebug` returned `BUILD SUCCESSFUL`.
  - Locale key parity against base translatable strings: 0 missing / 0 extra in all 8 locale dirs.
  - Debug APK installed successfully on device `4ab0d020`.

## Resume Prompt

Continue DotCal in `D:\Caveman\caveman\Nothing-Calendar` on branch `main`. Read `Docs/HANDOFF.md` and `Docs/DotCal — FINAL PACKAGE 14 Feature.txt`.

**All work happens on `main`.** `versionCode 22`, `versionName 1.3.0`. Every other local branch is already merged into `main` — see `## Worktree Notes`.

**Next up is no longer blocked on Month View Batch 2** -- batch 1 is DONE and approved, batch 2 drag
panel was rejected/reverted, and batch 2 redefined as Google-style event title chips is now DONE and
approved on device. Batch 3 accessibility/dead-param cleanup is also DONE. Priority widget date
clarity + opacity is built and installed for review. Remaining Batch 3 visual polish is optional
unless the user asks for more month-view polish.

**Nothing in the working tree is committed.** `HEAD` is `fd906fc`, 2 commits ahead of `origin/main`, nothing pushed. Uncommitted: the batch 1A `+N` alignment fix and batch 2 title chips in `CalendarViews.kt`, ALL of batch 1B in `AppChrome.kt`, month overflow string updates in `values*/strings.xml`, widget date clarity + opacity slider changes in `DotCalWidgets.kt`, `DotCalGlanceTheme.kt`, `CalendarPreferences.kt`, `DotCalApp.kt`, `SettingsScreens.kt`, widget opacity strings in `values*/strings.xml`, this file, and the user's untracked `Docs/FEEDBACK.md`. **Do not commit unless explicitly told to. Do not create or switch branches.**

**Batch 1 is approved — do not redo it.** Do **not** revert the nav haptics to the Compose API, do **not** add `IGNORE_VIEW_SETTING`, and do **not** add an accent dot under the active nav icon without asking.

**Batch 2's drag panel is dead — do not rebuild it.** See `### Batch 2 — REJECTED AND REVERTED`. `showSheet` and `EventListSheet` are back in place and staying.

**Batch 2 redefined is built, installed, and approved.** It adds event title chips in month cells. Cells are **tall rectangles** (`DayCell`'s `aspectRatio(1f)` is gone, **no inline events list below it**), and chips have **no rectangle border** — flat tinted fills only. Budget on `4ab0d020` (393x873dp): original full-height split was ~612dp for 6 rows → 56w x ~102h cells; after user feedback, rows are capped at `dayCellWidth * 1.60f` (~90dp on this device) to keep rows closer than the first build while adding back a little day-row gap. Current cap is **3 chips + `+N more`**. ~11 characters per chip; truncation is inherent to a 7-column phone grid and the user accepted it.

Settled during implementation: day tap still opens `EventListSheet`, chips are visual-only, and all-day events use the same chip treatment for this batch.

**Do not touch** the `Scaffold` `bottomBar` zero-height spacer (`DotCalApp.kt:1295`) or the bottom-nav render ordering (`DotCalApp.kt:1953`) — both deliberate, both carry comments. The selected-week-chips and drag-panel approaches stay rejected; "Fantastical-style chips in every cell" was **overruled by the user** and is now the plan.

Shell note: absolute Windows paths get mangled by the bash tool — `cd "d:\Caveman\caveman\Nothing-Calendar"` at the start of each Bash call. Python is not on PATH; use sed/awk for file edits.

Manual QA for the camera/microphone device-reach fix is still pending — see `## Manual QA`.

The UI string extraction job is paused mid-way. Passes 1, 2, 3a-i, 3a-ii, 4 and 5 are done and fully audited; pass 6 (leftovers) onward is not started. Read `## Planned: UI String Extraction` for exactly where it stopped and what is left.

**Settings, the Event editor, Tasks and the shared Dialogs are now fully extracted and fully
translated to Spanish.** Switching to `Español` changes Settings, the whole event/task create-edit
flow, and every confirm/update dialog. Spanish is awaiting the user's on-device judgement; German and
the other 7 locales stay picker-only until Spanish is approved.

Two scope decisions the user already made — do not re-ask:

1. **Default language is `System default`** (follows the phone). Already correct in code, no change needed: `AppLanguage.fromTag(null)` returns `System` and `attachBaseContext` leaves the context untouched on an empty tag.
2. **One language at a time.** Spanish first, on-device judgement, then German. Do not batch. Extraction itself is language-neutral and continues for all strings; only `values-es/` gets filled until Spanish is approved.

Also settled: Privacy Policy body stays English (`translatable="false"`) because machine-translated legal text is a liability. `AppLanguage.native` always stays in its own language; `AppLanguage.label` does get translated.

Next step for that job is **pass 6** — `DotCalApp.kt`, `CalendarViews.kt`, `QrEventScreens.kt`, `AvailabilityScreen.kt`, `AgendaScreens.kt` leftovers (see the work order).

Keep Room at 5 tables; no package/deep-link/DB filename changes, Hilt, or Compose Nav. After app changes run required tests/build, then install debug APK when device connected. Report exact manual QA steps and expected results. Do not run manual phone QA yourself unless asked.

## Hard Rules

- Workdir: `D:\Caveman\caveman\Nothing-Calendar`
- Branch: `main`
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

- Version: `versionCode 22`, `versionName 1.3.0`
- Product: black/white/red offline Android calendar.
- Tabs: Calendar, Tasks, Settings.
- Views: Year, Month, Week, Day, Agenda. Keep hidden ThreeDay unexposed.
- Events/tasks stored in `calendar_events`; tasks use `isTask = 1`.
- Existing media fields: `imageUris`, `voiceNotePath`.
- Existing recurrence field: `rrule`.
- DataStore: `calendar_preferences`.
- Pro entitlement: `KEY_IS_PRO`.
- Billing products:
  - `dotcal_pro`: INAPP one-time Lifetime product; option `dotcal-pro-lifetime`; live INR 149.
  - `dotcal_pro_subscription`: SUBS product for Monthly/Yearly plans.
  - `dotcal_pro_sub`: legacy/fallback SUBS product id still queried by code.
- Paywall price is loaded dynamically from Play Billing. Subscription offers come from
  `subscriptionOfferDetails`; lifetime offers come from `oneTimePurchaseOfferDetailsList`. Checkout
  passes the selected `offerToken`. Lifetime default price still falls back to
  `oneTimePurchaseOfferDetails?.formattedPrice`.
- Paywall route: `dotcal://paywall`.
- Billing library: `billing-ktx` 8.0.0; do not downgrade below v8 (Play requires 8.0.0+ from Aug 31, 2026).
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
- C4 Dead Time Finder
- C6 Ghost Events / Pencil-In
- C3 On This Day (Free — shipped in `916dfe3`, ahead of the roadmap entry)

Earlier complete: A1/A2/A3/A5, C2, B1, B5, Search, Templates, Calendar Sets, Shift Patterns, Private Vault, App Lock, import/export, backup/restore, widgets, holidays, birthdays, reminders, billing.

## Latest Feature

In-app Language picker complete locally on `pro-features`.

- Entry: Settings > Appearance > Language. Free, not Pro-gated.
- 10 options: System default plus English, Spanish, Portuguese, Indonesian, German, French, Russian,
  Turkish, Arabic.
- Hindi, Japanese, Korean, and Chinese Simplified were implemented first (the original 13-language
  spec) then **dropped at the user's request**. `AppLanguage.fromTag` maps their tags back to
  `System`, and `AppLanguageTest` locks that in, so a device left on a dropped locale cannot land on
  a stale entry.
- Mirrors DotFiles: framework `LocaleManager` on API 33+, `attachBaseContext` + manual `recreate()`
  below 33. **No `androidx.appcompat` added, no themes re-parented.**
- Persisted to `KEY_APP_LANGUAGE` in the existing `calendar_preferences` DataStore, mirrored into
  the existing `dotcal_boot` SharedPreferences under `app_language` (same pattern as `BOOT_THEME_KEY`)
  because `attachBaseContext` runs before DataStore can be read.
- `reconcileAppLanguage()` on startup (API 33+ only) resyncs DataStore + boot mirror from the OS
  locale, and clears the OS locale if it names a language DotCal does not ship.
- `res/xml/locales_config.xml` + `android:localeConfig` added, so Android's own per-app language
  screen lists DotCal.
- Sheet copies `FontPickerSheet` palette/shape; list is scroll-capped so it cannot overflow short screens.
- JVM test `AppLanguageTest` covers null/blank/unknown -> System, `pt-BR`/`zh-Hans-CN` region and
  script stripping, case-insensitivity, and tag round-trip.
- No Room/package/deep-link/DB/Hilt/Nav changes.
- Required tests/build passed (58 JVM tests, 6 new).
- Latest debug APK installed on device `4ab0d020`.
- **Known gap, by design:** switching language visibly changes almost nothing yet. See
  `## Hardcoded String Inventory`.

C6 Ghost Events / Pencil-In complete locally on `pro-features`.

- Pro-gated `Pencil in` toggle added to the event editor.
- Ghost flags persist in shared side-store namespace `ghost_flags`; no Room/schema change.
- Calendar/agenda rendering now shows ghost events with reduced opacity and dotted/hollow treatment.
- Conflict warnings use softer "Tentatively clashes with..." copy when a ghost event is involved.
- Availability and Dead Time continue to use shared `FreeSlotEngine` ghost busy/free policy.
- JVM coverage includes ghost flag side-store reload plus existing FreeSlotEngine ghost busy/free policy.
- Required tests/build passed after C6.
- Latest debug APK installed on device `4ab0d020` after C6.

C4 Dead Time Finder polish complete locally on `pro-features`.

- Pro entry: Settings > Tools > Dead Time.
- Finds >=60-minute open slots across next 7 days using shared `FreeSlotEngine` via `DeadTimeFinder`.
- Bounds still use `KEY_FREE_TIME_START_HOUR` / `KEY_FREE_TIME_END_HOUR`; no Room/schema changes.
- Slot tap opens event creation prefilled with that date/time.
- Each slot has `Share availability` cross-link, opening Availability for that exact day.
- Dead-time refresh now reacts to event-list changes while the screen is open.
- JVM tests cover exact 7-day window plus all-day and ghost events blocking dead-time slots.
- Required tests/build passed after C4 polish.
- Latest debug APK installed on device `4ab0d020` after C4 polish.

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

1. C1 Life-in-Dots
2. C7 Year Wrapped
3. Vault Decoy PIN

C3 On This Day was **removed from this list** — it was already implemented in `916dfe3`
`feat(calendar): add on this day memories` and this file simply never got updated. See
`## Completed Roadmap`. Do not re-implement it.

## Requested Backlog

- **Widget date clarity + opacity control.** DONE and installed for review. Medium agenda widget now
  keeps today's date number paired with today's weekday, and transparent widgets have a gradual
  opacity slider in `Settings > Widgets`.
- **Month view + bottom nav UX.** Batch 1 and Batch 2 are approved; Batch 3 accessibility/dead-param
  cleanup is done. Only optional visual polish remains — see `## Planned: Month View + Bottom Nav UX`.
- **Pro billing plans.** Current billing supports Yearly, Monthly, and Lifetime via Google Play
  Billing. Lifetime is the one-time `dotcal_pro` INAPP product; subscriptions use SUBS product ids
  `dotcal_pro_subscription` / `dotcal_pro_sub`. See `## Current: Pro Billing Plans`.
- **Shift Worker Convenience Pack.** SuperShift-style user feedback asked for easier work-shift
  entry, schedule sharing, and a two-week widget. Add later as general-calendar shift convenience,
  not as a DotCal rebrand. First scope should be quick shift add, shift-plan image/PDF/ICS export,
  DotCal QR import for small plans, and a two-week widget. See
  `## Planned: Shift Worker Convenience Pack`.
- **Calendar overflow menu customization.** DONE. Calendar overflow actions can be hidden/shown from
  `Settings > Calendar Preferences > Calendar menu`, with Reset support and stable DataStore action
  ids. See `## Current: Calendar Overflow Menu Customization`.
- **PDF event attachments.** DONE. Gary's request is implemented for PDFs using SAF,
  app-private copies, side-store metadata, FileProvider open/share, and backup/restore support. See
  `## Current: PDF Event Attachments`.
- **Full UI string extraction + translation.** The Language picker ships and works, but the app is
  still hardcoded English. This is now the blocking follow-up for the feature to mean anything to a
  non-English user. Scope it as one job — see `## Hardcoded String Inventory`.

## Current: Pro Billing Plans

Current state:

- DotCal supports a one-time Lifetime product and auto-renewing subscription plans.
- `ProManager` queries `BillingClient.ProductType.INAPP` for lifetime and
  `BillingClient.ProductType.SUBS` for subscriptions.
- Purchase restore checks both INAPP and SUBS purchases.
- Paywall uses a shared `ProPurchaseOffer` model for Yearly, Monthly, and Lifetime offers.
- Billing library is `billing-ktx` 8.0.0; do not downgrade below v8.

Product setup:

- Existing one-time product:
  - Product id: `dotcal_pro`
  - Product type: in-app product / one-time
  - Meaning: Lifetime Pro
- Subscription products queried by app:
  - Primary product id: `dotcal_pro_subscription`
  - Legacy/fallback product id: `dotcal_pro_sub`
  - Type: auto-renewing
  - Base plan ids mapped by code: `monthly`, `yearly`
  - Optional offers/trials are allowed, but paywall copy must clearly explain renewal terms.

App implementation:

- Query product details for both product types. Subscription details come from
  `subscriptionOfferDetails`; one-time/lifetime details come from
  `oneTimePurchaseOfferDetails` / `oneTimePurchaseOfferDetailsList`.
- Purchase flow:
  - Lifetime: INAPP product details + selected one-time offer token when present.
  - Monthly/Yearly: SUBS product details + selected subscription offer token.
- Restore/refresh:
  - Query `INAPP` purchases for lifetime.
  - Query `SUBS` purchases for active subscription.
  - Set Pro true if either entitlement is valid.
- Current boolean `isPro` entitlement remains the app gate. Add a display-only source later if needed:
  `None`, `Lifetime`, `Subscription`.
- Paywall:
  - Shows Yearly, Monthly, and Lifetime options when Play Billing returns them.
  - Prices always come from Play Billing.
  - Copy distinguishes subscription renewal from one-time lifetime purchase.
  - Restore button checks both products.
  - `Manage subscription` link appears only when a subscription purchase is detected.

Entitlement rules:

- Lifetime purchased = Pro forever, regardless of subscription state.
- Else active subscription = Pro while Google Play reports it active.
- Else no Pro.
- Existing lifetime buyers must never be downgraded.
- If subscription expires/cancels/account-hold ends and no lifetime purchase exists, Pro should turn
  off after refresh.

Lifetime while subscribed:

- If a subscriber buys Lifetime, DotCal should immediately unlock Lifetime and keep Pro
  forever.
- DotCal app-only code cannot automatically cancel that user's Google Play subscription. The purchase
  is attached to the user's Play account, and the one-time INAPP product is not a subscription
  replacement.
- Show a post-purchase message such as:
  `Lifetime is active. If you also have an active monthly subscription, manage or cancel it in Google
  Play to avoid future renewal.`
- Provide a `Manage subscription` button/deep link to the Google Play subscription management page.
- Do not build backend cancellation now. Developer-initiated cancellation is possible with Google
  Play Developer API, but it requires backend/server auth, purchase-token storage/verification, and
  notification handling. That is out of scope for DotCal's current offline-first/app-only model.

Promo-code behavior:

- Lifetime one-time promo codes redeem through Google Play or inside the Play purchase sheet.
- Subscription one-time-use promo codes can redeem through Google Play or inside the Play purchase
  sheet for the selected Monthly/Yearly plan.
- Subscription custom promo codes are app-flow only: user opens DotCal paywall, selects the plan,
  opens the Google Play purchase sheet, then uses the payment-method dropdown/redeem option.
- If redemption happens outside the app, `Restore Purchase` or app restart refreshes entitlement.

## Planned: Shift Worker Convenience Pack

User feedback:

- User referenced SuperShift-style workflows and liked ideas around easier work-shift entry, sharing
  plans with other DotCal users, and a two-week widget.
- Internal product decision: this is worth adding, but DotCal should stay a general-purpose calendar.
  Build shift convenience around existing events/shift patterns instead of turning DotCal into a
  shift-only app.

Assessment:

- Worth adding after current approved UI/widget polish is accepted.
- Fit is strong because `Shift Patterns` already exist in DotCal and are stored outside Room via the
  existing side-store pattern. The missing value is speed and visibility, not a brand-new data model.
- User segment is broad enough for DotCal: nurses, retail workers, drivers, security, freelancers,
  trainers, support teams, and anyone with repeating irregular schedules.
- Keep as a Pro value pack, but do not hide existing normal event editing behind shift-specific UX.

Phase 1 — Quick Shift Add:

- Add a fast path from Calendar Month/Week/Day: long-press a day/time or use an action such as
  `Add shift`.
- Reuse saved shift types from the current Shift Patterns implementation.
- Let a user pick a shift type and date, then create normal `CalendarEvent` rows through the existing
  repository path. No new Room tables/columns.
- Support multiple shifts per day because SuperShift users explicitly expect split shifts and second
  jobs, but keep this as multiple normal events rather than a separate shift table.
- Keep generated shift events editable like regular DotCal events.

Phase 2 — Share Shift Plan:

- Add range-based export for shifts/calendar plan: week, two-week, month, or custom range.
- First outputs should be image/PDF and ICS, plus DotCal QR for compact/small plans. This satisfies
  "share my roster" without accounts, backend, contacts access, or live sync, while still giving a
  strong DotCal-to-DotCal install/import loop.
- PDF/image should include title, dates, shift names, times, notes/location when present, and optional
  total hours summary if cheap to compute from event durations.
- ICS export can reuse DotCal's existing calendar/event export direction where possible.
- DotCal QR import should reuse the current event QR/ICS import pattern where possible, but show a
  plan-level preview such as `Import 12 shifts?` before writing events.
- Put hard limits on DotCal QR sharing: default to next 14 days or a max event count/payload size;
  for larger plans, route users to ICS/PDF/image instead of generating unreliable QR codes.
- The QR share screen should clearly say it is scanned with DotCal to import the shift plan.
- Do not implement true live sharing between DotCal users in this phase.

Phase 3 — Two-Week Widget:

- Add a compact 14-day widget showing shift/event chips by day. This is useful beyond shift workers
  and fits Android widgets well.
- Widget configuration should allow calendar/account filtering if existing widget config supports it;
  otherwise keep first version simple and reuse visible calendars.
- Reuse the existing widget settings surface so widget settings do not fragment.
- Keep text dense, monochrome/red DotCal identity, and avoid a shift-app-only visual style.

Out of scope unless user demand repeats:

- Live shared calendars between DotCal users. Requires accounts, backend, conflict handling, invites,
  privacy controls, deletion semantics, and likely notification infrastructure.
- Payroll/overtime reports as a full module. A simple total-hours line in export is acceptable, but
  pay calculations would pull DotCal toward a niche workforce app.
- Multi-job management as a first-class entity. Use calendars/colors for now.
- Shift alarms. Useful, but permission/platform behavior and reliability need a separate design pass.

Hard boundaries:

- No Room schema change for this pack unless explicitly approved later. Keep Room exactly 5 tables.
- No cloud sync/backend/live account sharing in v1.
- No package/deep-link/DB filename changes.
- Do not start this before Batch 2 month chips are approved.

## Current: Calendar Overflow Menu Customization

Status:

- Implemented show/hide customization for Calendar tab three-dot overflow actions.
- Route: `Settings > Calendar Preferences > Calendar menu`.
- UI: one switch per overflow action plus a `Reset` action in the header.
- Storage: DataStore key `hidden_calendar_menu_actions` stores stable hidden action ids. Missing or
  blank storage means all actions are visible by default.
- Current configurable actions: Search, New Event, Add Shift, Go to date, Quick Add, Share
  availability, Templates, Calendar Sets, and Shift Patterns.
- The fixed top-bar `+` button and hardware-gated QR scanner icon remain outside this customization
  and keep their prior behavior.
- Pro/free gating, Pro badges, menu subtitles, and hardware checks remain unchanged.
- Added locale keys for the new settings labels in every shipped locale.
- Added `CalendarOverflowActionTest` for persisted hidden-action parsing.

Verification:

- `git diff --check` passed with CRLF warnings only.
- `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`
  returned `BUILD SUCCESSFUL`.

Possible follow-up:

- Add drag reorder only if users ask for ordering control.

## Current: PDF Event Attachments

User request from Gary:

> I love the app, and bought the pro licence shortly after installing it.
> Would it be possible to have file attachments as well as images please? I'll often attach venue
> tickets to an event if a PDF has been provided.

Current shipped scope:

- Event editor has an `Add PDF` attachment flow near event media.
- Event detail displays attached PDFs.
- PDF import uses Android document picking; no broad storage permission is requested.
- Selected PDFs are copied into app-private event attachment storage and exposed for viewing through
  `FileProvider`.
- Attachment metadata is stored in side-store data under the event id; Room schema remains unchanged.
- Initial limits: 5 PDFs per event and 20 MB per PDF.
- Adding PDFs is Pro-gated. Viewing/opening/removing existing PDFs remains available if Pro
  entitlement is later lost.
- Backup/export serializes PDF attachment metadata and bytes; restore recreates app-private files and
  reports restored PDF count.

Important boundaries:

- Generic document attachments are not shipped yet. Current user-facing scope is PDF only.
- Do not implement Google Calendar / Drive attachment sync without a separate product/privacy review.
  DotCal remains offline-first and CalendarProvider-based.
- Do not store arbitrary file blobs in Room.
- Do not request broad storage permissions. SAF + app-private copies remains the preferred Android
  path.

Possible follow-up:

- Add common document MIME types only if UX copy, opener behavior, backup size impact, and support
  expectations are reviewed first.

## Hardcoded String Inventory

Measured on `pro-features` after the Language picker landed. **This is the open i18n job.** The
picker switches locale and refreshes correctly, but the only strings that actually change are the
picker's own three, because everything else is a Kotlin literal rather than a resource.

Correcting the earlier estimate in this file: the real count is much higher than "141+", and there
were **zero** `stringResource` call sites before this change, not 3. The 13 base strings are consumed
only from `AndroidManifest.xml` and the six `res/xml/dotcal_widget_*.xml` provider files — no Kotlin
code read a string resource at all.

| Category | Count |
|---|---|
| `Text("literal")` positional literals | 301 |
| Named user-facing params (`text`/`title`/`subtitle`/`label`/`message`/`placeholder`/`confirmText`/`dismissText`) | 196 |
| **Total user-facing literals** | **497** |
| `contentDescription` literals (accessibility, separate pass) | 49 |
| `stringResource` call sites | 4 (all added by the Language picker) |
| Strings in `values/strings.xml` | 16 (13 widget/Glyph + 3 picker) |

**Progress against this baseline** (after passes 1/2/3a-i/3a-ii/4/5): `values/strings.xml` holds
**570 strings + 27 plurals**, Kotlin has **575** `stringResource` + **18** `pluralStringResource`
call sites, and `AppChrome.kt`, `SettingsScreens.kt`, `EventScreens.kt`, `TaskScreens.kt`,
`DialogScreens.kt` and `ProFeatureScreens.kt` are at **0** user-facing literals. The remaining files
still match the table below.

`Text("literal")` literals by file — use this to shard the extraction work:

| File | Count |
|---|---|
| `ui/ProFeatureScreens.kt` | 80 |
| `ui/SettingsScreens.kt` | 63 |
| `ui/EventScreens.kt` | 49 |
| `ui/TaskScreens.kt` | 19 |
| `ui/DialogScreens.kt` | 16 |
| `widget/DotCalWidgets.kt` | 14 |
| `ui/QrEventScreens.kt` | 10 |
| `ui/AvailabilityScreen.kt` | 10 |
| `ui/DotCalApp.kt` | 9 |
| `ui/AgendaScreens.kt` | 8 |
| `ui/CalendarViews.kt` | 7 |
| `ui/OnboardingScreens.kt` | 5 |
| `ui/OnThisDayCard.kt` | 4 |
| `widget/WidgetConfigActivity.kt` | 3 |
| `share/CardImageExporter.kt` | 2 |
| `ui/AppChrome.kt` | 1 |
| `share/QrEventImageExporter.kt` | 1 |

Notes for whoever scopes the extraction:

- Enum `label`/`tagline`/`native` fields (`AppFont`, `AppLanguage`, `CalendarTab`, `WeekStartOption`,
  `AccentColor`, `ShiftType`) hold display text outside Compose, so they need `@StringRes` ids or a
  mapping composable rather than a plain literal swap.
- Widget and Glyph text runs outside an activity context, so it does not pick up the per-app locale
  the same way — verify separately.
- Date/time formatting already goes through `java.time` formatters; those localize off the context
  locale once the wrapped context reaches them, so they are not part of the literal count.
- Reproduce the counts with:
  `rg -U -o 'Text\(\s*"[^"\n]{2,}"' --glob '*.kt' app/src/main/java | wc -l`

## Planned: Month View + Bottom Nav UX

**Batch 1 DONE and approved on device (uncommitted). Batch 2 was built, rejected on sight, and fully
reverted — see `### Batch 2 — REJECTED AND REVERTED`. Batch 2 is now redefined as Google-style event
title chips in the month cells and approved on device. Batch 3 accessibility/dead-param cleanup is
also DONE.** Remaining Batch 3 visual polish is optional; keep any future polish in small batches
with `## Required Verification` between each.

### What is there today

- `MonthView` (`CalendarViews.kt:112`) — 42 fixed cells (6 rows always), horizontal swipe at a
  50dp threshold flips month, bulk-select bar at the bottom when `selectedBulkDates` is non-empty.
- `DayCell` (`CalendarViews.kt:223`) — tall rectangular cell, 28dp day-number circle, max 3 flat
  tinted event-title chips via `events.take(3)`, `+N more` overflow indicator, dimmed out-of-month
  day numbers (batch 1A).
- Day tap → `viewModel.selectDate()` + `showSheet = true` (`DotCalApp.kt:1409`) → `EventListSheet`
  (`AgendaScreens.kt:73`), a `ModalBottomSheet` owned at app level (`DotCalApp.kt:2988`) with a
  **fixed 260dp** `LazyColumn` (`AgendaScreens.kt:96`). **Still in place — batch 2's removal of this
  was reverted.**
- `DotCalBottomNav` (`AppChrome.kt:421`) — floating 68dp pill, 3 items, `weight(1f)` per item, 48dp
  touch target, 26dp/1.85dp Canvas icons incl. hand-drawn gear (all batch 1B).

### Research summary

Two industry patterns: **dots + tap-to-reveal** (Apple, Google Calendar mobile month — what DotCal
does) and **chips + overflow** (Fantastical, Notion Calendar, Outlook). Findings that drove the plan:

- The standard overflow affordance is 2-3 events plus a `+N more` indicator. Silently dropping the
  4th event is the single most-reported complaint about dot-style month views.
- Guidance is consistent: treat the grid as a **navigator** and keep a detail region visible at the
  same time so month context never disappears.
- Google Calendar's own event sheet is capped at roughly a quarter of the screen precisely so the
  grid stays visible and directly tappable behind it.
- Colour-only encoding is never accessible on its own; pair it with shape, position or text.

**Rejected on purpose:** the "expand the selected week row into chips" hybrid. It is a real
best-practice, but the persistent panel (batch 2) solves the same problem, Week and Agenda views
already exist, and animating row heights adds layout risk for no extra gain. Do not revive it.

**Also rejected:** Fantastical-style chips in every cell. Titles in a 7-column mono grid break the
dot-matrix identity the product is built on. Density tint carries the same "this day is busy" signal
without the clutter.

### Batch 1 — low-risk polish (DONE, approved on device, uncommitted)

All 8 items shipped. 1A (month grid) and 1B (bottom nav) were both approved on device `4ab0d020`.

1. ✅ **`+N` overflow indicator.** String resource `month_day_more_count`, base + all 8 locales,
   key parity 720/755. Includes the vertical-alignment fix: `includeFontPadding = false` plus a
   `LineHeightStyle` trim, because the Row otherwise centres the taller font-metrics box and the
   glyphs sit visibly below the 4dp dots.
2. ✅ **Out-of-month day numbers** — dimmed on `palette.disabledText`, no dots.
3. ✅ **Bottom nav touch target 30dp -> 48dp.** Icon visual size unchanged; only the clickable box grew.
4. ✅ **Bottom nav spacing** — `spacedBy(80.dp)` replaced with `weight(1f)` per item.
5. ✅ **Normalised nav icon sizes** — 26dp / 1.85dp stroke via `NAV_ICON_SIZE` + `NAV_ICON_STROKE`
   (Tasks went 28dp -> 26dp).
6. ✅ **Nav haptics.** Uses `LocalView` + `HapticFeedbackConstants.VIRTUAL_KEY`. The first attempt with
   Compose `HapticFeedbackType.TextHandleMove` did **not** fire on this device. **Do not revert to the
   Compose API here, and do not add `IGNORE_VIEW_SETTING`** — the system haptic preference must win.
7. ✅ **Settings icon** — hand-drawn Canvas gear (8 teeth, 5.2dp inner / 7.6dp outer) replacing
   Material `Icons.Filled.Settings`. The `SettingsGearIcon` import was removed from `AppChrome.kt`
   **only**; `DotCalApp.kt`, `EventScreens.kt` and `ProFeatureScreens.kt` still use it.
8. ✅ **Selected-state indicator** — dead `selectedFill` block deleted. **No accent dot was added; ask
   before adding one.**

### Batch 2 — REJECTED AND REVERTED (2026-08-11)

**A persistent drag-resizable panel was fully implemented, installed on `4ab0d020`, rejected on sight
by the user, and reverted byte-exact. Do not rebuild it.**

What was built: a bottom-anchored `MonthEventPanel` inside `MonthView` drawn *over* the grid, with an
animated `heightIn` cap, drag-to-expand, `BackHandler` collapse, and removal of all five `showSheet`
sites plus `EventListSheet`. It built clean (65 tests) and installed fine.

**Why it was rejected:** it read as a bottom sheet that would not go away, and it is not how other
calendars present month events. The revert restored `CalendarViews.kt` to the batch-1A blob (`9fb40f8`)
and `DotCalApp.kt` / `AgendaScreens.kt` to HEAD with zero diff. `showSheet` and `EventListSheet` are
**back in place and staying** unless a future batch deliberately removes them.

Lesson for whoever picks this up: the written plan said "grid above, drag-resizable panel below" but
never settled whether the grid shrinks or the panel floats. That ambiguity is what produced a rejected
build. **Settle the visual shape against a reference screenshot before writing code.**

### Batch 2 (redefined) — Google-style event title chips in month cells

**Built, installed on `4ab0d020`, and approved on device.** The user's chosen direction, from a
reference screenshot of a month grid showing event title blocks inside the cells.

Two decisions the user made explicitly:

1. **Cells become tall rectangles, not squares.** `DayCell`'s `aspectRatio(1f)` has to go. Chips need
   the grid to fill the screen, so there is **no events list below the grid** — the user was shown
   that trade-off and chose chips.
2. **No rectangle border on the chips.** Flat tinted fills only, no outline.

Settled during implementation:

- Day tap still opens `EventListSheet`; chips are visual-only and have no direct tap target.
- All-day events use the same chip treatment as timed events for this batch.
- There is no inline event list below the grid. The existing modal `EventListSheet` remains owned by
  `DotCalApp`.

Implementation:

- `MonthView` grid `BoxWithConstraints` now takes `weight(1f)` and computes row height from the
  available height, capped at `dayCellWidth * 1.60f` so adjacent day rows stay closer than the first
  full-height build while leaving room for three chips.
- `DayCell` no longer applies `aspectRatio(1f)`. It fills the row height, keeps the 28dp day number,
  then renders up to three `MonthEventChip`s with tight vertical spacing.
- `MonthEventChip` is a 14dp-high flat tinted rounded fill, 8sp monospace title, no border. Ghost
  events keep the same shape with lower fill/text alpha.
- Overflow copy now uses the existing `month_day_more_count` key as `+N more` (base + all 8 locale
  files).
- Jump-to-date highlight was re-anchored around the day number because tall cells made the old
  height-relative circle drift into chip space.

Vertical budget, measured on `4ab0d020` (1080x2400 @440dpi = 393x873dp):

| Element | Height |
|---|---|
| Chrome above grid (56 action bar + 12 + 42 segmented + 4 + 32 weekday header) | 146dp |
| Bottom nav pill clearance | 90dp |
| **Left for 6 grid rows** | **~612dp** |
| Per cell | 56w x **~102h** |
| Day number | 28dp |
| **Left for chips** | **~70dp; current cap is 3 chips + `+N more` after user spacing feedback** |

Known constraint the user accepted: a 56dp-wide cell in mono at ~7sp fits about **11 characters**, so
"Team Stand-up" truncates to `Team Stand…`. Google Calendar's mobile month view is equally cramped —
this is inherent to a 7-column phone grid, not a bug to fix.

**This overrules the earlier "Also rejected: Fantastical-style chips in every cell" note below.** That
rejection was a brand judgement (chips break the dot-matrix identity) and the user has now overruled
it deliberately. The *other* rejection — the selected-week-expands-into-chips hybrid — still stands.

Verification:

- `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug` returned
  `BUILD SUCCESSFUL`.
- Debug APK installed successfully on device `4ab0d020`.
- User reviewed on device and approved.

### Batch 3 — density and motion

**Partial low-risk polish DONE and installed on `4ab0d020`:**

- Month day cells now expose TalkBack labels with date, today/selected/bulk/out-of-month state, event
  count, visible event titles, and `+N more` equivalent.
- Bottom nav items now expose `Role.Tab`, selected state, and labels for Calendar, Tasks, Settings.
- `MonthView` dead params `onJumpToday` and `onJumpPickerRequest` were removed from the Month path.

Verification:

- `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug` returned
  `BUILD SUCCESSFUL`.
- Debug APK installed successfully on device `4ab0d020`.

Remaining optional visual polish:

1. **Density tint.** Reuse the `DayDensityDot` logic (`AgendaScreens.kt:350`) to tint a busy day's
   cell background. More information than dots, no added clutter, and it strengthens the dot-matrix
   look rather than fighting it.
2. **All-day vs timed distinction.** Every dot is an identical 4dp circle today, separated only by
   colour. Give all-day events a short horizontal bar and timed events the dot. The ghost-event
   dotted-border treatment is the precedent to follow.
3. **Month transition animation.** Swipe currently jumps with no motion, while tab switching uses
   `Crossfade` (`DotCalApp.kt:1374`) — inconsistent. Add direction-aware `AnimatedContent` slide.
4. **Conditional 6th row.** Skip the last grid row when it is entirely out-of-month, giving the
   remaining cells more height.

### Cleanup noted while reading

- `MonthView` dead params `onJumpToday` and `onJumpPickerRequest` were removed from the Month path.

### Do not touch

- The `Scaffold` `bottomBar` zero-height spacer (`DotCalApp.kt:1295`) and the bottom-nav render
  ordering (`DotCalApp.kt:1953`). Both carry comments explaining why they are that way; they are
  deliberate, not oversights.

## Planned: UI String Extraction

**In progress, paused.** Passes 1, 2, 3a-i, 3a-ii, 4 and 5 are done, verified and audited (see
`### Post-pass-4 audit`); **pass 6 (leftovers) onward is not started.** This job is behind
`## Planned: Month View + Bottom Nav UX` in priority — resume it after those batches unless asked
otherwise.
The inventory below (`## Hardcoded String Inventory`) was measured *before* this work — the per-file
counts there are still broadly right for the untouched files, but `AppChrome.kt`,
`SettingsScreens.kt`, `EventScreens.kt`, `TaskScreens.kt`, `DialogScreens.kt`, `ProFeatureScreens.kt`
and the converted enums no longer match it.

### Scope decisions (settled — do not re-ask)

- **Default language: `System default`.** Already correct in code; no change was needed.
- **One language at a time.** Spanish first, user judges on-device, then German. Extraction is
  language-neutral and covers every string, but only `values-es/` gets filled until Spanish passes.
- **Privacy Policy body stays English** via `translatable="false"` — machine-translated legal text is
  a liability. Only the screen title / Settings row label (`Privacy Policy`) is translated. Pass
  3a-ii deliberately left the ten section headings (`01  Overview`…) and the hero line English too:
  translating headings over English bodies produces a half-Spanish page that reads as broken, not
  deliberate. The whole policy page is one English block behind one translated title.
- **`AppLanguage.native` is never translated** (each entry shows its own language, by design).
  `AppLanguage.label`, the English name underneath, is translated.

### Order of work

1. ✅ **DONE — Month/weekday names.** Turned out to be **7** sites, not the 2 originally listed:
   `CalendarViews.kt:505,1569,1737`, `AgendaScreens.kt:375,523,531`, `EventScreens.kt:1946`. All now
   use `getDisplayName(TextStyle.SHORT|NARROW, Locale.getDefault())`. No `strings.xml` work.
   Also swept the display date/time formatters off `Locale.US` (see the formatter-caching note below).
2. ✅ **DONE — Navigation shell.** `AppChrome.kt` overflow menu (8 items + subtitles) extracted;
   `CalendarTab` converted to `@StringRes` with `@Composable` `label`/`shortLabel` getters.
3. ✅ **DONE — Settings.**
   - ✅ **3a-i:** `WeekStartOption`, `DotCalThemeMode`, `AppFont`, `AccentColor` all converted to
     `@StringRes` + `@Composable` getters.
   - ✅ **3a-ii:** every remaining `SettingsScreens.kt` literal. `SettingsScreens.kt` now has **0**
     `Text("literal")` and **0** user-facing named-param literals; it holds **194** of the app's
     `stringResource` call sites. Covered: all panel titles, screen headers, row labels, toggle
     subtitles, sheet titles, button text, the App Lock screen + PIN dialog, the Private Vault rows,
     the Tools cards, the Pro card/row, the accent picker (HUE/SATURATION/BRIGHTNESS), and the
     Privacy Policy screen scaffolding.
   - Also swept the shared Settings label helpers in `UiHelpers.kt` — `reminderLabel`,
     `syncIntervalLabel`, `calendarAccountsLabel`, `selectedHolidayCountriesLabel`,
     `lastSyncedSubtitle`/`lastSyncedRelativeLabel`, `secondaryCalendarLabel` — plus
     `eventDurationLabel` and `privateVaultWhenLabel` in place. All are now `@Composable`.
     `readableCalendarLabel` was left alone on purpose (see the not-translated list).
4. ✅ **DONE — Event editor, Tasks, Dialogs.**
   - ✅ `DialogScreens.kt` — **0** literals left. `ConfirmDeleteDialog`, `DragConflictDialog`,
     `TemplateNameDialog`, `UpdateAvailableDialog`, `UpdateReadyDialog` all resource-backed. The
     conflict body became a `<plurals>` (`dialog_conflict_overlaps`) instead of a hand-built
     `if (count == 1)`.
   - ✅ `EventScreens.kt` — **0** user-facing literals left (one `Text("+")` remains: an add-image
     glyph, not copy). Covered: detail screen sections + action sheet, the whole editor body,
     image/voice-note sections, conflict warnings, calendar picker, Go-to-date sheet, countdown
     card, date/time pickers, Reminder/Repeat/Apply-to sheets, and the full custom-recurrence
     builder.
   - ✅ `TaskScreens.kt` — **0** literals left. Detail screen, action sheet, filter control, all four
     empty states, the editor, and both picker sheets.
   - `TaskFilter` and `RecurrenceOption` converted to the `@StringRes` + `@Composable` getter
     pattern. `repeatRowLabel`, `freqUnitLabel`, `nthWeekdayPhrase`, `taskReminderMetadataLabel`
     and `CalendarAccount.cleanPickerSubtitle` are now `@Composable`.
   - Two more inline `DateTimeFormatter.ofPattern(...)` calls in `JumpToDateSheet` /
     `DateTimeChoiceSheet` were moved onto `localizedFormatter`.
5. ✅ **DONE — Pro/Paywall.** Split into two sub-passes. **Onboarding was dropped from this pass
   on purpose** — it is permanently English (see the not-translated list).
   - ✅ **5a — the shared recurrence/reminder labels.** `RecurrenceRule.humanLabel()` was
     **deleted** from the data layer along with the now-orphaned `ordinalWord()`,
     `DayOfWeek.shortName()`, `DISPLAY_FORMAT` and the `TextStyle` import.
     `RecurrenceRule.kt` is now Context-free *and* English-free. Replacement: `@Composable`
     `recurrenceHumanLabel(rrule: String?)` plus a `RecurrenceRule` overload, both in
     `ui/UiModels.kt`; all 5 call sites rewired. `nthWeekdayPhrase` moved from
     `EventScreens.kt` to `UiModels.kt` (now `internal`) and gained a
     `style: TextStyle = TextStyle.FULL` param — the picker row spells the day out
     ("The 1st Monday"), the compact sentence abbreviates ("on the 1st Mon"). **Do not collapse
     those two back together.** 5 new sentence-fragment strings; the last four are **quoted**
     in XML because their leading spaces are load-bearing.
     - **Real bug fixed here:** `recurrenceDetailLabel` built `"REPEATS / " + …uppercase()`, but
       both call sites then applied `.toSentenceCase()`, so the caps never reached the screen —
       visible text was always `Repeats / daily`. Resources are now written in the case they
       display in and both transforms are gone, which also killed a latent Turkish dotless-i bug.
       Same treatment for `EventReminder.detailLabel()`. `String.toSentenceCase()` had zero
       callers left and was **deleted** from `UiHelpers.kt`.
     - **Accepted cosmetic delta:** the old `toSentenceCase()` lowercased the whole string, so
       "Every 2 weeks on Mon, Fri" displayed as "…on mon, fri". Natural casing is kept now rather
       than reproducing that with a locale-hostile `lowercase()`.
   - ✅ **5b — `ProFeatureScreens.kt`.** **0** user-facing literals left; the only remaining
     string literals are `Text("0", …)` (a numeric placeholder) and the Quick Add example
     prompts, which stay English because the parser only understands English tokens.
     Covered: the whole paywall + "You're Pro!" screen, all 14 `PRO_FEATURES` rows, Global
     Search (headers, facets, empty states), Quick Add (preview chips + "Try one"), Templates,
     Calendar Sets, Time Insights, Availability, Shift Patterns (types, builder, generator),
     Recently Deleted, and the Date Calculator. `ProFeature`, `TimeInsightRange`,
     `SearchTypeFilter` and `SearchDatePreset` all converted to the `@StringRes` +
     `@Composable` getter pattern. `quickAddRepeatLabel`, `quickAddWhenLabel`,
     `quickAddTimeLabel`, `templateSummaryLabel`, `shiftTypeSummary`, `shiftPatternSummary`
     and `shiftCycleLabel` are now `@Composable`.
     - **Three more locale bugs fixed:** `CalcSectionLabel` and the Quick Add preview chip both
       ran `uppercase(Locale.getDefault())` on display text — the resources are now stored
       ALL CAPS and the transforms are gone. Three raw `DateTimeFormatter.ofPattern` calls
       (search task row, dead-time finder, availability) moved onto `localizedFormatter` /
       the shared `detailDateFormatter`, and the top-level `timeInsightDateFormatter` val was
       deleted in favour of the existing `compactDateFormatter`. Time Insights also rendered
       the busiest weekday via `dayOfWeek.name.take(3)` — the raw English enum constant — now
       `getDisplayName(TextStyle.SHORT, Locale.getDefault())`.
6. ⬜ **Leftovers:** `DotCalApp.kt` (3+14), `CalendarViews.kt` (7+5), `QrEventScreens.kt` (4+3),
   `AvailabilityScreen.kt` (2+7), `AgendaScreens.kt` (2).
7. ⬜ **Plurals partially wired.** `values/strings.xml` now holds **27** `<plurals>`. The 4 added in
   3a-ii (`holiday_countries_selected`, `sync_minutes_ago`, `sync_hours_ago`, `sync_days_ago`), the
   7 added in pass 4 (`dialog_conflict_overlaps`, `countdown_days_until`, `recurrence_times`,
   `recurrence_unit_day`/`_week`/`_month`/`_year`) and the 8 added in pass 5
   (`reminder_detail_minutes_before`, `trash_minutes_ago`/`_hours_ago`/`_days_ago`,
   `shift_day_cycle`, `calc_days_total`, `calc_days_before_start`, `calc_days_after_start`)
   **are** wired. The original 8 event/selection
   plurals still have **no** call site — 16 interpolated sites build strings by hand
   (`AgendaScreens.kt:307`, `CalendarViews.kt:201`, and ~14 `showBulkResult`/toast calls in
   `DotCalApp.kt:2214-2851`).

### Spanish (`values-es/`) — filled through pass 5

`values-es/strings.xml` is **complete for everything extracted so far**: 3 picker strings before,
**542 strings + 27 plurals** now (181 values added in pass 5). This is the first language for
on-device judgement, per the one-language-at-a-time decision. `values-de/` and the other 7 locale
folders are still picker-only and stay that way until Spanish is approved.

Verified with a key diff (base translatable keys vs `values-es`): **zero** stray keys in Spanish, and
the only base keys still missing are `app_name` plus the **11** widget/Glyph strings, which are
deliberately their own later pass. A format-specifier diff also passes across all **577** shared
entries — every `%1$s`/`%1$d`/`%2$s` matches the base arity and ordering, and the four load-bearing
leading spaces in the quoted `recurrence_label_*` fragments survive the XML round-trip.
Reproduce with:

```bash
rg -o 'name="([a-z0-9_]+)"' -r '$1' app/src/main/res/values/strings.xml | sort > base_all.txt
rg -o '<(string|plurals) name="([a-z0-9_]+)"[^>]*translatable="false"' -r '$2' \
  app/src/main/res/values/strings.xml | sort > base_nontrans.txt
comm -23 base_all.txt base_nontrans.txt > base_trans.txt
rg -o 'name="([a-z0-9_]+)"' -r '$1' app/src/main/res/values-es/strings.xml | sort > es.txt
comm -13 base_trans.txt es.txt   # stray keys in ES — must be empty
comm -23 base_trans.txt es.txt   # untranslated — expect app_name + 12 widget/Glyph only
```

Translation notes for the reviewer:

- `%1$s`/`%1$d` placeholders and their order are preserved in every Spanish string; `settings_pair_value`
  (`%1$s / %2$s`) replaced two Kotlin `"${a} / ${b}"` interpolations so the separator is translatable.
- `settings_syncing` keeps the literal `...` (not `…`) to match the English source exactly.
- Spanish uses the same `one`/`other` plural split as English, so all 27 plurals map 1:1.
- The pass-4 conflict lines (`event_conflict_tentative` / `event_conflict_overlaps`) are **whole
  sentences** with `%1$s` (title) and `%2$s` (time range), not a prefix concatenated in Kotlin —
  Spanish needs to control the whole word order.
- Watch for truncation: Spanish runs ~20% longer than English. The known tight spots are
  `settings_app_lock_vault` ("Bloqueo de app y bóveda privada") on the Settings row,
  `settings_widget_dot_texture_subtitle_disabled`, and — new in pass 4 —
  `event_pencil_in_subtitle`, `event_voice_permission_denied` (an all-caps single-line row), and
  `task_mark_complete`.

Current totals: **570 strings + 27 plurals** in `values/strings.xml` (36 of the strings are
`translatable="false"`), **575** `stringResource` + **18** `pluralStringResource` call sites in Kotlin
(was 235 `stringResource` after 3a-ii, 399 after pass 4).

### Trap: display text lives in enums, not just Compose

**Resolved for the 5 enums converted so far** (`CalendarTab`, `WeekStartOption`, `DotCalThemeMode`,
`AppFont`, `AccentColor`). The pattern used: store `@StringRes val labelRes: Int` on the entry, expose
a `val label: String @Composable get() = stringResource(labelRes)`. Call sites are unchanged because
the property name stayed `label`.

Two gotchas this pattern brings, both already hit and fixed:

- A `@Composable` getter can only be read from a composable. `SettingsOptionSheet`'s
  `label: (T) -> String` parameter had to become `label: @Composable (T) -> String`. Pass 3a-ii hit
  the same wall in `EventScreens.kt` and did the same to **`ChoiceSheetContent`** (used by the
  Reminder / time / "Apply to" sheets in Event and Task editors) once `reminderLabel` became
  `@Composable`.
- Kotlin rejects **function references** to `@Composable` lambdas
  (`Function References of @Composable functions are not currently supported`). Three call sites had
  to change from `label = ::reminderLabel` to `label = { reminderLabel(it) }`.

A third gotcha, new in 3a-ii: `LazyColumn`'s **`key = { label(it) }`** cannot call a `@Composable`
label — `LazyListScope` lambdas are not composable. `ChoiceSheetContent` dropped its `key` entirely.
Safe here: these option lists are short, static, and hold no per-row state. Do **not** reintroduce a
`key` that calls a display label; key on a stable id if one is ever needed.

Pass 4 converted two more with the same pattern: **`TaskFilter`** (`TaskScreens.kt`) and
**`RecurrenceOption`** (`UiModels.kt`, keyed on `labelRes` with `rrule` still the stable storage
value). `repeatRowLabel` is now `@Composable` too.

Still literal, needing the same treatment: `TimeInsightRange`, `SearchTypeFilter`,
`SearchDatePreset`, `AvailabilityPreset` (`AvailabilityScreen.kt:57`), and `ShareEventOption`
(`DotCalApp.kt:2950`). `ScreenTab` and `ShiftType` carry **no** display label — the original note
was wrong about those.

A fourth gotcha, new in pass 4: **`buildList { }` is an inline lambda, but a `@Composable` call
inside it is fragile** — the action-sheet lists in `EventScreens.kt` and `TaskScreens.kt` now hoist
every label into a `val` *before* `buildList`, then reference it inside. Do the same for any new
`CompactActionItem` list. Same rule for `showDotCalToast` strings and `ifBlank { }` fallbacks: they
run in non-composable lambdas, so `stringResource` is read into a local first (see
`savedNoReminderToast` / `templateSavedToast` / `templateDefaultName` in both editors).

Do **not** translate the enum `storageValue`/`id`/`storageKey`/`tag` strings (`ndot`, `ntype`,
`system`, `MONDAY`, `REGION_DEFAULT`, `AppLanguage.tag`) — persisted to DataStore, must stay stable.

### Editor scroll + keyboard fixes (found during pass-4 QA)

Three pre-existing bugs the user hit while testing pass 4. The first two were in the event **and**
task editors; the third is event-editor only (the task editor has no conflict warnings at all).

1. **Tapping any row scrolled the form back to the top.** `clearEditorFocus()` / `clearTaskFocus()`
   ended with `focusSinkRequester.requestFocus()` on a 1.dp `Box` parked as the **first child of the
   scrolling column**. Compose scrolls a newly focused node into view, so every row tap (Starts,
   Ends, Reminder, Repeat, Date, Time…) dragged the user back to the top. Fix: the sink `Box`,
   its `FocusRequester`, and the `requestFocus()` call are gone. `keyboardController?.hide()` +
   `focusManager.clearFocus(force = true)` already drop the text-field focus without touching scroll
   position. **Do not reintroduce a focus sink inside a scrolling container.**
2. **The overlap/conflict warning rendered under the soft keyboard and looked cut off.** Neither
   editor had `imePadding()`, and `ConflictWarningSection` sits directly under the Starts/Ends rows —
   exactly where the keyboard covers. Fix: `.imePadding()` added to both scroll columns, after
   `verticalScroll(...)` and before the content padding.
3. **The conflict warning flashed half-drawn on editor entry, then vanished.** (Confirmed fixed on
   device `4ab0d020`.) `conflictWarnings` is
   shared **ViewModel** state, not per-editor state, so opening an editor rendered the *previous*
   session's hits, held them through the 300 ms debounce, then swapped them out.
   `refreshConflictWarnings` now takes a **`sessionKey`** (the editor's existing `editorStateKey` —
   `event?.id ?: editorSessionKey`, `EventScreens.kt:1208`) and clears only when that key changes,
   i.e. only when a genuinely new editor session opens.

   The first cut of this fix cleared `_conflictWarnings` unconditionally at the top of the function.
   That is wrong: the `LaunchedEffect` at `EventScreens.kt:1362` fires on **every** start/end
   date-time change, so a user who already had a real warning showing and then nudged the end time
   would see it blink out and reappear 300 ms later. Clearing is now scoped to two cases that
   genuinely need it — a new session, and the all-day / non-positive-duration early return (no
   lookup follows that branch, so nothing downstream would replace a stale list). **Within one
   session the last result is deliberately held across the debounce window.**
   `clearConflictWarnings()` (called on editor dismiss and after save) also resets the session key,
   so reopening the same event still counts as a new session.

Note the `LaunchedEffect` key changed from `event?.id` to `editorStateKey`. `event?.id` is `null`
for every brand-new event, so back-to-back new-event sessions previously shared one key; the
session-scoped clear needs them to be distinct.

Not a regression from the string extraction — the literals there were already resource-backed. The
conflict copy itself is unchanged in length; it was a layout/inset problem.

### Post-pass-4 audit — complete, no defects found

Six checks over everything extracted through pass 4. All clean; nothing needed fixing.

1. **Duplicate keys** — zero in `values/` and zero in `values-es/`.
2. **Spanish key parity** — zero stray keys; the only untranslated base keys are `app_name` plus the
   11 widget/Glyph strings (the deliberate later pass). Placeholder count, type and order verified
   identical on every shared key.
3. **Extraction fidelity** — all **400** translatable resource values (strings + plural items) were
   diffed against the string literals in `git show 95d3a7f:<path>`. **351** are byte-identical;
   **32** match once format placeholders are normalised (`%1$s` vs the original `${...}`
   interpolation). The remaining **17** are accounted for individually:
   - 3 apostrophe strings (`menu_share_availability_subtitle`, `font_system_tagline`,
     `settings_birthday_calendar_subtitle`) differ only by the required XML `\'` escape.
   - 9 are new-by-design: the 8 pending event/selection `<plurals>` `one` forms plus
     `holiday_countries_selected[one]`, which have no single-count original (the originals hand-built
     `"$n events updated"` with no singular branch).
   - 3 are the new Language picker (`settings_language_title`, `settings_language_system_default`,
     `language_sheet_subtitle`), which have no pre-extraction original at all.
   - `task_reminder_minutes_before` (`%1$d min before`) intentionally unifies three identical
     literals (`"5 min before"` / `"10 min before"` / `"30 min before"`).
   - `event_conflict_tentative` / `event_conflict_overlaps` are the deliberate whole-sentence
     rewrite of one hand-concatenated line (was `"${prefix} ${title} ${range}"`) so Spanish controls
     word order. Wording, casing and punctuation otherwise preserved.
4. **RRULE / storage-value integrity** — verified no localized string can reach `RecurrenceRule`.
   `RecurrenceOption` keeps `rrule` as the stable field and only `labelRes` is a resource; the custom
   builder's `buildRule()` composes from typed enums and `toRRule()` emits `freq.name` +
   `rruleCode()`. `parse` keys, `Locale.US` call sites, and the `"AM"`/`"PM"` token pair are all
   untouched.
5. **`@Composable` gotchas** — no `= ::fn` reference points at a `@Composable` lambda (all 17
   remaining ones are plain handlers like `::resetDrag`, `::submit`), and every surviving Lazy
   `key = { }` keys on a stable id (`it.id`, `it.code`, a uri, a date tuple) — none calls a display
   label. No `stringResource` sits inside a `buildList` / `ifBlank` / `showDotCalToast` lambda; the
   three hits near those constructs are ordinary composable-scope `title =` arguments.
6. **Dead keys & leftover literals** — zero dangling references (every `R.string`/`R.plurals` in
   Kotlin, XML and the manifest resolves). The only 8 unreferenced keys are exactly the pending
   event/selection plurals from item 7 of the work order. In the three pass-4 files the sole
   remaining literals are `Text("+")` (add-image glyph, `EventScreens.kt:804`) and
   `label = "segBg"` (a Compose `animateColor` label, `TaskScreens.kt:439`) — neither is copy.

Near-duplicate **values** across differently-named keys were reviewed and left as-is on purpose:
they are the ALL-CAPS section headers vs sentence-case row labels (`event_section_reminder`
"REMINDER" vs `event_reminder` "Reminder"), or per-screen keys that share an English word but may
diverge in other languages (`event_repeat` / `task_repeat`, `tab_month_short` /
`recurrence_freq_month`). Do **not** merge these — collapsing them removes a translator's ability to
case or inflect them independently.

### Trap: formatters cached in top-level vals

`UiModels.kt` used to hold `val editorDateFormatter = DateTimeFormatter.ofPattern(..., Locale.US)` as
plain top-level properties. Swapping the locale to `Locale.getDefault()` in place would have bound
whichever locale was active **at class-load** and then rendered stale month/day names after an
in-session language switch. They are now `get()` accessors over a
`ConcurrentHashMap<Pair<String, Locale>, DateTimeFormatter>` cache (`localizedFormatter(pattern)`),
so they follow the live locale without re-allocating per frame. Any new formatter must go through
`localizedFormatter`, not a top-level `val`.

### Deliberately NOT translated

Verified case by case; changing any of these breaks parsing, storage, or brand:

- **Storage/protocol:** enum `name`/`id`/`storageKey`/`tag`/`storageValue`, RRULE codes
  (`MO`/`TU`/`FREQ=DAILY`), `AccentColor` storage hex.
- **Parsing-critical `Locale.US`** (left alone on purpose): `QuickAddParser` (all token matching),
  `RecurrenceRule` parse keys, `DotCalRepository.kt:1789` account matching,
  `CardImageExporter.kt:116` and `DotCalApp.kt:3085` filename slugs.
- **`"AM"`/`"PM"`** — `UiHelpers.kt:163` `toHour24` compares against the literal `"PM"` emitted by the
  picker in `SettingsScreens.kt:2297`. These are an internal token pair, not display text. If the
  visible picker is ever localized, that comparison must be refactored **first**.
- **`readableCalendarLabel`** (`UiHelpers.kt:247`) — kept on `Locale.US` because Turkish casing turns
  `GMAIL` into `gmaıl` (dotless ı). Account names are proper nouns from CalendarProvider.
- **`Custom.label`** — a hex value; uppercased with `Locale.US` for stability.
- **Brand/contact:** DotCal, DotCal Pro, Ndot, NType 82, `Pro`/`PRO` badges,
  `DotFiles — File Manager`, `Google`, `dotfieldstudio@gmail.com`, `BuildConfig.VERSION_NAME`.
- **Quick Add parser hint AND its example prompts** — the parser only understands English tokens, so
  a translated hint (or a translated "Try one" example that the parser then fails to read) would
  promise behavior that does not work. The examples in `ProFeatureScreens.kt` stay English for the
  same reason the hint does.
- **Onboarding** (`OnboardingScreens.kt`, 5 strings + 10 call sites) — **permanently English by
  product decision.** This is not a deferred pass; the file must not be touched. It was explicitly
  dropped from pass 5 rather than left as a leftover.
- **Privacy Policy** — the entire page body stays English, as does `AppLanguage.native` (each
  language's endonym is by definition not translated).
- **Widget/Glyph text** (`DotCalWidgets.kt` 14, `WidgetConfigActivity.kt` 3) — runs outside an
  activity context, own pass.
- **`contentDescription`** (49) — accessibility, separate pass.

### Other traps

- Widget and Glyph text runs outside an activity context and does not pick up the per-app locale the
  same way — verify separately, treat as its own pass.
- `contentDescription` literals (49) are accessibility text; separate pass from visible copy.

### Verification

Required: `.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug`.
Watch for missing-translation lint. Confirm every extracted string still renders in English under
`System default` before checking other locales. Last run on this work (after pass 4): **BUILD
SUCCESSFUL, 59 tests, 0 failures**; APK installed on `4ab0d020`.

## Play Console Compliance

Both Play warnings ("target API 36 or higher", "Billing 8.0.0 or later") are **already fixed in
code** by `b2d57c1 build(android): target API 36 and Billing 8` — `targetSdk = 36`,
`compileSdk = 36`, `billing = "8.0.0"`. Local `versionCode` is 15.

The warnings persist because the **compliant bundle has not been published**. No code change is
needed — build and upload versionCode 15. Play checks *all* active tracks, so any old bundle sitting
on internal/closed/open testing must be deactivated too. Deadline: **Aug 31, 2026**.

## Manual QA

Optional camera / microphone device reach (**pending — not yet run**):

- Camera device: Calendar top bar. Expected: QR scan icon visible immediately left of `+`, and it
  opens the scanner.
- Event Detail > More > Share as QR, then scan it. Expected: QR renders and scanning opens the ICS
  import preview.
- Mic device: event editor > Voice note. Expected: recording works.
- Save an event with a voice note, reopen it. Expected: playback works.
- **Camera-less device** (or a build with the feature masked): Expected: the QR scan icon is hidden,
  and no path can reach the scanner.
- **Mic-less device:** Expected: the voice-note recorder is hidden, but an event that already has a
  voice note still plays it back.

Language picker:

- Settings > Appearance. Expected: a `Language` panel sits directly under `Font`, subtitle reads `System default` on a fresh install.
- Tap the Language row. Expected: bottom sheet opens listing System default + 9 languages, each showing its native name with the English name beneath; current selection has an accent border and check icon.
- Select `हिन्दी`. Expected: sheet closes, UI refreshes (API 33+ recreates via the framework; API 30-32 recreates manually), Settings > Appearance > Language subtitle now reads `हिन्दी`. Almost no other text changes — that is the known gap, not a bug.
- Force-close DotCal and reopen. Expected: selection persists, subtitle still reads the chosen language.
- Select `العربية`. Expected: sheet and Settings row render RTL, layout does not clip or overlap; the picker's own subtitle text is Arabic.
- Return to `System default`. Expected: per-app locale clears; on API 33+ Android Settings > Apps > DotCal > Language shows `System default`.
- API 33+ only: change the language from Android Settings > Apps > DotCal > Language, then reopen DotCal. Expected: the in-app picker shows the same language (startup reconcile syncs them).
- API 33+ only: set the OS per-app locale to a language DotCal does not ship (e.g. Italian) via adb, then launch. Expected: reconcile clears it back to System default rather than leaving the app on an unlisted language.

UI string extraction (passes 1, 2, 3a-i, 3a-ii, 4):

**Settings, the event editor, tasks and the shared dialogs are all translated now.** Everything
outside those (Pro/paywall, onboarding, QR, availability, widgets) is still English — that is the
expected mid-job state, not a bug.

Spanish judgement pass — set Language to `Español` first, then walk these:

**Event editor and details (new in pass 4):**

- Calendar > `+` > new event. Expected: title `Añadir evento`, placeholders `Título del evento` /
  `Ubicación` / `Descripción`, rows `Comienza` / `Termina` / `Recordatorio` / `Repetir`, toggles
  `Todo el día` and `Marcar como tentativo` with the subtitle
  `Tentativo; las herramientas de tiempo libre pueden ignorarlo`. **Look for truncation on that
  subtitle** — it is one of the longest strings in the app.
- Save with an empty title. Expected: `Título obligatorio` under the field.
- Set the end before the start. Expected: `El fin debe ser posterior al inicio` (timed) or
  `La fecha de fin debe ser igual o posterior a la de inicio` (all-day).
- Open an existing event. Expected: header `Detalles del evento`, section labels `HORA`,
  `UBICACIÓN`, `RECORDATORIO`, `CALENDARIO`, `DESCRIPCIÓN`, `IMÁGENES`, `NOTA DE VOZ`, and
  `Eliminar evento` at the bottom.
- Event detail > `⋮`. Expected: sheet titled `Opciones del evento` with `Editar`, `Compartir`,
  `Compartir como QR`, `Fijar como cuenta atrás`, `Duplicar`, `Copiar a una fecha`,
  `Mover a la bóveda privada`.
- Pin a countdown, reopen the event. Expected: section `CUENTA ATRÁS`, caption
  `DÍAS HASTA <TÍTULO>` (singular `DÍA HASTA …` at 1 day), and `Compartir como imagen`.
- Create an event overlapping an existing one. Expected: `Se superpone con <título> <hora>` — check
  the **word order reads naturally**, since Spanish controls the whole sentence here. With a
  penciled-in event: `Choca tentativamente con …`. With 4+ conflicts: `+1 más`.
- Editor > calendar pill. Expected: label `Calendario`, dialog `Elegir calendario`, account
  subtitles `En este dispositivo` / `Calendario de Google`.
- Pro: editor > Images and Voice note. Expected: `Imágenes`, `Nota de voz`, `TOCA PARA GRABAR`;
  while recording `PARAR`; after recording `REPRODUCIR` / `PAUSA` and `N SEG`. Deny the mic
  permission once. Expected: `PERMISO DE MICRÓFONO DENEGADO - TOCA PARA ACTIVAR` — **this is an
  all-caps single-line row, the most likely place to clip.**

**Repeat / recurrence (new in pass 4):**

- Editor > Repetir. Expected: `Ninguno` / `Diario` / `Semanal` / `Mensual` / `Anual` /
  `Personalizado...`.
- Pro: Repetir > `Personalizado...`. Expected: sheet `Repetición personalizada`; section labels
  `FRECUENCIA`, `CADA`, `EN LOS DÍAS`, `TERMINA`; segmented control `Día` / `Semana` / `Mes` /
  `Año`; the interval reads `2 semanas` and `1 semana` (singular/plural both correct).
- Custom repeat, frequency `Mes`. Expected: `EL`, then `Día N del mes` and `El 2.º martes`-style
  rows. Confirm the ordinal + weekday reads naturally in Spanish.
- Custom repeat > TERMINA. Expected: `Nunca`, `En una fecha` (after picking: `El <fecha>`), and
  `Después de` + `un número de veces`; with a count set, `1 vez` / `5 veces`. Buttons
  `Cancelar` / `Listo`; the date sheet title is `Termina el`.
- Edit one occurrence of a recurring event. Expected: row `Aplicar a`, hint
  `Los cambios se aplican solo a este evento` / `Los cambios se aplican a toda la serie`, and the
  delete link switching between `Eliminar evento` and `Eliminar serie`.

**Tasks (new in pass 4):**

- Tasks tab. Expected: title `Tareas`, filter chips `Todas` / `Hoy` / `Próximas` / `Completadas`.
- Empty each filter. Expected: `Aún no hay tareas` + `Toca para crear tu primera tarea`;
  `Nada vence hoy` + `Disfruta de tu tiempo libre`; `Todo despejado` +
  `No hay tareas próximas programadas`; `No hay tareas completadas` +
  `Las tareas completadas aparecen aquí`.
- New task. Expected: `Añadir tarea`, field `Título`, rows `Fecha` / `Hora` / `Recordatorio` /
  `Repetir` with value `Ninguno`, button `GUARDAR TAREA`. Set a date, then check
  `Borrar fecha` / `Borrar hora` appear.
- Task detail. Expected: `Detalles de la tarea`, sections `ESTADO` (`Abierta` / `Completada`) and
  `VENCE`, links `Marcar como completada` and `Eliminar tarea`. `⋮` opens `Opciones de tarea` with
  `Editar` / `Añadir al calendario`. **`Marcar como completada` is long — check it does not clip.**
- A task with no due date in the list. Expected: the group header reads `SIN FECHA`.

**Dialogs (new in pass 4):**

- Delete an event. Expected: `¿Eliminar evento?` / `Esto no se puede deshacer.` /
  `Eliminar` / `Cancelar`. On a series: `¿Eliminar serie?` / `Eliminar serie`.
- Drag an event onto a busy slot. Expected: `Conflicto de horario`,
  `Este horario se superpone con otro evento.` (singular) or `… con N eventos más.` (plural), and
  `Mover de todas formas`.
- Event or task editor > `⋮` > `Guardar como plantilla`. Expected: dialog
  `Guardar como plantilla`, placeholder `Nombre de la plantilla`, buttons `Guardar` / `Cancelar`,
  then the toast `Plantilla guardada`.
- Save an event with a reminder while notification permission is denied. Expected toast:
  `Evento guardado sin recordatorio` (tasks: `Tarea guardada sin recordatorio`).

**Settings (from pass 3a-ii, still worth re-checking):**

- Settings root. Expected: title `Ajustes`; panels `Cuentas` / `Ajustes` / `Herramientas` /
  `Acerca de`; rows `Cuentas de calendario`, `Preferencias del calendario`,
  `Recordatorios predeterminados`, `Apariencia`, `Widgets`, `Bloqueo de app y bóveda privada`,
  `Sincronización`, `Datos y restauración`. **Look for truncation** — the App Lock row is the
  longest string in the app and the most likely to clip or ellipsize.
- Settings > Preferencias del calendario. Expected: `Inicio de la semana`, `Vista predeterminada`,
  `Números de semana`, `Calendario de cumpleaños`, `Festivos internacionales`. Open each sheet and
  confirm the options are Spanish and a new pick persists after reopening.
- Settings > Recordatorios predeterminados. Expected: `Ninguno` / `1 hora antes` / `1 día antes` /
  `%d minutos antes` in the reminder sheet, and `1 hora` / `2 horas` / `%d min` for duration.
- Settings > Apariencia. Expected: subtitle `Elige la apariencia de la app`; panels `Fuente`,
  `Idioma`, `Tema`, `Color de acento`; theme rows read `Claro` / `Oscuro` / `Sistema` with
  `Activo` / `Toca para aplicar` beneath; accent swatch names are Spanish (`Rojo`, `Azul`…).
- Settings > Apariencia > Color de acento > custom. Expected: dialog title `Acento personalizado`,
  slider labels `TONO` / `SATURACIÓN` / `BRILLO`, and **`HEX` stays `HEX`** (deliberate).
  Buttons read `Aplicar` / `Cancelar`.
- Settings > Sincronización. Expected: `Sincronización activada`, `Intervalo de sincronización`,
  `Sincronizar ahora`, and the subtitle `Última sincronización: hace N min` (Spanish puts "hace"
  first — confirm it reads naturally, not `N min hace`).
- Settings > Cuentas de calendario. Expected: `Solo local` / `Conectado` / `N/N seleccionadas` on the
  root row. Tap through: `Añadir cuenta` button, `Añadir una cuenta` screen.
- Settings > Bloqueo de app y bóveda privada (Pro). Expected: `Solicitar PIN` with
  `Define un PIN de 4 a 8 dígitos`, `Definir PIN` / `Cambiar PIN` / `Eliminar PIN`. Open the PIN
  dialog: field label `PIN`, buttons `Guardar` / `Cancelar`; enter a wrong PIN and confirm
  `Introduce el PIN correcto de 4 a 8 dígitos`.
- Force-close, reopen with App Lock on. Expected: lock screen reads `DotCal bloqueado` /
  `Introduce tu PIN para continuar` / `Desbloquear`; a wrong PIN shows `PIN incorrecto`.
- Settings > Datos y restauración. Expected: `Exportar calendario`, `Importar calendario`,
  `Crear copia de seguridad`, `Restaurar datos`, `Eliminados recientemente`, with Spanish subtitles.
- Settings > Festivos internacionales. Expected: section headers `SELECCIONADOS` / `DISPONIBLES`;
  root row reads `Ninguno seleccionado` or `N países seleccionados` (singular `1 país seleccionado`).
- Settings > Herramientas. Expected: `Tiempo muerto` / `Huecos libres` and
  `Calculadora de fechas` / `Cálculo de fechas`. Free users: badge reads `Función Pro`.
- Settings > Política de privacidad. Expected: **the row label and screen title are Spanish, the
  entire policy body stays English** — including the `01 Overview`-style headings. This is
  deliberate (legal text). A fully-Spanish policy page would be the bug here.
- Settings > Acerca de. Expected: `Buscar actualizaciones`, `Política de privacidad`,
  `Valorar DotCal`, `Más apps nuestras`, `Enviar comentarios`, `Versión`. The value beside
  "Más apps nuestras" stays `DotFiles — File Manager` (brand), and `DotCal Pro` stays English.

English no-regression checks (set Language back to `System default` on an English phone):

- Settings > Appearance. Expected: Theme row still `Light`/`Dark`/`System`, Font row still
  `Ndot / Monospaced. Technical. Precise.`, accent swatch still `Red`.
- Settings > Calendar Preferences > Start of the week. Pick `Monday`, reopen. Expected: selection
  stuck and the Month grid starts Monday — confirms `WeekStartOption.storageKey` still persists
  after the enum reshape.
- Default view / Default event duration / Default reminder / Sync interval. All four go through
  `SettingsOptionSheet`; a regression shows as an empty or crashing sheet.
- **Event editor > Reminder, and Task editor > Reminder.** These go through `ChoiceSheetContent`,
  whose `label` became `@Composable` and whose `LazyColumn` **`key` was removed** in 3a-ii. Open
  both sheets, scroll, pick a value, reopen. Expected: full option list, correct selection, no
  crash. This is the highest-risk regression in this pass.
- Event editor > date/time rows and the "Apply to" sheet on a recurring event. Same
  `ChoiceSheetContent` path.
- Calendar > overflow menu. Expected: all 8 items and subtitles unchanged with `Pro` badges intact.

Pass-4-specific English regression checks — these cover the code that actually changed shape:

- **Event detail > `⋮`, and Task detail > `⋮`.** Both action sheets moved their labels out of
  `buildList` into hoisted `val`s. Expected: every row present, correct order, correct
  `Pin as Countdown` vs `Remove Countdown` and `Move to`/`Restore From Private Vault` wording for
  the current state. A regression here shows as a missing row or a wrong label.
- **Tasks tab filter chips.** `TaskFilter` became `@StringRes`. Expected: `All` / `Today` /
  `Upcoming` / `Completed`, tapping each still filters, and the selected chip still highlights.
- **Event editor > Repeat.** `RecurrenceOption` became `@StringRes` and `repeatRowLabel` became
  `@Composable`. Expected: `None` / `Daily` / `Weekly` / `Monthly` / `Yearly` / `Custom...`, the
  current value shows on the Repeat row, and an existing custom rule still renders its
  `humanLabel()` sentence (still English by design).
- **Pro: custom repeat builder end-to-end.** Set `Every 2 weeks on Mon, Fri`, then `After 5 times`,
  save, reopen. Expected: the same rule comes back — confirms `RecurrenceOption.rrule` still
  persists the RRULE code and nothing got localized into storage.
- **Save an event/task as a template with a blank title.** Expected: the template is named
  `Template` (the `ifBlank` fallback now reads a hoisted `val`, not a literal).
- **Voice note playback on event *detail*** (not the editor). Expected: `PLAY`/`PAUSE` toggle and
  `N SEC` duration; `UNAVAILABLE` if the file is missing.

Deliberately-not-translated checks:

- Pro: Accent Color > custom hex. Expected: uppercase hex (e.g. `#3A86FF`) in every locale.
- Language `Türkçe` > Settings > Calendar Accounts. Expected: a Google account reads `Gmail`, not
  `Gmaıl` — confirms `readableCalendarLabel` stayed on `Locale.US`.
- Quick Add (Pro) with Language `Español`: `lunch tomorrow 1pm` still parses (parser is English-only).
- Widgets with Language `Español`: still English (own pass).
- Month/weekday names still localize through `java.time` — Month view shows
  `lun mar mié jue vie sáb dom`, Year shows `ene feb mar`, and switching to `Deutsch` gives
  `Mo Di Mi Do Fr Sa So` without a restart (formatter-cache check).

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

C4 Dead Time Finder:

- Pro: Settings > Tools > Dead Time. Expected: Time Insights opens with Dead Time Finder section.
- Move free-time bounds. Expected: slots refresh after slider release and all slots stay within selected hours.
- Tap a dead-time slot row. Expected: event editor opens prefilled with that slot date/start/end.
- Tap Share availability on a slot. Expected: Availability opens for that exact day only.
- Add/delete an event while Dead Time is open, then return. Expected: dead-time slots refresh to reflect the changed calendar.
- Free user: Settings > Tools > Dead Time. Expected: Paywall opens.

C6 Ghost Events / Pencil-In:

- Pro: open any event editor, enable `Pencil in`, save. Expected: event persists and renders lighter with a dotted/hollow treatment in Month, Week, Day, and Agenda.
- Reopen the same event editor. Expected: `Pencil in` is still enabled.
- Disable `Pencil in`, save, and reopen. Expected: event returns to normal opacity/solid treatment and toggle stays off.
- Free user: tap `Pencil in` in the editor. Expected: paywall opens and the flag does not change.
- Create a real event overlapping a ghost event. Expected: conflict message says `Tentatively clashes with...` and uses softer styling.
- Availability: toggle ghost policy between busy/free. Expected: when ghosts are free, penciled events stop blocking generated slots; when busy, they block.

## Worktree Notes

**Branch state as of 2026-08-10.** All work happens on `main`.

- `main` is at `6793737` and in sync with `origin/main`.
- Every other local branch is **fully merged** into `main` — each has ahead-count 0, so no branch
  holds work that `main` does not already have:

  | Branch | Tip | Behind `main` |
  |---|---|---|
  | `live` | `95d3a7f` | 8 |
  | `opentesting` | `95d3a7f` | 8 |
  | `feature/event-color-picker-and-billing-offers` | `95d3a7f` | 8 |
  | `feature/google-calendar-outbound-sync` | `b9e8e33` | 7 |
  | `pro-features` | `7b790ff` | 6 |

- Remotes: `origin/main`, `origin/live`, `origin/open-testing`, `origin/open-testing-22-07-2026`,
  `origin/pro-features`.
- Local `opentesting` maps to remote `origin/open-testing`; local `opentesting`/`live` have no
  upstream tracking set. `origin/open-testing-22-07-2026` is a dated snapshot — leave alone.
- The Language picker and UI string extraction work described above is **committed and merged into
  `main`** (it was uncommitted on `pro-features` when the earlier notes were written).
- An earlier AppCompat-based Language picker attempt was written and **fully reverted**. The shipped picker does not use AppCompat.
- User-owned untracked files: `Docs/HANDOFF - Copy.md`, `build-b4.log`. Leave untouched.
