package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What a screenshot cannot pin: one commit per landing, taps commit, and a held thumb stays where the caller holds it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeStopSliderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val stops =
        listOf(
            BingeStop("Strict", Icons.Filled.Star),
            BingeStop("Default", Icons.Filled.Public),
            BingeStop("Wide", Icons.Filled.Share),
            BingeStop("Anything", Icons.Filled.Block, BingeStopEmphasis.Warning),
        )

    private var selected by mutableStateOf(1)
    private var hold by mutableStateOf<Int?>(null)
    private val commits = mutableListOf<Int>()
    private val positions = mutableListOf<Int>()

    private fun setSlider() {
        composeTestRule.setContent {
            TestTheme {
                BingeStopSlider(
                    stops = stops,
                    selectedIndex = selected,
                    onCommit = {
                        commits += it
                        selected = it
                    },
                    description = "Licences",
                    holdIndex = hold,
                    onPositionChange = { positions += it },
                )
            }
        }
    }

    private fun announced(): String? =
        composeTestRule
            .onNodeWithContentDescription("Licences")
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    @Test
    fun `the slider announces the selected stop's label`() {
        setSlider()

        assertEquals("Default", announced())
    }

    @Test
    fun `a stop with a content description is announced by it, not by its short label`() {
        selected = 0
        composeTestRule.setContent {
            TestTheme {
                BingeStopSlider(
                    stops = listOf(BingeStop("CC BY", Icons.Filled.Star, contentDescription = "Public domain and CC BY")) + stops.drop(1),
                    selectedIndex = selected,
                    onCommit = { selected = it },
                    description = "Licences",
                )
            }
        }

        assertEquals("Public domain and CC BY", announced())
        composeTestRule.onNodeWithText("CC BY").assertExists()
    }

    @Test
    fun `a drag across the track commits once, where the thumb lands, and not on every step`() {
        setSlider()

        composeTestRule.onNodeWithContentDescription("Licences").performTouchInput {
            down(centerLeft)
            moveTo(center)
            moveTo(centerRight)
            up()
        }
        composeTestRule.waitForIdle()

        assertEquals("one commit for one drag: $commits", 1, commits.size)
        assertEquals(stops.lastIndex, commits.single())
        assertTrue("the caller was told of the thumb on the way: $positions", positions.isNotEmpty())
    }

    @Test
    fun `tapping a stop's name moves the thumb there and commits it once`() {
        setSlider()

        composeTestRule.onNodeWithText("Wide").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(2), commits)
        assertEquals("Wide", announced())
    }

    @Test
    fun `a held stop keeps the thumb there until the hold clears, then it returns to the selected stop`() {
        setSlider()

        hold = 3
        composeTestRule.waitForIdle()
        assertEquals("Anything", announced())

        hold = null
        composeTestRule.waitForIdle()
        assertEquals("Default", announced())
    }

    @Test
    fun `fewer than two stops is a mistake`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                TestTheme { BingeStopSlider(stops = stops.take(1), selectedIndex = 0, onCommit = {}, description = "Licences") }
            }
        }
    }
}
