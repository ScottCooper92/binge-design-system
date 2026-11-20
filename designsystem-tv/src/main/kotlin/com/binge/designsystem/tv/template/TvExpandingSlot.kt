package com.binge.designsystem.tv.template

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.TvExpandingMeta
import com.binge.designsystem.tv.component.TvSkeletonMetaLine
import com.binge.designsystem.tv.component.TvSkeletonParagraph
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SLOT_CROSSFADE_MILLIS = 200
private const val SCRIM_START = 0.5f
private const val SCRIM_ALPHA = 0.85f
private const val LOGO_MAX_WIDTH = 0.55f
private const val LOGO_MAX_HEIGHT = 0.35f
private const val SYNOPSIS_LINES = 3
private const val PLATE_META_FRACTION = 0.7f
private const val PLATE_LAST_LINE_FRACTION = 0.6f
private const val PROGRESS_TRACK_ALPHA = 0.35f

/** A poster in a row's strip, sized to the row. */
@Composable
internal fun TvExpandingPoster(
    item: TvHubItem,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width, height)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        TvHubPosterImage(item.posterUrl, item.posterKind, item.title)
        item.progress?.let { TvExpandingProgress(it, Modifier.align(Alignment.BottomCenter)) }
    }
}

/**
 * How far into the title the user is, a pill along the bottom of a "Continue watching" poster or slot, inset clear of the
 * rounded corners. [progress] is clamped to 0..1, and reads as a progress bar to a screen reader, as the phone card's does.
 */
@Composable
internal fun TvExpandingProgress(progress: Float, modifier: Modifier = Modifier) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .padding(dimensionResource(TvR.dimen.tv_expanding_progress_inset))
            .fillMaxWidth()
            .height(dimensionResource(DesR.dimen.card_progress_height))
            .clip(BingeShapes.Pill)
            .background(BingeTheme.colors.onScrim.copy(alpha = PROGRESS_TRACK_ALPHA))
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
    ) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
    }
}

/**
 * The focused row's fixed slot: the title's backdrop and name, crossfading as ←/→ change the title. The name is its logo when
 * [extras] has one, its text once [extras] says there is none, and nothing while they load, so a name never flashes ahead of a logo.
 */
@Composable
internal fun TvExpandingSlot(
    item: TvHubItem?,
    extras: (String) -> TvTitleExtras?,
    isFocused: Boolean,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val ring = dimensionResource(TvR.dimen.tv_focus_ring_width)
    Box(
        modifier = modifier
            .size(width, height)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Crossfade(
            targetState = item,
            animationSpec = if (LocalReduceMotion.current) snap() else tween(SLOT_CROSSFADE_MILLIS),
            label = "tvExpandingSlot",
        ) { shown -> if (shown != null) TvExpandingSlotFace(shown, extras(shown.key), width, height) }
        item?.progress?.let { TvExpandingProgress(it, Modifier.align(Alignment.BottomCenter)) }
        if (isFocused) Box(Modifier.fillMaxSize().border(ring, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.medium))
    }
}

@Composable
private fun TvExpandingSlotFace(
    shown: TvHubItem,
    extras: TvTitleExtras?,
    width: Dp,
    height: Dp,
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(
            model = shown.backdropUrl ?: shown.posterUrl,
            contentDescription = shown.title,
            // A poster is portrait, so cropped to the wide slot it is a fragment; with no backdrop it is shown whole.
            contentScale = if (shown.backdropUrl != null) ContentScale.Crop else ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(SCRIM_START to Color.Transparent, 1f to Color.Black.copy(alpha = SCRIM_ALPHA))),
        )
        val barRoom = dimensionResource(DesR.dimen.card_progress_height) + dimensionResource(TvR.dimen.tv_expanding_progress_inset)
        val inset = Modifier
            .align(Alignment.BottomStart)
            .padding(dimensionResource(TvR.dimen.tv_overscan_vertical) / 2)
            // Over a progress bar the name sits just above it rather than on it.
            .then(if (shown.progress != null) Modifier.padding(bottom = barRoom) else Modifier)
        val logo = extras?.logoUrl
        when {
            logo != null -> AsyncImage(
                model = logo,
                contentDescription = shown.title,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomStart,
                modifier = inset.size(width * LOGO_MAX_WIDTH, height * LOGO_MAX_HEIGHT),
            )
            extras == null -> Unit
            else -> Text(
                text = shown.title,
                style = MaterialTheme.typography.headlineSmall,
                color = BingeTheme.colors.onScrim,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = inset,
            )
        }
    }
}

/** What sits under the slot: the meta line held to the slot's width, then a synopsis that is always three lines tall. */
@Composable
internal fun TvExpandingDetails(
    item: TvHubItem,
    certification: String?,
    metaWidth: Dp,
) {
    Column(
        modifier = Modifier
            .padding(start = tvContentGutterStart())
            .width(dimensionResource(TvR.dimen.tv_expanding_row_details_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        TvExpandingMeta(
            genres = item.genres,
            // A series card that opens one episode names it where a title's year would be.
            trailingFacts = listOfNotNull(item.year),
            rating = item.rating,
            certification = certification,
            modifier = Modifier.width(metaWidth),
            failure = item.failure,
        )
        // Three lines whatever the text, so a short synopsis does not pull the rows below up.
        Text(
            text = item.overview.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = SYNOPSIS_LINES,
            maxLines = SYNOPSIS_LINES,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A poster of a row whose titles have not arrived, the footprint [TvExpandingPoster] draws. */
@Composable
internal fun TvExpandingPosterPlate(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(width, height)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** What [TvExpandingDetails] reserves under the slot before there is a title: the meta line and the synopsis's three lines. */
@Composable
internal fun TvExpandingDetailsPlate(metaWidth: Dp) {
    Column(
        modifier = Modifier
            .padding(start = tvContentGutterStart())
            .width(dimensionResource(TvR.dimen.tv_expanding_row_details_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        TvSkeletonMetaLine(PLATE_META_FRACTION, Modifier.width(metaWidth))
        TvSkeletonParagraph(MaterialTheme.typography.bodyMedium, SYNOPSIS_LINES, PLATE_LAST_LINE_FRACTION)
    }
}

/**
 * A row whose titles are still loading: its heading over placeholder posters at the row's resting height, holding its place.
 * A blank [title] is a row that has no name yet, drawn as the heading plate the skeleton uses at [rowIndex].
 */
@Composable
internal fun TvExpandingPendingRow(title: String, rowIndex: Int = 0) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap))) {
        if (title.isBlank()) {
            RowHeadingPlate(MaterialTheme.typography.titleLarge, rowIndex)
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal))
                    .tvLayoutAnchor(TvExpandingAnchors.heading(rowIndex)),
            )
        }
        TvExpandingPosterStrip()
    }
}
