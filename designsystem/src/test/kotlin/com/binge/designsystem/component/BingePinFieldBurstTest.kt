package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextInput
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A PIN field whose caller hands the value back a few frames late must not lose a digit typed in between. Each test drives the frame clock by
 * hand, so digits are committed between frames and the caller's value arrives behind them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingePinFieldBurstTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var value by mutableStateOf("")
    private val reported = mutableListOf<String>()
    private val completed = mutableListOf<String>()

    private fun show(initial: String = "") {
        value = initial
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme {
                BingePinField(
                    value = value,
                    onValueChange = { reported += it },
                    label = "PIN",
                    autoFocus = false,
                    onComplete = { completed += it },
                )
            }
        }
        rule.mainClock.advanceTimeByFrame()
    }

    private fun field(): SemanticsNodeInteraction = rule.onNodeWithContentDescription("PIN")

    private fun shownText() = field().fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun frame(count: Int = 1) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    @Test
    fun `digits typed between frames are all kept while the caller's value lags`() {
        show()
        field().performTextInput("4")
        frame()
        field().performTextInput("8")
        frame()
        field().performTextInput("2")
        frame()

        assertEquals("482", shownText())
        assertEquals(listOf("4", "48", "482"), reported)
    }

    @Test
    fun `values handed back one frame behind each digit do not undo the typing`() {
        show()
        field().performTextInput("4")
        frame()
        field().performTextInput("8")
        value = "4"
        frame()
        field().performTextInput("2")
        value = "48"
        frame()
        value = "482"
        frame()

        assertEquals("482", shownText())
        assertEquals(listOf("4", "48", "482"), reported)
    }

    @Test
    fun `the last digit completes the code once, with every digit in it, when the caller lags`() {
        show()
        field().performTextInput("4")
        frame()
        field().performTextInput("8")
        frame()
        field().performTextInput("2")
        frame()
        field().performTextInput("1")
        frame()

        assertEquals(listOf("4821"), completed)
    }

    @Test
    fun `a value cleared from outside empties the boxes even with a digit pending`() {
        show("48")
        field().performTextInput("2")
        frame()
        value = ""
        frame()

        assertEquals("", shownText())
    }

    @Test
    fun `a caller that never takes a digit puts the boxes back to what it holds`() {
        show("48")
        field().performTextInput("2")
        frame(ANSWER_GRACE_FRAMES + FRAMES_AFTER_GRACE)

        assertEquals("48", shownText())
    }

    @Test
    fun `characters that are not digits are dropped and not reported`() {
        show()
        field().performTextInput("4a")
        frame()

        assertEquals("4", shownText())
        assertEquals(listOf("4"), reported)
    }

    private companion object {
        const val FRAMES_AFTER_GRACE = 2
    }
}
