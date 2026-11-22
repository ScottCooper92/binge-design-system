package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.advanceFrames
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The tab row of a [BingeTabbedPager] follows a tap at once, and the tabs, the page and the caller's selection agree once the pager is idle,
 * however a move was interrupted. The same sync as the filter chip pager's, so the same cases. The clock is paused so a tap can land
 * part-way through a move; nothing here retries or waits on a timer.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTabbedPagerReconcileTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var selected by mutableIntStateOf(0)
    private val reports = mutableListOf<Int>()
    private val tabs = List(TABS) { "Tab $it" }

    private fun show() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme {
                BingeTabbedPager(
                    tabs = tabs,
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

    private fun tap(index: Int) {
        selected = index
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun `a tab tap shows as selected at once, not once the scroll passes the midpoint`() {
        show()
        tap(TABS - 1)
        // The move starts a frame after the tap (the effect that starts it runs after the first recomposition), and the tabs follow on the next.
        rule.advanceFrames(2)

        rule.onNodeWithText("Tab ${TABS - 1}").assertIsSelected()
    }

    @Test
    fun `a tap back to the page the move started from leaves the tab and the page agreeing`() {
        show()
        tap(TABS - 1)
        rule.advanceFrames(MID_FRAMES)
        tap(0)
        rule.mainClock.advanceTimeBy(SETTLE_MS)

        rule.onNodeWithText("Tab 0").assertIsSelected()
        rule.onNodeWithText("page 0").assertExists()
        assertEquals("a settle overwrote a tap: $reports", emptyList<Int>(), reports)
    }

    private companion object {
        const val TABS = 4
        const val MID_FRAMES = 3
        const val SETTLE_MS = 3_000L
    }
}
