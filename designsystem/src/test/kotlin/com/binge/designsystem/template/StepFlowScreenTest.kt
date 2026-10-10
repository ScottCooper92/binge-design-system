package com.binge.designsystem.template

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.theme.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STEPS = 3
private const val MID_SLIDE_MILLIS = 100L

/**
 * What the flow does that no frame can show: the dots name the step for a screen reader, BACK and its gesture
 * step back from the second step and fall through on the first, a changing step keeps the leaving one drawing
 * itself while it slides, and a step that scrolls itself can hold a lazy grid, which a forced outer scroll would
 * refuse to measure.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StepFlowScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var step by mutableIntStateOf(0)
    private var backs = 0

    private fun setFlow(
        reduceMotion: Boolean = false,
        contentScrolls: Boolean = true,
        content: @Composable (Int) -> Unit = { Text("Step $it") },
    ) {
        composeTestRule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
                    StepFlowScreen(
                        stepCount = STEPS,
                        currentStep = step,
                        onBack = {
                            backs++
                            step--
                        },
                        contentScrolls = { contentScrolls },
                    ) { content(it) }
                }
            }
        }
    }

    /** Steps forward with the clock paused, then runs to the middle of the slide. */
    private fun goForward() {
        step = 1
        // The paused clock runs no frame of its own to announce the write, so the recomposer is told directly.
        Snapshot.sendApplyNotifications()
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeBy(MID_SLIDE_MILLIS)
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()
    }

    @Test
    fun backStepsBackFromTheSecondStep() {
        step = 1
        setFlow()

        pressBack()

        assertEquals(1, backs)
        composeTestRule.onNodeWithText("Step 0").assertExists()
    }

    @Test
    fun backOnTheFirstStepFallsThroughToTheHost() {
        setFlow()

        pressBack()

        assertEquals(0, backs)
    }

    @Test
    fun theLeavingStepKeepsDrawingItselfWhileItSlides() {
        setFlow()
        composeTestRule.mainClock.autoAdvance = false

        goForward()

        composeTestRule.onNodeWithText("Step 0").assertExists()
        composeTestRule.onNodeWithText("Step 1").assertExists()
    }

    @Test
    fun reducedMotionSwapsTheStepInPlace() {
        setFlow(reduceMotion = true)
        composeTestRule.mainClock.autoAdvance = false

        goForward()

        composeTestRule.onNodeWithText("Step 0").assertDoesNotExist()
        composeTestRule.onNodeWithText("Step 1").assertExists()
    }

    @Test
    fun theDotsNameTheCurrentStepAndTheCount() {
        step = 1
        setFlow()

        composeTestRule.onNodeWithContentDescription("Step 2 of 3").assertExists()
    }

    private fun setTitledFlow(onExit: () -> Unit) {
        composeTestRule.setContent {
            TestTheme {
                StepFlowScreen(
                    stepCount = STEPS,
                    currentStep = step,
                    onBack = {
                        backs++
                        step--
                    },
                    title = "Edit connection",
                    onExit = onExit,
                ) { Text("Step $it") }
            }
        }
    }

    @Test
    fun aTitledFlowShowsItsTitleAsAHeading() {
        setTitledFlow(onExit = {})

        composeTestRule.onNode(hasText("Edit connection") and isHeading()).assertExists()
    }

    @Test
    fun theFirstStepsBackLeavesTheFlowWhenItHasAnExit() {
        var exits = 0
        setTitledFlow(onExit = { exits++ })

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        pressBack()

        assertEquals(2, exits)
        assertEquals(0, backs)
    }

    @Test
    fun aLaterStepsBackStillStepsBackWhenTheFlowHasAnExit() {
        var exits = 0
        step = 1
        setTitledFlow(onExit = { exits++ })

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(0, exits)
        assertEquals(1, backs)
    }

    @Test
    fun aStepSplitsInASpaceWiderThanTallOrExpandedAndStacksInANarrowPane() {
        val expanded = 840.dp
        assertTrue(splitsStep(width = 891.dp, height = 411.dp, expandedWidth = expanded))
        assertTrue(splitsStep(width = 840.dp, height = 1180.dp, expandedWidth = expanded))
        assertFalse(splitsStep(width = 400.dp, height = 1280.dp, expandedWidth = expanded))
        assertFalse(splitsStep(width = 411.dp, height = 891.dp, expandedWidth = expanded))
    }

    @Test
    fun theSplitHoldsWhileTheKeyboardShrinksTheSpace() {
        // A portrait flow whose height the keyboard cuts below its width would measure as split.
        val measuredWithKeyboard = splitsStep(width = 690.dp, height = 470.dp, expandedWidth = 840.dp)
        assertTrue(measuredWithKeyboard)
        assertFalse(heldWhileIme(measured = measuredWithKeyboard, imeVisible = true, held = false))
        assertTrue(heldWhileIme(measured = false, imeVisible = true, held = true))
        assertTrue(heldWhileIme(measured = true, imeVisible = false, held = false))
        assertTrue(heldWhileIme(measured = true, imeVisible = true, held = null))
    }

    @Test
    fun aStepThatScrollsItselfHoldsALazyGrid() {
        setFlow(contentScrolls = false) {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
                items(40) { index -> Text("Tile $index", modifier = Modifier.height(80.dp)) }
            }
        }

        composeTestRule.onNodeWithText("Tile 0").assertExists()
    }
}
