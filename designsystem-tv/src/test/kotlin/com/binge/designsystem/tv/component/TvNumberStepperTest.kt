package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val RANGE = 0..50
private const val STEP = 5

/** The number stepper's keys under a real D-pad: ← → step by the step, the range's ends are inert, and OK commits nothing. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvNumberStepperTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val reported = mutableListOf<Int>()
    private var value by mutableIntStateOf(0)

    private fun setStepper(start: Int) {
        value = start
        reported.clear()
        composeTestRule.setContent {
            BingeTvTheme {
                TvNumberStepper(
                    value = value,
                    onValueChange = {
                        reported += it
                        value = it
                    },
                    range = RANGE,
                    step = STEP,
                    valueLabel = "$value requests",
                )
            }
        }
        stepper().requestFocus()
        composeTestRule.waitForIdle()
        stepper().assertIsFocused()
    }

    private fun stepper() = composeTestRule.onNode(isFocusable())

    private fun press(key: Key) {
        stepper().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `right raises the value by the step and left lowers it`() {
        setStepper(start = 10)

        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionLeft)

        assertEquals(listOf(15, 20, 15), reported)
    }

    @Test
    fun `a step past the end stops at the end`() {
        setStepper(start = 48)

        press(Key.DirectionRight)

        assertEquals(listOf(50), reported)
    }

    @Test
    fun `at the top the press is consumed and reports nothing`() {
        setStepper(start = RANGE.last)

        press(Key.DirectionRight)

        assertEquals(emptyList<Int>(), reported)
        stepper().assertIsFocused()
    }

    @Test
    fun `at the bottom the press is consumed and reports nothing`() {
        setStepper(start = 0)

        press(Key.DirectionLeft)

        assertEquals(emptyList<Int>(), reported)
        stepper().assertIsFocused()
    }

    @Test
    fun `OK commits nothing`() {
        setStepper(start = 4)

        press(Key.DirectionCenter)

        assertEquals(emptyList<Int>(), reported)
    }

    @Test
    fun `the value is announced as the control's state`() {
        setStepper(start = 7)

        stepper().assert(
            SemanticsMatcher("state description is the label") {
                SemanticsProperties.StateDescription in it.config && it.config[SemanticsProperties.StateDescription] == "7 requests"
            },
        )
    }
}
