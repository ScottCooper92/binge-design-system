package com.binge.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROOT = "Root page"
private const val DETAIL = "Detail page"

/**
 * A task sheet's pages transition (#1272): a pushed page slides in from the end edge as the old one eases away, a popped one the
 * reverse, mirrored in a right-to-left layout; under reduced motion nothing slides and the swap is a short fade. A page that is popped
 * is reported to `onPageBack` once it has finished leaving, not while it is still on screen. The clock is paused so a frame part-way
 * through can be read.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetPageTransitionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var nav: BingeSheetNav? = null
    private var open by mutableStateOf(true)
    private val leftPages = mutableListOf<String>()

    private fun show(reduceMotion: Boolean = false) {
        open = true
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                if (open) {
                    BingeAdaptiveSheet(
                        kind = BingeSheetKind.Task,
                        onDismissRequest = {},
                        onPageBack = { leftPages += it },
                        stackPagesWhenCompact = true,
                    ) { handle -> Pages(handle) }
                }
            }
        }
        rule.mainClock.advanceTimeBy(SETTLE_MS)
    }

    @Composable
    private fun ColumnScope.Pages(handle: BingeSheetNav) {
        nav = handle
        Text(if (handle.page == "detail") DETAIL else ROOT)
    }

    private fun act(block: () -> Unit) {
        block()
        Snapshot.sendApplyNotifications()
    }

    private fun advanceFrames(count: Int = MIDWAY_FRAMES) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    private fun settle() = rule.mainClock.advanceTimeBy(SETTLE_MS)

    private fun left(text: String): Dp = rule.onNodeWithText(text).getUnclippedBoundsInRoot().left

    @Test
    fun `a pushed page slides in over the old one, then only it is left`() {
        show()
        act { nav!!.push("detail") }
        advanceFrames()

        rule.onAllNodesWithText(ROOT).assertCountEquals(1)
        rule.onAllNodesWithText(DETAIL).assertCountEquals(1)
        assertTrue(
            "the new page should be further towards the end edge than the old, was ${left(DETAIL)} and ${left(ROOT)}",
            left(DETAIL) > left(ROOT),
        )

        settle()
        rule.onAllNodesWithText(ROOT).assertCountEquals(0)
        rule.onAllNodesWithText(DETAIL).assertCountEquals(1)
    }

    @Test
    fun `a popped page leaves towards the end edge and is reported once it has gone`() {
        show()
        act { nav!!.push("detail") }
        settle()

        act { nav!!.pop() }
        advanceFrames()
        rule.onAllNodesWithText(DETAIL).assertCountEquals(1)
        rule.onAllNodesWithText(ROOT).assertCountEquals(1)
        assertTrue("the leaving page should still be on screen, reported: $leftPages", leftPages.isEmpty())
        assertTrue(
            "the leaving page should be towards the end edge of the one it reveals, was ${left(DETAIL)} and ${left(ROOT)}",
            left(DETAIL) > left(ROOT),
        )

        settle()
        rule.onAllNodesWithText(DETAIL).assertCountEquals(0)
        assertEquals(listOf("detail"), leftPages)
    }

    @Test
    fun `a page popped and pushed again before its exit ends is not reported once it is back`() {
        show()
        act { nav!!.push("detail") }
        settle()

        act { nav!!.pop() }
        advanceFrames()
        act { nav!!.push("detail") }
        assertEquals("heard going back at the re-push, before it lands", listOf("detail"), leftPages)

        settle()
        rule.onAllNodesWithText(DETAIL).assertCountEquals(1)
        assertEquals("not reported again while it is the live page", listOf("detail"), leftPages)
    }

    @Test
    fun `reduced motion fades without sliding and settles at once`() {
        show(reduceMotion = true)
        act { nav!!.push("detail") }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        if (rule.onAllNodesWithText(ROOT).fetchSemanticsNodes().isNotEmpty() &&
            rule.onAllNodesWithText(DETAIL).fetchSemanticsNodes().isNotEmpty()
        ) {
            assertEquals("a fade does not move the page", left(ROOT).value, left(DETAIL).value, 0.5f)
        }

        rule.mainClock.advanceTimeBy(REDUCED_SETTLE_MS)
        rule.onAllNodesWithText(ROOT).assertCountEquals(0)
        rule.onAllNodesWithText(DETAIL).assertCountEquals(1)
    }

    @Test
    fun `a sheet that closes before the exit ends still reports the page that was popped`() {
        show()
        act { nav!!.push("detail") }
        settle()
        act { nav!!.pop() }
        advanceFrames()
        assertTrue(leftPages.isEmpty())

        act { open = false }
        rule.mainClock.advanceTimeByFrame()

        assertEquals(listOf("detail"), leftPages)
    }

    private companion object {
        const val MIDWAY_FRAMES = 4
        const val SETTLE_MS = 2_000L
        const val REDUCED_SETTLE_MS = 300L
    }
}
