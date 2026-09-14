package com.binge.designsystem.tv.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.TV_NAV_RAIL_COLLAPSED_ITEMS
import com.binge.designsystem.tv.focus.tvSelectionTarget
import com.binge.designsystem.tv.R as TvR

/**
 * The rail's items between the fixed rail chrome — Account pinned top, then the destinations, then Settings.
 *
 * Both shapes are **one scrolling list at the same scroll position**, so the icons keep their exact place when
 * the rail opens and the collapsed strip reflects wherever the expanded list was scrolled to (the YouTube model:
 * the items stay put; collapsing only hides the labels via [labelsVisible]). [RailSnapBringIntoViewSpec] parks
 * the focused row at the collapsed band's centre, scrolling a whole row at a time, so no row is half-clipped
 * under the pinned Account and the focused destination is always still visible after a collapse.
 *
 * Collapsed, the strip has two modes on Shield-verified YouTube behaviour: while the list's tail is off screen it
 * cuts to a [TV_NAV_RAIL_COLLAPSED_ITEMS]-row band, fades the band's bottom, and re-pins a Settings stand-in to
 * the panel bottom; once the tail — through the terminal Settings row — is on screen, nothing is cut and no
 * stand-in renders, every row simply keeping its place.
 *
 * One `key`-stable set of nodes across both shapes — never a collapsed→expanded subtree swap, which would dispose
 * the row focus just entered on and drop it (the bug behind `TvSettingsFocusArrivalTest`, masked on device by the
 * shell's own focus plumbing). A [FocusRequester] is pinned to the selected row (`tvSelectionTarget`).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ColumnScope.RailItemsRegion(
    header: TvNavRailItem?,
    items: List<TvNavRailItem>,
    footer: TvNavRailItem?,
    selectedKey: Any?,
    expanded: Boolean,
    onSelect: (Any) -> Unit,
    railEntry: FocusRequester,
    scrollState: ScrollState,
    labelsVisible: Boolean = expanded,
) {
    val hPad = dimensionResource(TvR.dimen.tv_nav_rail_padding)
    val overscanV = dimensionResource(TvR.dimen.tv_overscan_vertical)
    val gapDp = dimensionResource(TvR.dimen.tv_nav_rail_item_gap)
    val gap = Arrangement.spacedBy(gapDp)
    val bleed = dimensionResource(TvR.dimen.tv_focus_ring_bleed)
    val itemHeight = dimensionResource(TvR.dimen.tv_nav_rail_item_height)
    // Collapsed the strip shows a fixed [TV_NAV_RAIL_COLLAPSED_ITEMS] rows (Account · these · Settings); a taller
    // panel puts the slack below them, above the pinned Settings.
    val collapsedHeight = itemHeight * TV_NAV_RAIL_COLLAPSED_ITEMS + gapDp * (TV_NAV_RAIL_COLLAPSED_ITEMS - 1) + bleed * 2
    val density = LocalDensity.current
    val gapPx = with(density) { gapDp.toPx() }
    // The bottom rows fade out over one row's height as they reach the Settings edge rather than hard-clipping.
    val fadePx = with(density) { itemHeight.toPx() }
    val snapSpec = remember(gapPx) { RailSnapBringIntoViewSpec(gapPx) }

    val item: @Composable (TvNavRailItem) -> Unit = { railItem ->
        RailItem(
            item = railItem,
            selectedKey = selectedKey,
            expanded = expanded,
            onSelect = onSelect,
            modifier = Modifier.tvSelectionTarget(isSelected = railItem.key == selectedKey, selected = railEntry),
            showLabel = labelsVisible,
        )
    }

    // Account pins top in both states, inside the overscan-safe area — one of the two rows a TV that overscans
    // could otherwise crop.
    Column(modifier = Modifier.padding(start = hPad, top = overscanV, end = hPad), verticalArrangement = gap) {
        header?.let { item(it) }
    }

    var availablePx by remember { mutableIntStateOf(0) }
    var contentPx by remember { mutableIntStateOf(0) }
    // Once the tail — through the terminal Settings row — is on screen, nothing needs to hide on collapse: the rows and Settings
    // keep the places the expanded list showed them, and the pinned Settings (there only while the real row is off-screen) is
    // skipped. Mid-list, the strip cuts to the [TV_NAV_RAIL_COLLAPSED_ITEMS]-row band. `> 0` guards the first unmeasured frame.
    val tailVisible = contentPx > 0 && contentPx - scrollState.value <= availablePx
    Box(modifier = Modifier.weight(1f).onSizeChanged { availablePx = it.height }) {
        CompositionLocalProvider(LocalBringIntoViewSpec provides snapSpec) {
            Column(
                modifier = (
                    if (expanded || tailVisible) {
                        Modifier.fillMaxSize()
                    } else {
                        // Fade the band's bottom so overflow rows dissolve above the pinned Settings instead of
                        // clipping. Off-screen compositing so the `DstIn` gradient erases the content's alpha
                        // rather than painting over it.
                        Modifier.fillMaxWidth().height(collapsedHeight).fadeBottom(fadePx)
                    }
                ).verticalScroll(scrollState)
                    // Placed after `verticalScroll`, so it reports the *content* height, not the viewport's.
                    .onSizeChanged { contentPx = it.height }
                    .padding(start = hPad, end = hPad, top = bleed, bottom = bleed),
                verticalArrangement = gap,
            ) {
                items.forEach { railItem -> key(railItem.key) { item(railItem) } }
                // Settings is the terminal scroll row when expanded or when the tail is on screen; only a
                // mid-list collapse re-pins it to the bottom below.
                if (expanded || tailVisible) footer?.let { key(it.key) { item(it) } }
            }
        }
    }
    // The stand-in gear *rises into* its pinned slot as the rail collapses (the YouTube gear-slide). Entry-only:
    // exit is instant because the expand path is focus-critical — an exit animation would keep this RailItem (and
    // the selected-row FocusRequester it can carry) alive alongside the terminal Settings row it hands over to.
    AnimatedVisibility(
        visible = !expanded && !tailVisible,
        enter = if (LocalReduceMotion.current) EnterTransition.None else slideInVertically { it },
        exit = ExitTransition.None,
    ) {
        Column(modifier = Modifier.padding(start = hPad, end = hPad, bottom = overscanV)) {
            footer?.let { item(it) }
        }
    }
}

/** Fades the bottom [heightPx] of a surface to transparent, so scrolled-off rows dissolve rather than clip. */
private fun Modifier.fadeBottom(heightPx: Float): Modifier =
    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - heightPx,
                    endY = size.height,
                ),
                blendMode = BlendMode.DstIn,
            )
        }

/**
 * Parks the focused row at the collapsed band's **centre slot**, scrolling a whole row per focus move — the
 * YouTube pivot. Because the stride is uniform (`size + gapPx`), each move scrolls exactly one row, so rows
 * scroll fully in and out and no row is half-clipped under the pinned Account. And because the centre slot is
 * inside the [TV_NAV_RAIL_COLLAPSED_ITEMS]-row collapsed band, the focused row is always still visible when the
 * rail collapses. Near the ends the container clamps the scroll, so the first/last rows sit at their natural
 * place. The spec fires on every focus move, so the rail's scroll must be resettable by the reachability
 * harness's arrival (`TvDpadReachability` replays key paths and needs a canonical start) — hence the hoisted
 * scroll state on `BingeTvNavRail`.
 */
@OptIn(ExperimentalFoundationApi::class)
private class RailSnapBringIntoViewSpec(
    private val gapPx: Float,
) : BringIntoViewSpec {
    override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float,
    ): Float {
        val stride = size + gapPx
        if (stride <= 0f) return offset
        val centreSlot = (TV_NAV_RAIL_COLLAPSED_ITEMS - 1) / 2
        return offset - centreSlot * stride
    }
}
