package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * [BingeTabRow] wired to a [HorizontalPager], so tapping a tab and swiping a page stay in sync — both move
 * the selection. The caller owns the selection ([selectedIndex] / [onSelectedIndexChange], typically
 * ViewModel state) and supplies each page's content by index. The tab counterpart to [BingeFilterChipPager]:
 * tabs switch between different content, one view per tab, where chips narrow one list. A page uses one or
 * the other, never both.
 *
 * The tabs, and any [header] above them, are **overlaid** on a full-height pager rather than stacked above it,
 * exactly as [BingeFilterChipPager] does: a page's content runs to the top of the window and travels under
 * them, dissolving through a short scrim. That is why [pageContent] is handed a `contentPadding`, which
 * measures the overlay: a lazy page folds it into its own `contentPadding`, and a page that does not scroll (a
 * loading, empty or error state) applies it as padding, or it sits behind the tabs.
 *
 * Window insets are deliberately not applied here, as for the chip pager: a screen nested in a scaffold has
 * already had them handled, and one that owns its window puts them in [header].
 *
 * [beyondViewportPageCount] is how many pages either side of the visible one stay composed; see
 * [BingeFilterChipPager] for what raising it costs.
 */
@Composable
fun BingeTabbedPager(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    headerBackground: Color = MaterialTheme.colorScheme.background,
    scrimFraction: Float = 0f,
    scrimColor: Color = MaterialTheme.colorScheme.background,
    beyondViewportPageCount: Int = 0,
    pageContent: @Composable (contentPadding: PaddingValues, page: Int) -> Unit,
) {
    val pagerState = rememberFilterPagerState(
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
        pageCount = tabs.size,
    )
    OverlaidHeaderContent(
        modifier = modifier,
        headerBackground = headerBackground,
        scrimFraction = scrimFraction,
        scrimColor = scrimColor,
        header = {
            header?.invoke()
            BingeTabRow(
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                onSelect = onSelectedIndexChange,
            )
        },
    ) { contentPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = beyondViewportPageCount,
        ) { page ->
            pageContent(contentPadding, page)
        }
    }
}
