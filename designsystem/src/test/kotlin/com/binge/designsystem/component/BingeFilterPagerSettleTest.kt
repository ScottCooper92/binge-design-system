package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A settle the pager reports is a swipe the reader made, and nothing else. A page the pager passes through, or is left on when one move
 * is replaced by the next, is not a swipe, and reporting it overwrites the selection the caller has just made (a chip tapped while the pager
 * is still moving). The clock is paused so a selection can be made part-way through a move.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterPagerSettleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var selected by mutableIntStateOf(0)
    private val reports = mutableListOf<Int>()
    private val pages = List(PAGES) { FilterChipItem("Chip $it") }

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme {
                BingeFilterChipPager(
                    items = pages,
                    selectedIndex = selected,
                    onSelectedIndexChange = {
                        reports += it
                        selected = it
                    },
                ) { _, page -> Text(text = "page $page", modifier = Modifier.fillMaxSize()) }
            }
        }
        rule.mainClock.advanceTimeBy(SETTLE_MS)
    }

    /** A chip tap: the caller's selection changes. A paused clock does not deliver the write by itself, so it is applied by hand. */
    private fun tap(index: Int) {
        selected = index
        Snapshot.sendApplyNotifications()
    }

    private fun advanceFrames(count: Int) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    @Test
    fun `a chip tapped while the pager is still moving to the last one is not overwritten by the page it was left on`() {
        show()
        tap(PAGES - 1)
        advanceFrames(MID_FRAMES)
        tap(2)
        rule.mainClock.advanceTimeBy(SETTLE_MS)

        assertEquals("a settle overwrote the tap: $reports", emptyList<Int>(), reports)
        assertEquals(2, selected)
        rule.onNodeWithText("page 2").assertExists()
    }

    @Test
    fun `a run of taps in a row ends on the last one with nothing reported`() {
        show()
        listOf(3, 1, 4, 2, 5, 0).forEach {
            tap(it)
            advanceFrames(2)
        }
        rule.mainClock.advanceTimeBy(SETTLE_MS)

        assertEquals("a settle overwrote a tap: $reports", emptyList<Int>(), reports)
        assertEquals(0, selected)
        rule.onNodeWithText("page 0").assertExists()
    }

    @Test
    fun `a swipe the reader makes is still reported`() {
        show()
        rule.mainClock.autoAdvance = true
        rule.onRoot().performTouchInput { swipeLeft() }
        rule.waitForIdle()

        assertEquals("the swipe was not reported: $reports", listOf(1), reports)
        assertEquals(1, selected)
        rule.onNodeWithText("page 1").assertExists()
    }

    @Test
    fun `a reader who takes the pager over mid-move is still reported`() {
        show()
        tap(PAGES - 1)
        advanceFrames(MID_FRAMES)
        // A finger down and dragged back while the move to the last page is under way: the reader has the pager now.
        rule.onRoot().performTouchInput {
            down(center)
            moveBy(Offset(DRAG_PX, 0f))
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onRoot().performTouchInput { up() }
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        assertNotEquals("the reader's page was never reported: $reports", PAGES - 1, selected)
        // The chips and the page agree: the pager is not left on a page the caller never heard of.
        rule.onNodeWithText("page $selected").assertExists()
    }

    private companion object {
        const val PAGES = 6
        const val MID_FRAMES = 3
        const val SETTLE_MS = 3_000L
        const val DRAG_PX = 300f
    }
}
