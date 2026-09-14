package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontStyle
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
private const val CINEMATIC_TAGLINE_ALPHA = 0.85f
private const val CINEMATIC_EYEBROW_ALPHA = 0.85f

/**
 * Immersive expanded-width detail header: full-bleed backdrop gradient-blended into the background,
 * and an inline poster beside a copy column (eyebrow → title → [tagline] → [actions]). Sizes to its
 * container width so it composes correctly in a side pane; the height is the fixed cinematic
 * header height. The expanded counterpart to [DetailHero]; the single-column detail keeps using
 * [DetailHero].
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
    tagline: String?,
    backdropUrl: String?,
    posterUrl: String?,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
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
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
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
            tagline = tagline,
            posterUrl = posterUrl,
            actions = actions,
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
    tagline: String?,
    posterUrl: String?,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.detail_cinematic_header_padding),
                end = dimensionResource(R.dimen.detail_cinematic_header_padding),
                bottom = dimensionResource(R.dimen.detail_cinematic_header_bottom_padding),
            ),
        horizontalArrangement =
            Arrangement.spacedBy(dimensionResource(R.dimen.detail_cinematic_poster_copy_spacing)),
        verticalAlignment = Alignment.Bottom,
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
        Column(modifier = Modifier.weight(1f)) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = BingeTheme.colors.onScrim.copy(alpha = CINEMATIC_EYEBROW_ALPHA),
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
            if (!tagline.isNullOrBlank()) {
                Spacer(Modifier.height(dimensionResource(R.dimen.detail_cinematic_copy_spacing)))
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BingeTheme.colors.onScrim.copy(alpha = CINEMATIC_TAGLINE_ALPHA),
                    fontStyle = FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.detail_cinematic_copy_spacing)))
            actions()
        }
    }
}
