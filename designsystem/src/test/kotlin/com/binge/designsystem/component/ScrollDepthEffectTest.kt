package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LIST = "list"
private val ROWS = List(30) { "row $it" }

/**
 * The depth detector reports **once, on leave**, with the deepest index the visit reached (#2269).
 *
 * Worth a test rather than trusting the `snapshotFlow`: a detector that reported per scroll frame, or
 * reported the *current* index instead of the deepest, would look identical from the call site and be
 * wrong in the dashboard weeks later — and reporting more than once is the specific failure the
 * on-leave design exists to avoid.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ScrollDepthEffectTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val reported = mutableListOf<Int?>()
    private var attached by mutableStateOf(true)

    private fun setList(rows: List<String> = ROWS) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                if (attached) {
                    val listState = rememberLazyListState()
                    ScrollDepthEffect(listState) { reported += it }
                    LazyColumn(state = listState, modifier = Modifier.testTag(LIST)) {
                        items(rows, key = { it }) { row ->
                            Text(row, Modifier.fillMaxWidth().height(40.dp))
                        }
                    }
                }
            }
        }
    }

    private fun leave() {
        attached = false
        composeTestRule.waitForIdle()
    }

    @Test
    fun `nothing is reported until the visit ends`() {
        setList()

        composeTestRule.onNodeWithTag(LIST).performScrollToIndex(20)
        composeTestRule.waitForIdle()

        assertTrue("reported before leaving: $reported", reported.isEmpty())
    }

    @Test
    fun `leaving reports the deepest index reached, exactly once`() {
        setList()
        composeTestRule.onNodeWithTag(LIST).performScrollToIndex(20)
        composeTestRule.waitForIdle()

        leave()

        assertEquals(1, reported.size)
        val deepest = reported.single()
        assertTrue("expected at least 20, got $deepest", (deepest ?: -1) >= 20)
    }

    /**
     * The distinction the whole design rests on: scrolling back up must not lower the number. A
     * detector reporting the *current* index would say the visit never left the top.
     */
    @Test
    fun `scrolling back up keeps the deepest index, not the last one`() {
        setList()
        composeTestRule.onNodeWithTag(LIST).performScrollToIndex(25)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(LIST).performScrollToIndex(0)
        composeTestRule.waitForIdle()

        leave()

        assertTrue("expected at least 25, got ${reported.single()}", (reported.single() ?: -1) >= 25)
    }

    /** A list that laid out nothing had no depth to reach, which is not a depth of zero. */
    @Test
    fun `an empty list reports null rather than zero`() {
        setList(rows = emptyList())

        leave()

        assertEquals(1, reported.size)
        assertNull(reported.single())
    }
}
