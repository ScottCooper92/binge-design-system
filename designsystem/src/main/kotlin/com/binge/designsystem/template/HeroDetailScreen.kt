package com.binge.designsystem.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSnackbarHost
import com.binge.designsystem.component.DarkStatusBarEffect
import com.binge.designsystem.component.DetailOverlayTopBar
import com.binge.designsystem.paneBackOrNull

/**
 * A detail page under a hero: the [hero] runs full-bleed to the top of the window, a bar fades in over it as
 * the page scrolls past it, and [content] follows in one scroll. For a title, an episode, a person, a request.
 *
 * [hero] is the header the page uses (`DetailHero`, `DetailCinematicHeader`, or a person's own); [heroHeight]
 * is where the bar's fade starts, and [horizontalInset] lines the bar's controls up with the hero's copy. A
 * [footer] sits below the scroll rather than over it, so the scroll always clears it. It reaches the true
 * edge of the window, so it clears the navigation bar itself, as `FormFooter` does. [inFlight] draws a thin
 * bar under the top bar without reflowing the page. [onBack] is dropped in a pane whose list already offers it.
 * The hero runs full-bleed; [content] centres at [contentMaxWidth] on a wide window.
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
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = darkStatusBar, modifier = modifier, footer = footer) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .then(if (footer == null) Modifier.windowInsetsPadding(pageBottomInset()) else Modifier),
        ) {
            hero()
            Column(
                modifier = Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = contentMaxWidth),
                content = content,
            )
        }
        HeroDetailBar(title, onBack, heroHeight, horizontalInset, inFlight, actions) { scrollState.value.toFloat() }
    }
}

/**
 * [HeroDetailScreen] for a long page that stays lazy: [content] adds items after the [hero] item, and the bar
 * fades on how far the hero has scrolled out of [listState].
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
    hero: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    HeroDetailFrame(snackbarHostState = snackbarHostState, darkStatusBar = darkStatusBar, modifier = modifier, footer = footer) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().then(if (footer == null) Modifier.windowInsetsPadding(pageBottomInset()) else Modifier),
        ) {
            item(key = HERO_ITEM_KEY) { hero() }
            content()
        }
        HeroDetailBar(title, onBack, heroHeight, horizontalInset, inFlight, actions) {
            // Past the hero item the offset is unknown and no longer matters: the bar is fully in.
            if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset.toFloat() else Float.MAX_VALUE
        }
    }
}

private const val HERO_ITEM_KEY = "hero-detail-hero"

/**
 * The outer frame both forms share: no bar of the Scaffold's own, since the overlay bar clears the status bar
 * itself; the page's sides clear a cutout in landscape, and the footer reaches the true edge.
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
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
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
    WindowInsets.safeDrawing.only(if (hasFooter) WindowInsetsSides.Horizontal else WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)

/** The bottom alone, for the scroll: its box has already cleared the sides. */
@Composable
private fun pageBottomInset(): WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
