package com.binge.designsystem.tv.template

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.formatRuntime
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.TvExpandingMeta
import com.binge.designsystem.tv.component.TvHeroTrendingPill
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.nav.tvContentGutterStart
import kotlinx.coroutines.delay
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SLIDE_MILLIS = 8_000L
private const val CROSSFADE_MILLIS = 400
private const val SCRIM_ALPHA = 0.9f
private const val SIDE_SCRIM_END = 0.7f
private const val BOTTOM_SCRIM_START = 0.4f
private const val LOGO_MAX_WIDTH_FRACTION = 0.65f
private const val SYNOPSIS_LINES = 3
private const val EXTRAS_AHEAD = 1

/**
 * The hub's hero as Netflix draws it: a rounded backdrop card, its copy bottom-left over a fade from the start edge, the trending
 * pill bottom-right. ←/→ step through [items] and wrap at both ends, since with no side rail there is nothing else for them to
 * reach; it advances on a timer only while it is not focused.
 *
 * With no [items] it is the plate the hero arrives in: the same focusable card, so focus placed on it stays put when the slides
 * arrive, and OK does nothing until they do.
 */
@Composable
internal fun TvExpandingHero(
    items: List<HeroItem>,
    heroKey: (HeroItem) -> String,
    extras: TvTitleExtrasCache,
    onItemClick: (Int) -> Unit,
    onItemTapped: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    backdropModel: (String?) -> String? = { it },
) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val current = index.coerceIn(0, maxOf(items.lastIndex, 0))
    var focused by remember { mutableStateOf(false) }
    val reduceMotion = LocalReduceMotion.current
    LaunchedEffect(current, focused, items.size) {
        if (focused || items.size <= 1 || reduceMotion) return@LaunchedEffect
        delay(SLIDE_MILLIS)
        index = (current + 1) % items.size
    }
    // This slide's extras and the next one's, so the next logo is in hand before the slide changes.
    LaunchedEffect(current, items.size) {
        if (items.isEmpty()) return@LaunchedEffect
        for (offset in 0..EXTRAS_AHEAD) extras.ensure(items[(current + offset) % items.size].toTvHubItem(heroKey))
    }
    TvExpandingHeroCard(
        hero = items.getOrNull(current),
        extras = { extras[heroKey(it)] },
        isFocused = focused,
        backdropModel = backdropModel,
        modifier = modifier
            .onPreviewKeyEvent { event ->
                if (!focused || event.type != KeyEventType.KeyDown || items.size <= 1) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionRight -> {
                        index = (current + 1) % items.size
                        true
                    }
                    Key.DirectionLeft -> {
                        index = (current - 1 + items.size) % items.size
                        true
                    }
                    else -> false
                }
            }.tvClickable(
                role = Role.Button,
                onFocusChanged = { focused = it },
                onClick = {
                    val hero = items.getOrNull(current) ?: return@tvClickable
                    onItemTapped(current)
                    onItemClick(hero.id)
                },
            ),
    )
}

/** One slide of the hero, stateless: [isFocused] draws its ring, and a change of [hero] crossfades. A null [hero] is the plate. */
@Composable
internal fun TvExpandingHeroCard(
    hero: HeroItem?,
    extras: (HeroItem) -> TvTitleExtras?,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    backdropModel: (String?) -> String? = { it },
) {
    val shape = MaterialTheme.shapes.large
    val ring = dimensionResource(TvR.dimen.tv_focus_ring_width)
    Box(
        modifier = modifier
            .padding(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal))
            .fillMaxWidth()
            .height(dimensionResource(TvR.dimen.tv_hero_height))
            .clip(shape)
            .then(if (hero != null) Modifier.background(MaterialTheme.colorScheme.surfaceVariant) else Modifier),
    ) {
        Crossfade(
            targetState = hero,
            animationSpec = if (LocalReduceMotion.current) snap() else tween(CROSSFADE_MILLIS),
            label = "tvExpandingHero",
        ) { shown -> if (shown == null) TvExpandingHeroPlate() else TvExpandingHeroSlide(shown, extras(shown), backdropModel) }
        if (isFocused) Box(Modifier.fillMaxSize().border(ring, MaterialTheme.colorScheme.onSurface, shape))
    }
}

@Composable
private fun TvExpandingHeroSlide(
    hero: HeroItem,
    extras: TvTitleExtras?,
    backdropModel: (String?) -> String?,
) {
    val inset = dimensionResource(DesR.dimen.padding_l)
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = backdropModel(hero.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = HERO_CROP_ALIGNMENT,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(0f to Color.Black.copy(alpha = SCRIM_ALPHA), SIDE_SCRIM_END to Color.Transparent)),
        )
        // A second fade up from the bottom edge, so the copy reads over a bright backdrop as well as a dark one.
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(BOTTOM_SCRIM_START to Color.Transparent, 1f to Color.Black.copy(alpha = SCRIM_ALPHA))),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(inset)
                .width(dimensionResource(TvR.dimen.tv_expanding_row_details_width)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        ) {
            TvExpandingHeroTitle(hero, extras)
            TvExpandingHeroMeta(hero, extras)
            Text(
                text = extras?.overview.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = BingeTheme.colors.onScrim,
                minLines = SYNOPSIS_LINES,
                maxLines = SYNOPSIS_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
        hero.rank?.let { rank ->
            Box(Modifier.align(Alignment.BottomEnd).padding(inset)) { TvHeroTrendingPill(rank) }
        }
    }
}

/** The title logo; the name once the extras say there is no logo; nothing while they load, so the name never flashes first. */
@Composable
private fun TvExpandingHeroTitle(hero: HeroItem, extras: TvTitleExtras?) {
    val logoHeight = dimensionResource(TvR.dimen.tv_expanding_hero_logo_height)
    val logo = extras?.logoUrl
    when {
        logo != null -> AsyncImage(
            model = logo,
            contentDescription = hero.title,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomStart,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_expanding_row_details_width) * LOGO_MAX_WIDTH_FRACTION, logoHeight),
        )
        extras == null -> Box(Modifier.height(logoHeight))
        else -> Text(
            text = hero.title,
            style = MaterialTheme.typography.displaySmall,
            color = BingeTheme.colors.onScrim,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Genres, then year and runtime or seasons, then the rating and certification badges, as the rows draw them. */
@Composable
private fun TvExpandingHeroMeta(hero: HeroItem, extras: TvTitleExtras?) {
    val length = extras?.runtimeMinutes?.let { formatRuntime(it) }
        ?: extras?.seasons?.let { pluralStringResource(DesR.plurals.hero_meta_seasons, it, it) }
    TvExpandingMeta(
        genres = hero.genres,
        trailingFacts = listOfNotNull(extras?.year, length),
        rating = hero.rating,
        certification = extras?.certification,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun HeroItem.toTvHubItem(heroKey: (HeroItem) -> String) =
    TvHubItem(
        key = heroKey(this),
        posterUrl = null,
        title = title,
        rating = rating,
        backdropUrl = imageUrl,
        genres = genres,
        heroSource = this,
    )

/** Crops the backdrop mostly from the bottom: a backdrop's faces sit high, and the card is far wider than 16:9. */
private val HERO_CROP_ALIGNMENT = BiasAlignment(horizontalBias = 0f, verticalBias = -0.6f)
