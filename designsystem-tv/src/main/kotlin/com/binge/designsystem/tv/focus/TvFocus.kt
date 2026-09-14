package com.binge.designsystem.tv.focus

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.R as TvR

/**
 * The **border** focus treatment — an amber outline in a gap — for elements whose own content a fill would cover
 * (a poster, a provider logo, anything image-bearing); text/icons take [tvFocusFill] instead. See
 * `docs/tv-foundation.md` > The accent model.
 *
 * **Nothing scales** — focus is colour, never geometry: a growing element pushes its outline into its neighbour
 * (on the ~424dp onboarding watch-type cards 16dp apart, an 8% lift overlapped them), and dropping the scale
 * means focus needs only the outline's own 5.5dp.
 *
 * That 5.5dp is `tv_focus_ring_offset` + half of `tv_focus_ring_width`, not their sum: [focusRing] inflates the
 * outline by the offset and `Stroke` is **centred** on the path it strokes, so half the 3dp width falls inside
 * the 4dp gap and half outside it. Measured, not derived — a 200dp probe renders the band from 2.5dp to 5.5dp
 * out, centred on 4.0dp, 3.0dp thick (#2134). The reservations around the app state 7dp; that over-reserves by
 * 1.5dp and is kept deliberately, each for its own reason recorded at the token.
 *
 * Focus is a **parameter, not a runtime state**, so a focused appearance is previewable/screenshot-testable by
 * passing `true`; a baseline that depended on a real `FocusRequester` firing by capture time would be flaky, so
 * nothing here reads actual focus. Pair with [tvFocusTarget] (or [tvClickable]) when the element should also
 * *acquire* focus from the D-pad, hoisting the focused flag into the caller's state.
 *
 * The outline is drawn **outside** the element's bounds — inset, it would eat the content padding and read as a
 * plain border.
 *
 * [restingColor] draws the same outline in a second colour while *un*focused; `Unspecified` (nothing drawn)
 * everywhere but an immersive hero. Rule 2 of the accent model keeps the accent out of resting states (a resting outline may be any
 * colour except amber; the hero's is white). Same geometry as the focused ring, not a `border`, so focus is a
 * pure colour change with no pixel shift.
 *
 * Two consequences for callers, both about space the element does not own:
 * - **Overscan margins are measured to the outline**, not the element.
 * - **A lazy container clips it.** `LazyRow`/`LazyColumn` clip their viewport, so without care the outline is cut
 *   off the first and last items of every row (the ones reached most). Give the lazy container `contentPadding`
 *   of at least `tv_focus_ring_offset + tv_focus_ring_width`; same for any `clip`/`clipToBounds` ancestor.
 */
@Composable
fun Modifier.tvFocusIndicator(
    isFocused: Boolean,
    shape: Shape = BingeShapes.MediaCard,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    restingColor: Color = Color.Unspecified,
): Modifier =
    this
        .focusRing(
            isFocused = isFocused,
            shape = shape,
            color = ringColor,
            restingColor = restingColor,
            width = dimensionResource(TvR.dimen.tv_focus_ring_width),
            gap = dimensionResource(TvR.dimen.tv_focus_ring_offset),
        )

/**
 * The **fill** focus treatment — a solid amber surface behind the element — and the default way focus
 * reads on TV (`docs/tv-foundation.md` > The accent model). Google TV's rail fills white with black
 * content; this is that, in the theme's accent.
 *
 * Pass [fillColor] to keep a control's own role when focused: a destructive action fills `error`, so
 * it stays dangerous exactly when the user is about to press it. The caller is responsible for the
 * matching content colour — see [tvFocusContentColor].
 *
 * Unlike [tvFocusIndicator] this draws *inside* the element's bounds, so it costs no extra space and
 * a clipping ancestor can't shave it. Place it after the element's `clip` so it takes that shape.
 *
 * Like [tvFocusIndicator] it does not scale — see there for why focus is colour rather than geometry.
 */
@Composable
fun Modifier.tvFocusFill(
    isFocused: Boolean,
    shape: Shape = BingeShapes.Pill,
    fillColor: Color = MaterialTheme.colorScheme.primary,
): Modifier = this.then(if (isFocused) Modifier.background(color = fillColor, shape = shape) else Modifier)

/**
 * The content colour to use on top of [tvFocusFill]: `onPrimary` (a near-black brown) when focused,
 * otherwise the caller's resting colour. Kept next to the fill so no screen re-derives the pairing
 * and a focused row can't end up amber-on-amber.
 */
@Composable
fun tvFocusContentColor(isFocused: Boolean, resting: Color): Color = if (isFocused) MaterialTheme.colorScheme.onPrimary else resting

/** Strokes [shape]'s outline inflated by [gap], so the ring floats clear of the element. */
private fun Modifier.focusRing(
    isFocused: Boolean,
    shape: Shape,
    color: Color,
    restingColor: Color,
    width: Dp,
    gap: Dp,
): Modifier =
    this.drawWithContent {
        drawContent()
        val outlineColor = if (isFocused) color else restingColor
        if (outlineColor.isUnspecified) return@drawWithContent
        val gapPx = gap.toPx()
        val inflated = Size(size.width + gapPx * 2, size.height + gapPx * 2)
        translate(left = -gapPx, top = -gapPx) {
            drawOutline(
                outline = shape.createOutline(inflated, layoutDirection, this@drawWithContent),
                color = outlineColor,
                style = Stroke(width = width.toPx()),
            )
        }
    }

/**
 * Makes the element D-pad focusable and reports transitions to [onFocusChanged]. Pair with
 * [tvFocusIndicator], hoisting the focused flag into the caller's state so the appearance stays a parameter.
 *
 * **Do not put this in front of `clickable`, `selectable` or `toggleable`** — reach for [tvClickable],
 * or bare `onFocusChanged` as `TvProviderTile` and `TvChoicePanel` do. Each of those already installs a
 * focus target, and the `focusable()` here adds a second, outer one that wins focus — leaving their
 * OK/Enter handler on a node that never holds it, so the element highlights and does nothing.
 */
fun Modifier.tvFocusTarget(onFocusChanged: (Boolean) -> Unit): Modifier =
    this
        .onFocusChanged { onFocusChanged(it.isFocused) }
        .focusable()

/**
 * A D-pad target that runs [onClick] on OK/Enter and reports focus to [onFocusChanged].
 *
 * One modifier rather than `tvFocusTarget { }.clickable { }`, because that combination is **silently
 * broken**: `focusable()` and `clickable()` each install a focus target, the outer `focusable()` takes
 * focus, and `clickable`'s DPAD_CENTER/Enter handling then sits on an unfocused node — every OK press
 * is swallowed while the focus treatment still paints. Letting `clickable` own focusability keeps the
 * key handler and the focus target on the same node.
 *
 * A **disabled** control stays focusable but inert: `clickable(enabled = false)` drops focusability
 * too, which on a D-pad turns ↓ into a dead end rather than landing on a control that isn't ready yet.
 * So disabled falls back to a bare `focusable()`.
 *
 * **There is no long-press.** One existed as an "accelerator" into the library's action sheet (#1427)
 * and #1498 removed both it and its last caller: a hold is an invisible affordance on a remote (#1262),
 * so an action reachable only that way is an action most users never find. Anything worth doing to a
 * card belongs on a visible surface — on the collections that had it, OK now opens the sheet itself.
 */
@Composable
fun Modifier.tvClickable(
    onFocusChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    this
        .onFocusChanged { onFocusChanged(it.isFocused) }
        .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier.focusable())

/**
 * Marks a row/grid as one focus unit so the D-pad restores the child that last held focus instead of snapping
 * to the first — without it, leaving a carousel and returning re-enters at item 0, the most common 10-foot
 * navigation complaint.
 *
 * It restores on a directional re-entry but gives nothing on the *first* entry, having saved nothing until the
 * group has been left once (both pinned by `TvFocusSemanticsTest`). So a surface whose *arrival* must land
 * somewhere specific needs [tvEntryFocusGroup]; one that only resumes where the user left needs this. Not for a
 * focus-is-selection surface — [tvSelectionFocusGroup] — nor a click-to-open one whose entry should land on a
 * specific child — [tvEntryFocusGroup], which a restorer silently will not do.
 */
fun Modifier.tvFocusGroup(): Modifier = this.focusRestorer().focusGroup()

/**
 * One focus unit whose **entry is routed to a chosen child** — the first cell of a lazy row/grid, or a
 * remembered one — instead of left to a directional search.
 *
 * For **click-to-open** surfaces where arriving selects nothing (a media row, a grid, the immersive hub, a
 * settings pane). A lazy container's default entry lands on whatever child sits nearest the beam, which for a
 * row still scrolled from a previous browse is its *last* cell (#1500's episodes row landing on the see-all
 * tile). Routing entry to [entry] lands it where it should every time.
 *
 * Not [tvFocusGroup] (`focusRestorer().focusGroup()`, the one reached for by name): a restorer has nothing saved
 * until the group has been *left* once, so the **first** entry falls through to geometry — exactly the arrival
 * this routes. Not [tvSelectionFocusGroup] either: that routes to the *selected* child with a `!selected` commit
 * guard, for surfaces where arriving *is* selecting; here there is no guard and [entry] is just the first
 * child's requester.
 *
 * `runCatching` because [entry]'s node may not be composed yet (a lazy child past the buffer's edge), in which
 * case the directional search still runs and nothing is worse off.
 */
fun Modifier.tvEntryFocusGroup(entry: FocusRequester): Modifier =
    this
        .focusProperties { onEnter = { runCatching { entry.requestFocus() } } }
        .focusGroup()

/**
 * One focus unit that **traps focus inside itself** — every directional attempt to leave is cancelled, so the
 * only way out is the surface's own dismiss path (a ← handler, BACK). The exit mirror of [tvEntryFocusGroup]:
 * that routes arrival *in*, this refuses every drift *out*. A surface that does both composes both.
 *
 * For a **modal overlay laid over a still-composed screen** — [TvSideSheet] and [TvFilterPanel] both sit over a
 * grid the scrim only hides. Without the trap, ↑ from the first row escapes into the band behind the scrim, or a
 * press lands on a grid cell the user cannot see; ← is meant to dismiss, not to leak focus out a side.
 *
 * `onExit` **before** `focusGroup()`, not after: the boundary callback belongs to the node that owns the group,
 * so the two must sit on that one node in this order. That ordering is the easy half to get wrong, and both modal
 * surfaces were open-coding it — naming the pair once is the point.
 */
fun Modifier.tvExitFocusGroup(): Modifier =
    this
        .focusProperties { onExit = { cancelFocusChange() } }
        .focusGroup()

/**
 * Offer arrival focus to [target] each frame until [taken] reports the surface holds it — the
 * screen-side sibling of the shell's `offerFocusToContent`, and retried for the same measured reason:
 * a single mount-time request fires inside the push transition and fails silently, ~300ms before the
 * outgoing screen's disposal hands focus over. By then the disposal's own geometric reassignment is
 * the writer, and an `onEnter` redirect does not reliably cancel it — logged on a Shield as the
 * action row gaining focus for 5ms before the original move continued into the cast row.
 *
 * Claiming early is the whole fix: once the surface holds focus, the outgoing screen's nodes are
 * unfocused when they dispose, so no reassignment ever runs. The loop stops the moment [taken] is
 * true, so it cannot fight the user, and a surface that never takes focus stops it at the same
 * frame/time budget the shell's drill-down handoff uses.
 */
suspend fun offerTvArrivalFocus(target: FocusRequester, taken: () -> Boolean) {
    val deadline = SystemClock.uptimeMillis() + FOCUS_OFFER_TIMEOUT_MS
    repeat(FOCUS_OFFER_FRAMES) {
        if (taken() || SystemClock.uptimeMillis() > deadline) return
        // Deliberately not stopping on the granted request: [taken] is the caller's own "my subtree holds it"
        // and is strictly better informed, because a grant can still be reassigned within the same frame.
        target.tryRequestFocus()
        withFrameNanos { }
    }
}

/**
 * The shell's drill-down budgets (TvNavRailFocus), restated here because those are internal to the
 * nav package's callers: a frame count to ride "retry when the content changes", a time cap so a
 * static destination cannot keep the loop alive past its window.
 */
private const val FOCUS_OFFER_FRAMES = 600
private const val FOCUS_OFFER_TIMEOUT_MS = 3_000L

/**
 * Requests focus and answers whether it was granted, without letting a sentinel requester throw.
 *
 * `requestFocus()` returns `false` for a requester with no attached node — it prints a warning and does **not**
 * throw (`FocusRequester.findFocusTarget`, Compose UI 1.12); only [FocusRequester.Default] and
 * [FocusRequester.Cancel] `check`. So the `runCatching` that wrapped every call site here was discarding a
 * *result*, not catching an exception, and every one of those discards was a retry thrown away.
 */
private fun FocusRequester.tryRequestFocus(): Boolean = runCatching { requestFocus() }.getOrDefault(false)

/**
 * Offer focus back to [target] until it is granted — the close half of every sheet/panel round trip, and the
 * return half of an overlay pop.
 *
 * **Offered, not fired once, because one frame is reliably too early**: the outgoing surface stays composed and
 * focus-trapped for its whole exit transition, and a trap *cancels* a request from outside it, so a single shot
 * into that window cannot succeed however well timed (#2517). Stopping on the grant rather than on a caller
 * predicate means it cannot fight a user who got there first. Bounded in wall-clock as well as frames.
 *
 * @return whether focus was granted within the budget; `false` means the target never attached.
 */
suspend fun restoreTvOverlayFocus(target: FocusRequester): Boolean {
    withFrameNanos { }
    val deadline = SystemClock.uptimeMillis() + FOCUS_OFFER_TIMEOUT_MS
    repeat(FOCUS_OFFER_FRAMES) {
        if (target.tryRequestFocus()) return true
        if (SystemClock.uptimeMillis() > deadline) return false
        withFrameNanos { }
    }
    return false
}

/**
 * One focus unit whose **entry is routed to the selected child** rather than left to a directional search.
 *
 * For a surface where **focus is the commit** — moving onto a thing selects it, no OK — as `BingeTvNavRail`,
 * `TvTabRow` and the TV Lists column all do. There an unrouted entry is not cosmetic: the geometric search picks
 * whatever child sits nearest the beam, and arriving there *is* a selection change (the rail recorded it first —
 * "merely opening the menu navigated the user somewhere they hadn't asked to go"; since re-fixed on the tab row
 * #1395 and the lists column). This exists to stop the rediscovery.
 *
 * Not [tvFocusGroup]: `focusRestorer` has nothing saved until the group has held focus once, the first-entry
 * case that breaks — and it is less correct even afterwards, since when focus is the commit the selected child
 * *is* the last to have held focus, so routing to the selection is the same answer plus being right the first time.
 *
 * The companion rule is not a modifier: each child must guard its own commit on already being selected
 * (`if (gained && !selected) onSelect()`), or landing on the current item re-fires the selection and, on a paged
 * surface, re-runs its query. That can only live on the child, but it is half of the same fix.
 *
 * Pair with [tvSelectionTarget] on the child, which gives [selected] something to point at.
 */
fun Modifier.tvSelectionFocusGroup(selected: FocusRequester): Modifier =
    this
        // `runCatching` because the requester has no node whenever nothing is selected yet — an empty list, or a
        // surface whose selection has not resolved. A failed request falls through to the ordinary search, which
        // is the right outcome: there is no selected child to prefer.
        .focusProperties { onEnter = { runCatching { selected.requestFocus() } } }
        .focusGroup()

/**
 * Marks this child as the one [tvSelectionFocusGroup] should route entry to, when [isSelected].
 *
 * Applied conditionally rather than to a fixed child so it **follows the selection** — entry always lands on what
 * is currently on screen rather than on whatever was selected when the group was composed. Safe against fighting
 * the user for the reason the rail records: on these surfaces the selection only ever moves *because* focus moved,
 * and `onEnter` does not fire for moves within the group.
 */
fun Modifier.tvSelectionTarget(isSelected: Boolean, selected: FocusRequester): Modifier =
    if (isSelected) this.focusRequester(selected) else this
