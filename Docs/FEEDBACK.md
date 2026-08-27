# DotCal Feedback

Updated: 2026-08-10

## Priority: Medium Agenda Widget Date Clarity + Transparency

Source: Spanish user feedback with widget screenshot.

### Original Feedback

```text
Buenos días
No entiendo el widget, pone 12, entiendo que se refiere a los eventos del 12/6, antes no tengo nada, pero debajo pone lunes, no sería mejor poner 10 Lunes, y en el campo de los eventos comenzar con la fecha 12/6 para los mismos.
Por último, tenéis pensado definir una transparencia gradual para ajustarla al fondo de la pantalla

Muy buena aplicación
Gracias
```

### English Translation

```text
Good morning,

I don't quite understand the widget. It displays "12", which I assume refers to the events on 6/12, since I don't have anything scheduled before that. However, below it says "Monday". Wouldn't it be better to display "Monday 10", and then start the events field with the date "6/12"?

Lastly, do you plan on adding a gradual transparency setting so it can be adjusted to the background wallpaper?

Great app!

Thanks
```

### App Check

- The medium agenda widget currently shows the next event day in the left badge:
  `data.nextEvent?.dayOfMonth ?: data.todayLabel`.
- The weekday under that badge is today's weekday from `todayDayAbbrev()`.
- This can produce a mixed display like `12` + `LUN`, while the event rows show `MIÉ, AGO 12`.
- User confusion is valid: the badge can read like the next event date, but the weekday reads like
  today.
- DotCal already supports transparent widgets at `Settings > Widgets > Transparent widgets`.
- Current transparency is binary on/off only. There is no gradual opacity slider yet.

### Action Items

1. Fix medium agenda widget date block so the day number and weekday refer to the same date.
2. Prefer showing today's date/day in the left block, for example `10` + `LUN`.
3. Keep future event dates inside event rows, for example `MIÉ, AGO 12`.
4. Add gradual widget background opacity control after the date clarity fix.
5. Keep the existing transparent-widget toggle path discoverable:
   `Settings > Widgets > Transparent widgets`.

### Suggested User Reply

```text
Good morning,

Thank you for the feedback and the screenshot.

You are right, this widget layout can be confusing. I checked it and the large number is currently showing the next event day, while the weekday below is showing today's weekday. That can make it look inconsistent when the next event is not today.

I'll adjust this in an upcoming update so the widget is clearer: the date area should represent today more clearly, and future event dates should be shown inside the event list.

About transparency: DotCal already supports transparent widgets. You can enable it from:
Settings > Widgets > Transparent widgets

Currently it is an on/off option. A gradual opacity control is not available yet, but it's a good suggestion and I'll consider adding more control for widget background transparency.

Thanks again for using DotCal and for helping improve it.
```
