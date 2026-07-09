# DotCal Refactor Plan

Updated: 2026-07-08

Goal: split `app/src/main/java/com/dotfield/dotcal/ui/DotCalApp.kt` into focused files without changing behavior, UI, storage, dependencies, navigation style, or Room schema.

## Guardrails

- No Room schema change. Keep exactly 5 tables.
- No new dependency, no Hilt, no Compose Navigation.
- Keep manual DI and boolean overlay state.
- Preserve current UI/UX pixel behavior unless a later step explicitly says otherwise.
- Keep all extracted code in package `com.dotfield.dotcal.ui` at first to minimize import and visibility churn.
- Prefer mechanical moves before edits. Change `private` to package-visible/internal only when cross-file access requires it.
- Build after every app-code extraction step:

```powershell
.\gradlew.bat --no-daemon --console=plain :app:assembleDebug
```

- Do not run phone/manual QA unless user asks.
- Leave `Docs/HANDOFF - Copy.md` untouched.

## Current Baseline

- Branch: `pro-features`.
- `DotCalApp.kt`: 12,969 lines.
- Worktree known untracked file: `Docs/HANDOFF - Copy.md`.
- Latest app-code build in handoff: passed and installed after Week/Day fixed bottom boundary polish.
- Refactor work not started yet beyond this plan.

## Extraction Order

### Step 0: Planning And Baseline

Status: DONE.

- Read `Docs/HANDOFF.md` in full.
- Confirmed hard rules and latest feature status.
- Captured symbol clusters from `DotCalApp.kt`.
- Created this plan and linked it from `Docs/HANDOFF.md`.

### Step 1: Calendar Views Split

Status: DONE (2026-07-09).

Move only calendar-view composables and their local helpers into `ui/CalendarViews.kt`:

- `MonthView`, `DayCell`
- `WeekView`, `WeekDayHeader`, `WeekTimeColumn`, `WeekDayColumn`, `WeekEventBlock`
- `DayView`, `DayHeader`, `DayTimeColumn`, `DayTimelineColumn`, `TimelineBottomBoundary`
- `ThreeDayView`, `ThreeDayHourRow`
- `YearView`, `YearMonthCell`, `MiniMonthGridCanvas`
- timeline helpers: `WeekEventLayout`, `layoutTimedEvents`, `monthGrid`, `weekDays`, `weekDayLabels`, offset/height helpers

Rules:

- Preserve Week/Day fixed bottom boundary behavior exactly.
- Preserve Day selected-date header, chevrons, center-tap-to-today, and swipe behavior.
- Preserve Month long-press bulk select behavior and day-tap sheet behavior.
- Build immediately after move.

Result:

- Created `app/src/main/java/com/dotfield/dotcal/ui/CalendarViews.kt`.
- Moved Month, Week, Day, ThreeDay, Year, mini-month canvas, and shared timeline layout helpers out of `DotCalApp.kt`.
- Kept behavior/UI unchanged; only package visibility changed for shared helpers/types needed across files.
- `DotCalApp.kt` reduced from 12,969 lines to 11,905 lines.
- Verified: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed, BUILD SUCCESSFUL in 3m 32s.
- No phone/manual UI QA run.

### Step 2: Shared UI Shell And Theme Split

Status: DONE (2026-07-08).

Move shared app chrome, palette, and small common UI into focused files:

- `ui/DotCalTheme.kt`: `DotCalPalette`, `DotCalThemeMode`, `AccentColor`, `dotCalPalette`, `dotCalBootPalette`, color helpers.
- `ui/AppChrome.kt`: `CalendarTabContainer`, `CalendarActionBar`, `ActionBarMenuItem`, bottom nav, segmented controls, icon helpers, `SystemBarColorSync`.
- `ui/CommonUi.kt`: generic rows/dividers/dialog helpers reused by several screens.

Rules:

- Keep same colors, text sizes, ripple suppression, haptics, and menu behavior.
- Avoid renaming public-looking composables unless needed by compiler.
- Build immediately after move.

Result:

- Created `app/src/main/java/com/dotfield/dotcal/ui/DotCalTheme.kt`.
- Moved `DotCalPalette`, `DotCalThemeMode`, `AccentColor`, `dotCalPalette`, `dotCalBootPalette`, and luminance helper out of `DotCalApp.kt`.
- Created `app/src/main/java/com/dotfield/dotcal/ui/AppChrome.kt`.
- Moved `SystemBarColorSync`, `CalendarTabContainer`, `CalendarActionBar`, action-bar overflow row, bottom nav, segmented calendar view control, bottom nav icons, `CalendarTab`, `ScreenTab`, and `noRippleClickable`.
- Kept behavior/UI unchanged; only package visibility changed where moved code is shared across files.
- `DotCalApp.kt` reduced from 11,905 lines to 11,211 lines.
- Verified theme split: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed, BUILD SUCCESSFUL in 3m 10s.
- Verified final chrome split: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug` passed, BUILD SUCCESSFUL in 2m 46s.
- No phone/manual UI QA run.

### Step 3: Event Detail And Event Editor Split

Status: NEXT.

Move event detail/editor/media/date-time editor code into:

- `ui/EventDetailScreens.kt`
- `ui/EventEditorScreens.kt`
- `ui/DateTimePickerSheets.kt`
- `ui/MediaAttachmentUi.kt`

Rules:

- Preserve Quick Add prefill, template prefill, recurrence edit scope, image/voice Pro gates, calendar selector behavior, and recurring instance edit/delete behavior.
- No repository/ViewModel logic changes.
- Build immediately after move.

### Step 4: Tasks Split

Status: TODO.

Move task UI into `ui/TaskScreens.kt`:

- `TaskDetailScreen`
- `TasksScreen`
- task top chrome/filter rows
- `TaskRow`, metadata, empty state
- `TaskEditorSheet`, `TaskTimeChoiceSheet`

Rules:

- Preserve Task Time Blocking behavior.
- Preserve task recurrence Pro gate.
- Preserve original task on "Add to Calendar".
- Build immediately after move.

### Step 5: Settings And Pro Feature Screens Split

Status: TODO.

Move settings and feature management screens into focused files:

- `ui/SettingsScreens.kt`: settings root/preview, account/settings rows, sync rows, widget toggles.
- `ui/PrivacyScreens.kt`: app lock/private vault screens.
- `ui/ThemeSettingsScreens.kt`: theme/accent UI and color picker.
- `ui/TemplateScreens.kt`: templates and bulk template picker.
- `ui/FocusProfileScreens.kt`: calendar sets.
- `ui/ShiftPatternScreens.kt`: shift patterns.
- `ui/RecentlyDeletedScreens.kt`: recently deleted list.
- `ui/SearchScreens.kt`: global search.
- `ui/QuickAddScreens.kt`: quick add.
- `ui/DateCalculatorScreens.kt`: date calculator.
- `ui/PaywallScreens.kt`: paywall and Pro feature list.

Rules:

- Preserve full-screen right-slide overlays and back-stack close order.
- Preserve Pro gates and paywall routing.
- Preserve Settings row typography and current entries.
- Build after each logical sub-split if one file move becomes too large.

### Step 6: Onboarding Split

Status: TODO.

Move onboarding screen, copy/color models, and draw-scope helpers into `ui/OnboardingScreens.kt`.

Rules:

- No onboarding UX change.
- Keep deep-link onboarding skip behavior untouched in root app state.
- Build immediately after move.

### Step 7: Root App Cleanup

Status: TODO.

Reduce `DotCalApp.kt` to root composition, app-level state, launchers, overlay orchestration, and navigation between major surfaces.

Target:

- `DotCalApp.kt` handles state wiring and delegates UI surfaces.
- Feature files own feature UI.
- Common helpers live outside root file only when shared.

Rules:

- Do not introduce a Compose Nav graph.
- Do not change manual DI.
- Do not alter overlay booleans or close order except compiler-required references.
- Build immediately after cleanup.

### Step 8: Low-Risk Optimization Pass

Status: TODO.

Only after split is compiling:

- Add `remember` around repeated formatters, shapes, and static lists where missing.
- Add stable lazy keys where obvious and behavior-neutral.
- Reduce broad state reads in repeated rows if a local move makes it easy.
- Remove dead imports and truly dead helper code produced by extraction.

Rules:

- No visual redesign.
- No behavior changes.
- Build after changes.

## Validation Checklist Per Step

- `:app:assembleDebug` passes.
- No Room entity/schema/DAO migration changes unless explicitly approved later.
- No dependency or Gradle version changes.
- `DotCalApp.kt` line count decreases.
- `Docs/HANDOFF.md` records completed step and build result.
- `Docs/HANDOFF - Copy.md` remains untouched.
