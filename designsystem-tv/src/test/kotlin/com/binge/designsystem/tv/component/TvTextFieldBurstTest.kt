package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val NAME = "Display name"

/**
 * A television field whose caller hands the value back a few frames late must not lose a character typed in between. Each test opens the input,
 * then drives the frame clock by hand, so characters are committed between frames and the caller's value arrives behind them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTextFieldBurstTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    private var value by mutableStateOf("")
    private val reported = mutableListOf<String>()

    private fun show(initial: String = "") {
        value = initial
        rule.setContent { BingeTvTheme { TvTextField(value = value, onValueChange = { reported += it }, label = NAME) } }
        rule.waitForIdle()
        rule.onNode(hasContentDescription(NAME) and isFocusable() and !hasSetTextAction()).requestFocus()
        rule.waitForIdle()
        rule.onRoot().performKeyInput { pressKey(Key.DirectionCenter) }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
    }

    private fun input(): SemanticsNodeInteraction = rule.onNode(hasContentDescription(NAME) and hasSetTextAction())

    private fun shownText() = input().fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun caret() = input().fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].start

    private fun frame(count: Int = 1) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    @Test
    fun `characters typed between frames are all kept while the caller's value lags`() {
        show()
        input().performTextInput("a")
        frame()
        input().performTextInput("b")
        frame()
        input().performTextInput("c")
        frame()

        assertEquals("abc", shownText())
        assertEquals(listOf("a", "ab", "abc"), reported)
    }

    @Test
    fun `values handed back one frame behind each keystroke do not undo the typing`() {
        show()
        input().performTextInput("a")
        frame()
        input().performTextInput("b")
        value = "a"
        frame()
        input().performTextInput("c")
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
        input().performTextInput("s")
        frame()
        value = "transcoding"
        frame()

        assertEquals("transcoding", shownText())
        assertEquals("transcoding".length, caret())
    }

    @Test
    fun `a caller that never takes a character puts the field back to what it holds`() {
        show("gpu")
        input().performTextInput("s")
        frame(GRACE_FRAMES + FRAMES_AFTER_GRACE)

        assertEquals("gpu", shownText())
    }

    private companion object {
        const val GRACE_FRAMES = 4
        const val FRAMES_AFTER_GRACE = 6
    }
}
