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

Acquiring focus is the caller's half. `Modifier.tvFocusTarget` — or `tvClickable`, which includes
it — wires the real focus event and hoists the flag into the caller's own state.

The same trade shows up again in `TvLayoutAnchors`: a small, named affordance in production, so an
invariant is checkable rather than reviewable.

## A destination must never request focus. An overlay must.

A screen that appears while the user is somewhere else must not take focus, because taking focus
*moves* them. Declare where focus should land instead — `focusProperties { onEnter = … }` on a
`focusGroup()` — so the redirect happens only when focus genuinely arrives. That is what the
`tv*FocusGroup` units here are for, and why a component in this module never fires a requester it
was handed.

A user-invoked overlay — a dialog, a filter panel, a setup step — is the opposite case, and
**should** pull focus: the user's own press is what put it on screen, and focus arriving is
precisely what they asked for. `TvOverlayArrivalFocusEffect` is that, packaged.

The shell is the third case. Something must hold focus on a D-pad surface at startup and nothing
else can yet, so the navigation rail hands focus *out* to the content on first composition. It
aims that request at the content group rather than at itself, so focus begins on the start
destination's first item and the rail stays collapsed until the user navigates to it. The
destination is still not the one asking.

## Move focus a frame after the thing holding it goes

When focus must move *because* something is being removed, wait a frame before requesting it.
Never request in the handler that triggers the removal: the outgoing node stays composed, and
stays focused, until it is disposed, and Compose's own focus recovery for it then runs over the
top of the request.

This fails silently, which is what makes the class expensive — the fix looks applied, and the
symptom is focus landing somewhere odd. `TvArrivalFocusEffect` and `TvOverlayArrivalFocusEffect`
are the frame wait, so a caller does not re-derive it.

Bound any such wait in wall-clock time rather than in frames. `withFrameNanos` resumes only on
frames the app actually draws, so a frame budget against a static screen never expires.

## The accent model: the accent is focus, a tick is selection

The reference is Google TV's own rail. There, focus is a solid white fill — white surface, black
icon and label — and that fill is the only thing telling you where you are. This model replaces
the white with the theme's `primary`.

> Focus is a **solid `primary` fill**, with `onPrimary` for the content, wherever a fill works.
> Where a fill would cover the content it sits on — a poster, a provider logo — focus is a
> **border** in the same colour instead. Nothing scales: focus is colour, never geometry.
> Selection is a **tick drawn inside the item**, never a container treatment.

Three rules follow, and they are the whole model.

1. **The fill is the focus channel; the hue stays the role.** A focused destructive control fills
   with the error colour, not the accent, so it stays dangerous exactly when the user is about to
   press it. Everything else fills with the accent. `TvButtonStyle.Destructive` is this rule.
2. **No control carries the accent at rest, and emphasis is shape rather than colour.** A primary
   control rests as a filled neutral; everything else rests as an outline. A screen still reads as
   one prominent action among quieter ones, in greys, and the accent appears only under the user's
   focus. Giving an accent back to a resting state re-opens the collision the rule exists to
   prevent: while the accent means both "primary" and "focused", an unfocused primary button looks
   focused.
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

### The rail marks current-but-unfocused with tint, not a second fill

The navigation rail selects on focus, so it already distinguishes two states: an accent fill while
it holds focus and is expanded, and an accent *tint* on no fill when focus is out in the content
and it is collapsed. That collapsed state is current-but-unfocused, expressed as fill-versus-tint
rather than as two fills. A dim fill there would be a third mark for a state the rail already
marks.
