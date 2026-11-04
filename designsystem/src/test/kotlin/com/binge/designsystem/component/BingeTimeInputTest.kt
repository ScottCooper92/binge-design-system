package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextRange
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTimeInputTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var hour by mutableStateOf("03")
    private var minute by mutableStateOf("07")
    private var afternoon by mutableStateOf(false)

    private fun show(is24Hour: Boolean) {
        rule.setContent {
            TestTheme {
                BingeTimeInput(
                    hour,
                    minute,
                    { hour = it },
                    { minute = it },
                    is24Hour,
                    afternoon = afternoon,
                    onAfternoon = { afternoon = it },
                )
            }
        }
    }

    private fun checkFocus(is24Hour: Boolean) {
        show(is24Hour)
        val hourField = rule.onNodeWithText("Hour")
        val minuteField = rule.onNodeWithText("Minute")
        hourField.assertIsNotFocused()
        minuteField.assertIsNotFocused()
        hourField.performTouchInput { click(Offset(width * 0.2f, height * 0.65f)) }
        rule.waitForIdle()
        hourField.assertIsFocused()
        assertEquals(TextRange(2), hourField.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
        hourField.performImeAction()
        rule.waitForIdle()
        minuteField.assertIsFocused()
        assertEquals(TextRange(2), minuteField.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
    }

    @Test
    fun `24 hour entry opens without focus and puts the caret after the digits on focus`() = checkFocus(is24Hour = true)

    @Test
    fun `12 hour entry opens without focus and puts the caret after the digits on focus`() = checkFocus(is24Hour = false)

    @Test
    fun `typed digits and period changes stay with the caller`() {
        show(is24Hour = false)
        rule.onNodeWithText("Hour").performTextReplacement("ab123")
        assertEquals("12", hour)
        rule.onNodeWithText("p.m.").performClick()
        assertEquals(true, afternoon)
        assertEquals(727, timeOfDayMinutes(hour, minute, is24Hour = false, afternoon = afternoon))
    }

    @Test
    fun `times round trip midnight noon and minute precision and refuse invalid drafts`() {
        assertEquals(0, timeOfDayMinutes("00", "00", is24Hour = true))
        assertEquals(1439, timeOfDayMinutes("23", "59", is24Hour = true))
        assertEquals(0, timeOfDayMinutes("12", "00", is24Hour = false))
        assertEquals(720, timeOfDayMinutes("12", "00", is24Hour = false, afternoon = true))
        assertEquals(1050, timeOfDayMinutes("05", "30", is24Hour = false, afternoon = true))
        assertNull(timeOfDayMinutes("24", "00", is24Hour = true))
        assertNull(timeOfDayMinutes("00", "00", is24Hour = false))
        assertNull(timeOfDayMinutes("03", "60", is24Hour = true))
        assertNull(timeOfDayMinutes("", "07", is24Hour = true))
    }
}
