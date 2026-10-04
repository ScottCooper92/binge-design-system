@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Top app bars")

package com.binge.designsystem.catalog

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeMediumTopBar
import com.binge.designsystem.component.BingePaneTopBar
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.DarkStatusBarEffect
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailOverlayTopBar
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.IconButtonTone
import com.binge.designsystem.component.LocalTopBarActionTint
import com.binge.designsystem.component.LocalTopBarActionTone
import com.binge.designsystem.component.StatusBarScrim
import com.binge.designsystem.component.rememberPaneTopBarScrollBehavior
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme
import kotlin.math.abs

private const val DEMO_ROW_COUNT = 60
private const val DEMO_TITLE = "Popular Movies"
private const val DEMO_ARTWORK_CELLS = 3

/**
 * Each top bar live over a long list that opens on a band of artwork and runs into banded rows, so a
 * transparent bar has imagery to sit on at rest and both bands pass beneath it as you scroll. The bar
 * is drawn over the list, as the app's screens draw it, and is built from one number the behaviour
 * gives, which is printed at the bottom. That number is `collapsedFraction` for a bar that collapses
 * in place, and `overlappedFraction` for a pinned or enter-always bar, which each demo passes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScrollingBarDemoHost(
    behavior: TopAppBarScrollBehavior,
    fractionName: String = "collapsedFraction",
    fraction: (TopAppBarState) -> Float = { it.collapsedFraction },
    bar: @Composable (fraction: Float) -> Unit,
) {
    ScreenshotTheme {
        Box(Modifier.fillMaxSize().nestedScroll(behavior.nestedScrollConnection)) {
            LazyColumn(Modifier.fillMaxSize()) {
                item { DemoArtwork() }
                items(DEMO_ROW_COUNT) { index -> DemoRow(index) }
            }
            val value = fraction(behavior.state)
            bar(value)
            FractionLabel(fractionName, value, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun DemoArtwork() {
    val scheme = MaterialTheme.colorScheme
    val swatches = listOf(scheme.primaryContainer, scheme.tertiaryContainer, scheme.secondaryContainer)
    Row(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.catalog_demo_artwork_height))) {
        repeat(DEMO_ARTWORK_CELLS) { Box(Modifier.weight(1f).fillMaxSize().background(swatches[it])) }
    }
}

@Composable
private fun DemoRow(index: Int) {
    val band = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    Box(
        modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.catalog_demo_list_row_height)).background(band),
        contentAlignment = Alignment.Center,
    ) {
        Text("Row $index")
    }
}

@Composable
private fun FractionLabel(
    name: String,
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$name = ${"%.2f".format(abs(fraction))}",
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(dimensionResource(R.dimen.padding_s)),
    )
}

/** A search action that takes the bar's tone and tint, so it follows the bar from glass to solid. */
@Composable
private fun SearchAction(glassBackgroundAlpha: Float) {
    ExpressiveIconButton(
        onClick = {},
        icon = Icons.Filled.Search,
        contentDescription = null,
        tint = LocalTopBarActionTint.current,
        tone = LocalTopBarActionTone.current,
        size = dimensionResource(R.dimen.top_bar_icon_size),
        glassBackgroundAlpha = glassBackgroundAlpha,
    )
}

/** [BingeTopBar] pinned and opaque, the plain screen bar, with its scrim following what scrolls under it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarDemo() {
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    ScrollingBarDemoHost(behavior, "overlappedFraction", { it.overlappedFraction }) {
        BingeTopBar(title = DEMO_TITLE, onBack = LocalDemoBack.current, scrollBehavior = behavior, actions = { SearchAction(it) })
    }
}

/**
 * [BingeTopBar] with the enter-always behaviour: the bar leaves on a scroll down and returns on a scroll up.
 * Its scrim follows `overlappedFraction`, as [BingeTopBar]'s KDoc says. On this behaviour
 * `collapsedFraction` measures how far the bar has slid off screen, which would clear the scrim exactly
 * when the bar comes back over content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarEnterAlwaysDemo() {
    val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    ScrollingBarDemoHost(behavior, "overlappedFraction", { it.overlappedFraction }) { fraction ->
        BingeTopBar(
            title = DEMO_TITLE,
            onBack = LocalDemoBack.current,
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
            actions = { SearchAction(it) },
        )
    }
}

/** [BingeTopBar] with the exit-until-collapsed behaviour, to compare with enter-always. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarExitUntilCollapsedDemo() {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ScrollingBarDemoHost(behavior) { fraction ->
        BingeTopBar(
            title = DEMO_TITLE,
            onBack = LocalDemoBack.current,
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
            actions = { SearchAction(it) },
        )
    }
}

/**
 * [BingeTopBar]'s hero treatment: transparent, with glass buttons over the artwork and the always-dark
 * scrim fading in as content scrolls under it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarGlassDemo() {
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    ScrollingBarDemoHost(behavior, "overlappedFraction", { it.overlappedFraction }) { fraction ->
        BingeTopBar(
            title = DEMO_TITLE,
            onBack = LocalDemoBack.current,
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
            scrimColor = BingeTheme.colors.scrim,
            scrimForegroundColor = BingeTheme.colors.onScrim,
            actions = { SearchAction(it) },
        )
    }
}

/** [BingeMediumTopBar] collapsing from its large title as the list scrolls, scrim following the collapse. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarDemo() {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ScrollingBarDemoHost(behavior) { fraction ->
        BingeMediumTopBar(
            title = DEMO_TITLE,
            onBack = LocalDemoBack.current,
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
            actions = { SearchAction(it) },
        )
    }
}

/** [BingeMediumTopBar]'s hero treatment: glass buttons over the artwork and the always-dark scrim. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeMediumTopBarGlassDemo() {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    ScrollingBarDemoHost(behavior) { fraction ->
        BingeMediumTopBar(
            title = DEMO_TITLE,
            onBack = LocalDemoBack.current,
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
            scrimColor = BingeTheme.colors.scrim,
            scrimForegroundColor = BingeTheme.colors.onScrim,
            actions = { SearchAction(it) },
        )
    }
}

/** [BingePaneTopBar] with the behaviour it is documented to pair with, [rememberPaneTopBarScrollBehavior]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBarDemo() {
    val behavior = rememberPaneTopBarScrollBehavior()
    ScrollingBarDemoHost(behavior) { fraction ->
        BingePaneTopBar(
            title = DEMO_TITLE,
            onBack = LocalDemoBack.current,
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
        )
    }
}

/**
 * [BingePaneTopBar] as a detail pane beside a list: the two-row form, its large title on the narrow
 * inner-edge inset, collapsing to the small title as the pane scrolls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePaneTopBarDetailPaneDemo() {
    val behavior = rememberPaneTopBarScrollBehavior()
    CompositionLocalProvider(LocalPaneInnerEdge provides PaneEdge.Start) {
        ScrollingBarDemoHost(behavior) { fraction ->
            BingePaneTopBar(
                title = DEMO_TITLE,
                onBack = LocalDemoBack.current,
                scrollBehavior = behavior,
                containerColor = Color.Transparent,
                scrimFraction = fraction,
                actions = { SearchAction(it) },
            )
        }
    }
}

/**
 * [DetailOverlayTopBar] over a real [DetailHero] and the page below it: glass at rest, the wash thinning
 * and the bar's own scrim coming in as the hero scrolls up, then a solid titled bar. The status bar
 * icons stay light over the hero through [DarkStatusBarEffect] and hand off to the theme's once the
 * scrim is past halfway.
 */
@Composable
fun DetailOverlayTopBarDemo() {
    DarkStatusBarEffect()
    val scroll = rememberScrollState()
    ScreenshotTheme {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                DetailHero(
                    title = DEMO_DETAIL_TITLE,
                    backdropUrl = null,
                    tagline = "Why So Serious?",
                    metaText = "",
                    genres = listOf("Action", "Crime", "Drama"),
                    onBack = LocalDemoBack.current,
                    showChrome = false,
                )
                repeat(DEMO_ROW_COUNT) { DemoRow(it) }
            }
            DetailOverlayTopBar(title = DEMO_DETAIL_TITLE, scrollState = scroll, onBack = LocalDemoBack.current) { glassBackgroundAlpha ->
                ShareAction(glassBackgroundAlpha)
            }
            FractionLabel("scrolled", scroll.heroFraction(), Modifier.align(Alignment.BottomCenter))
        }
    }
}

/**
 * [StatusBarScrim] over a page that scrolls under the status bar with light icons held by
 * [DarkStatusBarEffect]: the artwork and then the banded rows pass beneath, and the icons stay on a
 * dark floor throughout. The light theme shows it best, where the rows would otherwise leave white
 * icons on a light page.
 */
@Composable
fun StatusBarScrimDemo() {
    DarkStatusBarEffect()
    ScreenshotTheme {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize()) {
                item { DemoArtwork() }
                items(DEMO_ROW_COUNT) { index -> DemoRow(index) }
            }
            StatusBarScrim()
        }
    }
}

private const val DEMO_DETAIL_TITLE = "The Dark Knight"

/** How far the page has scrolled, as a share of its range, for the printed label. */
private fun ScrollState.heroFraction(): Float = if (maxValue == 0) 0f else value.toFloat() / maxValue

@Composable
private fun ShareAction(glassBackgroundAlpha: Float) {
    ExpressiveIconButton(
        onClick = {},
        icon = Icons.Filled.Share,
        contentDescription = null,
        tint = lerp(BingeTheme.colors.onScrim, MaterialTheme.colorScheme.onBackground, 1f - glassBackgroundAlpha),
        tone = IconButtonTone.Glass,
        size = dimensionResource(R.dimen.top_bar_icon_size),
        glassBackgroundAlpha = glassBackgroundAlpha,
    )
}
