# DotCal — New Features Handoff (for Codex)

Created: 2026-07-09
Companion to `Docs/HANDOFF.md`. This file specs the next feature batch: 5 FREE, 5 PRO, 7 UNIQUE.
All specs respect existing Hard Rules. Read `Docs/HANDOFF.md` first.

## Hard Rules (inherited — repeat for every task)

- Package/app id `com.dotfield.dotcal`, DB `dotcal.db`, deep link `dotcal://` — never change.
- Room schema locked: exactly 5 tables (`calendar_accounts`, `calendar_events`, `event_reminders`, `sync_metadata`, `deleted_event_log`). NO new tables, NO new columns.
- Extra storage only via: DataStore `calendar_preferences` keys, or app-private file/JSON stores (follow `RecentlyDeletedStore` / templates-store pattern).
- Offline only. No network, no REST, no cloud. Sync stays CalendarProvider-only.
- No Hilt, no Compose Nav graph. Manual DI + boolean full-screen overlays (`showPaywall` pattern).
- Pro gating: `ProManager` + `KEY_IS_PRO`; non-Pro interaction opens existing Paywall overlay.
- Theme: black/white/red dot-matrix. Destructive = centered red text. Respect `KEY_24_HOUR_FORMAT`, `KEY_WEEK_START`, accent color prefs.
- After code changes: `.\gradlew.bat --no-daemon --console=plain :app:assembleDebug`
- Update `Docs/HANDOFF.md` after each completed step.

---

# BATCH A — FREE (trust/retention; competitors give these away)

## A1. Duplicate Event / Copy to Date

Add a "Duplicate" action to the Event Detail screen. On tap, open the existing
event editor pre-filled with a copy of the event (title, calendar, duration,
reminders from `event_reminders`, `rrule`, colorHex, notes, location); user can
adjust before saving. Save via existing repository insert path (end >= start;
never reuse original event ID or `googleEventId` — duplicates are always new
LOCAL events). Also add "Copy to date…": date picker first, then pre-filled
editor with that date. Hide both actions for `source = BIRTHDAY` events.
No schema change, no network.

## A2. Conflict Warning on Save

In the event editor, add a non-blocking overlap warning. When start/end changes
(debounced ~300ms), query `calendar_events` for visible, non-all-day,
non-completed events overlapping the chosen range (exclude the event being
edited). If overlaps exist, show inline warning under the time pickers:
"Overlaps with {title} {start}–{end}" (max 3, then "+N more"). Hint only —
never block save. Respect `KEY_24_HOUR_FORMAT`. No new tables, no new keys.

## A3. Share Event (text / single .ics)

Add "Share" to Event Detail with two share-sheet options:
(a) Share as text — formatted plain text (title, date/time per
`KEY_24_HOUR_FORMAT`, location, notes).
(b) Share as .ics — reuse existing ICS export code to serialize this single
event (including `rrule`) to a cache temp file, share via `FileProvider`,
mime `text/calendar`. Add FileProvider manifest entry if missing.
Exclude voice notes and image attachments from the payload.

## A4. Jump to Date

Long-press the "Today" button (all views) opens a minimal dot-matrix date
picker overlay (boolean overlay pattern, no nav graph). On pick, current view
(Month/Week/Day/Agenda) scrolls/animates to that date; Today short-press
unchanged. Also add "Go to date" in the view switcher menu. Pure ViewModel
navigation state — no storage.

## A5. Scheduling Defaults + ISO Week Numbers

Three Settings additions, persisted in `calendar_preferences`:
- `KEY_DEFAULT_EVENT_DURATION` (Int minutes, options 15/30/60/90/120,
  default 60) — event editor pre-fills end = start + value.
- `KEY_DEFAULT_VIEW` already exists — surface a picker if not already exposed.
- `KEY_SHOW_WEEK_NUMBERS` (Boolean, default false) — when true, render
  ISO-8601 week numbers (`WeekFields.ISO`) in a slim leading column in Month
  and Week views, small gray dot-matrix digits. Respect `KEY_WEEK_START` for
  row layout but always ISO numbering for the label.

---

# BATCH B — PRO (proven paid draws)

## B1. Time Insights (Statistics)

Pro-gated "Time Insights" screen opened from Settings. Aggregate existing
`calendar_events` in-memory in a ViewModel: range selector (This week / This
month / Custom), total scheduled hours, hours per calendar account (colored
bars using account colors), busiest day, event count, task completion rate
(`isTask = 1`), and a 7-column dot-matrix bar chart of hours per weekday.
Exclude all-day and BIRTHDAY-source events from hour totals. No chart library —
plain Compose boxes/Canvas. Gate via `ProManager`; non-Pro opens Paywall.

## B2. Countdown / D-Day Counters

Pro-gated "Countdowns". From Event Detail: "Pin as countdown". Countdowns
screen lists pinned events as "D-23 · Trip to Goa · Mar 14" dot-matrix cards
sorted by nearest. Storage: app-private JSON file store (event IDs + cached
title/date), same pattern as `RecentlyDeletedStore`. Auto-unpin events >1 day
past; recurring events show next occurrence via existing `rrule` expansion.
Bonus: allow a pinned countdown as source for the existing countdown widget.

## B3. Drag-and-Drop Reschedule + Resize (Week/Day)

Pro-gated. Long-press an event block to lift (haptic + elevation), drag
vertically in 15-min snap increments, drag across day columns in Week view to
change date; top/bottom handles resize duration (min 15 min). On drop, persist
via existing repository update path (synced Google events via CalendarProvider
write path, LOCAL via Room; enforce end >= start). Recurring events: show the
existing "This event / Whole series" choice; if not feasible, restrict drag to
non-recurring in v1. All-day and BIRTHDAY events not draggable. Non-Pro:
long-press shows "Pro" lock hint + Paywall overlay.

## B4. Bulk Edit / Multi-Select

Pro-gated multi-select in Agenda view (and search results if trivial):
long-press enters selection mode with checkboxes and a contextual top bar
(count + actions): Delete (routes through existing Trash flow), Move to date
(date picker; offset each event by same delta, preserve times), Copy to date
(bulk duplicate via insert path, new LOCAL events), Change calendar (LOCAL
only; skip synced with "N skipped" snackbar). Loop existing single-event repo
methods inside one coroutine. Exclude BIRTHDAY events from selection.

## B5. Year-in-Pixels Heatmap

Pro-gated "Heatmap" toggle in Year view top bar. When on, each day dot shaded
by event density: events-per-day for visible year computed in a ViewModel
(count non-all-day, non-BIRTHDAY events; single in-memory pass). Map counts to
4 intensity steps (0 = faint outline dot, 3+ = solid accent dot) + small
legend row. Tap day still navigates to Day view. Persist toggle as
`KEY_YEAR_HEATMAP` Boolean.

---

# BATCH C — UNIQUE (no direct competitor has these; dot-matrix brand moat)

## C1. Life-in-Dots (memento mori view) — PRO

New "Life" view in the view switcher. First open asks birth date (+ optional
life expectancy, default 80), stored as `KEY_BIRTH_DATE` /
`KEY_LIFE_EXPECTANCY` in DataStore. Render a scrollable 52-column dot grid
(one row per year of life): past weeks solid foreground, current week pulsing
accent, future weeks faint outline. Header: "Week 1,691 of 4,160". Tap current
week jumps to Week view. Gate behind `KEY_IS_PRO`.

## C2. Day Density Forecast strip — FREE

Slim "next 7 days" strip at top of Agenda view: 7 dots labeled by weekday
initial, each sized/shaded in 4 steps by that day's total scheduled hours
(from `calendar_events`, excluding all-day and BIRTHDAY). Tap a dot to jump to
that day. Computed in existing ViewModel; no storage.

## C3. On This Day (offline memories) — PRO

Pro-gated card at top of Day view: query `calendar_events` for non-BIRTHDAY
events on same month/day 1, 2, 5 years ago; show up to 3 rows
("1 year ago · Dinner with Sana"), tap opens the event. Dismissible per-day
(session state only). `KEY_ON_THIS_DAY` Boolean toggle in Settings.

## C4. Dead Time Finder — PRO

Pro-gated "Free time" section in Time Insights (or Agenda header): compute
gaps >= 60 min between 8:00–22:00 (bounds via two DataStore keys,
`KEY_FREE_TIME_START_HOUR` / `KEY_FREE_TIME_END_HOUR`) across next 7 days from
`calendar_events`. List as "Thu · 14:00–17:00 · 3h free" rows; tap opens event
editor pre-filled with that slot. Pure in-memory computation.

## C5. Punch-Card Day Complete — FREE

Month view: render a day's dot as "punched" (solid + small ring) when all of
that day's tasks are completed (`isTask = 1`, all `isCompleted`) and day has
ended. Completing the final task of today plays a brief dot-ripple animation
on today's cell. Derived state only — no storage.

## C6. Ghost Events (pencil-in mode) — PRO

"Pencil in" toggle in event editor (Pro-gated). Ghost events stored as normal
LOCAL rows but flagged via a file-based JSON id-list store (same pattern as
`RecentlyDeletedStore`). Render in all views as dotted-outline blocks at 50%
opacity; exclude from conflict warning (A2) and Time Insights totals (B1).
Event Detail shows "Confirm" button that removes id from the ghost store.
Never sync ghost events to CalendarProvider until confirmed.

## C7. Year Wrapped (offline recap) — PRO

Pro-gated "Wrapped" entry in Settings (badge in December). Aggregate current
year's `calendar_events` in-memory into 5 swipeable full-screen dot-matrix
cards: total events, total scheduled hours, busiest month/day, top calendar,
task completion rate. Each card has "Share as image" — render composable to
bitmap, share via `FileProvider`. No new tables, no network.

---

# Cross-Cutting Notes

- Dependencies between specs: C4 and C6 reference B1 (Time Insights) and
  A2 (Conflict Warning). Build order below resolves this.
- All BIRTHDAY-source events excluded from: duplication, drag, bulk edit,
  hour totals, density strips, memories.
- Bulk delete must route through the existing Trash / `RecentlyDeletedStore`
  flow, never hard delete.
- Any new share uses `FileProvider` from cache dir; never external storage.
- New DataStore keys introduced by this handoff:
  `KEY_DEFAULT_EVENT_DURATION`, `KEY_SHOW_WEEK_NUMBERS`, `KEY_YEAR_HEATMAP`,
  `KEY_BIRTH_DATE`, `KEY_LIFE_EXPECTANCY`, `KEY_ON_THIS_DAY`,
  `KEY_FREE_TIME_START_HOUR`, `KEY_FREE_TIME_END_HOUR`.
- New file stores (app-private JSON, `RecentlyDeletedStore` pattern):
  `CountdownPinStore`, `GhostEventStore`.

# Suggested Build Order

1. A2 Conflict Warning (smallest, unlocks C6 exclusion logic)
2. A5 Defaults + Week Numbers
3. A4 Jump to Date
4. A1 Duplicate / Copy to Date
5. A3 Share Event
6. C2 Density Strip (free, render-only)
7. C5 Punch-Card (free, render-only)
8. B5 Year Heatmap (shares aggregation groundwork with B1)
9. B1 Time Insights
10. C4 Dead Time Finder (extends B1 screen)
11. C3 On This Day
12. B2 Countdowns
13. C1 Life-in-Dots
14. C7 Year Wrapped
15. C6 Ghost Events
16. B4 Bulk Edit
17. B3 Drag-and-Drop (largest)

# Per-Task Checklist (repeat for every task)

- [ ] No new Room tables/columns
- [ ] No network calls
- [ ] Pro gating via ProManager + Paywall overlay where specified
- [ ] BIRTHDAY-source exclusions applied
- [ ] `KEY_24_HOUR_FORMAT` / `KEY_WEEK_START` / accent color respected
- [ ] `:app:assembleDebug` passes
- [ ] `Docs/HANDOFF.md` updated
