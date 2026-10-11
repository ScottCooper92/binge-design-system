@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLargeTopBar
import com.binge.designsystem.preview.ScreenshotTheme

/** Large collapsing top bar, opaque on the app background (the explicit `Color.Unspecified`): the title with its summary line. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeLargeTopBarSample() {
    ScreenshotTheme {
        BingeLargeTopBar(
            title = "Requests",
            subtitle = "12 pending, 3 failed",
            onBack = {},
            containerColor = Color.Unspecified,
            actions = { TopBarShareAction(it) },
        )
    }
}

/**
 * The bar with a title, a summary and a behaviour: transparent over the content, at rest, so no scrim yet. This is
 * the gallery chrome by default; see [BingeLargeTopBarDefaultsCollapsedSample] for it scrolled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeLargeTopBarDefaultsSample() {
    ScreenshotTheme {
        TopBarOverContent {
            BingeLargeTopBar(
                title = "Requests",
                subtitle = "12 pending, 3 failed",
                onBack = {},
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                actions = { TopBarShareAction(it) },
            )
        }
    }
}

/** [BingeLargeTopBarDefaultsSample] scrolled past the title: the scrim has faded in with the bar's own collapse. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeLargeTopBarDefaultsCollapsedSample() {
    ScreenshotTheme {
        TopBarOverContent {
            BingeLargeTopBar(
                title = "Requests",
                subtitle = "12 pending, 3 failed",
                onBack = {},
                scrollBehavior = collapsedTopBarScrollBehavior(largeTopBarCollapseLimit()),
                actions = { TopBarShareAction(it) },
            )
        }
    }
}

/** How far the large bar collapses, in pixels: its expanded height down to M3's single-row height, as a negative offset. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun largeTopBarCollapseLimit(): Float {
    val distance = dimensionResource(R.dimen.large_top_bar_expanded_height) - TopAppBarDefaults.TopAppBarExpandedHeight
    return -with(LocalDensity.current) { distance.toPx() }
}
