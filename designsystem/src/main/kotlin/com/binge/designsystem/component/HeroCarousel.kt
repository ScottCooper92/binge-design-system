package com.binge.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.component.HeroBackdrop
import com.binge.designsystem.component.ImagePlaceholder
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalAssistiveTechnology
import com.binge.designsystem.theme.LocalReduceMotion
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

private const val AUTO_ADVANCE_MS = 6_000L
private const val CROSSFADE_MS = 700
private const val INDICATOR_UNSELECTED_ALPHA = 0.42f
private const val HERO_COMPACT_HEIGHT_FRACTION = 0.6f

/**
 * A featured title in the hub [HeroCarousel]. [year], [runtimeMinutes], [seasons] and [tagline] are
 * optional: the cinematic meta row renders whatever the caller supplies — today the hub mappers pass
 * only rating + genres, so the richer fields are a future data-layer follow-up. [rank] is the title's place
 * in a trending list, drawn and announced as "#N trending today"; leave it null for a list that is not one.
 * [externalScores] sit on a line of their own, last in the meta block, each an icon and its value. [status], when set,
 * ends the rating and genre line: its sentiment-coloured dot and translated label.
 */
data class HeroItem(
    val id: Int,
    val imageUrl: String?,
    val title: String,
    val rating: Float?,
    val genres: List<String> = emptyList(),
    val year: String? = null,
    val runtimeMinutes: Int? = null,
    val seasons: Int? = null,
    val tagline: String? = null,
    val rank: Int? = null,
    val externalScores: List<ExternalScore> = emptyList(),
    val status: MediaCardStatus? = null,
)

/**
 * The hub featured carousel: a full-bleed cinematic hero — one backdrop at a time, crossfading
 * between titles, autoplaying every [AUTO_ADVANCE_MS] (paused while the user drags, and held still under reduced motion or an assistive technology), with
 * left-anchored copy and a dot rail.
 *
 * Crossfade, not a `HorizontalPager`: the mock fades opacity between full-bleed backdrops, which a
 * translating pager can't reproduce — so paging is a [Crossfade] over the focused index plus manual
 * horizontal-drag navigation.
 *
 * The backdrop never shows a placeholder over the previous one. It holds the previous slide until the next slide's image has
 * loaded (or failed, or has none), the neighbours' images load while a slide dwells, and autoplay advances only once the next
 * image is in. The copy and the dots change at once.
 *
 * Height adapts to the window's *height* (see [rememberHeroHeight]): short windows (landscape phone)
 * cap it off the window height so the first content rail peeks; tall windows (portrait phone, tablet)
 * use the full [R.dimen.hero_height].
 *
 * [heroActions] is a caller-filled slot for per-item CTAs (e.g. Play / Add to list); the
 * backdrop itself stays tappable via [onItemClick].
 *
 * [onItemTapped] reports the slide whose art was tapped, which is the slide [onItemClick] routes to. It rides alongside
 * [onItemClick] rather than widening it, for the reason `PosterCarousel.onItemTapped` gives: routing
 * wants the id and reporting wants the position. The paged index lives in here and nowhere else, so
 * a caller could not derive it. Defaults to a no-op so previews compose unchanged.
 *
 * @need feature a few titles in an autoplaying full-bleed carousel
 * @group media
 * @replaces a hand-built HorizontalPager of backdrops
 */
@Composable
fun HeroCarousel(
    items: List<HeroItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onItemTapped: (index: Int) -> Unit = {},
    heroActions: @Composable (HeroItem) -> Unit = {},
) {
    val heroHeight = rememberHeroHeight()
    if (items.isEmpty()) {
        ImagePlaceholder(
            modifier
                .fillMaxWidth()
                .height(heroHeight),
        )
        return
    }

    val count = items.size
    var index by rememberSaveable { mutableIntStateOf(0) }
    var dragging by remember { mutableStateOf(false) }
    val current = index.coerceIn(0, count - 1)

    // The backdrop holds the previous slide until the wanted one's image is in. Without that, the incoming slide's
    // placeholder fades in over the outgoing backdrop for as long as the image takes, which on a first, cold load is
    // what the user watches. The copy and the dots follow `current`, so they never wait.
    val loader = rememberHeroBackdropLoader()
    val backdropSize = with(LocalDensity.current) {
        IntSize(LocalWindowInfo.current.containerSize.width, heroHeight.roundToPx())
    }
    var shown by remember { mutableIntStateOf(current) }
    val shownIndex = shown.coerceIn(0, count - 1)
    // Which images the carousel has already waited for. A slide whose image is in this set arrives loaded, so it draws no plate
    // while it settles; any other (the first slide, or a slide a refreshed list gave a new image) shows the plate until it loads.
    val readiness = remember { HeroBackdropReadiness() }
    LaunchedEffect(current, items) {
        if (shown != current) {
            loader.loadAndMark(readiness, items[current].imageUrl, backdropSize)
            shown = current
        }
    }
    // The neighbours load while the current slide dwells, so an advance or a swipe finds its image in the cache.
    LaunchedEffect(current, items) {
        if (count > 1) {
            loader.loadAndMark(readiness, items[(current + 1) % count].imageUrl, backdropSize)
            loader.loadAndMark(readiness, items[(current - 1 + count) % count].imageUrl, backdropSize)
        }
    }

    // Autoplay is a motion effect, so reduce-motion holds on the current slide (manual paging only). So does an assistive technology, which
    // is reading the page to a user who has not moved it: it changes under them, and with it the description they are on.
    val reduceMotion = LocalReduceMotion.current
    val assistiveTechnology = LocalAssistiveTechnology.current

    // Keyed on `current` so every index change — swipe, dot tap, or auto-advance — restarts the delay,
    // giving each visible slide a full AUTO_ADVANCE_MS. Paused (returns early) while dragging.
    LaunchedEffect(count, dragging, current, reduceMotion, assistiveTechnology) {
        if (count <= 1 || dragging || reduceMotion || assistiveTechnology) return@LaunchedEffect
        delay(AUTO_ADVANCE_MS)
        // The next slide's art first, so the title, the dots and the backdrop change together.
        val next = (index + 1) % count
        loader.loadAndMark(readiness, items[next.coerceIn(0, count - 1)].imageUrl, backdropSize)
        index = next
    }

    val dragThreshold = dimensionResource(R.dimen.hero_drag_threshold)
    val dragThresholdPx = with(LocalDensity.current) { dragThreshold.toPx() }
    // The gesture-detector coroutine below is keyed only on `count` and so outlives many
    // recompositions; read `current` through rememberUpdatedState, or a swipe after the first would
    // compute the new page against the stale value it captured when the coroutine first launched —
    // the same failure BingeFilterChipPager's pager-settle effect guards against.
    val latestCurrent by rememberUpdatedState(current)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight)
            .pointerInput(count) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        if (count > 1 && totalDrag.absoluteValue > dragThresholdPx) {
                            index = if (totalDrag < 0) {
                                (latestCurrent + 1) % count
                            } else {
                                (latestCurrent - 1 + count) % count
                            }
                        }
                        totalDrag = 0f
                        dragging = false
                    },
                    onDragCancel = {
                        totalDrag = 0f
                        dragging = false
                    },
                    onHorizontalDrag = { _, delta -> totalDrag += delta },
                )
            },
    ) {
        // Each slide's copy bounds, by item id, as its copy last laid out. Each backdrop draws its own
        // fade from them, so the fade changes shape inside the crossfade along with the art, rather
        // than jumping once the new copy has measured.
        val copyBounds = remember { mutableStateMapOf<Int, HeroCopyBounds>() }
        HeroBackdrop(
            items = items,
            index = shownIndex,
            readiness = readiness,
            // The slide the tap landed on: while the backdrop holds the previous art, that is not `current`, and the id
            // routed and the position reported must name the same slide.
            onItemClick = { page ->
                onItemTapped(page)
                onItemClick(items[page].id)
            },
            reduceMotion = reduceMotion,
            copyBounds = copyBounds,
        )
        val currentItem = items[current]
        HeroCopyOverlay(
            item = currentItem,
            heroActions = heroActions,
            onCopyBounds = { copyBounds[currentItem.id] = it },
        )
        if (count > 1) {
            HeroDots(items = items, selectedIndex = current, onSelect = { index = it })
        }
    }
}

@Composable
private fun BoxScope.HeroBackdrop(
    items: List<HeroItem>,
    index: Int,
    readiness: HeroBackdropReadiness,
    onItemClick: (Int) -> Unit,
    reduceMotion: Boolean,
    copyBounds: Map<Int, HeroCopyBounds>,
) {
    // Instant swap (0ms) under reduce-motion; the 700ms fade is a motion effect the a11y pref opts out of.
    val crossfadeMs = if (reduceMotion) 0 else CROSSFADE_MS
    Crossfade(
        targetState = index,
        animationSpec = tween(durationMillis = crossfadeMs, easing = LinearEasing),
        modifier = Modifier.fillMaxSize(),
        label = "hero-backdrop",
    ) { page ->
        val item = items[page]
        val description = heroContentDescription(item)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(role = Role.Button) { onItemClick(page) }
                .semantics { contentDescription = description },
        ) {
            SubcomposeAsyncImage(
                model = item.imageUrl,
                // The button above carries the name; the backdrop is decoration inside it. A slotted
                // SubcomposeAsyncImage keeps its own description off the merged node anyway.
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                // A slide the carousel waited for arrives loaded, and while one is still loading the previous backdrop is what is
                // held, so a plate there would be the flash this avoids. One it never waited for (the first, or a refreshed list's
                // new image) shows the plate until its image arrives.
                loading = if (readiness.needsPlaceholder(item.imageUrl)) {
                    { ImagePlaceholder(Modifier.fillMaxSize()) }
                } else {
                    null
                },
                error = { ImagePlaceholder(Modifier.fillMaxSize()) },
            )
            copyBounds[item.id]?.let { HeroCopyHuggingScrim(it) }
        }
    }
}

/**
 * The slide picker, drawn as dots and announced as what it actually is: a tab strip.
 *
 * `Role.Tab` inside a [selectableGroup] rather than five `Role.Button`s, because the position and the
 * current slide then come from the framework's collection info instead of a hand-written string — and
 * because they were previously carried in **pixels alone**: `selected` drove only width and alpha, so a
 * screen reader heard "button, button, button" with nothing saying which one was current.
 *
 * Each dot is named for the title it goes to. The backdrop's own description cannot stand in: it names
 * the featured *title* including any trending rank, which belongs to the item rather than to the carousel's
 * position.
 */
@Composable
private fun BoxScope.HeroDots(
    items: List<HeroItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    // onBackground, as the copy is: the dots sit on the solid foot of the fade, not on the art.
    val dotColor = MaterialTheme.colorScheme.onBackground
    val selectedWidth = dimensionResource(R.dimen.hero_indicator_size_selected)
    val unselectedWidth = dimensionResource(R.dimen.hero_indicator_size_unselected)
    val height = dimensionResource(R.dimen.hero_indicator_height)
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = dimensionResource(R.dimen.hero_dots_bottom_padding))
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.hero_indicator_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, item ->
            val selected = i == selectedIndex
            val label = stringResource(R.string.cd_hero_slide, item.title)
            Box(
                modifier = Modifier
                    .width(if (selected) selectedWidth else unselectedWidth)
                    .height(height)
                    .clip(BingeShapes.Pill)
                    .background(if (selected) dotColor else dotColor.copy(alpha = INDICATOR_UNSELECTED_ALPHA))
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelect(i) },
                    ).semantics { contentDescription = label },
            )
        }
    }
}

/**
 * Hero height resolved off the window's height tier. Short windows (landscape phone, via
 * [R.bool.binge_hero_height_compact]) cap to `min(60% of the window height, hero_height_compact_cap)`
 * so the hero never fills a short viewport and the first rail peeks; tall windows use the full
 * [R.dimen.hero_height] (lifted on tablets by the values-sw600dp qualifier).
 */
@Composable
private fun rememberHeroHeight(): Dp {
    val fullHeight = dimensionResource(R.dimen.hero_height)
    if (!booleanResource(R.bool.binge_hero_height_compact)) return fullHeight
    val windowHeight = LocalConfiguration.current.screenHeightDp.dp
    val cap = dimensionResource(R.dimen.hero_height_compact_cap)
    return minOf(windowHeight * HERO_COMPACT_HEIGHT_FRACTION, cap)
}

@Suppress("UnusedPrivateProperty")
private val previewHeroItems = listOf(
    HeroItem(
        id = 1,
        imageUrl = null,
        title = "Inception",
        rating = 8.8f,
        genres = listOf("Sci-Fi", "Action"),
        year = "2010",
        runtimeMinutes = 148,
        tagline = "Your mind is the scene of the crime.",
        rank = 1,
    ),
    HeroItem(id = 2, imageUrl = null, title = "The Dark Knight", rating = 9.0f, genres = listOf("Action", "Crime"), rank = 2),
    HeroItem(id = 3, imageUrl = null, title = "Interstellar", rating = 8.6f, genres = listOf("Sci-Fi", "Drama"), rank = 3),
)

@Preview(showBackground = true)
@Composable
private fun PreviewHeroCarousel() {
    BingeExpressiveTheme(dynamicColor = false) {
        HeroCarousel(items = previewHeroItems, onItemClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHeroCarouselEmpty() {
    BingeExpressiveTheme(dynamicColor = false) {
        HeroCarousel(items = emptyList(), onItemClick = {})
    }
}
