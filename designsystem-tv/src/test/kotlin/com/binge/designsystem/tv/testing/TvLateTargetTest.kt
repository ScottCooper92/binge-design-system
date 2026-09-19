package com.binge.designsystem.tv.testing

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TARGET = "target"

/** A cushion past a delay's own frame count, comfortably covering the recomposition it triggers. */
private const val SETTLE_FRAMES = 10

/**
 * [TvLateTarget] must actually delay as advertised, or every consumer test built on it would go green for the
 * wrong reason (#2521's own concern about a fixture that doesn't delay). This pins its two halves: the sibling
 * shows until [TvLateTarget.dispose], and the real content composes only [TvLateTarget.delayFrames] frames after.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvLateTargetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the sibling shows until dispose flips true`() {
        var dispose by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    TvLateTarget(delayFrames = 1, dispose = dispose) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(TV_LATE_TARGET_SIBLING_TAG).assertExists()
        composeTestRule.onNodeWithTag(TARGET).assertDoesNotExist()

        dispose = true
        composeTestRule.waitForIdle()
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertExists()
        composeTestRule.onNodeWithTag(TV_LATE_TARGET_SIBLING_TAG).assertDoesNotExist()
    }

    @Test
    fun `content does not compose until delayFrames have ticked past dispose`() {
        val delayFrames = 10
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    TvLateTarget(delayFrames = delayFrames, dispose = true) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        repeat(delayFrames - 1) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(TARGET).assertDoesNotExist()

        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertExists()
    }

    @Test
    fun `a zero delay composes content on the first frame`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    TvLateTarget(delayFrames = 0, dispose = true) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TARGET).assertExists()
    }
}
