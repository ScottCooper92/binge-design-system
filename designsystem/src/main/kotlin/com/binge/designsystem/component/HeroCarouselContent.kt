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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.binge.designsystem.R
import com.binge.designsystem.formatRating
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

private const val HERO_PILL_ALPHA = 0.16f
private const val HERO_PILL_LIGHT_THEME_ALPHA = 0.88f
private const val HERO_TAGLINE_ALPHA = 0.86f
private const val HERO_META_ALPHA = 0.9f
private const val HERO_META_DOT_ALPHA = 0.45f

/** The smallest the title shrinks to stay on one line, before it wraps at full size instead. */
private val HERO_TITLE_MIN_SIZE = 28.sp
private const val MINUTES_PER_HOUR = 60
internal const val MAX_META_GENRES = 2

/**
 * Where a slide's copy column sits inside the carousel, in px: its top edge and its horizontal
 * extent, so the end edge can be read for either layout direction.
 */
internal data class HeroCopyBounds(
    val top: Float,
    val left: Float,
    val right: Float,
) {
    /** The copy's end edge: its right in a left-to-right layout, its left in a right-to-left one. */
    fun end(layoutDirection: LayoutDirection): Float = if (layoutDirection == LayoutDirection.Rtl) left else right
}

/**
 * The backing behind one slide's copy, in the theme's page colour, so the copy reads in
 * `onBackground` in either theme and the carousel ends on the colour the page starts with.
 *
 * Two layers. The copy fade eases in over [R.dimen.hero_copy_fade_band] above the copy, is 45% at its
 * top and solid at the foot. It is masked horizontally: full behind the copy, falling away over
 * [R.dimen.hero_copy_fade_falloff] past its end edge, so the art beyond the copy stays raw. The seam
 * band then runs the full width at the foot, [R.dimen.hero_seam_band] tall, so the art meets the page
 * cleanly on the far side too, with raw art between it and the copy fade.
 *
 * Drawn inside each slide's backdrop, from that slide's own [bounds], so it crossfades with the art.
 */
@Composable
internal fun BoxScope.HeroCopyHuggingScrim(bounds: HeroCopyBounds) {
    val page = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    val bandPx = with(density) { dimensionResource(R.dimen.hero_copy_fade_band).toPx() }
    val falloffPx = with(density) { dimensionResource(R.dimen.hero_copy_fade_falloff).toPx() }
    val seamPx = with(density) { dimensionResource(R.dimen.hero_seam_band).toPx() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            // The horizontal mask applies to the copy fade only, so it needs its own layer to mask into.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawPageFade(page, copyTop = bounds.top, bandPx = bandPx, ramp = CopyFadeRamp)
                maskPastCopyEnd(copyEnd = bounds.end(layoutDirection), falloffPx = falloffPx)
                drawSeamBand(page, bandPx = seamPx)
            },
    )
}

/**
 * Start-anchored cinematic copy: trending pill, title, optional tagline, meta row, actions slot. The
 * still behind it runs to the panel edge under an overlaying rail; the copy does not, so it takes the
 * nav overlay's start inset on top of its own padding. [R.dimen.hero_copy_max_width] caps the copy
 * itself, inside those insets, so a wide rail inset does not squeeze the title.
 *
 * Reports its bounds through [onCopyBounds], for the slide's [HeroCopyHuggingScrim].
 */
@Composable
internal fun BoxScope.HeroCopyOverlay(
    item: HeroItem,
    rank: Int,
    heroActions: @Composable (HeroItem) -> Unit,
    onCopyBounds: (HeroCopyBounds) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .onGloballyPositioned {
                val position = it.positionInParent()
                onCopyBounds(HeroCopyBounds(top = position.y, left = position.x, right = position.x + it.size.width))
            }.padding(
                start = dimensionResource(R.dimen.hero_copy_start_padding) + navOverlayStart(),
                end = dimensionResource(R.dimen.hero_metadata_end_padding),
                bottom = dimensionResource(R.dimen.hero_copy_bottom_padding),
            ).widthIn(max = dimensionResource(R.dimen.hero_copy_max_width)),
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

/**
 * The rank pill. In a dark theme it is a faint light wash over the art. In a light theme that wash
 * turns a muddy grey over the art, so the pill inverts: a near-solid dark pill with light ink.
 */
@Composable
private fun HeroTrendingPill(rank: Int) {
    val scheme = MaterialTheme.colorScheme
    val lightTheme = !BingeTheme.isDark
    val pillColor = if (lightTheme) {
        scheme.inverseSurface.copy(alpha = HERO_PILL_LIGHT_THEME_ALPHA)
    } else {
        scheme.onBackground.copy(alpha = HERO_PILL_ALPHA)
    }
    val pillInk = if (lightTheme) scheme.inverseOnSurface else scheme.onBackground
    Row(
        modifier = Modifier
            .padding(bottom = dimensionResource(R.dimen.padding_s))
            .clip(BingeShapes.Pill)
            .background(pillColor)
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
            tint = pillInk,
            modifier = Modifier.size(dimensionResource(R.dimen.hero_pill_icon_size)),
        )
        Text(
            text = stringResource(R.string.hero_trending_today, rank),
            style = MaterialTheme.typography.labelMedium,
            color = pillInk,
        )
    }
}

/**
 * The title, in `onBackground` over the page-colour fade, so it needs no shadow. It shrinks a step or
 * two to stay on one line before it wraps.
 */
@Composable
private fun HeroTitle(title: String) {
    val style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
    Text(
        text = title,
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        autoSize = OneLineOrWrapAutoSize(max = style.fontSize, min = HERO_TITLE_MIN_SIZE, step = HeroTitleSizeStep),
    )
}

@Composable
private fun HeroTagline(tagline: String) {
    Text(
        text = tagline,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = HERO_TAGLINE_ALPHA),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = dimensionResource(R.dimen.hero_tagline_top_spacing)),
    )
}

/** A meta-row entry: [isGenre] entries (the trailing genre list) take the accent, as on the detail heroes. */
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
                color = if (part.isGenre) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = HERO_META_ALPHA)
                },
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
            // primary rather than the fixed star amber, which is too pale on a light theme's page.
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(dimensionResource(R.dimen.hero_meta_star_size)),
        )
        Text(
            text = rating.formatRating(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = HERO_META_ALPHA),
        )
    }
}

@Composable
private fun HeroMetaDot() {
    Box(
        modifier = Modifier
            .size(dimensionResource(R.dimen.hero_meta_dot_size))
            .clip(BingeShapes.Pill)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = HERO_META_DOT_ALPHA)),
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
