package com.binge.designsystem.tv.nav

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.startHorizontalGradient
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.TV_IMMERSIVE_CROSSFADE_MILLIS
import com.binge.designsystem.tv.focus.tvSelectionFocusGroup
import kotlinx.coroutines.withTimeoutOrNull
import com.binge.designsystem.tv.R as TvR

/** The glass ramp's densest stop — near-solid under the glyphs, so they stay legible over any artwork. */
private const val RAIL_SCRIM_ALPHA = 0.94f

/**
 * Held through [RAIL_SCRIM_HOLD_FRACTION] of the rail's width, then falling to clear at its right edge.
 *
 * The *collapsed* strip's width, and only that: the stops are fractions, so the same ramp stretched over the
 * expanded panel ends its hold 38dp inside where the labels end. An expanded rail is solid instead.
 *
 * The *right* edge whatever the layout direction, deliberately: the shell's directional keys are physical — a
 * side sheet that mirrors to the other edge is still dismissed by LEFT — so paint that mirrored on its own
 * would disagree with the contract the D-pad keeps. The two move together.
 */
private const val RAIL_SCRIM_HOLD_ALPHA = 0.86f
private const val RAIL_SCRIM_HOLD_FRACTION = 0.72f

/**
 * The 10-foot navigation rail: a D-pad focusable left sidebar that **expands on focus** to reveal labels,
 * collapsing to an icon strip when the content takes focus. Container and items are ours rather than
 * tv-material's `NavigationDrawer`, whose item styling isn't this module's accent model and whose layout puts
 * the rail *beside*
 * the content (the resize this avoids); only expand-on-focus was worth borrowing.
 *
 * Because focus **is** selection here, the current destination needs one state treatment: a solid amber fill
 * with dark content when expanded, an amber icon when collapsed. Google TV's white-pill rail in Binge's amber.
 * No border and no tick — a tick in a nav row reads as a checklist item rather than a location.
 *
 * **Focus is the commit** — [onSelect] fires on focus, not OK — so walking the rail walks the app and there is
 * no press that confirms where you already are (the leanback expectation, and it spares a D-pad user learning
 * that ↓↓ then OK differs from ↓↓). Click still selects for a pointer/OK, but only re-selects where focus is.
 *
 * **The rail is a genuine overlay over a full-width content pane.** The pane is measured once at the panel's
 * width and translated right via a draw-time `translationX` as the rail opens, so nothing ever re-measures —
 * resizing (what `NavigationDrawer` does, and what this used to do) reflows the screen every time focus enters
 * the rail. Content is therefore **full-bleed by default**: backdrops and lazy rows reach the panel edge and
 * pass under the rail, and what must stay clear of it pads by [LocalTvContentInset] (the collapsed width)
 * instead of the pane being structurally inset. The resting strip paints a scrim, not an opaque fill — but only
 * over real artwork: it fades between a solid panel and glass in step with the backdrop's own crossfade
 * ([TvRailArtworkPresence]), because a rail left translucent over the hub's hero↔backdrop transition reads as
 * the rail itself flickering. An expanded rail is solid whatever is behind it. Fixed-width in both states
 * rather than content-sized, since the items fill its width.
 *
 * Three slots: [header] pinned top (account avatar), [items] the destination body, [footer] pinned bottom
 * (Settings). [expanded] is normally `null` (*follow focus*); a preview passes `true`/`false` to pin the state
 * without a real focus event (focus-as-parameter — see `docs/tv-foundation.md`).
 *
 * [artworkBehind] pins the glass/solid fill the same way, and for the same reason. Production leaves it `null`
 * and the rail follows [TvRailArtworkPresence], which a backdrop reports into from a `DisposableEffect` — and
 * effects do not run when a `@Preview` is rendered, so the glass state would be uncapturable and the whole
 * scrim ramp would go unscreenshotted while every baseline still passed.
 *
 * [contentDepth] is the current destination's back-stack depth, used for one thing: a change means the content
 * navigated itself, so focus should follow into what replaced it. Only the shell can tell a content-initiated
 * navigation from a rail-initiated one (a rail move rebuilds the stack at depth 1; a drill-down or in-destination
 * Back moves the depth). Left at its default the rail never re-hands focus — the old behaviour.
 *
 * [railFocusRequester] lets an owner drive focus *into* the rail — the shell's Back handler uses it to land focus
 * on the selected item when Back is pressed with focus in the content (step 1 of the host app's TV Back
 * hierarchy). It *is* the rail's own selected-item requester (the one
 * `tvSelectionTarget` pins to the selection), so it lands on the current destination's row — the same node and
 * path the startup fallback uses. Default `null` keeps a private requester.
 *
 * [onRailFocusChanged] reports whether focus is anywhere inside the rail, so the same owner can tell the two Back
 * cases apart (focus in content vs. focus already on the rail) without duplicating the rail's `hasFocus`
 * bookkeeping. Read-only mirror; the rail steers its own expansion off the internal flag.
 *
 * [contentFocusRequester], when supplied, *is* the requester aimed at the content [focusGroup] — the same node
 * the rail's own startup handoff targets. The shell holds it so it can restore focus to the content group after
 * a full-screen overlay pops (`restoreTvOverlayFocus`), landing the user back on the browse pane rather than the
 * rail. Default `null` keeps a private requester, exactly as [railFocusRequester] does for the rail side.
 */
@Composable
fun BingeTvNavRail(
    header: TvNavRailItem?,
    items: List<TvNavRailItem>,
    footer: TvNavRailItem?,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    contentDepth: Int = 1,
    railFocusRequester: FocusRequester? = null,
    onRailFocusChanged: (Boolean) -> Unit = {},
    contentFocusRequester: FocusRequester? = null,
    // Hoisted so a test can reset it: the pivot scrolls on every focus move, and the reachability harness's
    // path replay needs a canonical scroll position at arrival (`TvDpadReachability` assumes one).
    itemsScrollState: ScrollState = rememberScrollState(),
    artworkBehind: Boolean? = null,
    content: @Composable () -> Unit,
) {
    // The rail's entry point: whichever item is selected. Used for the three ways focus arrives here — the ←
    // redirect, the startup fallback, and an owner's [railFocusRequester] (which, when supplied, *is* this
    // requester, so Back-to-rail lands on the selected item exactly as the fallback does).
    val railEntry = railFocusRequester ?: remember { FocusRequester() }
    val contentFocus = contentFocusRequester ?: remember { FocusRequester() }
    var railHasFocus by remember { mutableStateOf(false) }
    var contentHasFocus by remember { mutableStateOf(false) }

    // Focus starts in the content, not the rail — requesting it on mount opened the app rail-focused over the start destination.
    // The content `focusGroup` delegates to its first child (the startup hand-off in `docs/tv-foundation.md`),
    // retried per frame since a cold-start destination is a
    // target-less placeholder (Shield-verified); it yields to a user already in the rail, and the fallback covers a nothing-focusable one.
    LaunchedEffect(Unit) {
        offerFocusToContent(
            contentFocus = contentFocus,
            yieldToRail = { railHasFocus },
            taken = { contentHasFocus },
        )
        if (!contentHasFocus && !railHasFocus) runCatching { railEntry.requestFocus() }
    }

    val handingOffToContent = contentHandoffInFlight(
        contentDepth = contentDepth,
        contentFocus = contentFocus,
        contentHasFocus = { contentHasFocus },
    )

    // Not expanded mid-handoff: Compose parks focus in the rail from the moment the activated control is disposed until
    // the handoff takes focus back — up to two seconds on a cold query — so following `railHasFocus` alone opened the rail
    // over a screen the user just asked to see. Debouncing would work too, but delays opening for a user who really pressed ←.
    val isExpanded = expanded ?: (railHasFocus && !handingOffToContent)

    val collapsedWidth = dimensionResource(TvR.dimen.tv_nav_rail_collapsed_width)
    val expandedWidth = dimensionResource(TvR.dimen.tv_nav_rail_expanded_width)
    val motion = if (LocalReduceMotion.current) snap() else spring<Dp>()
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) expandedWidth else collapsedWidth,
        animationSpec = motion,
        label = "tvNavRailWidth",
    )
    val contentShiftState = animateDpAsState(
        targetValue = if (isExpanded) expandedWidth - collapsedWidth else 0.dp,
        animationSpec = motion,
        label = "tvNavRailContentShift",
    )
    val contentShift by contentShiftState
    // Labels pop the instant the rail opens, tied to the expansion state rather than the width animation.
    val labelsVisible = isExpanded

    val artwork = remember { TvRailArtworkPresence() }
    val glass by railGlassFraction(artworkBehind ?: artwork.isPresent)
    // Glass is for the resting strip; expanding is a deliberate ask to read a menu. Two causes, two specs — the
    // artwork keeps its crossfade, the expansion rides the width's own spring — so neither resolves after the
    // other has settled, and either one saying "solid" wins.
    val collapsedFraction by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = if (LocalReduceMotion.current) snap() else spring(),
        label = "tvNavRailExpandedSolid",
    )
    val fillGlass = glass * collapsedFraction

    // The shell paints the theme background so the two halves of the screen agree. When only the rail strip
    // painted one, a destination that drew no background showed the window's black beside the rail's #0E0E0F —
    // two blacks a shade apart read as a seam down the edge of the content rather than as a sidebar.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                // Full panel width — the rail overlays the pane rather than insetting it, so backdrops and lazy
                // rows are full-bleed by default and content clears the rail via [LocalTvContentInset] padding.
                // The width never changes, so opening the rail stays pure draw-phase translation, no relayout.
                .fillMaxSize()
                .graphicsLayer { translationX = contentShift.toPx() }
                .onFocusChanged { contentHasFocus = it.hasFocus }
                // A group rather than a focus target: the shell must not become focusable itself, it only
                // needs somewhere to aim the initial request so it lands on the destination's own first item.
                .focusRequester(contentFocus)
                .focusGroup(),
        ) {
            // The overlay contract's three ambients: what in-pane content pads by to clear the rail, the pane
            // shift a backdrop cancels to hold still during the expansion, and where a backdrop reports its
            // presence so the rail turns to glass only over real artwork.
            CompositionLocalProvider(
                LocalTvContentInset provides collapsedWidth,
                LocalTvPaneShift provides contentShiftState,
                LocalTvRailArtwork provides artwork,
            ) {
                content()
            }
        }
        Column(
            modifier = Modifier
                .width(railWidth)
                .fillMaxHeight()
                // A scrim rather than an opaque fill — but only over artwork, and only while collapsed. `fillGlass` rides
                // from 0 (solid panel, as when the hero is up, the screen is flat, or the rail is open) to 1 (near-solid
                // under the glyphs, falling away at the outer edge), so the rail is never translucent over nothing.
                .background(
                    startHorizontalGradient(
                        0f to MaterialTheme.colorScheme.background.copy(alpha = lerp(1f, RAIL_SCRIM_ALPHA, fillGlass)),
                        RAIL_SCRIM_HOLD_FRACTION to
                            MaterialTheme.colorScheme.background.copy(alpha = lerp(1f, RAIL_SCRIM_HOLD_ALPHA, fillGlass)),
                        1f to MaterialTheme.colorScheme.background.copy(alpha = lerp(1f, 0f, fillGlass)),
                    ),
                ).selectableGroup()
                // hasFocus, not isFocused: this observes the focus *group*, so the rail stays open while
                // any item inside it holds focus and closes the moment focus crosses into the content.
                .onFocusChanged {
                    railHasFocus = it.hasFocus
                    onRailFocusChanged(it.hasFocus)
                }
                // ← must land on the current destination; a plain focus group leaves it to a geometric search
                // for the nearest item, so — focus being selection here — merely opening the menu would navigate
                // somewhere the user hadn't asked to go (Shield-verified). Mechanism lives in `tvSelectionFocusGroup`.
                .tvSelectionFocusGroup(railEntry),
        ) {
            RailItemsRegion(
                header = header,
                items = items,
                footer = footer,
                selectedKey = selectedKey,
                expanded = isExpanded,
                onSelect = onSelect,
                railEntry = railEntry,
                scrollState = itemsScrollState,
                labelsVisible = labelsVisible,
            )
        }
    }
}

/**
 * Hands focus back to the content when the content replaces *itself*, and reports whether that is in flight.
 *
 * Unlike the startup handoff, this one does not yield to the rail. A drill-down disposes the very control the
 * user activated and the destination replacing it is a skeleton with nothing focusable yet, so focus has nowhere
 * to go and Compose parks it in the rail — measured at ~400ms warm, just under two seconds cold, from the
 * outgoing screen's disposal until the incoming grid has a cell.
 *
 * Re-running the startup handoff (or widening its window) does not fix this — both tried: it bails on
 * `railHasFocus`, true by then, and the state it checks belongs to the still-composed *outgoing* screen. Hence a
 * separate, sequenced effect. A depth change is only ever content-initiated, so there is no user-in-the-rail
 * case to yield to (a rail navigation rebuilds the stack at depth 1, moving the depth only when leaving a
 * drill-down — and then the user is returning to a root they asked for).
 *
 * What made the rail transit *harmful* rather than untidy is fixed separately in [RailItem]: focus landing on a
 * rail item used to commit a selection, rebuilding the back stack and throwing the drill-down's route away. This
 * only puts focus back where it belongs.
 *
 * Skipping the first run matters: [contentDepth] arrives with a value, and treating that as a change would
 * duplicate the startup handoff and fight it.
 */
@Composable
private fun contentHandoffInFlight(
    contentDepth: Int,
    contentFocus: FocusRequester,
    contentHasFocus: () -> Boolean,
): Boolean {
    var inFlight by remember { mutableStateOf(false) }
    var lastDepth by remember { mutableIntStateOf(contentDepth) }
    // Read inside a withTimeoutOrNull that waits on focus, so the effect outlives the lambda it
    // captured — a recomposition during the handoff supplies a new one.
    val currentContentHasFocus by rememberUpdatedState(contentHasFocus)
    LaunchedEffect(contentDepth) {
        if (contentDepth == lastDepth) return@LaunchedEffect
        lastDepth = contentDepth
        // Sequenced, not raced: wait for the old screen's focus loss, then take focus back — the obvious race does nothing (see
        // [awaitContentFocusLost]). Bounded in wall-clock time, not frames: the handoff never succeeds against a nothing-focusable
        // destination (`TvPlaceholderPane` — every detail route today), where frames barely advance with rail expansion suppressed.
        inFlight = true
        try {
            withTimeoutOrNull(CONTENT_HANDOFF_TIMEOUT_MS) {
                if (awaitContentFocusLost(currentContentHasFocus)) {
                    offerFocusToContent(
                        contentFocus = contentFocus,
                        // Deliberately never yields: a depth change is content-initiated, so there is no
                        // user-in-the-rail case to protect — and by this point Compose has parked focus in the
                        // rail, which is the very thing being corrected.
                        yieldToRail = { false },
                        taken = currentContentHasFocus,
                        frames = CONTENT_HANDOFF_FRAMES,
                    )
                }
            }
        } finally {
            inFlight = false
        }
    }
    return inFlight
}

/**
 * How far the rail's fill has resolved from solid panel (`0`) to glass (`1`) — solid until artwork is
 * full-bleed behind the rail, moved on the backdrop's own crossfade duration so the two read as one
 * transition rather than the rail flickering against it. Snapped under reduced motion like the rail's
 * other transitions.
 */
@Composable
private fun railGlassFraction(artworkPresent: Boolean): State<Float> =
    animateFloatAsState(
        targetValue = if (artworkPresent) 1f else 0f,
        animationSpec = if (LocalReduceMotion.current) snap() else tween(TV_IMMERSIVE_CROSSFADE_MILLIS),
        label = "tvNavRailGlass",
    )

/** A placeholder content pane for previews of the rail in isolation. */
@Composable
internal fun TvNavRailContentPlaceholder() {
    Box(
        // Centred in the content region beside the rail, not the full panel the pane spans — this caption is
        // also the baselines' shift-don't-resize probe, so its centre must track the pane's visible half.
        modifier = Modifier
            .fillMaxSize()
            .padding(start = LocalTvContentInset.current)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Content",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
