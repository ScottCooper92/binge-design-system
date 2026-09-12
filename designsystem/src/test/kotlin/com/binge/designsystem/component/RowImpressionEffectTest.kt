package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LIST = "list"
private val ROWS = listOf("hero", "from_watchlist", "on_your_services", "popular", "upcoming")

/**
 * The impression detector reports rows by key as they reach the viewport (#2269).
 *
 * Worth a test rather than trusting the `snapshotFlow`: a detector that reported nothing, or reported
 * every row on first composition regardless of the viewport, would both look identical from the call
 * site — the events would simply be wrong in the dashboard weeks later.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RowImpressionEffectTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val seen = mutableListOf<String>()

    private fun setList() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val listState = rememberLazyListState()
                RowImpressionEffect(listState) { key -> seen += key }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .size(200.dp)
                        .testTag(LIST),
                ) {
                    items(items = ROWS, key = { it }) { row ->
                        Text(text = row, modifier = Modifier.fillMaxWidth().height(150.dp))
                    }
                }
            }
        }
    }

    @Test
    fun `rows in the viewport at first composition are reported`() {
        setList()

        composeTestRule.waitForIdle()

        assertTrue("Expected the first row, got $seen", "hero" in seen)
    }

    /** The rows below the fold are the point: a detector that reported all five would be useless. */
    @Test
    fun `a row below the fold is not reported until it is scrolled to`() {
        setList()
        composeTestRule.waitForIdle()

        assertTrue("Expected 'upcoming' to be off screen, got $seen", "upcoming" !in seen)

        composeTestRule.onNodeWithTag(LIST).performScrollToIndex(ROWS.lastIndex)
        composeTestRule.waitForIdle()

        assertTrue("Expected 'upcoming' after scrolling, got $seen", "upcoming" in seen)
    }

    @Test
    fun `every reported key is one the caller supplied`() {
        setList()
        composeTestRule.onNodeWithTag(LIST).performScrollToIndex(ROWS.lastIndex)
        composeTestRule.waitForIdle()

        assertEquals(emptyList<String>(), seen.filterNot { it in ROWS })
    }
}
