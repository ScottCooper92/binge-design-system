# The TV foundation

Two decisions run through every component in `designsystem-tv`, and most of the module's less
obvious behaviour follows from one of them. The KDoc points here rather than restating them at
each call site.

Binge follows the same two rules and keeps its own record of how they were arrived at, alongside
the app-level consequences. This document is the part a companion author needs, in this module's
own terms.

## Focus is a parameter, not a runtime state

A component takes `isFocused: Boolean` and styles itself from it. Nothing here reads real focus.

That is an API decision, and it is about coverage. A preview renders one frame. A baseline that
depended on a real `FocusRequester` having fired by the time that frame was captured would be
flaky, where a parameter is deterministic. So a focused appearance is testable by passing `true`.

Acquiring focus is the caller's half. `Modifier.tvFocusTarget` for a plain element, or `tvClickable`
for one that also takes OK, wires the real focus event and hoists the flag into the caller's own state.
`tvClickable` is its own `onFocusChanged` plus `clickable`, so it is never combined with `tvFocusTarget`.

The same trade shows up again in `TvLayoutAnchors`: a small, named affordance in production, so an
invariant is checkable rather than reviewable.

## A destination must never request focus. An overlay must.

A screen that appears while the user is somewhere else must not take focus, because taking focus
*moves* them. Declare where focus should land instead — `focusProperties { onEnter = … }` on a
`focusGroup()` — so the redirect happens only when focus genuinely arrives. That is what the
`tv*FocusGroup` units here are for, and why a component in this module never fires a requester it
was handed.

A lazy row, column or grid is already its own focus group, so it takes `tvLazyFocusGroup` — the restorer alone,
with an optional fallback for the first entry — rather than `tvFocusGroup`. `TvListPane` is the case: its first
entry lands on the first value below the pinned controls, and re-entry resumes on the option last focused.

A user-invoked overlay — a dialog, a filter panel, a setup step — is the opposite case, and
**should** pull focus: the user's own press is what put it on screen, and focus arriving is
precisely what they asked for. `TvOverlayArrivalFocusEffect` is that, packaged.

The shell is the third case. Something must hold focus on a D-pad surface at startup and nothing
else can yet, so the shell's navigation hands focus *out* to the content on first composition. It
aims that request at the content group rather than at itself, so focus begins on the start
destination's first item until the user navigates to the bar. The
destination is still not the one asking.

## Move focus a frame after the thing holding it goes

When focus must move *because* something is being removed, wait a frame before requesting it.
Never request in the handler that triggers the removal: the outgoing node stays composed, and
stays focused, until it is disposed, and Compose's own focus recovery for it then runs over the
top of the request.

This fails silently, which is what makes the class expensive — the fix looks applied, and the
symptom is focus landing somewhere odd. `TvArrivalFocusEffect` is the arrival half: one request, made
once the surface's target has reported it is attached, while the outgoing screen may still hold focus.
Claiming early means the outgoing nodes are already unfocused when they dispose, so Compose's recovery
never runs. It does not wait or retry, so the target must be attached from the first frame (a plate
that becomes the real control in place), and it makes the request once per arrival: a surface that
arrives again without leaving the composition passes a new `arrivalKey`.

A user's own direction press since the arrival composed cancels the request, so it never pulls focus
back from the bar. That guard acts only when the host provides `LocalTvUserMoves` and feeds it from the
key handler that sees every press; without a host nothing is noted and the arrival always claims.

`TvOverlayArrivalFocusEffect` and `restoreTvOverlayFocus` are the older frame-wait-then-retry shape.
Bound any such wait, in wall-clock time where the thing waited on can stop drawing: `withFrameNanos`
resumes only on frames the app actually draws, so a frame budget against a static screen never expires.

## The accent model: the accent is focus, a tick is selection

The reference is Google TV's own rail. There, focus is a solid white fill — white surface, black
icon and label — and that fill is the only thing telling you where you are. This model replaces
the white with the theme's `primary`.

> Focus is a **solid `primary` fill**, with `onPrimary` for the content, wherever a fill works.
> Where a fill would cover the content it sits on — a poster, a provider logo — focus is a
> **border** in the same colour instead. Nothing scales: focus is colour, never geometry.
> Selection is a **tick drawn inside the item**, never a container treatment.

Three rules follow, and they are the whole model.

1. **The fill is the focus channel; the hue stays the role.** A focused destructive outline control fills
   with the error colour, not the accent, so it stays dangerous exactly when the user is about to
   press it. Everything else fills with the accent. `TvButtonStyle.DestructiveOutline` is this rule.
   Destructive actions go last in an action row. A reversible one (block) takes `DestructiveOutline`; an
   irreversible one (remove, delete) takes `Destructive`.
2. **No control carries the accent at rest, and emphasis is shape rather than colour.** A primary
   control rests as a filled neutral; everything else rests as an outline, except the red styles and the text styles. `Destructive`
   rests as a solid error fill and shows focus with a light border, since its fill already is the colour focus would
   bring; `DestructiveOutline` rests as a red outline. `Text` and `TextDestructive` are for a dialog's answers,
   where the panel is the shape: they rest as a bare label with no edge, and `TextDestructive` keeps its red label. A screen still reads as
   one prominent action among quieter ones, in greys, and the accent appears only under the user's
   focus. Giving an accent back to a resting state re-opens the collision the rule exists to
   prevent: while the accent means both "primary" and "focused", an unfocused primary button looks
   focused.
   **A disabled control shows focus as an edge, not a fill.** It stays focusable so ↓ isn't a dead end, but a fill
   would promise a press that does nothing. So a focused disabled control draws an opaque edge, for every
   style including `Text` and `TextDestructive`: `border` with its alpha raised to 1 for the neutral styles (`border` itself is translucent, fainter than the dimmed resting edge), `error` for the red ones, so it still
   reads as destructive. The container stays transparent and the label stays dimmed.
3. **Selected and focused are independent, and both can be true.** A chosen tile keeps its tick
   whether or not focus is on it, so a grid never loses which tiles are picked while the user moves
   through it. The tick is also legible where colour alone is not — a mis-calibrated panel, or a
   viewer who cannot separate the accent hue from grey.

### Nothing scales

A growing element pushes its focus outline into its neighbour, and on a row of wide cards a few
percent is enough to overlap them. Dropping the scale means focus needs only the outline's own
width and gap. That is why `tvFocusIndicator` draws *outside* the element's bounds, why overscan
margins are measured to the outline rather than the element, and why a lazy container has to carry
`contentPadding` for it or clip it off the first and last cell of every row.

### Current-but-unfocused is a tint, not a second fill

The navigation rail this module used to carry selected on focus, so it already distinguished two states: an accent
fill while it held focus and was expanded, and an accent *tint* on no fill when focus was out in the content and it
was collapsed. That is current-but-unfocused, expressed as fill-versus-tint rather than as two fills. A dim fill
there would have been a third mark for a state the rail already marked. The rule outlived the rail: a surface that
shows a current item without focus uses a tint.

### TV styles: what colour focus is

The model above is written for the accent, and `TvStyle.Accent` is that, and the default. `TvStyle.Monochrome` is
Google TV's own look: focus fills white (`onSurface`) with dark content (`surface`), the focus ring is white, a
switch is black and white, and a sheet's panel is `surfaceVariant`. The rules do not change, only the colour that
means focus.

A component reads focus, switch and panel colours from the style's roles (`MaterialTheme.tvStyle`), never from
`primary`. The accent keeps every job that is not focus, in both styles: a rating, a selection tick, a count badge,
the rail's current destination, the current row of a two-pane list. In the monochrome style that splits the two
meanings cleanly. White is where you are, and the accent is what is chosen.

Destructive focus is the error colour in both styles. A dangerous action stays red under focus whichever colour
focus otherwise is.

Set the style once, on the outermost `BingeTvTheme`. A nested `BingeTvTheme` with no `style` keeps the one around
it, so a screen that wraps itself in the theme again still draws in the app's style. Do not fake a style by copying
the colour scheme with a different `primary`: that also moves the accent, which is the collision the styles exist to
avoid.

## A refused save-as-made write is reported in place

A TV page that saves each change as it is made has no Save button, no snackbar and no timed message. When the server
*refuses* a write (a failure that would only be refused again, so there is nothing to retry), the page puts the saved
value back and sets `SaveStatus.Refused(reason)`. `TvSaveStatus` draws it as an error-tone line with the reason and no
Retry. The page owns the words.

The line stays until the row is next changed or the page is left. There is no timer, because a timed message would
expire unread on a screen nobody is holding. This applies to every TV save-as-made row. A transient failure, which keeps
the change and can be sent again, is `SaveStatus.Failed` with Retry instead.
