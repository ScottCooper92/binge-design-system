package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvSkeletonMetaLine
import com.binge.designsystem.tv.component.TvSkeletonParagraph
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SKELETON_ROW_COUNT = 3

/** Enough posters to run past the right edge of any panel; the strip clips them. */
private const val PLACEHOLDER_POSTER_COUNT = 12
private const val META_LINE_FRACTION = 0.7f
private const val SYNOPSIS_LAST_LINE_FRACTION = 0.6f
private const val SYNOPSIS_LINES = 3

/**
 * Loading placeholder for a TV page of expanding rows with no hero, such as My lists: rows of bare posters laid out where
 * [TvExpandingRowsHub] lays them, so resolving fills the shapes in place instead of moving the page. The first row is drawn
 * open, as the hub holds the entry row open while focus is in the top bar. A hub with a hero loads through
 * [TvExpandingRowsHub]'s `loading`, whose hero plate and rows can take focus.
 *
 * Non-focusable: while it is up the only place focus can be is the top bar.
 */
@Composable
fun TvExpandingHubLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        // Unbounded like the hub's LazyColumn: a bounded Column squeezes the row that crosses the bottom edge,
        // and Compose then centres the too-tall poster over the row's title.
        modifier = modifier.fillMaxSize().clipToBounds().wrapContentHeight(Alignment.Top, unbounded = true),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_row_gap)),
    ) {
        repeat(SKELETON_ROW_COUNT) { index ->
            val anchor = Modifier.tvLayoutAnchor(TvLayoutAnchors.hubRow(index))
            if (index == 0) TvExpandingOpenRowSkeleton(index, anchor) else TvExpandingRowSkeleton(index, anchor)
        }
    }
}

/** One resting row with its heading as a plate, at the heading's gap, the card gap and the row's resting height. */
@Composable
private fun TvExpandingRowSkeleton(rowIndex: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        RowHeadingPlate(MaterialTheme.typography.titleLarge, rowIndex)
        TvExpandingPosterStrip()
    }
}

/**
 * A row drawn as [TvExpandingRow] holds it open: the stepped-down heading, the backdrop-sized slot with the posters beside it
 * at the open height, and the details under it (the meta line, then the synopsis's three lines).
 */
@Composable
private fun TvExpandingOpenRowSkeleton(rowIndex: Int, modifier: Modifier = Modifier) {
    val height = dimensionResource(TvR.dimen.tv_expanding_row_height_focused)
    val slotWidth = height * EXPANDING_BACKDROP_ASPECT
    val gutter = tvContentGutterStart()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        RowHeadingPlate(MaterialTheme.typography.titleMedium, rowIndex)
        Box(Modifier.fillMaxWidth().height(height)) {
            TvExpandingPosterStrip(open = true)
            Box(
                Modifier
                    .offset(x = gutter)
                    .tvLayoutAnchor(TvExpandingAnchors.OPEN_SLOT)
                    .size(slotWidth, height)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
        Column(
            modifier = Modifier.padding(start = gutter).width(dimensionResource(TvR.dimen.tv_expanding_row_details_width)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        ) {
            TvSkeletonMetaLine(META_LINE_FRACTION, Modifier.width(slotWidth))
            TvSkeletonParagraph(MaterialTheme.typography.bodyMedium, SYNOPSIS_LINES, SYNOPSIS_LAST_LINE_FRACTION)
        }
    }
}

/** A row's heading as a plate at the real heading's [style], so it reserves the line the title arrives in. */
@Composable
internal fun RowHeadingPlate(style: TextStyle, rowIndex: Int) {
    // An empty Text at the real heading's style reserves its height, as TvHubRowSkeleton does.
    Text(
        text = "",
        style = style,
        modifier = Modifier
            .padding(start = tvContentGutterStart())
            .tvLayoutAnchor(TvExpandingAnchors.heading(rowIndex))
            .width(dimensionResource(TvR.dimen.tv_hub_skeleton_title_width))
            .clip(BingeShapes.Tag)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/**
 * A row's posters as plates, at [TvExpandingRow]'s resting height, inset and card gap, or with [open] the open height beside the
 * slot's place. The pending row draws it under its real heading, so a row that is still loading holds the slot its titles arrive in.
 */
@Composable
internal fun TvExpandingPosterStrip(modifier: Modifier = Modifier, open: Boolean = false) {
    val height = dimensionResource(if (open) TvR.dimen.tv_expanding_row_height_focused else TvR.dimen.tv_expanding_row_height)
    val width = height * EXPANDING_POSTER_ASPECT
    val gutter = tvContentGutterStart()
    // Open, the strip starts at the slot's edge and its posters at one card gap beyond it, as TvExpandingRowBody lays it out.
    LazyRow(
        modifier = modifier.fillMaxWidth().height(height).padding(start = if (open) gutter + height * EXPANDING_BACKDROP_ASPECT else 0.dp),
        contentPadding = PaddingValues(
            start = if (open) dimensionResource(TvR.dimen.tv_media_row_card_gap) else gutter,
            end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
        userScrollEnabled = false,
    ) {
        items(PLACEHOLDER_POSTER_COUNT) { index ->
            Box(
                Modifier
                    .then(if (open && index == 0) Modifier.tvLayoutAnchor(TvExpandingAnchors.OPEN_FIRST_POSTER) else Modifier)
                    .size(width, height)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}
