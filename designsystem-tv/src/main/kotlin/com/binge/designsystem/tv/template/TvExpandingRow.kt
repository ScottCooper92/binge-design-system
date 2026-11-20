package com.binge.designsystem.tv.template

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.readAs
import com.binge.designsystem.tv.component.tvExpandingStepKeys
import com.binge.designsystem.tv.component.unreadWhen
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvShellArrivalTarget
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.R as TvR

internal const val EXPANDING_POSTER_ASPECT = 2f / 3f
internal const val EXPANDING_BACKDROP_ASPECT = 16f / 9f
private const val ARRIVAL_MILLIS = 180
private const val ARRIVAL_START_SCALE = 0.92f

/** The slot settles in about its start edge, vertically centred. */
private const val SLOT_ORIGIN_Y = 0.5f
private const val LOOP_MIN_ITEMS = 8
private const val EXTRAS_PREFETCH = 2

/**
 * The titles a row steps through. [loops] rows wrap past their end; a row with pages left grows instead, and a short one that has
 * ended stops at its last title, since wrapping it would repeat posters on screen. A row that sets `TvHubRow.loops` false never wraps.
 * A row with a [trailing] See-all tile never wraps either, and has one more step: position [count] is the tile.
 */
internal class TvExpandingTitles(
    val count: Int,
    val at: (Int) -> TvHubItem?,
    val loops: Boolean,
    val trailing: Boolean = false,
) {
    /** The title [position] steps in to: modulo [count] for a looping row, else held within it. */
    fun index(position: Int): Int =
        if (count == 0) {
            0
        } else if (loops) {
            position % count
        } else {
            position.coerceIn(0, count - 1)
        }

    fun canStepRight(position: Int): Boolean = loops || position < count - if (trailing) 0 else 1

    /**
     * Where [position] belongs now that the titles may have changed under it: on the title [key] wherever it moved to, else on the
     * one that took its place (the previous title when it was the last). Unchanged while [key] is still there or not yet known.
     */
    fun follow(position: Int, key: String?): Int {
        if (key == null || count == 0 || isSeeAll(position)) return position
        val at = at(index(position)) ?: return position
        if (at.key == key) return position
        return (0 until count).firstOrNull { at(it)?.key == key } ?: index(position).coerceAtMost(count - 1)
    }

    /** The position the slot and the strip are laid out for: [position], or the last title while it is on the See-all card. */
    fun shownPosition(position: Int): Int = if (isSeeAll(position)) position - 1 else position

    /** Whether [position] is past the last title, on the trailing See-all card. */
    fun isSeeAll(position: Int): Boolean = trailing && count > 0 && position >= count
}

/** The card that closes a row whose full set lives on another screen: its [label], and [onClick] to open that screen. */
internal class TvExpandingSeeAll(
    val label: String,
    val onClick: () -> Unit,
)

/** [row]'s titles, from [pages] once their first page is in, else the hub's own first page. */
internal fun tvExpandingTitles(
    row: TvHubRow<TvHubItem>,
    pages: TvRowPages<TvHubItem>?,
    trailing: Boolean = false,
): TvExpandingTitles {
    val paged = pages?.takeIf { it.count > 0 }
    val count = paged?.count ?: row.items.size
    val loops = !trailing && row.loops && (paged == null || paged.endReached) && count >= LOOP_MIN_ITEMS
    return TvExpandingTitles(count, paged?.at ?: { index -> row.items.getOrNull(index) }, loops, trailing)
}

/**
 * One hub row. Resting it is bare posters; focused (or [heldOpen], as when Back goes to the top bar) it opens a backdrop-sized
 * slot at its start that crossfades to the title ←/→ pick while the posters slide along beneath, with the title's details under it.
 * Its position is how far along the row focus has moved; it survives the row closing, so returning finds the same title.
 *
 * With no titles yet it is the plate they arrive in: the same focusable node, open or resting like any row, with placeholder
 * posters and slot, and OK does nothing until the titles are there.
 *
 * @param seeAll a See-all tile to end the row with. A step right from the last title puts it in the slot, at the slot's size and
 *   with its ring, and OK opens it. The row stays one focus target, so the tile is a position like any title.
 */
@Composable
internal fun TvExpandingRow(
    row: TvHubRow<TvHubItem>,
    pages: TvRowPages<TvHubItem>?,
    heldOpen: Boolean,
    settleIn: Boolean,
    focusable: Boolean,
    extras: TvTitleExtrasCache,
    entryRequester: FocusRequester?,
    onRowFocused: () -> Unit,
    onItemClick: (Int, TvHubItem) -> Unit,
    openHeight: Dp? = null,
    headingAnchor: String? = null,
    seeAll: TvExpandingSeeAll? = null,
    initiallyFocused: Boolean = false,
    onEntryPlaced: () -> Unit = {},
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    var savedPosition by rememberSaveable(row.key) { mutableIntStateOf(0) }
    // The title at the position, so the position follows it when titles leave or re-sort; an index alone would land on another.
    var positionKey by rememberSaveable(row.key) { mutableStateOf<String?>(null) }
    val titles = tvExpandingTitles(row, pages, trailing = seeAll != null)
    val position = titles.follow(savedPosition, positionKey)
    val index = titles.index(position)
    val item = titles.at(index) ?: row.items.firstOrNull()
    val expanded = focused || heldOpen
    // Reaching a plate counts once its titles are there, as the hero's does.
    val loaded = item != null
    SideEffect {
        savedPosition = position
        titles.at(index)?.let { positionKey = it.key }
    }
    val currentOnRowFocused by rememberUpdatedState(onRowFocused)
    LaunchedEffect(loaded) { if (loaded && focused) currentOnRowFocused() }
    TvExpandingExtrasPrefetch(titles, extras, position, expanded)
    // At the See-all card the slot keeps the last title and the strip starts at the card, so both read the title's position.
    val shown = titles.shownPosition(position)
    val strip = rememberLazyListState(initialFirstVisibleItemIndex = if (expanded) shown + 1 else shown)
    TvExpandingStripSync(strip, expanded, focused, shown, openHeight)
    val interaction = Modifier
        .then(entryRequester?.let { Modifier.tvShellArrivalTarget(it).onGloballyPositioned { onEntryPlaced() } } ?: Modifier)
        .focusProperties { canFocus = focusable }
        .tvExpandingStepKeys(
            enabled = focused,
            canStepRight = titles.canStepRight(position),
            canStepLeft = position > 0,
            onStep = { delta ->
                // Read live, not the composed position: two presses can land before the row recomposes.
                val next = titles.follow(savedPosition, positionKey) + delta
                savedPosition = next
                positionKey = titles.at(titles.index(next))?.key
            },
        ).tvClickable(
            role = Role.Button,
            onFocusChanged = {
                focused = it
                if (it && loaded) onRowFocused()
            },
            onClick = {
                if (seeAll != null && titles.isSeeAll(position)) seeAll.onClick() else item?.let { onItemClick(index, it) }
            },
        )
    // The open state's settle, drawn only: arriving from above the slot grows the last few percent and the details fade in;
    // from below the row is simply open. Read in graphicsLayer, so it costs no recomposition or layout.
    val arrival by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (expanded && settleIn && !LocalReduceMotion.current) tween(ARRIVAL_MILLIS) else snap(),
        label = "tvExpandingRowArrival",
    )
    TvExpandingRowBody(
        title = row.title,
        titles = titles,
        position = position,
        item = item,
        extras = extras::get,
        state = TvExpandingRowState(isFocused = focused, expanded = expanded, arrival = { arrival }, openHeight = openHeight),
        strip = strip,
        interaction = interaction,
        headingAnchor = headingAnchor,
        seeAllLabel = seeAll?.label,
    )
}

/**
 * The open title's extras, then the next two, so a press right finds its logo already loaded. Open, not focused: a row held
 * open while focus is in the top bar shows its title too, and would otherwise wait for focus to fetch the logo.
 */
@Composable
private fun TvExpandingExtrasPrefetch(
    titles: TvExpandingTitles,
    extras: TvTitleExtrasCache,
    position: Int,
    expanded: Boolean,
) {
    LaunchedEffect(position, expanded) {
        if (!expanded) return@LaunchedEffect
        for (ahead in 0..EXTRAS_PREFETCH) {
            val aheadIndex = if (titles.loops) (position + ahead) % titles.count else position + ahead
            extras.ensure((if (aheadIndex < titles.count) titles.at(aheadIndex) else null) ?: break)
        }
    }
}

/**
 * What a row is doing, split from the content it draws: focus, whether it is open, and how far through the open settle it is. A page
 * with the screen to itself (a detail tab) draws the open row at [openHeight], taller than the hub can afford to.
 */
internal class TvExpandingRowState(
    val isFocused: Boolean,
    val expanded: Boolean,
    val arrival: () -> Float,
    val openHeight: Dp? = null,
)

/** The row as drawn, stateless: [state] carries focus and openness, [strip] the posters' scroll, [interaction] the key and click wiring. */
@Composable
internal fun TvExpandingRowBody(
    title: String,
    titles: TvExpandingTitles,
    position: Int,
    item: TvHubItem?,
    extras: (String) -> TvTitleExtras?,
    state: TvExpandingRowState,
    strip: LazyListState,
    interaction: Modifier = Modifier,
    headingAnchor: String? = null,
    seeAllLabel: String? = null,
) {
    val atSeeAll = seeAllLabel != null && titles.isSeeAll(position)
    val shown = titles.shownPosition(position)
    val height = tvExpandingRowHeight(state.expanded, state.openHeight)
    val posterWidth = height * EXPANDING_POSTER_ASPECT
    val slotWidth = height * EXPANDING_BACKDROP_ASPECT
    val gap = dimensionResource(TvR.dimen.tv_media_row_card_gap)
    val gutter = tvContentGutterStart()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        // A page that already names the set (a tab) passes no title, and the row draws no heading.
        if (title.isNotEmpty()) {
            Text(
                text = title,
                // The focused row's heading steps down: the slot and its details carry the row now.
                style = if (state.expanded) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(
                        start = gutter,
                        end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                    ).then(if (headingAnchor != null) Modifier.tvLayoutAnchor(headingAnchor) else Modifier),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .then(interaction)
                // On the card the row reads as it, not as the last title the slot keeps; the card's own node is cleared.
                .readAs(seeAllLabel.takeIf { atSeeAll }),
        ) {
            TvExpandingStrip(
                titles = titles,
                strip = strip,
                expanded = state.expanded,
                posterWidth = posterWidth,
                height = height,
                anchoredIndex = (shown + 1).takeIf { state.expanded },
                seeAllFocused = state.isFocused && atSeeAll,
                seeAllLabel = seeAllLabel,
            )
            if (state.expanded) {
                (shown - 1).takeIf { it >= 0 }?.let { titles.at(it % titles.count) }?.let { previous ->
                    TvExpandingPoster(previous, posterWidth, height, Modifier.offset(x = gutter - gap - posterWidth))
                }
                TvExpandingOpenSlot(item, extras, state, slotWidth, height, ringed = state.isFocused && !atSeeAll, readable = !atSeeAll)
            }
        }
        // Composed only while open, so it goes in the frame the row closes rather than lingering beneath it.
        if (state.expanded) TvExpandingOpenDetails(item, extras, state, slotWidth, readable = !atSeeAll)
    }
}

/** The open row's slot: the title stepped to, ringed while the row is on a title; on the See-all card the slot keeps the last title, unringed. */
@Composable
private fun TvExpandingOpenSlot(
    item: TvHubItem?,
    extras: (String) -> TvTitleExtras?,
    state: TvExpandingRowState,
    width: Dp,
    height: Dp,
    ringed: Boolean,
    readable: Boolean,
) {
    val modifier = Modifier
        .unreadWhen(!readable)
        .offset(x = tvContentGutterStart())
        .graphicsLayer {
            val scale = lerp(ARRIVAL_START_SCALE, 1f, state.arrival())
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, SLOT_ORIGIN_Y)
        }.tvLayoutAnchor(TvExpandingAnchors.OPEN_SLOT)
    TvExpandingSlot(item = item, extras = extras, isFocused = ringed, width = width, height = height, modifier = modifier)
}

/** The open title's details; unread while the row is on the See-all card, which the row announces instead. */
@Composable
private fun TvExpandingOpenDetails(
    item: TvHubItem?,
    extras: (String) -> TvTitleExtras?,
    state: TvExpandingRowState,
    width: Dp,
    readable: Boolean,
) {
    Box(Modifier.graphicsLayer { alpha = state.arrival() }.unreadWhen(!readable)) {
        if (item != null) {
            TvExpandingDetails(item, extras(item.key)?.certification, width)
        } else {
            TvExpandingDetailsPlate(width)
        }
    }
}

@Composable
@ReadOnlyComposable
internal fun tvExpandingRowHeight(expanded: Boolean, openHeight: Dp? = null): Dp =
    openHeight?.takeIf { expanded }
        ?: dimensionResource(if (expanded) TvR.dimen.tv_expanding_row_height_focused else TvR.dimen.tv_expanding_row_height)
