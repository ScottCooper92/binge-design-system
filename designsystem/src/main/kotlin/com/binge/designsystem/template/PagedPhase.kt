package com.binge.designsystem.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLoadingIndicator

/** What a paged list shows. Phone and TV both read it from [pagedPhase], so they agree. */
@Immutable
sealed interface PagedPhase {
    /** Nothing to show yet, and something is on its way. */
    data object Skeleton : PagedPhase

    /** The list has no rows. */
    data object Empty : PagedPhase

    /** Rows are on screen; [refreshing] and [refreshError] are what the refresh behind them is doing. */
    data class Rows(
        val refreshing: Boolean,
        val refreshError: Throwable?,
    ) : PagedPhase

    /** The first load failed and there is nothing cached to show instead. */
    data class Failed(
        val error: Throwable,
    ) : PagedPhase
}

/**
 * A finished network refresh of one list, as its view model records it: how many rows it wrote, and a
 * [sequence] that tells two refreshes writing the same count apart.
 */
@Immutable
data class PagedRefresh(
    val rowsWritten: Int,
    val sequence: Long,
)

/**
 * The phase for one frame of a paged list. A list with no rows is empty only when nothing is loading on
 * either side and [cacheBehind] is false: a network refresh may have written rows the cache has not shown
 * yet, and without that flag the gap reads as an empty list. The source's own refresh counts as loading.
 */
fun pagedPhase(
    loadState: CombinedLoadStates,
    itemCount: Int,
    cacheBehind: Boolean = false,
): PagedPhase {
    val source = loadState.source.refresh
    val remote = loadState.mediator?.refresh
    val error = (remote as? LoadState.Error ?: source as? LoadState.Error)?.error
    return when {
        itemCount > 0 -> PagedPhase.Rows(refreshing = (remote ?: source) is LoadState.Loading, refreshError = error)
        error != null -> PagedPhase.Failed(error)
        remote is LoadState.Loading || source is LoadState.Loading -> PagedPhase.Skeleton
        cacheBehind -> PagedPhase.Skeleton
        else -> PagedPhase.Empty
    }
}

/**
 * Follows one list from frame to frame to work out [pagedPhase]'s `cacheBehind`. A list with a mediator is
 * behind its cache until a refresh has finished, and after that only while the refresh wrote rows none of
 * which have shown yet: once rows have shown, an empty list is real. A list with no mediator is never behind.
 * So a mediator list must record a [PagedRefresh] even when `initialize()` skips its first refresh, or an
 * empty cache stays on the skeleton.
 *
 * [phase] is called during composition and writes the tracker's fields, so it must stay idempotent for a
 * repeated frame.
 */
class PagedPhaseTracker {
    private var refresh: PagedRefresh? = null
    private var shownSinceRefresh = false

    fun phase(
        loadState: CombinedLoadStates,
        itemCount: Int,
        lastRefresh: PagedRefresh?,
    ): PagedPhase {
        if (lastRefresh != refresh) {
            refresh = lastRefresh
            shownSinceRefresh = false
        }
        if (itemCount > 0) shownSinceRefresh = true
        val cacheBehind =
            loadState.mediator != null &&
                (lastRefresh == null || lastRefresh.rowsWritten > 0 && !shownSinceRefresh)
        return pagedPhase(loadState, itemCount, cacheBehind)
    }
}

/**
 * The phase of the list on screen, from a count and load states rather than `LazyPagingItems`, so the
 * code under test needs no paging collection. [key] is the list's identity; [lastRefresh] is null without a
 * mediator, and with one it must be recorded even for a skipped initial refresh (see [PagedPhaseTracker]).
 */
@Composable
fun rememberPagedPhase(
    key: Any?,
    loadState: CombinedLoadStates,
    itemCount: Int,
    lastRefresh: PagedRefresh? = null,
): PagedPhase {
    val tracker = remember(key) { PagedPhaseTracker() }
    return tracker.phase(loadState, itemCount, lastRefresh)
}

/** One arm per [PagedPhase], so a screen fills slots rather than copying the `when`. */
@Composable
fun PagedPhaseContent(
    phase: PagedPhase,
    skeleton: @Composable () -> Unit,
    empty: @Composable () -> Unit,
    failed: @Composable (error: Throwable) -> Unit,
    rows: @Composable (refreshing: Boolean, refreshError: Throwable?) -> Unit,
) {
    when (phase) {
        PagedPhase.Skeleton -> skeleton()
        PagedPhase.Empty -> empty()
        is PagedPhase.Failed -> failed(phase.error)
        is PagedPhase.Rows -> rows(phase.refreshing, phase.refreshError)
    }
}

/**
 * The footer under a list that is still showing: a spinner while the next page loads, or a tappable line
 * when it failed. The caller owns [retryLabel] and what [onRetry] does, a reconnect for a rejected session.
 */
@Composable
fun PagedAppendFooter(
    state: LoadState,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_m))
    when (state) {
        is LoadState.Loading ->
            Box(modifier = modifier.then(padding), contentAlignment = Alignment.Center) { BingeLoadingIndicator() }
        is LoadState.Error ->
            Text(
                text = retryLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = modifier.clickable(role = Role.Button, onClick = onRetry).then(padding),
            )
        is LoadState.NotLoading -> Unit
    }
}
