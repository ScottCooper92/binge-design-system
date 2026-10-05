@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.template.screenListPadding

/*
 * The scaffold's bar in three states, for each bar type. The bar's state is
 * seeded and handed in as the scroll behaviour, which is how a caller that owns the state uses it too.
 */

private const val SCROLLED_UNDER = 0.5f
private const val SCROLLED_ROW = 3
private const val BAR_STATE_ROWS = 30

/** The collapsing bar halfway through its collapse, with rows already passed under it. */
@Composable
fun BingeScreenScaffoldScrolledUnderSample() = BarStateSample(ScreenBar.Collapsing, collapsed = SCROLLED_UNDER, overlapped = SCROLLED_UNDER)

/** The collapsing bar fully collapsed over scrolled rows, as it sits once the body has moved on. */
@Composable
fun BingeScreenScaffoldShownOverContentSample() = BarStateSample(ScreenBar.Collapsing, collapsed = 1f, overlapped = 1f)

/** The collapsing bar with no way back, as on a screen that is the root of its stack. */
@Composable
fun BingeScreenScaffoldNoBackSample() = BarStateSample(ScreenBar.Collapsing, collapsed = 0f, overlapped = 0f, hasBack = false)

/** The small bar halfway through sliding away, with rows already passed under it. */
@Composable
fun BingeScreenScaffoldSmallBarScrolledUnderSample() =
    BarStateSample(ScreenBar.Small, collapsed = SCROLLED_UNDER, overlapped = SCROLLED_UNDER)

/** The small bar shown again over scrolled rows, as it returns when the list scrolls up. */
@Composable
fun BingeScreenScaffoldSmallBarShownOverContentSample() = BarStateSample(ScreenBar.Small, collapsed = 0f, overlapped = 1f)

/** The small bar with no way back. */
@Composable
fun BingeScreenScaffoldSmallBarNoBackSample() = BarStateSample(ScreenBar.Small, collapsed = 0f, overlapped = 0f, hasBack = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarStateSample(
    bar: ScreenBar,
    collapsed: Float,
    overlapped: Float,
    hasBack: Boolean = true,
) {
    // The limit the bar itself works to: the medium bar collapses by the difference of its two heights.
    val travel = if (bar == ScreenBar.Collapsing) {
        TopAppBarDefaults.MediumAppBarExpandedHeight - TopAppBarDefaults.MediumAppBarCollapsedHeight
    } else {
        TopAppBarDefaults.TopAppBarExpandedHeight
    }
    val limit = with(LocalDensity.current) { travel.toPx() }
    val state = rememberTopAppBarState(
        initialHeightOffsetLimit = -limit,
        initialHeightOffset = -limit * collapsed,
        initialContentOffset = -limit * overlapped,
    )
    val behavior: TopAppBarScrollBehavior =
        if (bar == ScreenBar.Collapsing) {
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
        } else {
            TopAppBarDefaults.enterAlwaysScrollBehavior(state)
        }
    val scrolled = overlapped > 0f
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(
            title = "Episodes",
            onBack = if (hasBack) LocalDemoBack.current else null,
            bar = bar,
            scrollBehavior = behavior,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState(initialFirstVisibleItemIndex = if (scrolled) SCROLLED_ROW else 0),
                contentPadding = padding.screenListPadding(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                items(BAR_STATE_ROWS) { index ->
                    ListRow { textModifier ->
                        Text(text = "Row ${index + 1}", style = MaterialTheme.typography.titleMedium, modifier = textModifier)
                    }
                }
            }
        }
    }
}
