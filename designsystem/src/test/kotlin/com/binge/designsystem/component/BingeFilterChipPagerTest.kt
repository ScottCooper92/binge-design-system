package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performScrollToIndex
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PAGE_TAG = "pager-page"

/**
 * Regression guard for #1436's stale-capture fix in [BingeFilterChipPager] (#1458).
 *
 * The settled-swipe effect is keyed only on the pager state, so its `collect` lambda outlives its
 * launching composition. Before the fix it compared each settled page against the `selectedIndex`
 * captured at first composition, so swiping *back* to the original page compared `0 != 0` and reported
 * nothing. The fix reads the selection through `rememberUpdatedState`; this pins it against a revert.
 *
 * No gesture needed — the fault is in the settled-page path, not the fling: [performScrollToIndex]
 * snaps via `scrollToPage`, exercising the same `snapshotFlow { settledPage }` path deterministically.
 * Mutation-checked: reverting to the raw `selectedIndex` capture drops the second report (`[1]`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterChipPagerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a settled swipe forward then back reports both selections`() {
        val reported = mutableListOf<Int>()
        composeTestRule.setContent {
            BingeExpressiveTheme {
                var selected by remember { mutableStateOf(0) }
                BingeFilterChipPager(
                    items = listOf(FilterChipItem("Movies"), FilterChipItem("TV")),
                    selectedIndex = selected,
                    onSelectedIndexChange = {
                        selected = it
                        reported += it
                    },
                ) { _, page ->
                    Text(text = "page $page", modifier = Modifier.fillMaxSize().testTag(PAGE_TAG))
                }
            }
        }

        // The chip LazyRow also carries a ScrollToIndex action; only the pager has a page as a
        // descendant, so the descendant match disambiguates the two scrollables.
        val pager = composeTestRule.onNode(
            hasScrollToIndexAction() and hasAnyDescendant(hasTestTag(PAGE_TAG)),
        )

        pager.performScrollToIndex(1)
        composeTestRule.waitForIdle()
        pager.performScrollToIndex(0)
        composeTestRule.waitForIdle()

        assertEquals(
            "A settled swipe forward then back must report both pages; the second is dropped when the " +
                "selection is captured stalely instead of read through rememberUpdatedState",
            listOf(1, 0),
            reported,
        )
    }
}
