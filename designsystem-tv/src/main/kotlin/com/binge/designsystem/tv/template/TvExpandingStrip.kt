package com.binge.designsystem.tv.template

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.TvImageKind
import com.binge.designsystem.tv.component.TvImagePlaceholder
import com.binge.designsystem.tv.component.TvStripSeeAllCard
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

private const val STRIP_SLIDE_MILLIS = 250

private const val ROW_TAIL_KEY = "tv-expanding-row-tail"

private const val ROW_SEE_ALL_KEY = "tv-expanding-row-see-all"

/** How many of the row's first titles are looked through for the card's fan: more than it shows, so a missing poster has a stand-in. */
private const val SEE_ALL_PEEK_WINDOW = 8

/** Enough placeholder posters to run past the right edge of any panel. */
private const val PLATE_POSTER_COUNT = 12

/** A looping row's strip length: far more presses than anyone makes, so it never visibly ends. */
private const val LOOP_VIRTUAL_COUNT = 100_000

@Composable
internal fun TvExpandingStrip(
    titles: TvExpandingTitles,
    strip: LazyListState,
    expanded: Boolean,
    posterWidth: Dp,
    height: Dp,
    anchoredIndex: Int?,
    seeAllLabel: String? = null,
    seeAllFocused: Boolean = false,
) {
    val gap = dimensionResource(TvR.dimen.tv_media_row_card_gap)
    val gutter = tvContentGutterStart()
    val slotWidth = height * EXPANDING_BACKDROP_ASPECT
    LazyRow(
        state = strip,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxSize()
            // Focused, the strip starts at the slot's edge, so posters slide into the slot rather than under its corners.
            .padding(start = if (expanded) gutter + slotWidth else gutter)
            .clipToBounds(),
        contentPadding = PaddingValues(
            start = if (expanded) gap else 0.dp,
            end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
        ),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        // Until the first title is in hand (none yet, or a count with its first page still paging in) the posters are plates.
        if (titles.at(0) == null) {
            items(PLATE_POSTER_COUNT) { i -> TvExpandingPosterPlate(posterWidth, height, openPosterAnchor(i, anchoredIndex)) }
        } else if (titles.loops) {
            items(LOOP_VIRTUAL_COUNT) { i ->
                titles.at(i % titles.count)?.let { TvExpandingPoster(it, posterWidth, height, openPosterAnchor(i, anchoredIndex)) }
            }
        } else {
            // Unkeyed: a paged list can repeat a title across pages, which a keyed list rejects.
            items(titles.count) { i ->
                titles.at(i)?.let { TvExpandingPoster(it, posterWidth, height, openPosterAnchor(i, anchoredIndex)) }
            }
            if (seeAllLabel != null) {
                item(key = ROW_SEE_ALL_KEY) {
                    TvStripSeeAllCard(
                        label = seeAllLabel,
                        isFocused = seeAllFocused,
                        onClick = {},
                        width = posterWidth,
                        height = height,
                        // The first few loaded titles, so a row still paging in falls back to what it has; blanks are skipped by the fan.
                        peek = (0 until SEE_ALL_PEEK_WINDOW).mapNotNull { titles.at(it) }.seeAllPeek(),
                    )
                }
            }
            // Room for the last posters to slide all the way to the slot; without it the strip stops short at the row's end.
            item(key = ROW_TAIL_KEY) { Spacer(Modifier.fillParentMaxWidth()) }
        }
    }
}

/** Tags the poster at [anchoredIndex], the first beside an open row's slot. */
private fun openPosterAnchor(index: Int, anchoredIndex: Int?): Modifier =
    if (index == anchoredIndex) Modifier.tvLayoutAnchor(TvExpandingAnchors.OPEN_FIRST_POSTER) else Modifier

/**
 * Keeps the poster strip lined up with the row. Resting, it starts at the remembered title; open, one past it, the remembered one
 * being in the slot (on a short row's last title that is the trailing spacer, so the title is not shown twice).
 */
@Composable
internal fun TvExpandingStripSync(
    strip: LazyListState,
    expanded: Boolean,
    focused: Boolean,
    position: Int,
    openHeight: Dp?,
) {
    val reduceMotion = LocalReduceMotion.current
    val target = if (expanded) position + 1 else position
    val stepPx = with(LocalDensity.current) {
        (tvExpandingRowHeight(expanded, openHeight) * EXPANDING_POSTER_ASPECT + dimensionResource(TvR.dimen.tv_media_row_card_gap)).toPx()
    }
    // Opening or closing moves the strip in the same frame, via requestScrollToItem at the next layout; an effect ran a frame
    // late, and on the Shield that frame lasts long enough to show the slot's own title beside it. Plain holders, not state:
    // written every apply, and a state write here would recompose the row each frame.
    val lastExpanded = remember { booleanArrayOf(expanded) }
    val lastTarget = remember { intArrayOf(target) }
    SideEffect {
        val opened = lastExpanded[0] != expanded
        val jumps = !focused || reduceMotion
        // Only on a change: requested on every recomposition, each row's strip re-laid out every frame of an animation.
        if (opened || (jumps && lastTarget[0] != target)) strip.requestScrollToItem(target)
        lastExpanded[0] = expanded
        lastTarget[0] = target
    }
    // Only ←/→ within an open row slides, which is why this keys on the position and not on the strip's target.
    LaunchedEffect(position) {
        if (focused && !reduceMotion) {
            strip.animateScrollBy(value = strip.offsetTo(target, stepPx), animationSpec = tween(STRIP_SLIDE_MILLIS))
        }
    }
}

/** How far [this] strip must scroll for [target] to be its first item, given every item takes [stepPx]. */
private fun LazyListState.offsetTo(target: Int, stepPx: Float): Float =
    (target - firstVisibleItemIndex) * stepPx - firstVisibleItemScrollOffset

private const val SEE_ALL_PEEK = 3

/** The row's own first posters that have artwork, fanned behind a See-all card's label, so the fan is not made of blank plates. */
internal fun List<TvHubItem>.seeAllPeek(): List<@Composable BoxScope.() -> Unit> =
    filter { it.posterUrl != null }.take(SEE_ALL_PEEK).map { title -> { TvHubPosterImage(title.posterUrl, title.posterKind, title.title) } }

/**
 * The artwork, or a flat plate where there is none: `surfaceVariant` rather than a spinner, since a row is a dozen of these at once.
 * Only the error slot, which Coil also routes a null model to, gets the typed placeholder.
 */
@Composable
internal fun TvHubPosterImage(
    posterUrl: String?,
    kind: TvImageKind,
    title: String?,
) {
    SubcomposeAsyncImage(
        model = posterUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        loading = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) },
        error = { TvImagePlaceholder(kind = kind, title = title) },
    )
}
