package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    val composeTestRule = createComposeRule()

    private var value by mutableStateOf<Int?>(10)

    private fun setSlider(
        initial: Int?,
        openEndLabel: String? = "Unlimited",
    ) {
        value = initial
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeNumberSlider(
                    value = value,
                    onValueChange = { value = it },
                    range = 1..100,
                    format = { "$it a week" },
                    description = "Movie requests",
                    openEndLabel = openEndLabel,
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
        assertEquals(2, composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("100 a week")).fetchSemanticsNodes().size)
    }

    @Test
    fun `the bottom of the range spends minus`() {
        setSlider(1)

        composeTestRule.onNodeWithContentDescription("Decrease Movie requests").assertIsNotEnabled()
    }

    @Test
    fun `the row shows its value and opens the slider in a sheet`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
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
}
