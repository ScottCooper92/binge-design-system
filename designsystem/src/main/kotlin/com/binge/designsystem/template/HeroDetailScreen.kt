package com.binge.designsystem.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSnackbarHost
import com.binge.designsystem.component.DarkStatusBarEffect
import com.binge.designsystem.component.DetailOverlayTopBar
import com.binge.designsystem.paneBackOrNull
import com.binge.designsystem.paneSideInsets

/**
 * A detail page under a hero: the [hero] runs full-bleed to the top of the window, a bar fades in over it as
 * the page scrolls past it, and [content] follows in one scroll. For a title, an episode, a person, a request.
 *
 * [hero] is the header the page uses (`DetailHero`, `DetailCinematicHeader`, or a person's own). The bar's fade
 * starts at the hero's measured height, so a hero that grows at a large font scale fades it on time; [heroHeight]
 * stands in until the hero has been measured. [horizontalInset] lines the bar's controls up with the hero's copy. A
 * [footer] sits below the scroll rather than over it, so the scroll always clears it. It reaches the true
 * edge of the window, so it clears the navigation bar and a side cutout itself, by
 * [com.binge.designsystem.bottomBarInsets], as `FormFooter` does. [inFlight] draws a thin
 * bar under the top bar without reflowing the page. [onBack] is dropped in a pane whose list already offers it.
 * The hero runs full-bleed; [content] centres at [contentMaxWidth] on a wide window. A page whose rails bleed to the
 * edge passes `Dp.Infinity`: [content] then fills the width, and pads each section by [LocalHeroReadingMargin].
 */
@Composable
fun HeroDetailScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
    heroHeight: Dp = dimensionResource(R.dimen.detail_hero_height),
    horizontalInset: Dp = dimensionResource(R.dimen.padding_m),
    contentMaxWidth: Dp = dimensionResource(R.dimen.content_max_width),
    darkStatusBar: Boolean = true,
    inFlight: Boolean = false,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
    hero: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val measured = remember { MeasuredHeroHeight() }
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = darkStatusBar, modifier = modifier, footer = footer) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .then(if (footer == null) Modifier.windowInsetsPadding(pageBottomInset()) else Modifier),
        ) {
            MeasuredHero(measured, hero)
            HeroDetailContent(contentMaxWidth, content)
        }
        HeroDetailBar(title, onBack, measured.height(heroHeight), horizontalInset, inFlight, actions) { scrollState.value.toFloat() }
    }
}

/**
 * The side margin that centres a reading column of `content_max_width` in the width a [HeroDetailScreen] page's
 * content gets, after the window's side insets: zero on a phone. A page that passes `contentMaxWidth = Dp.Infinity`
 * so its rails can bleed pads its other sections by this, rather than measuring the window outside the template,
 * where a side cutout's inset is not yet taken off.
 */
val LocalHeroReadingMargin = compositionLocalOf { 0.dp }

/**
 * [content] at [contentMaxWidth], centred. Unbounded, it fills the width rather than wrapping its widest child, so
 * narrow sections start at the edge instead of centring. Either way it provides [LocalHeroReadingMargin].
 */
@Composable
private fun HeroDetailContent(contentMaxWidth: Dp, content: @Composable ColumnScope.() -> Unit) {
    ProvideHeroReadingMargin(Modifier.fillMaxWidth()) {
        Column(
            modifier =
                if (contentMaxWidth == Dp.Infinity) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = contentMaxWidth)
                },
            content = content,
        )
    }
}

/** Measures the width [content] gets, and provides the [LocalHeroReadingMargin] that centres a reading column in it. */
@Composable
private fun ProvideHeroReadingMargin(modifier: Modifier, content: @Composable () -> Unit) {
    val readingWidth = dimensionResource(R.dimen.content_max_width)
    val zero = dimensionResource(R.dimen.zero)
    BoxWithConstraints(modifier = modifier) {
        val readingMargin = ((maxWidth - readingWidth) / 2).coerceAtLeast(zero)
        CompositionLocalProvider(LocalHeroReadingMargin provides readingMargin, content = content)
    }
}

/**
 * [HeroDetailScreen] for a long page that stays lazy: [content] adds items after the [hero] item, and the bar
 * fades on how far the hero has scrolled out of [listState]. [verticalArrangement], [horizontalAlignment] and
 * [contentPadding] are the list's own, and apply to the hero item too: a spacing between every row, rows
 * capped and centred on a wide window, a gap under the last one. Its items read [LocalHeroReadingMargin] as an
 * eager page's sections do, measured inside the page.
 */
@Composable
fun HeroDetailLazyScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState(),
    heroHeight: Dp = dimensionResource(R.dimen.detail_hero_height),
    horizontalInset: Dp = dimensionResource(R.dimen.padding_m),
    darkStatusBar: Boolean = true,
    inFlight: Boolean = false,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentPadding: PaddingValues = PaddingValues(),
    hero: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val measured = remember { MeasuredHeroHeight() }
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = darkStatusBar, modifier = modifier, footer = footer) {
        ProvideHeroReadingMargin(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().then(if (footer == null) Modifier.windowInsetsPadding(pageBottomInset()) else Modifier),
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                item(key = HERO_ITEM_KEY) { MeasuredHero(measured, hero) }
                content()
            }
        }
        HeroDetailBar(title, onBack, measured.height(heroHeight), horizontalInset, inFlight, actions) {
            // Past the hero item the offset is unknown and no longer matters: the bar is fully in.
            if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset.toFloat() else Float.MAX_VALUE
        }
    }
}

/**
 * The frame a [HeroDetailScreen] page shows before it has a hero: its loading skeleton or its failure, under the
 * same back control at rest, so back works whether or not the load does. [content] draws from the top of the
 * window, as the hero would, and the status bar follows the theme, since no artwork sits under it yet. Pass the
 * page's [snackbarHostState] so a message raised while it loads survives the swap to the loaded page.
 */
@Composable
fun HeroDetailStateScreen(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    horizontalInset: Dp = dimensionResource(R.dimen.padding_m),
    content: @Composable BoxScope.() -> Unit,
) {
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = false, modifier = modifier, footer = null) {
        content()
        HeroDetailBar(
            title = "",
            onBack = onBack,
            heroHeight = dimensionResource(R.dimen.detail_hero_height),
            horizontalInset = horizontalInset,
            inFlight = false,
            actions = {},
        ) { 0f }
    }
}

/** The hero's height as last laid out, in pixels; zero until it has been. */
private class MeasuredHeroHeight {
    var px by mutableIntStateOf(0)

    /** The measured height, or [fallback] before the hero has been laid out. */
    @Composable
    fun height(fallback: Dp): Dp = if (px > 0) with(LocalDensity.current) { px.toDp() } else fallback
}

/** [hero], reporting its laid-out height to [measured]. */
@Composable
private fun MeasuredHero(measured: MeasuredHeroHeight, hero: @Composable () -> Unit) {
    Box(modifier = Modifier.onSizeChanged { measured.px = it.height }) { hero() }
}

private const val HERO_ITEM_KEY = "hero-detail-hero"

/**
 * The outer frame both forms share: no bar of the Scaffold's own, since the overlay bar clears the status bar
 * itself; the page's outer sides clear a cutout in landscape, and the footer reaches the true edge.
 */
@Composable
private fun HeroDetailFrame(
    snackbarHostState: SnackbarHostState,
    darkStatusBar: Boolean,
    modifier: Modifier,
    footer: (@Composable () -> Unit)?,
    page: @Composable BoxScope.() -> Unit,
) {
    if (darkStatusBar) DarkStatusBarEffect()
    Scaffold(
        modifier = modifier,
        snackbarHost = { BingeSnackbarHost(snackbarHostState, Modifier.windowInsetsPadding(snackbarInsets(hasFooter = footer != null))) },
        bottomBar = { footer?.invoke() },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(
                modifier =
                    Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .windowInsetsPadding(paneSideInsets()),
                content = page,
            )
        }
    }
}

@Composable
private fun BoxScope.HeroDetailBar(
    title: String,
    onBack: (() -> Unit)?,
    heroHeight: Dp,
    horizontalInset: Dp,
    inFlight: Boolean,
    actions: @Composable RowScope.(Float) -> Unit,
    scrollOffsetPx: () -> Float,
) {
    DetailOverlayTopBar(
        title = title,
        scrollOffsetPx = scrollOffsetPx,
        onBack = onBack?.let { paneBackOrNull(it) },
        heroHeight = heroHeight,
        horizontalInset = horizontalInset,
        actions = actions,
    )
    if (inFlight) {
        LinearProgressIndicator(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = dimensionResource(R.dimen.person_profile_top_bar_offset)),
        )
    }
}

@Composable
private fun snackbarInsets(hasFooter: Boolean): WindowInsets =
    if (hasFooter) paneSideInsets() else paneSideInsets().union(pageBottomInset())

/** The bottom alone, for the scroll: its box has already cleared the sides. */
@Composable
private fun pageBottomInset(): WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
