package com.binge.designsystem.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Asks for the next page when a lazy grid or list scrolls near its end: the one watch behind [PagedGridScreen], [SearchScreen] and the credit rows of
 * [PersonDetailScreen], so the three ask by the same rule.
 *
 * It asks when the last visible item comes within [ahead] of the end ([isNearEnd]), once for each time it gets there, and not at all while there is nothing
 * more ([hasMore] false), while a page is in flight ([appending]) or after one failed ([appendFailed]; Retry asks then). The watch is keyed on those flags and on
 * [itemCount] as well as on [key], the scroll state: a page that added nothing leaves the count as it was, and only the flags' fall from appending back to idle
 * restarts the watch, so the list asks again while the app says there is more. [lastVisibleIndex] and [totalCount] are read from the layout info.
 */
@Composable
internal fun LoadMoreEffect(
    key: Any,
    itemCount: Int,
    hasMore: Boolean,
    appending: Boolean,
    appendFailed: Boolean,
    ahead: Int,
    lastVisibleIndex: () -> Int,
    totalCount: () -> Int,
    onLoadMore: () -> Unit,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val nearEnd by remember(key) { derivedStateOf { isNearEnd(lastVisibleIndex(), totalCount(), ahead) } }
    LaunchedEffect(key, hasMore, itemCount, appending, appendFailed) {
        if (hasMore && !appending && !appendFailed) {
            snapshotFlow { nearEnd }.distinctUntilChanged().filter { it }.collect { currentOnLoadMore() }
        }
    }
}
