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
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

/**
 * **A trap wired to its closer lets focus go as the exit starts, so the restore is a single request.**
 *
 * [TvOverlayExitTransitionFocusTest] shows a trapped overlay refusing a request from outside for its whole exit. Here the
 * trap reads [TvOverlayCloser.trapping], and the closer's one request is granted in the frame the exit starts, with focus on
 * the content on every frame of the fade after it.
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
                        Box(Modifier.size(80.dp).tvExitFocusGroup(trapping = { !wired || closer.trapping })) {
                            Box(Modifier.testTag(OVERLAY).size(40.dp).focusable())
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { open = true }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).assertIsFocused()
    }

    @Test
    fun `a wired trap lets the closer's single request through, and focus stays on the content through the exit`() {
        setContent(wired = true)
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.runOnIdle { closer.close() }
        // The close arms a latch and the closer's effect runs on the next frame; the exit then has nearly all its length to go.
        repeat(LATCH_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.waitForIdle()
        }

        repeat((EXIT_MILLIS / FRAME_MILLIS).toInt() - LATCH_FRAMES) {
            composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.waitForIdle()
        }
        composeTestRule.mainClock.advanceTimeBy(EXIT_MILLIS.toLong())
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    @Test
    fun `an unwired trap still refuses, and the offer after the dispose lands`() {
        setContent(wired = false)

        composeTestRule.runOnIdle { closer.close() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    @Test
    fun `trapping is false from close until the overlay has gone, then true again for the next open`() {
        setContent(wired = true)
        assertEquals(true, closer.trapping)

        composeTestRule.runOnIdle { closer.close() }
        composeTestRule.waitForIdle()

        assertFalse("A second open must trap again, not inherit the closed latch", !closer.trapping)
    }
}
