package com.binge.designsystem.template

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.binge.designsystem.component.BingeFilterChipPager
import com.binge.designsystem.component.FilterChipItem

/**
 * A list split by filters: [BingeScreenScaffold] with a row of filter chips under its bar, one page per
 * filter, swiped or tapped between. For requests, issues, users, a library or a list's contents.
 *
 * Until [ready], the chips have nothing to count, so the screen shows [notReady] (loading, or the error the
 * first load hit) and the bar scrims itself. Once ready, the chips' header draws one opaque ground under the
 * bar and the chips together. [search], when given, sits above the chips in that header. Each [page] is handed
 * its filter's index and the padding its list folds into `contentPadding`; pair a page with
 * [rememberPagedPhase] and [PagedPhaseContent] rather than writing the paging `when` again. The filters either
 * side of the selected one stay composed ([beyondViewportPageCount]), so their rows are loading before the
 * swipe reaches them.
 *
 * [scrollBehavior] replaces the one the template picks from [bar], for a caller that owns the bar's state. The
 * default is [BingeScreenScaffold]'s look: a transparent collapsing bar and header with the rows visible behind
 * them, and one scrim over both that fades in as the bar collapses ([scrimFraction] null follows the bar; pass a
 * fraction to drive it yourself). Pass an opaque [headerBackground] and a [scrimFraction] of zero for a header
 * that is a solid ground, and [ScreenBar.Small] for a single-line bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredListScreen(
    title: String,
    filters: List<FilterChipItem>,
    selectedFilter: Int,
    onFilterChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    bar: ScreenBar = ScreenBar.Collapsing,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    headerBackground: Color = Color.Transparent,
    scrimFraction: Float? = null,
    ready: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    search: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    beyondViewportPageCount: Int = 1,
    notReady: @Composable (padding: PaddingValues) -> Unit = { LoadingMessageScreen(Modifier.padding(it)) },
    page: @Composable (index: Int, contentPadding: PaddingValues) -> Unit,
) {
    // Held here, not left to the scaffold, so the header's scrim can follow the bar's own collapse.
    val behavior = scrollBehavior ?: rememberScreenBarScrollBehavior(bar)
    val scrim = scrimFraction ?: screenBarScrim(bar, behavior)
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = onBack,
        bar = bar,
        scrollBehavior = behavior,
        snackbarHostState = snackbarHostState,
        barScrim = !ready,
        actions = actions,
        bottomBar = bottomBar,
    ) { padding ->
        if (!ready) {
            notReady(padding)
        } else {
            BingeFilterChipPager(
                items = filters,
                selectedIndex = selectedFilter,
                onSelectedIndexChange = onFilterChange,
                modifier = Modifier.fillMaxSize().padding(padding.screenOuterPadding()).consumeWindowInsets(padding.screenOuterPadding()),
                // The bar's height joins the pager's header, so the rows reach the top of the window and pass under both.
                header = {
                    Spacer(Modifier.height(padding.calculateTopPadding()))
                    search?.invoke()
                },
                headerBackground = headerBackground,
                scrimFraction = scrim,
                beyondViewportPageCount = beyondViewportPageCount,
            ) { pagePadding, index ->
                page(index, PaddingValues(top = pagePadding.calculateTopPadding(), bottom = padding.calculateBottomPadding()))
            }
        }
    }
}
