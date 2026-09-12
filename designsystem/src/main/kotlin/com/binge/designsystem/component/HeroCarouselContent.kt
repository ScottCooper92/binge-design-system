package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.formatRating
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.startHorizontalGradient
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

private const val HERO_SIDE_GRADIENT_START_ALPHA = 0.55f
private const val HERO_SIDE_GRADIENT_MID_ALPHA = 0.28f
private const val HERO_SIDE_GRADIENT_MID_STOP = 0.34f
private const val HERO_SIDE_GRADIENT_CLEAR_STOP = 0.7f
private const val HERO_BOTTOM_GRADIENT_MID_ALPHA = 0.35f
private const val HERO_BOTTOM_GRADIENT_END_ALPHA = 0.82f
private const val HERO_BOTTOM_GRADIENT_START_STOP = 0.32f
private const val HERO_BOTTOM_GRADIENT_MID_STOP = 0.7f
private const val HERO_PILL_ALPHA = 0.16f
private const val HERO_TITLE_SHADOW_BLUR = 24f
private const val HERO_TITLE_SHADOW_Y = 4f
private const val HERO_TAGLINE_ALPHA = 0.86f
private const val HERO_META_ALPHA = 0.9f
private const val HERO_META_GENRE_ALPHA = 0.72f
private const val HERO_META_DOT_ALPHA = 0.45f
private const val MINUTES_PER_HOUR = 60
internal const val MAX_META_GENRES = 2

/**
 * Legibility treatment behind the hero copy: a bottom-up vertical scrim plus a gentle side scrim so
 * start-anchored copy holds over bright backdrops. The side ramp is anchored to the layout's start
 * edge, not the screen's left, because the copy it protects is [Alignment.BottomStart] and mirrors
 * under RTL. Both ramps share the [BingeTheme.colors.scrim] tone and top out below full opacity, so
 * the pair reads as one smooth vignette, not two crossing gradients.
 */
@Composable
internal fun BoxScope.HeroScrims() {
    val scrim = BingeTheme.colors.scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    HERO_BOTTOM_GRADIENT_START_STOP to Color.Transparent,
                    HERO_BOTTOM_GRADIENT_MID_STOP to scrim.copy(alpha = HERO_BOTTOM_GRADIENT_MID_ALPHA),
                    1f to scrim.copy(alpha = HERO_BOTTOM_GRADIENT_END_ALPHA),
                ),
            ),
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                startHorizontalGradient(
                    0f to scrim.copy(alpha = HERO_SIDE_GRADIENT_START_ALPHA),
                    HERO_SIDE_GRADIENT_MID_STOP to scrim.copy(alpha = HERO_SIDE_GRADIENT_MID_ALPHA),
                    HERO_SIDE_GRADIENT_CLEAR_STOP to Color.Transparent,
                ),
            ),
    )
}

/**
 * Start-anchored cinematic copy: trending pill, title, optional tagline, meta row, actions slot. The
 * still behind it runs to the panel edge under an overlaying rail; the copy does not, so it takes the
 * nav overlay's start inset on top of its own padding.
 */
@Composable
internal fun BoxScope.HeroCopyOverlay(
    item: HeroItem,
    rank: Int,
    heroActions: @Composable (HeroItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .widthIn(max = dimensionResource(R.dimen.hero_copy_max_width))
            .padding(
                start = dimensionResource(R.dimen.hero_copy_start_padding) + navOverlayStart(),
                end = dimensionResource(R.dimen.hero_metadata_end_padding),
                bottom = dimensionResource(R.dimen.hero_copy_bottom_padding),
            ),
    ) {
        // The backdrop button behind it announces all of this copy (heroContentDescription), so the
        // copy stays visual-only. HeroActions is outside: those are controls, not the hero's name.
        Column(modifier = Modifier.clearAndSetSemantics {}) {
            HeroTrendingPill(rank = rank)
            HeroTitle(title = item.title)
            item.tagline?.let { HeroTagline(it) }
            HeroMetaRow(item = item)
        }
        HeroActions(item = item, heroActions = heroActions)
    }
}

@Composable
private fun HeroTrendingPill(rank: Int) {
    Row(
        modifier = Modifier
            .padding(bottom = dimensionResource(R.dimen.padding_s))
            .clip(BingeShapes.Pill)
            .background(BingeTheme.colors.onScrim.copy(alpha = HERO_PILL_ALPHA))
            .padding(
                horizontal = dimensionResource(R.dimen.hero_pill_padding_h),
                vertical = dimensionResource(R.dimen.hero_pill_padding_v),
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.hero_pill_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = BingeTheme.colors.onScrim,
            modifier = Modifier.size(dimensionResource(R.dimen.hero_pill_icon_size)),
        )
        Text(
            text = stringResource(R.string.hero_trending_today, rank),
            style = MaterialTheme.typography.labelMedium,
            color = BingeTheme.colors.onScrim,
        )
    }
}

@Composable
private fun HeroTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            shadow = Shadow(
                color = BingeTheme.colors.titleShadow,
                offset = Offset(0f, HERO_TITLE_SHADOW_Y),
                blurRadius = HERO_TITLE_SHADOW_BLUR,
            ),
        ),
        color = BingeTheme.colors.onScrim,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun HeroTagline(tagline: String) {
    Text(
        text = tagline,
        style = MaterialTheme.typography.bodyLarge,
        color = BingeTheme.colors.onScrim.copy(alpha = HERO_TAGLINE_ALPHA),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = dimensionResource(R.dimen.hero_tagline_top_spacing)),
    )
}

/** A meta-row entry: [isGenre] entries render dimmer (the trailing genre list), per the mock. */
private data class HeroMetaPart(
    val text: String,
    val isGenre: Boolean,
)

@Composable
private fun HeroMetaRow(item: HeroItem) {
    val runtimeOrSeasons = heroRuntimeOrSeasons(item)
    val parts = buildList {
        item.year?.let { add(HeroMetaPart(it, isGenre = false)) }
        runtimeOrSeasons?.let { add(HeroMetaPart(it, isGenre = false)) }
        if (item.genres.isNotEmpty()) {
            add(HeroMetaPart(item.genres.take(MAX_META_GENRES).joinToString(" · "), isGenre = true))
        }
    }
    if (item.rating == null && parts.isEmpty()) return
    Row(
        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_s)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.hero_meta_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item.rating?.let { HeroRating(it) }
        parts.forEachIndexed { i, part ->
            if (i > 0 || item.rating != null) HeroMetaDot()
            Text(
                text = part.text,
                style = MaterialTheme.typography.labelLarge,
                color = BingeTheme.colors.onScrim.copy(
                    alpha = if (part.isGenre) HERO_META_GENRE_ALPHA else HERO_META_ALPHA,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun heroRuntimeOrSeasons(item: HeroItem): String? =
    when {
        item.runtimeMinutes != null -> stringResource(
            R.string.hero_meta_runtime,
            item.runtimeMinutes / MINUTES_PER_HOUR,
            item.runtimeMinutes % MINUTES_PER_HOUR,
        )
        item.seasons != null ->
            pluralStringResource(R.plurals.hero_meta_seasons, item.seasons, item.seasons)
        else -> null
    }

@Composable
private fun HeroRating(rating: Float) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.hero_pill_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = BingeTheme.colors.ratingStar,
            modifier = Modifier.size(dimensionResource(R.dimen.hero_meta_star_size)),
        )
        Text(
            text = rating.formatRating(),
            style = MaterialTheme.typography.labelLarge,
            color = BingeTheme.colors.onScrim.copy(alpha = HERO_META_ALPHA),
        )
    }
}

@Composable
private fun HeroMetaDot() {
    Box(
        modifier = Modifier
            .size(dimensionResource(R.dimen.hero_meta_dot_size))
            .clip(BingeShapes.Pill)
            .background(BingeTheme.colors.onScrim.copy(alpha = HERO_META_DOT_ALPHA)),
    )
}

@Composable
private fun HeroActions(item: HeroItem, heroActions: @Composable (HeroItem) -> Unit) {
    Row(
        modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_sm)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heroActions(item)
    }
}
