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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.R
import com.binge.designsystem.component.BingePullToRefresh
import com.binge.designsystem.component.BingeSnackbarHost
import com.binge.designsystem.component.DarkStatusBarEffect
import com.binge.designsystem.component.DetailOverlayTopBar
import com.binge.designsystem.component.MeasuredHeroHeight
import com.binge.designsystem.component.measuredHeroHeight
import com.binge.designsystem.component.rememberMeasuredHeroHeight
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
 * [com.binge.designsystem.bottomBarInsets], as [ScreenFooter] does. [inFlight] draws a thin
 * bar under the top bar without reflowing the page. A non-null [onRefresh] lets the page be pulled down to reload, with
 * [isRefreshing] showing the spinner under the bar, or in it with [pullIndicator] for a page with no image there; `null`, the default, adds no pull. [onBack] is dropped in a pane whose list already offers it.
 * The hero runs full-bleed; [content] centres at [contentMaxWidth] on a wide window. A page whose rails bleed to the
 * edge passes `Dp.Infinity`: [content] then fills the width, and pads each section by [LocalHeroReadingMargin].
 *
 * @need build a detail page under a full-bleed hero
 * @group screens
 * @replaces a hand-built scaffold with a hero and fading bar
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    pullState: PullToRefreshState? = null,
    pullIndicator: HeroPullIndicator = HeroPullIndicator.BelowBar,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
    hero: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val measured = rememberMeasuredHeroHeight()
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = darkStatusBar, modifier = modifier, footer = footer) {
        HeroDetailPull(onRefresh, isRefreshing, pullState, pullIndicator) {
            // The page's content is at least as tall as the window, so the page can always scroll the whole height of the hero and the
            // bar's hand-over (its surface and its title) can always finish. A page with little to show would otherwise stop part way,
            // leaving the bar's title half in over the content with no surface behind it.
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val viewportHeight = maxHeight
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .then(if (footer == null) Modifier.windowInsetsPadding(pageBottomInset()) else Modifier),
                ) {
                    MeasuredHero(measured, hero)
                    HeroDetailContent(contentMaxWidth, minHeight = viewportHeight, content)
                }
            }
        }
        HeroDetailBar(title, onBack, measured.height(heroHeight), horizontalInset, inFlight, actions) { scrollState.value.toFloat() }
    }
}

/**
 * Where a hero page's resting pull spinner sits. [BelowBar], the default, is under the overlay bar, over the hero, which is
 * where an image or a title card has room for it. [InBar] is for a page with no image under the bar, a person's, whose first
 * line of copy starts where the spinner would rest: the spinner sits in the bar's own row, clear of the copy and of the back
 * control at the start.
 */
enum class HeroPullIndicator { BelowBar, InBar }

/**
 * The side margin that centres a reading column of `content_max_width` in the width a [HeroDetailScreen] page's
 * content gets, after the window's side insets: zero on a phone. A page that passes `contentMaxWidth = Dp.Infinity`
 * so its rails can bleed pads its other sections by this, rather than measuring the window outside the template,
 * where a side cutout's inset is not yet taken off.
 */
val LocalHeroReadingMargin = compositionLocalOf { 0.dp }

/**
 * [content] at [contentMaxWidth], centred, in a block at least [minHeight] tall. Unbounded, it fills the width rather than wrapping its widest child, so
 * narrow sections start at the edge instead of centring. Either way it provides [LocalHeroReadingMargin].
 */
@Composable
private fun HeroDetailContent(
    contentMaxWidth: Dp,
    minHeight: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvideHeroReadingMargin(Modifier.fillMaxWidth().heightIn(min = minHeight)) {
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
 *
 * @need build a long lazy detail page under a hero
 * @group screens
 * @replaces a hand-built LazyColumn page with a hero item and fading bar
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    pullState: PullToRefreshState? = null,
    pullIndicator: HeroPullIndicator = HeroPullIndicator.BelowBar,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentPadding: PaddingValues = PaddingValues(),
    hero: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val measured = rememberMeasuredHeroHeight()
    val fill = rememberShortPageFill(listState)
    val bottomInset = if (footer == null) Modifier.windowInsetsPadding(pageBottomInset()) else Modifier
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = darkStatusBar, modifier = modifier, footer = footer) {
        HeroDetailPull(onRefresh, isRefreshing, pullState, pullIndicator) {
            ProvideHeroReadingMargin(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().then(bottomInset),
                    contentPadding = contentPadding,
                    verticalArrangement = verticalArrangement,
                    horizontalAlignment = horizontalAlignment,
                ) {
                    item(key = HERO_ITEM_KEY) { MeasuredHero(measured, hero) }
                    content()
                    item(key = FILL_ITEM_KEY) { Spacer(Modifier.height(with(LocalDensity.current) { fill.value.toDp() })) }
                }
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
 *
 * @need show the loading or failed state of a hero detail page
 * @group feedback
 * @replaces a hand-built loading or error frame with a back control
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

/**
 * [page] as is, or, when [onRefresh] is set, inside a pull to refresh whose spinner rests below the overlay bar, so it is
 * neither behind the bar nor over the hero's copy, or in the bar's row for [HeroPullIndicator.InBar]. The bar stays outside, over the pulled page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.HeroDetailPull(
    onRefresh: (() -> Unit)?,
    isRefreshing: Boolean,
    pullState: PullToRefreshState?,
    placement: HeroPullIndicator,
    page: @Composable BoxScope.() -> Unit,
) {
    if (onRefresh == null) {
        page()
        return
    }
    val statusTop = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Top)
        .asPaddingValues()
        .calculateTopPadding()
    val indicatorTopInset =
        when (placement) {
            HeroPullIndicator.BelowBar -> statusTop + dimensionResource(R.dimen.person_profile_top_bar_offset)
            // The spinner rests this far below its inset, so lifting by it puts the spinner in the bar's own row. The inset
            // goes negative under a status bar shorter than the lift, which is why the pull's inset is an offset, not padding.
            HeroPullIndicator.InBar -> statusTop - dimensionResource(R.dimen.hero_pull_in_bar_lift)
        }
    BingePullToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
        indicatorTopInset = indicatorTopInset,
        state = pullState ?: rememberPullToRefreshState(),
        content = page,
    )
}

/** [hero], reporting its laid-out height to [measured]. */
@Composable
private fun MeasuredHero(measured: MeasuredHeroHeight, hero: @Composable () -> Unit) {
    Box(modifier = Modifier.measuredHeroHeight(measured)) { hero() }
}

private const val HERO_ITEM_KEY = "hero-detail-hero"
private const val FILL_ITEM_KEY = "hero-detail-fill"

/**
 * The height, in px, of the spacer item that ends a lazy hero page, as the eager page's `heightIn(min = viewport)` does: the items under
 * the hero (the list's items less the hero and the spacer) are kept at least a viewport tall, so a short page can scroll the whole
 * hero away and the bar's hand-over finishes. It is the viewport less those items' laid-out extent, from the first of them to the
 * last, which takes in the spacing between them. The hero is left out of the sum, so it leaving the visible items on a scroll does
 * not change the size; the spacer is left out, so it does not feed back into its own height.
 *
 * Only an item below the viewport makes the page long, and then the spacer is zero. An item that has scrolled off the top does not:
 * at the end of a short page the first items can sit above the viewport (the fill puts the end of the content at the viewport's
 * bottom, so a small first item is pushed up past the spacing and the bottom padding). The size is then kept as it was, since it
 * was already in effect, rather than worked out again from the items still visible, which would shrink it and snap the list back.
 */
@Composable
private fun rememberShortPageFill(listState: LazyListState): State<Int> =
    remember(listState) {
        var last = 0
        derivedStateOf {
            val info = listState.layoutInfo
            val spacerIndex = info.totalItemsCount - 1
            val under = info.visibleItemsInfo.filter { it.index in 1 until spacerIndex }
            val first = under.minOfOrNull { it.index }
            last =
                when {
                    spacerIndex < 1 -> last
                    spacerIndex == 1 -> info.viewportSize.height
                    first == null || first > 1 -> last
                    under.none { it.index == spacerIndex - 1 } -> 0
                    else -> (info.viewportSize.height - (under.maxOf { it.offset + it.size } - under.minOf { it.offset })).coerceAtLeast(0)
                }
            last
        }
    }

/**
 * The outer frame both forms share: no bar of the Scaffold's own, since the overlay bar clears the status bar
 * itself; the page's outer sides clear a cutout in landscape, and the footer reaches the true edge.
 *
 * The overlay bar over the hero, rather than [BingeScreenScaffold]'s transparent medium bar, is deliberate: a
 * hero page wants its artwork to reach the top edge and its bar to fade in over it as the page scrolls.
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
        // Only the footer's height: the page owns its top and sides, so a bar added later cannot push the hero down.
        Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
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
