package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A time field whose caller hands its digits back a few frames late must not lose one typed in between. Each test drives the frame clock by hand,
 * so digits are committed between frames and the caller's value arrives behind them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTimeInputBurstTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var hour by mutableStateOf("")
    private val reported = mutableListOf<String>()

    private fun show(initial: String = "") {
        hour = initial
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme {
                BingeTimeInput(
                    hour = hour,
                    minute = "00",
                    onHour = { reported += it },
                    onMinute = {},
                    is24Hour = true,
                    afternoon = false,
                    onAfternoon = {},
                )
            }
        }
        rule.mainClock.advanceTimeByFrame()
        field().requestFocus()
        rule.mainClock.advanceTimeByFrame()
    }

    private fun field(): SemanticsNodeInteraction = rule.onNodeWithText("Hour")

    private fun shownText() = field().fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun frame(count: Int = 1) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    @Test
    fun `digits typed between frames are all kept while the caller's value lags`() {
        show()
        field().performTextInput("1")
        frame()
        field().performTextInput("2")
        frame()

        assertEquals("12", shownText())
        assertEquals(listOf("1", "12"), reported)
    }

    @Test
    fun `a value handed back one frame behind each digit does not undo the typing`() {
        show()
        field().performTextInput("1")
        frame()
        field().performTextInput("2")
        hour = "1"
        frame()
        hour = "12"
        frame()

        assertEquals("12", shownText())
        assertEquals(listOf("1", "12"), reported)
    }

    @Test
    fun `a value from outside replaces the digits, even with a digit pending`() {
        show("1")
        field().performTextInput("2")
        frame()
        hour = "07"
        frame()

        assertEquals("07", shownText())
    }

    @Test
    fun `a caller that never takes a digit puts the field back to what it holds`() {
        show("1")
        field().performTextInput("2")
        frame(ANSWER_GRACE_FRAMES + FRAMES_AFTER_GRACE)

        assertEquals("1", shownText())
    }

    @Test
    fun `characters that are not digits are dropped and not reported`() {
        show()
        field().performTextInput("1:")
        frame()

        assertEquals("1", shownText())
        assertEquals(listOf("1"), reported)
    }

    private companion object {
        const val FRAMES_AFTER_GRACE = 6
    }
}
