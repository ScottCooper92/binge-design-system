package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CONTENT = "content"
private const val OVERLAY = "overlay"
private const val EXIT_MILLIS = 300
private const val FRAME_MILLIS = 16L
private const val LATCH_FRAMES = 2
private const val MID_EXIT_FRAME = 8

/**
 * **A panel wired to its closer lets focus go as the exit starts, so the restore is a single request.**
 *
 * [TvOverlayExitTransitionFocusTest] shows a trapped overlay refusing a request from outside for its whole exit. Here the
 * real [TvSideSheetPanel] takes the closer: the closer's one request is granted once its latch frame has run, focus stays on the
 * content on every frame of the fade after it, and the trap is back when the panel has gone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOverlayCloserReleaseTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private var open by mutableStateOf(false)
    private lateinit var closer: TvOverlayCloser

    private fun setContent(wired: Boolean) {
        composeTestRule.setContent {
            val content = remember { FocusRequester() }
            closer = rememberTvOverlayCloser(restoreTo = content) { open = false }
            BingeTvTheme {
                Row {
                    Box(
                        Modifier
                            .testTag(CONTENT)
                            .size(80.dp)
                            .focusRequester(content)
                            .focusable(),
                    )
                    AnimatedVisibility(visible = open, enter = fadeIn(tween(EXIT_MILLIS)), exit = fadeOut(tween(EXIT_MILLIS))) {
                        TvSideSheetPanel(closer = closer.takeIf { wired }) {
                            Box(Modifier.testTag(OVERLAY).size(40.dp).focusable())
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        openAndFocusOverlay()
    }

    private fun openAndFocusOverlay() {
        composeTestRule.runOnIdle { open = true }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).assertIsFocused()
    }

    private fun advanceFrames(frames: Int) =
        repeat(frames) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.waitForIdle()
        }

    @Test
    fun `a wired panel lets the closer's single request through, and focus stays on the content through the exit`() {
        setContent(wired = true)
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.runOnIdle { closer.close() }
        // The close arms a latch and the closer's effect runs on the next frame; the exit then has nearly all its length to go.
        advanceFrames(LATCH_FRAMES)

        repeat((EXIT_MILLIS / FRAME_MILLIS).toInt() - LATCH_FRAMES) { frame ->
            composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
            if (frame == MID_EXIT_FRAME) {
                assertFalse("The trap must stay released in the middle of the exit, after the closer's latch has reset", closer.trapping)
            }
            advanceFrames(1)
        }
        composeTestRule.mainClock.advanceTimeBy(EXIT_MILLIS.toLong())
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
        assertTrue("The trap is back once the panel is gone", closer.trapping)
    }

    @Test
    fun `an unwired panel still refuses, and the offer after the dispose lands`() {
        setContent(wired = false)

        composeTestRule.runOnIdle { closer.close() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    @Test
    fun `a second open traps again, and its close is released again`() {
        setContent(wired = true)
        composeTestRule.runOnIdle { closer.close() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()

        openAndFocusOverlay()
        assertTrue("A reopened panel must trap, not inherit the last close's release", closer.trapping)

        composeTestRule.runOnIdle { closer.close() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }
}
