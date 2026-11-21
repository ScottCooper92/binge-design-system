package com.binge.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
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
 * The header is **transparent**, with no band of its own, and a translucent scrim fades in behind it once the page that is shown has scrolled off its
 * top, so content passes under the tabs through the scrim and never under an opaque band. [pageScrolled] is how the pager learns that: it is asked
 * whether page `page` is scrolled off its top, which a caller answers from the state it already holds for each page (`gridState.canScrollBackward`).
 * The pager follows the page that is shown, so after a swipe or a tab tap to a page that is already scrolled the scrim is already up. Pass
 * [scrimFraction] to drive the scrim from somewhere else instead, such as a collapsing bar's own collapse, and [headerBackground] for a header that
 * should have a ground of its own.
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
    beyondViewportPageCount: Int = 0,
    pageContent: @Composable (contentPadding: PaddingValues, page: Int) -> Unit,
) {
    val pager = rememberFilterPager(
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
        pageCount = tabs.size,
    )
    val pagerState = pager.state
    val followed by animateFloatAsState(
        targetValue = if (scrimFraction == null && pageScrolled(pager.shownIndex)) 1f else 0f,
        animationSpec = tween(durationMillis = SCRIM_FADE_MILLIS),
        label = "tabbed pager scrim",
    )
    val scrim = scrimFraction ?: followed
    LocalTabbedPagerScrimProbe.current?.invoke(scrim)
    OverlaidHeaderContent(
        modifier = modifier,
        headerBackground = headerBackground,
        scrimFraction = scrim,
        scrimColor = scrimColor,
        header = {
            header?.invoke()
            BingeTabRow(
                tabs = tabs,
                selectedIndex = pager.shownIndex,
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

/** How long the scrim takes to fade in or out as the shown page leaves or returns to its top. */
private const val SCRIM_FADE_MILLIS = 150

/** A test's way to read the scrim fraction the pager is drawing, which is otherwise only a colour in the header; null in production. */
internal val LocalTabbedPagerScrimProbe = compositionLocalOf<((Float) -> Unit)?> { null }
