package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeMediumTopBar
import com.binge.designsystem.component.BingePaneTopBar
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.rememberPaneTopBarScrollBehavior
import com.binge.designsystem.preview.ScreenshotTheme
import kotlin.math.abs

private const val DEMO_ROW_COUNT = 60
private const val DEMO_TITLE = "Popular Movies"

/**
 * A transparent bar over a long list of banded rows, so what scrolls beneath it is visible through
 * the scrim. The bar is built from the behaviour's `collapsedFraction`, which is also printed, since
 * the scrim, the title colour and the icon backing all follow that one number.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScrollingBarDemoHost(behavior: TopAppBarScrollBehavior, bar: @Composable (collapsedFraction: Float) -> Unit) {
    ScreenshotTheme {
        Box(Modifier.fillMaxSize().nestedScroll(behavior.nestedScrollConnection)) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(DEMO_ROW_COUNT) { index ->
                    val band = if (index % 2 ==
                        0
                    ) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.catalog_demo_list_row_height)).background(band),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Row $index")
                    }
                }
            }
            val collapsedFraction = behavior.state.collapsedFraction
            bar(collapsedFraction)
            Text(
                text = "collapsedFraction = ${"%.2f".format(abs(collapsedFraction))}",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(dimensionResource(R.dimen.padding_s)),
            )
        }
    }
}

/** [BingeTopBar] with the enter-always behaviour: the bar leaves on a scroll down and returns on a scroll up. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeTopBarEnterAlwaysDemo() {
    val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    ScrollingBarDemoHost(behavior) { fraction ->
        BingeTopBar(
            title = DEMO_TITLE,
            onBack = {},
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
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
            onBack = {},
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
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
            onBack = {},
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
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
            onBack = {},
            scrollBehavior = behavior,
            containerColor = Color.Transparent,
            scrimFraction = fraction,
        )
    }
}
