@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingePullToRefresh
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.template.screenListPadding
import kotlinx.coroutines.delay

private const val PULL_ROWS = 30
private const val REFRESH_MILLIS = 1_500L

/** Pull the list past the threshold: it refreshes for a moment and settles, as a read that answers would. */
@Composable
fun BingePullToRefreshDemo() {
    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(REFRESH_MILLIS)
            refreshing = false
        }
    }
    PullToRefreshScreen(refreshing = refreshing, onRefresh = { refreshing = true })
}

/** A small-bar screen of rows under [BingePullToRefresh], its spinner resting below the bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PullToRefreshScreen(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    state: PullToRefreshState = rememberPullToRefreshState(),
) {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(title = "Requests", bar = ScreenBar.Small) { padding ->
            BingePullToRefresh(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                indicatorTopInset = padding.calculateTopPadding(),
                state = state,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding.screenListPadding(),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                ) {
                    items(PULL_ROWS) { index ->
                        ListRow { textModifier ->
                            Text(text = "Request ${index + 1}", style = MaterialTheme.typography.titleMedium, modifier = textModifier)
                        }
                    }
                }
            }
        }
    }
}
