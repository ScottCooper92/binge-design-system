package com.binge.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
 * The chips, the pager's page and the caller's selection agree once the pager is idle, however a move to a selection was interrupted: a tap
 * on the page the pager is passing, a tap on another chip, a page count that changes mid-move. The clock is paused so a change can land
 * part-way through a move; nothing here retries or waits on a timer.
 */
@OptIn(ExperimentalFoundationApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterPagerReconcileTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var selected by mutableIntStateOf(0)
    private var count by mutableIntStateOf(PAGES)
    private var state: PagerState? = null
    private val reports = mutableListOf<Int>()

    private fun items() = List(count) { FilterChipItem("Chip $it") }

    /** The real pager, for what the chips show. */
    private fun showChips() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme {
                BingeFilterChipPager(
                    items = items(),
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

    /** The pager state a screen assembles for itself, for what the pager reports about its own page. */
    private fun showState() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme {
                val pagerState = rememberFilterPagerState(
                    selectedIndex = selected,
                    onSelectedIndexChange = {
                        reports += it
                        selected = it
                    },
                    pageCount = count,
                )
                state = pagerState
                HorizontalPager(state = pagerState) { page -> Text(text = "page $page", modifier = Modifier.fillMaxSize()) }
            }
        }
        rule.mainClock.advanceTimeBy(SETTLE_MS)
    }

    private fun tap(index: Int) {
        selected = index
        Snapshot.sendApplyNotifications()
    }

    private fun assertAgreeOnIdle() {
        rule.mainClock.advanceTimeBy(SETTLE_MS)
        val pager = checkNotNull(state)
        assertEquals("the pager is not on a whole page", 0f, pager.currentPageOffsetFraction, 0.001f)
        assertEquals("currentPage and settledPage disagree", pager.currentPage, pager.settledPage)
        assertEquals("the caller's selection and the pager disagree (reports $reports)", selected, pager.settledPage)
    }

    @Test
    fun `a chip tap shows as selected at once, not once the scroll passes the midpoint`() {
        showChips()
        tap(PAGES - 1)
        // The move starts a frame after the tap (the effect that starts it runs after the first recomposition), and the chips follow on the next.
        rule.advanceFrames(2)

        rule.onNodeWithText("Chip ${PAGES - 1}").assertIsSelected()
    }

    @Test
    fun `a tap on the page the pager is passing leaves everything agreeing`() {
        showState()
        tap(PAGES - 1)
        rule.advanceFrames(MID_FRAMES)
        val passing = checkNotNull(state).currentPage
        tap(passing)

        assertAgreeOnIdle()
    }

    @Test
    fun `a tap back to the page the pager started from leaves everything agreeing`() {
        showState()
        tap(PAGES - 1)
        rule.advanceFrames(MID_FRAMES)
        tap(0)

        assertAgreeOnIdle()
    }

    @Test
    fun `a page count that grows mid-move leaves everything agreeing`() {
        showState()
        tap(PAGES - 1)
        rule.advanceFrames(MID_FRAMES)
        count = PAGES + 2
        Snapshot.sendApplyNotifications()

        assertAgreeOnIdle()
    }

    @Test
    fun `a page count that shrinks mid-move leaves everything agreeing`() {
        showState()
        tap(PAGES - 1)
        rule.advanceFrames(MID_FRAMES)
        count = PAGES - 2
        Snapshot.sendApplyNotifications()

        assertAgreeOnIdle()
    }

    private companion object {
        const val PAGES = 6
        const val MID_FRAMES = 3
        const val SETTLE_MS = 3_000L
    }
}
