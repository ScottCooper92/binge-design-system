package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.resolvedContentInset

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
 * The header is **transparent**, with no band of its own, and a translucent scrim fades in behind it once the page that is shown has scrolled off its
 * top, so content passes under the tabs through the scrim and never under an opaque band. [pageScrolled] is how the pager learns that: it is asked
 * whether page `page` is scrolled off its top, which a caller answers from the state it already holds for each page (`gridState.canScrollBackward`).
 * The pager follows the page that is shown, so after a swipe or a tab tap to a page that is already scrolled the scrim is already up. Pass
 * [scrimFraction] to drive the scrim from somewhere else instead, such as a collapsing bar's own collapse, and [headerBackground] for a header that
 * should have a ground of its own.
 * **A caller whose pages scroll must pass [pageScrolled] or its own [scrimFraction]:** with neither, the header has no ground and no scrim, and rows show
 * through under the tabs.
 *
 * [edgePadding] is handed to the [BingeTabRow]: the room before the first tab and after the last. It defaults to the screen's content inset. A pager
 * whose content already sits inside the screen's gutter passes zero, so the first tab's cell starts at that content's edge instead of indenting twice.
 *
 * [beyondViewportPageCount] is how many pages either side of the visible one stay composed; see
 * [BingeFilterChipPager] for what raising it costs.
 *
 * @need switch between different views by tab or swipe
 * @group bars
 * @replaces a hand-built tab row wired to a HorizontalPager
 */
@Composable
fun BingeTabbedPager(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    headerBackground: Color = Color.Transparent,
    scrimFraction: Float? = null,
    pageScrolled: (page: Int) -> Boolean = { false },
    scrimColor: Color = MaterialTheme.colorScheme.background,
    edgePadding: Dp = resolvedContentInset(),
    beyondViewportPageCount: Int = 0,
    pageContent: @Composable (contentPadding: PaddingValues, page: Int) -> Unit,
) {
    HeaderPager(
        pageCount = tabs.size,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
        modifier = modifier,
        header = header,
        headerBackground = headerBackground,
        scrimFraction = scrimFraction,
        pageScrolled = pageScrolled,
        scrimColor = scrimColor,
        beyondViewportPageCount = beyondViewportPageCount,
        selector = { shown ->
            BingeTabRow(tabs = tabs, selectedIndex = shown, onSelect = onSelectedIndexChange, edgePadding = edgePadding)
        },
        pageContent = pageContent,
    )
}
