# DotCal — Complete Feature Upgrade and New Ideas List

Prepared: 10 September 2026  
Language: Hinglish  
Status: Product proposals, not an implementation audit or approved delivery commitment.

## Scope and how to read this document

Yeh conversation ke saare substantive feature ideas ki consolidated list hai: **26 feature proposals + 2 separate feedback items**. Duplicate ideas merge kiye gaye hain. Har entry mein exact addition, example, suggested Free/Pro split, relative effort, first-version scope aur research basis diya hai.

- **Upgrade**: DotCal ke already documented feature ka extension. Existing feature ko dobara new feature nahi bola gaya.
- **New capability candidate**: Available baseline mein confirmed nahi hai; full handoff/code check ke baad final status milega.
- **Distinctive proposal**: Product combination ya workflow idea. Worldwide unique hone ka claim nahi.
- **P1**: First shortlist; sab P1 ek release mein ship karne ka commitment nahi.
- **P2**: Next-stage candidate after dependencies and demand validation.
- **P3**: Prototype or later investment; effort/accuracy/dependencies zyada.
- Effort relative product estimates hain, developer-day estimates nahi. Code inspect nahi hua.
- Free/Pro suggestions sirf additions ke liye hain. Existing purchases, entitlements aur lifetime-Pro promise preserve karna hai.

Attached FEEDBACK.md completed-feature handoff nahi hai. Baseline user-provided DotCal 1.4 release briefing, official listing aur developer community post se liya gaya. Older public snapshots current build ko completely represent nahi kar sakte. Status ko final missing-feature claim na samjhein.

## Confirmed existing baseline — do not recommend these as new

Public listing: calendar views, events/tasks, recurring items, attachments/voice notes, templates, Calendar Sets, shift patterns, search, import/export, backup/restore, widgets, drag/drop, bulk edits, QR sharing and availability text. Developer launch post: Dead Time Finder, Pencil-In Events, On This Day and Glyph support. User's 1.4 briefing: snooze/stronger alerts/lock-screen reminder improvements, Google/system sync improvements, widget configuration/previews, Voice Quick Add, launcher shortcuts, image/shift-plan sharing and Features Guide.

Sources: [DotCal official listing](https://play.google.com/store/apps/details?id=com.dotfield.dotcal&hl=en_US), [developer launch post](https://nothing.community/d/60521-dotcal-a-privacy-first-minimalist-calendar-inspired-by-nothing-os). User briefing and attached feedback are separate first-party inputs.

## Master list

| ID | Feature | Suggested access for addition | Priority | Relative effort |
|---|---|---|---|---|
| 01 | Reminder Center | Basic list/actions Free; batch actions aur saved snooze presets Pro | P1 | Medium |
| 02 | Automatic Calendar Sets | Scheduled switching Pro; existing manual access preserve | P2 | Medium |
| 03 | Saved Smart Views | Basic filters Free; saved combinations Pro | P1 | Medium |
| 04 | Contextual Quick Add | Recent suggestions Free; configurable reusable presets Pro | P2 | Medium |
| 05 | Evening Task Review | Manual review Free; scheduling suggestions Pro via feature 06 | P1 | Medium |
| 06 | Find Time for This | Advanced matching Pro; existing Dead Time Finder access preserve | P1 | High |
| 07 | Repair My Day | Pro | P2 | High |
| 08 | Linked Event Kits | Pro extension; existing templates access preserve | P1 | Medium–High |
| 09 | Pencil-In Plan Comparison | Grouped alternatives/comparison Pro; existing Pencil-In access preserve | P2 | High |
| 10 | Advanced Widget Profiles | Advanced saved profiles Pro; basic readability and accessibility Free | P1 | Medium |
| 11 | Shift-aware Usable Time | Basic availability boundaries Free; shift-linked rules Pro | P2 | High |
| 12 | Advanced Availability Rules | Pro extension; current sharing access preserve | P2 | Medium–High |
| 13 | Timezone Comparison | Basic event timezone clarity Free; multiple saved comparison zones Pro | P2 | Medium |
| 14 | Occupied-Time Heatmap | Basic heatmap Free; custom periods/filters Pro | P2 | Medium |
| 15 | Conflict and Move-Impact Preview | Basic overlap warning Free; impact analysis/alternative suggestions Pro | P1 | Medium–High |
| 16 | Routines That Follow Your Day | Advanced flexible rules Pro; current recurrence access preserve | P3 | High |
| 17 | Offline Common-Time QR | Basic two-person comparison Free; longer ranges/advanced rules Pro | P2 | Medium–High |
| 18 | Screenshot-to-Events Import | Limited trial Free; batch import Pro, recurring compute costs assess karke | P3 | High |
| 19 | Schedule Change Detector | Pro | P3 — signature prototype | High |
| 20 | Leave Optimizer | Pro | P2 | Medium–High |
| 21 | Event Readiness | Basic checklist Free; reusable readiness setups/reminder rules Pro | P1 | Medium |
| 22 | Plan vs Actual | Manual recording Free; personal duration suggestions/history analysis Pro | P3 | Medium–High |
| 23 | Travel and Preparation Blocks | Manual buffer Free; linked reusable rules Pro | P2 | Medium–High |
| 24 | Usable-Time Insights | Basic weekly overview Free; trends and advanced filters Pro | P3 | Medium |
| 25 | Daily Briefing | Free | P1 | Low–Medium |
| 26 | Reminder Readiness Check | Free | P1 | Medium |

## Full explanations

### 01. Reminder Center

**Type:** Upgrade: existing reminders aur snooze.  
**What to add:** Saare pending aur snoozed reminders ek screen par, next alert time ke saath. Individual cancel, dismiss aur resnooze actions.

**Example:** Maine aaj kaunse four reminders postpone kiye? List kholkar unka next alert dekh saku.

**Free / Pro:** Basic list/actions Free; batch actions aur saved snooze presets Pro.  
**Priority / effort:** P1 / Medium.

**First version and boundaries:** Ek pending-reminders screen; app ke apne reminder records se shuru. Delivered notification aur completed task ko same status na samjhein.

**Research basis:** BusyCal snooze management related benchmark hai. [Official/reference source](https://www.busymac.com/)

### 02. Automatic Calendar Sets

**Type:** Upgrade: existing Calendar Sets.  
**What to add:** Work, Personal ya Family calendar sets ko user-defined schedule se switch karna; linked widget ko selected set follow karne ka option.

**Example:** Monday–Friday 9 AM par Work set, 6 PM par Personal set.

**Free / Pro:** Scheduled switching Pro; existing manual access preserve.  
**Priority / effort:** P2 / Medium.

**First version and boundaries:** Time-based switching pehle. Location-based switching later, demand aur platform feasibility check ke baad.

**Research basis:** Fantastical automatic set switching by time/location offer karta hai. [Official/reference source](https://flexibits.com/pricing)

### 03. Saved Smart Views

**Type:** Upgrade: existing calendar search.  
**What to add:** Date range, calendar, event/task type aur supported fields ke filters save karke ek tap se reopen karna.

**Example:** Next 30 days ke sirf client meetings ya incomplete work tasks.

**Free / Pro:** Basic filters Free; saved combinations Pro.  
**Priority / effort:** P1 / Medium.

**First version and boundaries:** Supported existing fields se filters; naya tagging system assume na karein. Saved search aur calendar set ke roles clear rakhein.

**Research basis:** BusyCal Smart Filters related benchmark hai. [Official/reference source](https://www.busymac.com/)

### 04. Contextual Quick Add

**Type:** Upgrade: existing Quick Add, voice input aur templates.  
**What to add:** User type kare toh previous matching events se location, duration aur reminder settings ka reusable preview suggest ho.

**Example:** Dentist type karne par last appointment ka address aur 45-minute duration suggest ho.

**Free / Pro:** Recent suggestions Free; configurable reusable presets Pro.  
**Priority / effort:** P2 / Medium.

**First version and boundaries:** Local event history se suggestions; user select kare tab fields fill hon. Existing template capabilities ka duplicate check karein.

**Research basis:** BusyCal past-event suggestions use karta hai. [Official/reference source](https://www.busymac.com/)

### 05. Evening Task Review

**Type:** Upgrade: existing task planner.  
**What to add:** Unfinished tasks ko short review flow mein Tomorrow, Pick slot, Backlog ya Done karna; repeated postponements visible hon.

**Example:** Aaj ke three pending tasks ko ek-ek karke decide karo.

**Free / Pro:** Manual review Free; scheduling suggestions Pro via feature 06.  
**Priority / effort:** P1 / Medium.

**First version and boundaries:** Manual review first. Recurring task ki current occurrence aur full series ko alag handle karein.

**Research basis:** Structured Replan related benchmark hai; reviewed guide Android par availability nahi batati. [Official/reference source](https://help.structured.app/en/articles/4511874)

### 06. Find Time for This

**Type:** Upgrade: Dead Time Finder + tasks.  
**What to add:** Task duration, deadline, preferred hours aur calendar conflicts ke basis par suitable slots suggest karna; har suggestion ka reason dikhana.

**Example:** 45-minute proposal Friday se pehle finish karna hai; DotCal Tuesday 4 PM ya Thursday 2 PM offer kare.

**Free / Pro:** Advanced matching Pro; existing Dead Time Finder access preserve.  
**Priority / effort:** P1 / High.

**First version and boundaries:** Single task, fixed duration, two or three suggestions, user-confirmed scheduling. First version ko local rules se scope karein.

**Research basis:** Morgen assisted time blocking related benchmark hai. [Official/reference source](https://www.morgen.so/frames)

### 07. Repair My Day

**Type:** Upgrade: rescheduling + feature 06.  
**What to add:** Day change hone par user-designated flexible tasks ko new slots mein move karne ka proposal; preview aur Undo.

**Example:** Meeting 30 minutes overrun hui; two flexible tasks ko later gaps mein adjust karne ka plan.

**Free / Pro:** Pro.  
**Priority / effort:** P2 / High.

**First version and boundaries:** Sirf flexible personal tasks. Fixed appointments, shared meetings aur locked items auto-move na hon. Feature 06 ke baad build karein.

**Research basis:** Reclaim flexible scheduling related benchmark hai. [Official/reference source](https://reclaim.ai/)

### 08. Linked Event Kits

**Type:** Upgrade: existing calendar templates.  
**What to add:** Ek anchor event ke saath relative preparation/follow-up items create karna; anchor move hone par linked changes offer karna.

**Example:** Client meeting + one day pehle preparation + next day follow-up.

**Free / Pro:** Pro extension; existing templates access preserve.  
**Priority / effort:** P1 / Medium–High.

**First version and boundaries:** One anchor aur relative offsets. Preview affected items and change only selected supported records.

**Research basis:** Business Calendar event templates benchmark hain; linked dependency behavior hamara proposed extension hai. [Official/reference source](https://play.google.com/store/apps/details?id=com.appgenix.bizcal.pro&hl=en_US)

### 09. Pencil-In Plan Comparison

**Type:** Upgrade: existing Pencil-In Events.  
**What to add:** Alternative dates ko ek plan ke under group karna, conflicts compare karna, decision deadline aur tentative busy/free treatment set karna.

**Example:** Trip is weekend rakhu ya next weekend? Conflicts compare karke ek option confirm karo.

**Free / Pro:** Grouped alternatives/comparison Pro; existing Pencil-In access preserve.  
**Priority / effort:** P2 / High.

**First version and boundaries:** Two alternatives, manual comparison, reminder to decide, selected alternative confirm. Real calendar par changes commit se pehle preview.

**Research basis:** Fantastical meeting proposals adjacent benchmark hain; personal alternatives comparison proposed direction hai. [Official/reference source](https://flexibits.com/pricing)

### 10. Advanced Widget Profiles

**Type:** Upgrade: existing configurable widgets.  
**What to add:** Per-widget calendar filters, layout, density, supported actions aur saved appearance profiles.

**Example:** Ek compact Work agenda widget aur ek Personal month widget; dono ke independent filters.

**Free / Pro:** Advanced saved profiles Pro; basic readability and accessibility Free.  
**Priority / effort:** P1 / Medium.

**First version and boundaries:** Current widget options audit karke sirf missing profile/filter actions add karein. Feature 02 ke saath automatic switching later.

**Research basis:** DigiCal+ per-widget calendar selection aur customization benchmark hai. [Official/reference source](https://digibites.nl/digical/plus)

### 11. Shift-aware Usable Time

**Type:** Upgrade: shift patterns + Dead Time Finder + availability.  
**What to add:** User-defined rest, sleep, commute aur preparation windows ko slot suggestions aur shared availability mein respect karna.

**Example:** Night shift 7 AM khatam; selected rest window ke andar appointment suggest na ho.

**Free / Pro:** Basic availability boundaries Free; shift-linked rules Pro.  
**Priority / effort:** P2 / High.

**First version and boundaries:** Manual boundaries first; shift-relative offsets second. User override aur excluded slot ka reason visible rahe.

**Research basis:** Reclaim buffers adjacent benchmark hain; DotCal shift integration proposed extension hai. [Official/reference source](https://reclaim.ai/)

### 12. Advanced Availability Rules

**Type:** Upgrade: Availability Text Generator.  
**What to add:** Meeting duration, before/after buffer, minimum notice, selected working hours, daily limit aur recipient timezone ke saath available slots generate karna.

**Example:** Client ko next week ke 30-minute slots bhejo, har meeting ke beech 15-minute gap ke saath.

**Free / Pro:** Pro extension; current sharing access preserve.  
**Priority / effort:** P2 / Medium–High.

**First version and boundaries:** Existing text output pe rules add karein. Live booking website ko first version mein include na karein; output snapshot hai.

**Research basis:** Fantastical Openings aur multiple duration options related benchmark hain. [Official/reference source](https://flexibits.com/blog/2026/07/new-feature-roundup-you-asked-we-built/)

### 13. Timezone Comparison

**Type:** Upgrade: existing Day/Week views.  
**What to add:** Calendar ke saath additional timezone columns aur readable dual-time event display; shared text mein timezone explicit.

**Example:** India mein 7:30 PM meeting New York mein kitne baje hai, same view mein dekho.

**Free / Pro:** Basic event timezone clarity Free; multiple saved comparison zones Pro.  
**Priority / effort:** P2 / Medium.

**First version and boundaries:** Two zones first, daylight-saving-aware calculations. Floating-time and all-day event behavior define karein.

**Research basis:** Fantastical additional Day/Week timezone columns offer karta hai. [Official/reference source](https://flexibits.com/blog/2026/07/new-feature-roundup-you-asked-we-built/)

### 14. Occupied-Time Heatmap

**Type:** Upgrade: existing Year/Month views.  
**What to add:** Busy days ko event count ke bajay selected calendar ke occupied duration se show karna.

**Example:** Five short calls aur five long workshops alag intensity se appear hon.

**Free / Pro:** Basic heatmap Free; custom periods/filters Pro.  
**Priority / effort:** P2 / Medium.

**First version and boundaries:** Overlapping intervals ko double-count na karein; all-day events ka treatment configurable ho. Ye capacity display hai, health score nahi.

**Research basis:** DigiCal+ year heatmap related benchmark hai; duration-based method proposed implementation hai. [Official/reference source](https://digibites.nl/digical/plus)

### 15. Conflict and Move-Impact Preview

**Type:** Upgrade: event creation + drag/drop rescheduling.  
**What to add:** Save karte waqt overlap warning; drag karte waqt affected events, buffers aur remaining gap ka preview.

**Example:** Event 4 PM par move karne se lunch overlap hoga aur next appointment se pehle sirf 10 minutes bachenge.

**Free / Pro:** Basic overlap warning Free; impact analysis/alternative suggestions Pro.  
**Priority / effort:** P1 / Medium–High.

**First version and boundaries:** Overlap detection first. Linked buffers and alternative slot proposals features 06, 08, 23 ke baad.

**Research basis:** Proposed DotCal extension; preview behavior ko competitor parity claim nahi kiya gaya.

### 16. Routines That Follow Your Day

**Type:** Upgrade: recurrence + shift planning.  
**What to add:** Routine ko fixed clock time ki jagah schedule-relative rule se plan karna.

**Example:** Gym three times/week, shift finish hone ke 90 minutes baad, lekin 10 PM ke baad nahi.

**Free / Pro:** Advanced flexible rules Pro; current recurrence access preserve.  
**Priority / effort:** P3 / High.

**First version and boundaries:** One supported anchor type and weekly target first. No suitable slot mile toh explain karein; routine silently drop na ho.

**Research basis:** Reclaim flexible Habits related benchmark hain; shift-relative local behavior proposed angle hai. [Official/reference source](https://reclaim.ai/features/habits)

### 17. Offline Common-Time QR

**Type:** Upgrade: existing QR event sharing + availability.  
**What to add:** Selected date range ke free/busy slots ka QR share karna; receiver ke device par common slots calculate hon, titles/notes share kiye bina.

**Example:** Friend scan kare aur dono ke Wednesday 6–7 PM free hone ka result mile.

**Free / Pro:** Basic two-person comparison Free; longer ranges/advanced rules Pro.  
**Priority / effort:** P2 / Medium–High.

**First version and boundaries:** Two DotCal users, short date range, explicit timezone and generated-at time. Snapshot ko live reservation na present karein. Date granularity/QR payload limits validate karein.

**Research basis:** Privacy-focused availability sharing ka related product BusyBoard hai; local QR intersection hamara proposed workflow hai. [Official/reference source](https://busyboard.app/)

### 18. Screenshot-to-Events Import

**Type:** New capability candidate; image attachment se different.  
**What to add:** Timetable, invitation ya shift roster image se structured event drafts extract karna, review karna aur selected entries import karna.

**Example:** Exam timetable ki image se six exams ke editable drafts milen.

**Free / Pro:** Limited trial Free; batch import Pro, recurring compute costs assess karke.  
**Priority / effort:** P3 / High.

**First version and boundaries:** One image, editable extraction, date/year/timezone confirmation, duplicates preview. Processing location aur supported formats clear hon.

**Research basis:** Original recommendation; competitor absence claim nahi hai.

### 19. Schedule Change Detector

**Type:** New capability candidate; feature 18 ka advanced extension.  
**What to add:** Updated timetable/roster ko original imported schedule se compare karke added, moved aur removed entries identify karna.

**Example:** Tuesday shift 9 AM se 11 AM; Friday shift remove; Saturday new shift. Sirf approved changes apply hon.

**Free / Pro:** Pro.  
**Priority / effort:** P3 — signature prototype / High.

**First version and boundaries:** Feature 18 plus source-linked import identity required. One selected roster only; ambiguous matches highlight. Image mein missing row ko automatically cancelled na samjhein.

**Research basis:** Original combined workflow proposal; globally unique hone ka claim nahi.

### 20. Leave Optimizer

**Type:** New planning capability using existing shifts/holidays.  
**What to add:** User ke leave allowance, workdays, shifts, selected holidays aur commitments se continuous-off options calculate karna.

**Example:** Three leave days ke budget mein next two months ka longest practical break find karo.

**Free / Pro:** Pro.  
**Priority / effort:** P2 / Medium–High.

**First version and boundaries:** User-selected calendar range/work pattern. Show leave days consumed and continuous days off. Employer approval/eligibility infer na karein.

**Research basis:** Standalone vacation/PTO optimization exists; in-calendar and shift-aware integration proposed value hai. [Official/reference source](https://www.vacation-maximizer.com/)

### 21. Event Readiness

**Type:** New workflow capability using existing notes/attachments.  
**What to add:** Event-linked preparation checklist, required files aur preparation reminders. Incomplete items event reminder ke saath visible hon.

**Example:** Passport appointment kal hai: confirmation ready, photos ready, original documents collect karne hain.

**Free / Pro:** Basic checklist Free; reusable readiness setups/reminder rules Pro.  
**Priority / effort:** P1 / Medium.

**First version and boundaries:** Manual checklist and attachment references. Feature 08 kit checklist ko reuse kar sakti hai; separate competing template system na banayein.

**Research basis:** Original recommendation; globally unique claim nahi.

### 22. Plan vs Actual

**Type:** New capability candidate.  
**What to add:** User Start/Finish record kare; planned duration preserve rahe aur actual duration separately save ho. Enough comparable samples par duration suggestions.

**Example:** Four editing sessions 30 minutes plan hue, actual 50–60 lage; next time 55 minutes propose ho.

**Free / Pro:** Manual recording Free; personal duration suggestions/history analysis Pro.  
**Priority / effort:** P3 / Medium–High.

**First version and boundaries:** Manual tracking first, passive surveillance nahi. Sparse data par prediction nahi. User edit/delete kar sake; fixed meetings ki booked times unchanged rahen.

**Research basis:** Original proposed personal-calibration workflow; generic time tracking se distinct scope.

### 23. Travel and Preparation Blocks

**Type:** New linked scheduling capability.  
**What to add:** Event se pehle/baad manual journey, preparation aur recovery blocks reserve karna; anchor move par linked update preview.

**Example:** 3 PM appointment ke liye 30-minute journey aur 15-minute preparation block.

**Free / Pro:** Manual buffer Free; linked reusable rules Pro.  
**Priority / effort:** P2 / Medium–High.

**First version and boundaries:** Manual duration and offset first. Feature 08 ka linking model reuse karein. Live traffic integration later and separately scoped.

**Research basis:** Fantastical travel time related benchmark hai. [Official/reference source](https://flexibits.com/pricing)

### 24. Usable-Time Insights

**Type:** New analysis capability; feature 14 se related, duplicate nahi.  
**What to add:** Scheduled categories, continuous free time aur fragmentation ka understandable report; optionally actual data from feature 22 compare karna.

**Example:** Four free hours hain, lekin longest uninterrupted gap sirf 25 minutes hai.

**Free / Pro:** Basic weekly overview Free; trends and advanced filters Pro.  
**Priority / effort:** P3 / Medium.

**First version and boundaries:** Scheduled time ko actual work na label karein. Calendar categories first; event-title AI classification assume na karein. Heatmap visual summary; report actionable breakdown hai.

**Research basis:** Reclaim time tracking and fragmentation analysis related benchmarks hain. [Official/reference source](https://reclaim.ai/)

### 25. Daily Briefing

**Type:** Free daily-use addition candidate.  
**What to add:** First/next event, due or overdue tasks, today's scheduled commitments aur useful available gap ka concise overview.

**Example:** Aaj first meeting 10 AM, two pending tasks aur 3 PM par 40-minute gap.

**Free / Pro:** Free.  
**Priority / effort:** P1 / Low–Medium.

**First version and boundaries:** One simple in-app or widget card, optional scheduled delivery. Feature 06 ke task suggestions later; unrelated notifications add na karein.

**Research basis:** Original product recommendation.

### 26. Reminder Readiness Check

**Type:** Free reliability addition candidate.  
**What to add:** Test reminder aur required app/platform settings ki understandable status guidance.

**Example:** Test reminder receive nahi hua; app relevant notification setting ka next action dikhaye.

**Free / Pro:** Free.  
**Priority / effort:** P1 / Medium.

**First version and boundaries:** Supported permission/settings checks and explicit test. App har vendor restriction reliably detect kar sakti hai, ye promise na karein.

**Research basis:** Original reliability recommendation; new alarm type nahi.

## Feedback maintenance — separate from competitive feature proposals

| Item | Current evidence | Action | Tier |
|---|---|---|---|
| Medium agenda widget date/weekday consistency | FEEDBACK.md describes next-event day number paired with today's weekday. Current-build status not verified. | Verify first; if reproducible, today date/day stay together and future event dates stay in event rows. | Free correctness fix |
| Gradual widget opacity | FEEDBACK.md documents binary transparency and requests gradual control. Current-build status not verified. | If still absent, add opacity slider with readable text and preview. Not a unique feature claim. | Free customization improvement |

Historical provider sync issues discussed publicly should be regression-checked after 1.4 improvements, not declared unresolved. No new premium feature is defined from that historical issue.

## Competitor interpretation

| Product | Relevant documented benchmark | DotCal implication |
|---|---|---|
| [Business Calendar 2](https://play.google.com/store/apps/details?id=com.appgenix.bizcal.pro&hl=en_US) | Templates, bulk operations, premium widgets, reminders, export | DotCal already overlaps; improve workflow depth. |
| [aCalendar+](https://play.google.com/store/apps/details?id=org.withouthat.acalendarplus&hl=en-US) | Recurrence, calendar/task handling, business controls | Compatibility and predictable behavior remain quality requirements. |
| [DigiCal+](https://digibites.nl/digical/plus) | Widget customization and year heatmap | Widget configuration alone is not unique. |
| [Fantastical](https://flexibits.com/pricing) | Calendar-set automation, scheduling, travel | Compare advanced behavior, not merely whether a feature exists. |
| [Fantastical July 2026 update](https://flexibits.com/blog/2026/07/new-feature-roundup-you-asked-we-built/) | Additional timezone columns and multiple Openings durations | Concrete extensions for existing views and availability sharing. |
| [BusyCal](https://www.busymac.com/) | Snooze management, saved filters and previous-event suggestions | Useful models for upgrades 01, 03, 04. |
| [Structured](https://help.structured.app/en/articles/4511874) | Guided unfinished-task review | Task Review is related to an existing competitor workflow, not globally unique. |
| [Morgen](https://www.morgen.so/frames) | Assisted time blocking | Task-to-gap matching is a premium workflow opportunity. |
| [Reclaim](https://reclaim.ai/) | Flexible scheduling, buffers and schedule analytics | Local, focused DotCal workflows can be differentiated in execution. |
| [Timepage](https://bonobolabs.com/timepage/) | Polished timeline, heatmap, weather and travel context | Preserve DotCal's coherent minimal experience. |
| [TickTick](https://ticktick.com/about/upgrade) | Calendar planning with tasks and durations | Task/calendar integration should save planning effort. |

## Recommended first shortlist

1. **Reminder Center (01)** — practical extension of a feature users already use.
2. **Saved Smart Views (03)** — reusable daily organization.
3. **Event Readiness (21)** — concrete preparation benefit with a bounded manual first version.
4. **Find Time for This (06)** — strongest initial planning upgrade; build after duration/boundary groundwork.
5. **Linked Event Kits (08)** — first confirm current template depth; reusable linking enables later features.

Daily Briefing (25), Reminder Readiness Check (26) and verified feedback fixes can be considered as small supporting work; they should not crowd out the selected flagship.

## Distinctive feature bets — prototype before committing

| Candidate | Why test it | What would validate it |
|---|---|---|
| Schedule Change Detector (19) | Frequent timetable/roster changes can create substantial re-entry work. | Users can match changes correctly and repeatedly choose it over manual editing. |
| Offline Common-Time QR (17) | Turns existing sharing into a joint planning workflow. | Two users find a suitable slot quickly and understand snapshot freshness. |
| Pencil-In Plan Comparison (09) | Helps decide between actual alternatives. | Users can compare and commit without confusing tentative and confirmed events. |
| Shift-aware Usable Time + flexible routines (11,16) | Uses DotCal's existing shift context. | Suggested slots respect user-defined rest and work boundaries and are accepted. |
| Leave Optimizer (20) | Easily understood planning outcome. | Results match users' work patterns and leave accounting. |

## Dependencies and duplicate avoidance

- Task durations + usable boundaries support 06. Feature 06 supports 07 and advanced 05.
- One shared event-linking model should support 08, 21 and 23. They are separate user workflows, not three separate linking systems.
- Existing shifts plus boundaries support 11 and later 16.
- Availability calculation should be shared across 06, 12 and 17.
- Reviewed image import with source identity (18) is a prerequisite for reliable change detection (19).
- Occupied interval calculations can be reused for 14 and 24. Tracking actual durations (22) is optional and must be clearly distinguished from scheduled time.
- Keep current QR event sharing separate in meaning from new QR availability exchange (17).
- Existing image attachments are not equivalent to structured image import (18).
- Existing templates are not automatically equivalent to linked relative event kits (08); verify before building.

## Suggested delivery sequence

| Stage | Goal | Candidate scope | Gate before moving on |
|---|---|---|---|
| 0 | Reconcile current implementation | Map all 26 IDs to complete / partially complete / absent in full handoff. | No duplicate development tickets. |
| 1 | Useful improvements with bounded scope | Pick 2–3 of 01,03,21; supporting 25/26 if feasible. | Successful user task completion and repeat use. |
| 2 | Stronger Pro planning | 06, followed by selected 08/15 improvements. | Suggestions accepted, low correction/Undo rate, understandable behavior. |
| 3 | One distinctive workflow | Choose 09,17 or20 according to user demand. | Prototype evidence from target users. |
| 4 | Larger bets | 18→19, 07,16,22/24. | Technical accuracy, data-model readiness and cost fit. |

This is sequencing guidance, not a promise to build every listed feature. Ask a mix of current Free/Pro users and shift users to use short prototypes with real scenarios. Choose based on repeat usefulness, not only feature votes.

## Pricing and product constraints

Maintain existing lifetime-Pro access and promises. Favor local rules and user-confirmed scheduling where feasible. Do not price-lock existing reminders, sync, export or already purchased capabilities. New Pro features should provide automation, reuse and deeper planning value.

Cloud AI, live traffic/weather, hosted booking, shared family/team collaboration and cross-device web infrastructure remain deferred directions. They were discussed as larger-cost possibilities, not approved additions. Optional future cloud services require a sustainable cost model and terms consistent with existing entitlements; no subscription change is proposed here.

Avoid clutter by exposing advanced tools contextually: task details → Find Time; tentative plan → Compare; imported roster → Check Changes. Preserve a usable basic calendar.

