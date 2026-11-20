package com.binge.designsystem.tv.template

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val ITEMS = List(3) { GridItem(id = 100 + it, title = "Title $it") }

/**
 * The pane reports the deepest cell focus reached, once, as it leaves — the wiring
 * counterpart to the design system's `TvVisitDepthTest`, which pins the bare class. What is only true here is that
 * [TvGridPage] actually hands the template's visit depth its own `onItemFocused` stream and reports on
 * dispose, mirroring `ScrollDepthEffectTest`'s attached-toggle leave pattern for the phone detector.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvGridPageVisitDepthTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val reported = mutableListOf<Int?>()
    private var attached by mutableStateOf(true)
    private var queryKey by mutableStateOf("first-query")

    private fun setGrid() {
        composeTestRule.setContent {
            BingeTvTheme {
                if (attached) {
                    TestGridPage(
                        heading = "Discover",
                        results = gridItems(ITEMS),
                        onItemClick = {},
                        onRetry = {},
                        emptyMessage = "Nothing here",
                        onVisitEnded = { reported += it },
                        queryKey = queryKey,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun leave() {
        attached = false
        composeTestRule.waitForIdle()
    }

    /** Entry, then → to [cell] — the same walk [TvGridTapReportingTest] uses for a pressed position. */
    private fun focus(cell: Int) {
        val cells = composeTestRule.onAllNodes(hasClickAction())
        val entry = cells[0]
        entry.requestFocus()
        composeTestRule.waitForIdle()
        entry.assertIsFocused()
        repeat(cell) {
            entry.performKeyInput { pressKey(Key.DirectionRight) }
            composeTestRule.waitForIdle()
        }
        cells[cell].assertIsFocused()
    }

    @Test
    fun `nothing is reported until the visit ends`() {
        setGrid()
        focus(2)

        assertTrue("reported before leaving: $reported", reported.isEmpty())
    }

    @Test
    fun `leaving reports the deepest cell reached, exactly once`() {
        setGrid()
        focus(2)

        leave()

        assertEquals(listOf(2), reported)
    }

    /**
     * A query change must not end the visit. Discover is the caller that makes this reachable — it passes its
     * applied filters as the [TvGridPage] `queryKey`, which resets the pane's focus and scroll state by design.
     * The depth deliberately does not follow: a visit is the screen, not the query, so keying it would split one
     * person's scroll into an event per filter edit — the same split `DiscoverScreenContent` hoists its detector
     * above the `when` to avoid on the phone.
     */
    @Test
    fun `changing the query key does not end the visit`() {
        setGrid()
        focus(2)

        queryKey = "second-query"
        composeTestRule.waitForIdle()

        assertTrue("a filter change reported a visit: $reported", reported.isEmpty())
    }

    /** And the depth it carried survives that reset, so the visit reports once with the deepest cell overall. */
    @Test
    fun `the deepest cell survives a query change`() {
        setGrid()
        focus(2)
        queryKey = "second-query"
        composeTestRule.waitForIdle()

        leave()

        assertEquals(listOf(2), reported)
    }

    /** Mirrors `ScrollDepthEffectTest`'s deepest-not-current case: walking back must not lower the report. */
    @Test
    fun `walking back to the first cell keeps the deepest one reached`() {
        setGrid()
        focus(2)
        composeTestRule.onAllNodes(hasClickAction())[2].performKeyInput {
            pressKey(Key.DirectionLeft)
            pressKey(Key.DirectionLeft)
        }
        composeTestRule.waitForIdle()

        leave()

        assertEquals(listOf(2), reported)
    }
}
