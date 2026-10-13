package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeLetterRailTest {
    @get:Rule
    val rule = createComposeRule()

    private val picked = mutableListOf<Char>()

    @Test
    fun `tapping the same letter twice jumps both times`() {
        rule.setContent {
            var current by remember { mutableStateOf<Char?>('A') }
            Box(Modifier.height(400.dp)) {
                BingeLetterRail(
                    letters = listOf('A', 'B', 'C', 'D'),
                    onLetter = {
                        picked += it
                        current = it
                    },
                    current = current,
                )
            }
        }

        // The top quarter of the rail is A, the second is B.
        rule.onRoot().performTouchInput { click(Offset(width / 2f, height * 0.375f)) }
        rule.waitForIdle()
        // The list is scrolled back by hand; the rail now says A again.
        rule.onRoot().performTouchInput { click(Offset(width / 2f, height * 0.375f)) }
        rule.waitForIdle()

        assertEquals(listOf('B', 'B'), picked)
    }

    private fun showRail() =
        rule.setContent {
            Box(Modifier.height(400.dp)) {
                BingeLetterRail(letters = listOf('A', 'B', 'C', 'D'), onLetter = { picked += it })
            }
        }

    /**
     * Each letter fires once per drag, the first included: the press that picked it is cancelled when the drag
     * takes over, and that cancel must not clear the drag's dedupe (#466).
     */
    private fun assertEachOnce(order: List<Char>, drags: Int = 1) {
        assertEquals(List(drags) { order }.flatten(), picked)
    }

    @Test
    fun `dragging down the rail jumps to each letter in order, once each`() {
        showRail()

        rule.onRoot().performTouchInput { swipeDown() }
        rule.waitForIdle()

        assertEachOnce(listOf('A', 'B', 'C', 'D'))
    }

    @Test
    fun `dragging back up the rail jumps to each letter in order, once each`() {
        showRail()

        rule.onRoot().performTouchInput { swipeUp() }
        rule.waitForIdle()

        assertEachOnce(listOf('D', 'C', 'B', 'A'))
    }

    @Test
    fun `a cancelled drag still lets the next one start on the same letter`() {
        showRail()

        // A drag that stays within A, then is cancelled rather than lifted.
        rule.onRoot().performTouchInput {
            down(Offset(width / 2f, height * 0.02f))
            moveTo(Offset(width / 2f, height * 0.2f))
            cancel()
        }
        rule.waitForIdle()
        rule.onRoot().performTouchInput { swipeDown() }
        rule.waitForIdle()

        assertEquals(listOf('A', 'A', 'B', 'C', 'D'), picked)
    }

    @Test
    fun `a second drag starts fresh after the finger lifts`() {
        showRail()

        rule.onRoot().performTouchInput { swipeDown() }
        rule.waitForIdle()
        rule.onRoot().performTouchInput { swipeDown() }
        rule.waitForIdle()

        assertEachOnce(listOf('A', 'B', 'C', 'D'), drags = 2)
    }

    private val alphabet = ('A'..'Z').toList() + '#'

    private fun showShortRail() =
        rule.setContent {
            Box(Modifier.height(300.dp)) {
                BingeLetterRail(letters = alphabet, onLetter = { picked += it })
            }
        }

    @Test
    fun `a rail too short for every letter draws alternate ones`() {
        showShortRail()

        // 27 letters need 540dp; at 300dp every third fits with a dot between each.
        listOf('A', 'D', 'G', 'J', 'M', 'P', 'S', 'V', 'Y').forEach { rule.onNodeWithText(it.toString()).assertExists() }
        listOf('B', 'C', 'E', 'Z', '#').forEach { rule.onNodeWithText(it.toString()).assertDoesNotExist() }
    }

    @Test
    fun `a tap on a letter that is drawn picks that letter`() {
        showShortRail()

        listOf('A', 'D', 'G', 'J', 'M', 'P', 'S', 'V', 'Y').forEach { letter ->
            picked.clear()
            rule.onNodeWithText(letter.toString()).performClick()
            rule.waitForIdle()

            assertEquals("a tap on $letter", listOf(letter), picked)
        }
    }

    @Test
    fun `a tap on a dot picks a letter beside it, never one that is not drawn`() {
        showShortRail()

        val a = rule.onNodeWithText("A").fetchSemanticsNode().boundsInRoot
        val d = rule.onNodeWithText("D").fetchSemanticsNode().boundsInRoot
        val between = (a.bottom + d.top) / 2f
        rule.onRoot().performTouchInput { click(Offset(a.center.x, between)) }
        rule.waitForIdle()

        assertEquals(1, picked.size)
        assertEquals(true, picked.single() == 'A' || picked.single() == 'D')
    }

    @Test
    fun `a rail with room for every letter draws them all and keeps no dots`() {
        val letters = ('A'..'P').toList()
        rule.setContent { Box(Modifier.height(400.dp)) { BingeLetterRail(letters = letters, onLetter = {}) } }

        letters.forEach { rule.onNodeWithText(it.toString()).assertExists() }
    }

    @Test
    fun `the plan keeps every letter that fits and thins the rest by a stride`() {
        val all = letterRailPlan(alphabet, heightPx = 540, letterPx = 20, dotPx = 10)
        assertEquals(alphabet, all.shown)
        assertEquals(false, all.dotted)

        val second = letterRailPlan(alphabet, heightPx = 420, letterPx = 20, dotPx = 10)
        assertEquals(alphabet.filterIndexed { i, _ -> i % 2 == 0 }, second.shown)
        assertEquals(true, second.dotted)

        val third = letterRailPlan(alphabet, heightPx = 300, letterPx = 20, dotPx = 10)
        assertEquals(alphabet.filterIndexed { i, _ -> i % 3 == 0 }, third.shown)
    }

    @Test
    fun `the plan never draws fewer than one letter`() {
        val plan = letterRailPlan(alphabet, heightPx = 5, letterPx = 20, dotPx = 10)

        assertEquals(listOf('A'), plan.shown)
    }
}
