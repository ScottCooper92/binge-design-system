package com.binge.designsystem.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/**
 * Pull down on [content] to re-read it: past the threshold, a release calls [onRefresh]. Phone only; a TV has no pull.
 *
 * [isRefreshing] is the caller's own refreshing state, such as a paged list's refresh in progress, and the spinner shows
 * while it is true. So a screen shows this spinner instead of an in-list refresh indicator for the same read, never both.
 *
 * [state] is M3's own, for a caller that drives or reads the pull itself; most leave it to the default.
 *
 * [indicatorTopInset] is how far down the spinner rests. Under a transparent top bar that the list scrolls beneath, as in
 * `BingeScreenScaffold`, pass the scaffold's top padding so the spinner shows below the bar rather than behind it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorTopInset: Dp = dimensionResource(R.dimen.zero),
    state: PullToRefreshState = rememberPullToRefreshState(),
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = indicatorTopInset),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        content = content,
    )
}
