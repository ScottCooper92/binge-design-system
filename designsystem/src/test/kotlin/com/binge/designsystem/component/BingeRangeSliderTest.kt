package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The thumbs' announced text is the whole accessible surface of the slider, and no screenshot shows
 * it: a thumb that read as a bare percentage would look identical from the call site.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeRangeSliderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setSlider(values: ClosedFloatingPointRange<Float>, openEndLabel: String? = null) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeRangeSlider(
                    values = values,
                    onValuesChange = {},
                    valueRange = 0f..240f,
                    steps = 15,
                    valueLabel = { "${it.toInt()} min" },
                    startThumbDescription = "Minimum runtime",
                    endThumbDescription = "Maximum runtime",
                    openEndLabel = openEndLabel,
                )
            }
        }
    }

    private fun stateOf(description: String): String? =
        composeTestRule
            .onAllNodesWithContentDescription(description, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .firstNotNullOfOrNull { it.config.getOrNull(SemanticsProperties.StateDescription) }

    @Test
    fun `each thumb announces its own description and its formatted value`() {
        setSlider(60f..150f)

        assertEquals("60 min", stateOf("Minimum runtime"))
        assertEquals("150 min", stateOf("Maximum runtime"))
    }

    @Test
    fun `the range reads as one line above the track, with the track's ends named under it`() {
        setSlider(60f..150f)

        composeTestRule.onNodeWithText("60 min – 150 min").assertExists()
        composeTestRule.onNodeWithText("0 min").assertExists()
        composeTestRule.onNodeWithText("240 min").assertExists()
    }

    @Test
    fun `a thumb on an open end reads the open end's label, and the track's top is not named as a number`() {
        setSlider(90f..240f, openEndLabel = "240+ min")

        composeTestRule.onNodeWithText("90 min – 240+ min").assertExists()
        assertEquals("240+ min", stateOf("Maximum runtime"))
        composeTestRule.onNodeWithText("240 min").assertDoesNotExist()
    }

    @Test
    fun `thumbs on one value read it once`() {
        setSlider(120f..120f)

        composeTestRule.onNodeWithText("120 min").assertExists()
    }

    @Test
    fun `there is one thumb per end, not a second copy of either`() {
        setSlider(0f..240f)

        composeTestRule
            .onAllNodesWithContentDescription("Minimum runtime", useUnmergedTree = true)
            .assertCountEquals(1)
        composeTestRule
            .onAllNodesWithContentDescription("Maximum runtime", useUnmergedTree = true)
            .assertCountEquals(1)
    }

    /**
     * Consumers keep a bound the slider never offered (a deep link's 6.3, off a half-point grid) by treating a thumb
     * that reports its old value as untouched. That only works if dragging the *other* thumb reports the untouched
     * one back exactly, not snapped to the nearest step, so it is pinned here rather than assumed.
     */
    @Test
    fun `dragging one thumb reports the other thumb's off-grid value unchanged`() {
        val reported = mutableListOf<ClosedFloatingPointRange<Float>>()
        var values by mutableStateOf(6.3f..8f)
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeRangeSlider(
                    values = values,
                    onValuesChange = {
                        reported += it
                        values = it
                    },
                    valueRange = 0f..10f,
                    steps = 19,
                    valueLabel = { it.toString() },
                    startThumbDescription = "Minimum",
                    endThumbDescription = "Maximum",
                )
            }
        }

        composeTestRule.onAllNodesWithContentDescription("Maximum", useUnmergedTree = true)[0].performTouchInput {
            swipe(center, center + Offset(x = -SWIPE_PX, y = 0f))
        }
        composeTestRule.waitForIdle()

        check(reported.isNotEmpty()) { "the drag reported no change, so the test proved nothing" }
        reported.forEach { assertEquals("the untouched start thumb moved", 6.3f, it.start) }
    }

    @Test
    fun `dragging a thumb a whole step still moves it`() {
        val reported = mutableListOf<ClosedFloatingPointRange<Float>>()
        var values by mutableStateOf(6f..8f)
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeRangeSlider(
                    values = values,
                    onValuesChange = {
                        reported += it
                        values = it
                    },
                    valueRange = 0f..10f,
                    steps = 19,
                    valueLabel = { it.toString() },
                    startThumbDescription = "Minimum",
                    endThumbDescription = "Maximum",
                )
            }
        }

        composeTestRule.onAllNodesWithContentDescription("Maximum", useUnmergedTree = true)[0].performTouchInput {
            swipe(center, center + Offset(x = -SWIPE_PX, y = 0f))
        }
        composeTestRule.waitForIdle()

        check(reported.isNotEmpty()) { "the drag reported no change, so the test proved nothing" }
        assertEquals("the dragged end thumb did not move", true, reported.last().endInclusive < 8f)
        assertEquals("the untouched start thumb moved", 6f, reported.last().start)
    }

    /**
     * `steps = 0` is Material 3's documented way to ask for a continuous, non-snapping slider, so
     * there is no grid for a small drag to be "off" of. The untouched-thumb filtering must not run
     * at all here, or every ordinary drag tick — smaller than half the whole track — would be
     * mistaken for the thumb not having moved and reverted.
     */
    @Test
    fun `a continuous slider with steps = 0 still reports an ordinary drag`() {
        val reported = mutableListOf<ClosedFloatingPointRange<Float>>()
        var values by mutableStateOf(6f..8f)
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeRangeSlider(
                    values = values,
                    onValuesChange = {
                        reported += it
                        values = it
                    },
                    valueRange = 0f..10f,
                    steps = 0,
                    valueLabel = { it.toString() },
                    startThumbDescription = "Minimum",
                    endThumbDescription = "Maximum",
                )
            }
        }

        composeTestRule.onAllNodesWithContentDescription("Maximum", useUnmergedTree = true)[0].performTouchInput {
            swipe(center, center + Offset(x = -SWIPE_PX, y = 0f))
        }
        composeTestRule.waitForIdle()

        check(reported.isNotEmpty()) { "the drag reported no change, so the test proved nothing" }
        assertEquals("the dragged end thumb did not move", true, reported.last().endInclusive < 8f)
        assertEquals("the untouched start thumb moved", 6f, reported.last().start)
    }
}

private const val SWIPE_PX = 120f
