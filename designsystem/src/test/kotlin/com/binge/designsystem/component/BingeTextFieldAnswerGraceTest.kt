package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** A field given an `answerGrace` keeps typed text for that long while the caller's value lags; without one it reverts after a few frames. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTextFieldAnswerGraceTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var value by mutableStateOf("")

    private fun show(answerGrace: Duration?) {
        value = ""
        rule.mainClock.autoAdvance = false
        rule.setContent {
            TestTheme { BingeTextField(value = value, label = "Name", onValueChange = {}, answerGrace = answerGrace) }
        }
        rule.mainClock.advanceTimeByFrame()
        field().requestFocus()
        rule.mainClock.advanceTimeByFrame()
    }

    private fun field(): SemanticsNodeInteraction = rule.onNode(hasSetTextAction())

    private fun shownText() = field().fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    @Test
    fun `a field with a grace keeps the typed text while the echo lags two seconds, then takes it`() {
        show(GRACE)
        field().performTextInput("abc")
        rule.mainClock.advanceTimeBy(ECHO_LAG_MILLIS)
        assertEquals("abc", shownText())

        value = "abc"
        rule.mainClock.advanceTimeBy(FRAME_MILLIS * 3)
        assertEquals("abc", shownText())
    }

    @Test
    fun `a field with a grace reverts to the caller's value once the grace is over`() {
        show(GRACE)
        field().performTextInput("abc")
        rule.mainClock.advanceTimeBy(GRACE.inWholeMilliseconds + SETTLE_MILLIS)
        assertEquals("", shownText())
    }

    @Test
    fun `a field with no grace reverts to the caller's value after a few frames`() {
        show(null)
        field().performTextInput("abc")
        rule.mainClock.advanceTimeBy(ECHO_LAG_MILLIS)
        assertEquals("", shownText())
    }

    private companion object {
        val GRACE = 3.seconds
        const val ECHO_LAG_MILLIS = 2_000L
        const val FRAME_MILLIS = 16L
        const val SETTLE_MILLIS = 500L
    }
}
