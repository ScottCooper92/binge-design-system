package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The steppers and the open end are what a drag test cannot pin: one stop each way, and past the top is `null`. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeNumberSliderTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    private var value by mutableStateOf<Int?>(10)
    private val commits = mutableListOf<Int?>()

    private fun setSlider(
        initial: Int?,
        openEndLabel: String? = "Unlimited",
        range: IntRange = 1..100,
        step: Int = 1,
    ) {
        value = initial
        composeTestRule.setContent {
            TestTheme {
                BingeNumberSlider(
                    value = value,
                    onValueChange = { value = it },
                    range = range,
                    format = { "$it a week" },
                    description = "Movie requests",
                    openEndLabel = openEndLabel,
                    onValueChangeFinished = { commits += value },
                    step = step,
                )
            }
        }
    }

    private fun sliderState(): String? =
        composeTestRule
            .onNodeWithContentDescription("Movie requests")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    @Test
    fun `the value reads large, and the slider announces it`() {
        setSlider(10)

        composeTestRule.onNodeWithText("10 a week").assertExists()
        assertEquals("10 a week", sliderState())
    }

    @Test
    fun `a drag across the track moves the value, and the caller gets it`() {
        setSlider(10)

        composeTestRule.onNodeWithContentDescription("Movie requests").performTouchInput {
            down(center)
            moveTo(centerRight)
            up()
        }
        composeTestRule.waitForIdle()

        assertTrue("a swipe towards the end must raise the value", (value ?: Int.MAX_VALUE) > 50)
    }

    @Test
    fun `dragging reports draft values and commits once on release`() {
        setSlider(10)
        val slider = composeTestRule.onNodeWithContentDescription("Movie requests")
        slider.performTouchInput {
            down(center)
            moveTo(centerRight)
        }
        assertTrue((value ?: 0) > 50)
        assertTrue(commits.isEmpty())
        slider.performTouchInput { up() }
        assertEquals(listOf(value), commits)
    }

    @Test
    fun `steppers and keyboard changes commit the value they picked`() {
        setSlider(10)
        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        val slider = composeTestRule.onNodeWithContentDescription("Movie requests")
        slider.requestFocus()
        slider.performKeyInput { pressKey(Key.DirectionRight) }
        assertEquals(listOf(11, 10, 11), commits)
    }

    @Test
    fun `stored values between steps or beyond the range survive until the user moves`() {
        setSlider(7, openEndLabel = null, range = 0..120, step = 5)
        composeTestRule.onNodeWithText("7 a week").assertExists()
        assertTrue(commits.isEmpty())
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(5, value)
        composeTestRule.runOnIdle { value = 300 }
        composeTestRule.onNodeWithText("300 a week").assertExists()
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals("the first step down from above the range lands on its last stop", 120, value)
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(115, value)
        assertEquals(listOf(5, 120, 115), commits)
    }

    @Test
    fun `the first step up from below the range lands on its first stop`() {
        setSlider(0, openEndLabel = null, range = 1..90)
        composeTestRule.onNodeWithText("0 a week").assertExists()
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertEquals(1, value)
        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertEquals(2, value)
    }

    @Test
    fun `from above the range, plus steps onto the open end and minus onto the range's last stop`() {
        setSlider(120, openEndLabel = "Unlimited", range = 1..90)
        composeTestRule.onNodeWithText("120 a week").assertExists()

        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(90, value)
        composeTestRule.runOnIdle { value = 120 }
        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertNull(value)
    }

    @Test
    fun `from between two stops a step lands on the stop on that side`() {
        setSlider(7, openEndLabel = null, range = 0..120, step = 5)
        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertEquals(10, value)
        composeTestRule.runOnIdle { value = 7 }
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(5, value)
    }

    @Test
    fun `the right arrow steps one stop and the left arrow steps back`() {
        setSlider(10)
        val slider = composeTestRule.onNodeWithContentDescription("Movie requests")

        slider.requestFocus()
        slider.performKeyInput { pressKey(Key.DirectionRight) }
        assertEquals(11, value)
        slider.performKeyInput { pressKey(Key.DirectionLeft) }
        slider.performKeyInput { pressKey(Key.DirectionLeft) }
        assertEquals(9, value)
    }

    @Test
    fun `the steppers move one stop each way`() {
        setSlider(10)

        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertEquals(11, value)
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(9, value)
    }

    @Test
    fun `plus from the top steps onto the open end, and minus steps back off it`() {
        setSlider(100)

        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertNull(value)
        composeTestRule.onNodeWithText("Unlimited").assertExists()
        composeTestRule.onNodeWithContentDescription("Increase Movie requests").assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(100, value)
    }

    @Test
    fun `a closed range stops at its top, and names it under the track`() {
        setSlider(100, openEndLabel = null)

        composeTestRule.onNodeWithContentDescription("Increase Movie requests").assertIsNotEnabled()
        composeTestRule.onNodeWithText("1 a week").assertExists()
        // The readout and the bound under the track's end.
        composeTestRule.onAllNodesWithText("100 a week").assertCountEquals(2)
    }

    @Test
    fun `the bottom of the range spends minus`() {
        setSlider(1)

        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").assertIsNotEnabled()
    }

    @Test
    fun `the row shows its value and opens the slider in a sheet`() {
        composeTestRule.setContent {
            TestTheme {
                ItemGroup(
                    title = null,
                    rows =
                        listOf(
                            bingeNumberItem(
                                icon = Icons.Filled.Movie,
                                title = "Movie requests",
                                value = value,
                                range = 1..100,
                                format = { "$it a week" },
                                onChange = { value = it },
                                openEndLabel = "Unlimited",
                            ),
                        ),
                )
            }
        }
        value = null
        composeTestRule.onNodeWithText("Unlimited").assertExists().performClick()

        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").performClick()
        assertEquals(100, value)
    }

    private val landed = mutableListOf<Int?>()

    private fun setItem() {
        value = 10
        composeTestRule.setContent {
            TestTheme {
                ItemGroup(
                    title = null,
                    rows =
                        listOf(
                            bingeNumberItem(
                                icon = Icons.Filled.Movie,
                                title = "Movie requests",
                                value = value,
                                range = 1..100,
                                format = { "$it a week" },
                                onChange = { value = it },
                                openEndLabel = "Unlimited",
                                onValueChangeFinished = { landed += it },
                            ),
                        ),
                )
            }
        }
        composeTestRule.onNodeWithText("10 a week").performClick()
    }

    @Test
    fun `the row's sheet reports the landed value once for a stepper tap`() {
        setItem()

        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()

        assertEquals(listOf<Int?>(11), landed)
        assertEquals(11, value)
    }

    @Test
    fun `the row's sheet reports nothing while a drag moves and the landed value on release`() {
        setItem()
        val slider = composeTestRule.onNodeWithContentDescription("Movie requests")
        slider.performTouchInput {
            down(center)
            moveTo(centerRight)
        }
        assertTrue((value ?: 0) > 50)
        assertTrue("reported before the drag ended: $landed", landed.isEmpty())

        slider.performTouchInput { up() }

        assertEquals(listOf(value), landed)
    }

    @Test
    fun `a row given no callback still changes its value as before`() {
        value = 10
        composeTestRule.setContent {
            TestTheme {
                ItemGroup(
                    title = null,
                    rows =
                        listOf(
                            bingeNumberItem(
                                icon = Icons.Filled.Movie,
                                title = "Movie requests",
                                value = value,
                                range = 1..100,
                                format = { "$it a week" },
                                onChange = { value = it },
                            ),
                        ),
                )
            }
        }
        composeTestRule.onNodeWithText("10 a week").performClick()
        composeTestRule.onNodeWithContentDescription("Increase Movie requests").performClick()
        assertEquals(11, value)
    }
}
