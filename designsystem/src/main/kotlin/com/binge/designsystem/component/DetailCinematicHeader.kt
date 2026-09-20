package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.component.HeroBackdropMeshWash
import com.binge.designsystem.component.ImagePlaceholder
import com.binge.designsystem.startHorizontalGradient
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

private const val CINEMATIC_SCRIM_TOP_ALPHA = 0.55f
private const val CINEMATIC_SCRIM_MID_ALPHA = 0.55f
private const val CINEMATIC_SCRIM_BOTTOM_ALPHA = 0.90f
private const val CINEMATIC_SCRIM_CLEAR_STOP = 0.30f
private const val CINEMATIC_SCRIM_MID_STOP = 0.62f
private const val CINEMATIC_SCRIM_DEEP_STOP = 0.88f
private const val CINEMATIC_SIDE_SCRIM_ALPHA = 0.80f
private const val CINEMATIC_SIDE_SCRIM_MID_ALPHA = 0.45f
private const val CINEMATIC_SIDE_SCRIM_MID_STOP = 0.45f
private const val CINEMATIC_SIDE_SCRIM_CLEAR_STOP = 0.85f
private const val CINEMATIC_SYNOPSIS_ALPHA = 0.85f

// The poster (196dp wide, 2:3) runs ~294dp tall against a 460dp header. The copy column now fills
// that same height and pins the facts row to its bottom edge, so this clamp is a safety cap for
// when the facts row is present too - not the full poster-height budget synopsis alone would get.
private const val CINEMATIC_SYNOPSIS_MAX_LINES = 4

/**
 * Immersive expanded-width detail header: full-bleed backdrop gradient-blended into the background,
 * and an inline poster beside a copy column (eyebrow → title → [synopsis] → [stats]). Sizes to its
 * container width so it composes correctly in a side pane; the height is the fixed cinematic header
 * height. The expanded counterpart to [DetailHero]; the single-column detail keeps using
 * [DetailHero].
 *
 * Shows the synopsis rather than a tagline, clamped to [CINEMATIC_SYNOPSIS_MAX_LINES], and the
 * rating/year/runtime/certification facts row beneath it — matching where TV's own hero puts both.
 * The primary actions no longer live here — a caller renders them lower on the page instead, within
 * thumb reach of where the header's fixed height would otherwise have pinned them.
 *
 * Draws no chrome of its own. Back and share come from the pinned [DetailOverlayTopBar] that both
 * detail widths share — a header that carried its own controls scrolled them away with itself and
 * left no way back, and its controls collided with this poster once the status bar inset grew past
 * 46dp (as it does on a large foldable).
 */
@Composable
fun DetailCinematicHeader(
    title: String,
    genres: List<String>,
    synopsis: String?,
    stats: List<DetailStat>,
    backdropUrl: String?,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    // Seeds CinematicSynopsis's overflow state the same way ExpandableOverview's own
    // initiallyOverflowing does, and for the same reason: onTextLayout fires a frame too late for
    // the preview screenshot lane. Preview and test use only.
    synopsisInitiallyOverflowing: Boolean = false,
) {
    val eyebrow = genres.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.detail_cinematic_header_height)),
    ) {
        SubcomposeAsyncImage(
            model = backdropUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            // The copy column below stretches up to the poster's height, reaching well past the
            // backdrop's vertical centre - ImagePlaceholder's default centred icon would land on
            // top of the synopsis. Top-aligned keeps it in the band above the copy row, which the
            // fixed header height and the row's bottom alignment guarantee stays clear of text.
            loading = { ImagePlaceholder(Modifier.fillMaxSize(), iconAlignment = Alignment.TopCenter) },
            error = { ImagePlaceholder(Modifier.fillMaxSize(), iconAlignment = Alignment.TopCenter) },
        )
        HeroBackdropMeshWash(
            accentStart = MaterialTheme.colorScheme.primary,
            accentEnd = BingeTheme.colors.accentPurple,
        )
        CinematicScrim()
        CinematicSideScrim()

        CinematicCopyRow(
            title = title,
            eyebrow = eyebrow,
            synopsis = synopsis,
            stats = stats,
            posterUrl = posterUrl,
            synopsisInitiallyOverflowing = synopsisInitiallyOverflowing,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun CinematicScrim() {
    val scrim = BingeTheme.colors.scrim
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.00f to scrim.copy(alpha = CINEMATIC_SCRIM_TOP_ALPHA),
                    CINEMATIC_SCRIM_CLEAR_STOP to Color.Transparent,
                    CINEMATIC_SCRIM_MID_STOP to scrim.copy(alpha = CINEMATIC_SCRIM_MID_ALPHA),
                    CINEMATIC_SCRIM_DEEP_STOP to scrim.copy(alpha = CINEMATIC_SCRIM_BOTTOM_ALPHA),
                    1.00f to background,
                ),
            ),
    )
}

/**
 * The start-side ramp the poster and copy sit on. [CinematicScrim] runs vertically and is at its
 * clearest around 30% of the header — which is exactly where the poster's top edge and the title
 * begin, so on a bright backdrop they land on the one band that darkens nothing.
 *
 * Horizontal rather than a deeper vertical ramp because the content it protects is start-aligned:
 * this darkens behind it and leaves the end of the backdrop — the half the image is composed
 * around — as visible as before.
 */
@Composable
private fun CinematicSideScrim() {
    val scrim = BingeTheme.colors.scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                startHorizontalGradient(
                    0f to scrim.copy(alpha = CINEMATIC_SIDE_SCRIM_ALPHA),
                    CINEMATIC_SIDE_SCRIM_MID_STOP to scrim.copy(alpha = CINEMATIC_SIDE_SCRIM_MID_ALPHA),
                    CINEMATIC_SIDE_SCRIM_CLEAR_STOP to Color.Transparent,
                ),
            ),
    )
}

@Composable
private fun CinematicCopyRow(
    title: String,
    eyebrow: String?,
    synopsis: String?,
    stats: List<DetailStat>,
    posterUrl: String?,
    synopsisInitiallyOverflowing: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.detail_cinematic_header_padding),
                end = dimensionResource(R.dimen.detail_cinematic_header_padding),
                bottom = dimensionResource(R.dimen.detail_cinematic_header_bottom_padding),
            )
            // Lets the copy column's fillMaxHeight() below latch onto the poster's own height
            // (computed from its fixed width via aspectRatio) rather than an unbounded one.
            .height(IntrinsicSize.Max),
        horizontalArrangement =
            Arrangement.spacedBy(dimensionResource(R.dimen.detail_cinematic_poster_copy_spacing)),
        verticalAlignment = Alignment.Top,
    ) {
        SubcomposeAsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(dimensionResource(R.dimen.detail_cinematic_poster_width))
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(BingeShapes.MediaCard),
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
        )
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    // Matches DetailHero and the TV hero, both of which key genres off the theme's
                    // accent rather than onScrim.
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.detail_meta_spacing)))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = BingeTheme.colors.onScrim,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!synopsis.isNullOrBlank()) {
                Spacer(Modifier.height(dimensionResource(R.dimen.detail_cinematic_copy_spacing)))
                CinematicSynopsis(synopsis, initiallyOverflowing = synopsisInitiallyOverflowing)
            }
            // Absorbs whatever's left, so the facts row below always sits at the poster's bottom
            // edge instead of trailing directly under a short synopsis.
            Spacer(Modifier.weight(1f))
            if (stats.isNotEmpty()) {
                DetailStatRow(
                    stats = stats,
                    valueColor = BingeTheme.colors.onScrim,
                    labelColor = BingeTheme.colors.onScrim.copy(alpha = CINEMATIC_SYNOPSIS_ALPHA),
                )
            }
        }
    }
}

/**
 * The synopsis line inside the cinematic header - clamped to [CINEMATIC_SYNOPSIS_MAX_LINES], with
 * a "Show more" that opens the untruncated text in a sheet. Shown only once the clamp actually
 * cuts something, via the same `onTextLayout`/`hasVisualOverflow` check [ExpandableOverview] uses -
 * the header's fixed height rules out growing the text in place the way that component does.
 *
 * [initiallyOverflowing] seeds that state for the same reason [ExpandableOverview]'s own parameter
 * of that name does: `onTextLayout` fires a frame after the one the preview screenshot lane
 * captures, so without seeding it the "Show more" state is unrenderable there. Preview and test use
 * only - at runtime the layout pass reports the truth.
 */
@Composable
private fun CinematicSynopsis(text: String, initiallyOverflowing: Boolean = false) {
    var overflows by remember(text, initiallyOverflowing) { mutableStateOf(initiallyOverflowing) }
    var showSheet by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = BingeTheme.colors.onScrim.copy(alpha = CINEMATIC_SYNOPSIS_ALPHA),
        maxLines = CINEMATIC_SYNOPSIS_MAX_LINES,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { if (!overflows) overflows = it.hasVisualOverflow },
    )
    if (overflows) {
        TextButton(
            onClick = { showSheet = true },
            contentPadding = PaddingValues(dimensionResource(R.dimen.zero)),
        ) {
            Text(
                text = stringResource(R.string.detail_show_more),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
    if (showSheet) {
        BingeBottomSheet(onDismissRequest = { showSheet = false }) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_l)),
            )
        }
    }
}
