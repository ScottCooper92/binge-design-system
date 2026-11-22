package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * The pager behind [BingeFilterChipPager] and [BingeTabbedPager]: a horizontal pager under a transparent header that holds the caller's [header]
 * and a [selector] row of its own (the chips, the tabs), with a scrim that fades in behind the header once the shown page has scrolled off its top.
 * The two differ only in the selector, so the page state, the scrim and the overlay are written once and cannot drift apart.
 *
 * [selector] is handed the index of the page that is shown, which follows a swipe as it settles and not only a tap. [pageContent] is handed the
 * padding that measures the header overlay. The other parameters are those of the two public pagers.
 */
@Composable
internal fun HeaderPager(
    pageCount: Int,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
    header: (@Composable () -> Unit)?,
    headerBackground: Color,
    scrimFraction: Float?,
    pageScrolled: (page: Int) -> Boolean,
    scrimColor: Color,
    beyondViewportPageCount: Int,
    selector: @Composable (shownIndex: Int) -> Unit,
    pageContent: @Composable (contentPadding: PaddingValues, page: Int) -> Unit,
) {
    val pager = rememberFilterPager(
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
        pageCount = pageCount,
    )
    val pagerState = pager.state
    val scrim = rememberPagerScrim(scrimFraction, scrolled = pageScrolled(pager.shownIndex))
    OverlaidHeaderContent(
        modifier = modifier,
        headerBackground = headerBackground,
        scrimFraction = scrim,
        scrimColor = scrimColor,
        header = {
            header?.invoke()
            selector(pager.shownIndex)
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
