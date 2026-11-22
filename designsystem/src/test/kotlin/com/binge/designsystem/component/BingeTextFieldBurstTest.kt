package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextRange
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A field whose caller hands the value back a few frames late (a view model, a validated flow) must not lose what was typed in between. Each
 * test drives the frame clock by hand, so characters are committed between frames and the caller's value arrives behind them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTextFieldBurstTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var value by mutableStateOf("")
    private val reported = mutableListOf<String>()

    private fun show(initial: String = "") {
        value = initial
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme { BingeTextField(value = value, label = "Name", onValueChange = { reported += it }) }
        }
        rule.mainClock.advanceTimeByFrame()
        field().requestFocus()
        rule.mainClock.advanceTimeByFrame()
    }

    private fun field(): SemanticsNodeInteraction = rule.onNode(hasSetTextAction())

    private fun shownText() = field().fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun caret() = field().fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].start

    private fun frame(count: Int = 1) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    @Test
    fun `characters typed between frames are all kept while the caller's value lags`() {
        show()
        field().performTextInput("a")
        frame()
        field().performTextInput("b")
        frame()
        field().performTextInput("c")
        frame()

        assertEquals("abc", shownText())
        assertEquals(listOf("a", "ab", "abc"), reported)
    }

    @Test
    fun `values handed back one frame behind each keystroke do not undo the typing`() {
        show()
        field().performTextInput("a")
        frame()
        field().performTextInput("b")
        value = "a"
        frame()
        field().performTextInput("c")
        value = "ab"
        frame()
        value = "abc"
        frame()

        assertEquals("abc", shownText())
        assertEquals(3, caret())
    }

    @Test
    fun `a value from outside replaces the text and puts the caret at its end, even with an edit pending`() {
        show("gpu")
        field().performTextInput("s")
        frame()
        value = "transcoding"
        frame()

        assertEquals("transcoding", shownText())
        assertEquals("transcoding".length, caret())
    }

    @Test
    fun `a cleared value puts the caret at zero`() {
        show("abc")
        field().performTextInput("d")
        frame()
        value = ""
        frame()

        assertEquals("", shownText())
        assertEquals(0, caret())
    }

    @Test
    fun `a value that arrives within a few frames keeps the caret where the user typed`() {
        show("8096")
        field().performTextInputSelection(TextRange(2))
        frame()
        field().performTextInput("7")
        frame()
        assertEquals("80796", shownText())
        assertEquals(3, caret())

        value = "80796"
        frame()

        assertEquals("80796", shownText())
        assertEquals(3, caret())
    }

    @Test
    fun `typing back to the text the caller holds is still reported once earlier edits are pending`() {
        show("ab")
        field().performTextInput("c")
        frame()
        field().performTextReplacement("ab")
        frame()

        assertEquals(listOf("abc", "ab"), reported)
    }

    @Test
    fun `a caller that takes an earlier text of a burst and then holds it ends the wait`() {
        show()
        field().performTextInput("a")
        frame()
        field().performTextInput("1")
        frame()
        // A view model that took "a" late and rejects "a1" by keeping "a".
        value = "a"
        frame(ANSWER_GRACE_FRAMES + FRAMES_AFTER_GRACE)

        assertEquals("a", shownText())
    }

    @Test
    fun `the search field keeps characters typed between frames while its query lags`() {
        value = ""
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme { BingeSearchField(query = value, onQueryChange = { reported += it }, onClear = {}, placeholder = "Search") }
        }
        rule.mainClock.advanceTimeByFrame()
        field().requestFocus()
        rule.mainClock.advanceTimeByFrame()

        field().performTextInput("d")
        frame()
        field().performTextInput("u")
        frame()
        field().performTextInput("n")
        frame()

        assertEquals("dun", shownText())
        assertEquals(listOf("d", "du", "dun"), reported)
    }

    private companion object {
        /** Frames past the wait, for the give-up and the recomposition it causes to land. */
        const val FRAMES_AFTER_GRACE = 6
    }
}
