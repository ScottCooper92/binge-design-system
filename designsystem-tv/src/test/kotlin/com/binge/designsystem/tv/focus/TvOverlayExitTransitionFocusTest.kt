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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * **A focus restore must outlive the exit transition of the surface it is restoring from.**
 *
 * [TvOverlayCloserTest] proves the one-frame wait matters when the overlay disposes on the close. The pop is the
 * harder shape and the one that reached a device: nav3 keeps the outgoing surface composed for its whole exit
 * transition, and a `tvExitFocusGroup` refuses every request from outside it for that whole window. So a restore
 * that fires once, a frame after the stack empties, aims into a window where it cannot succeed — and its result
 * was discarded, so nothing noticed.
 *
 * The overlay here leaves through an [AnimatedVisibility] exit rather than disposing on the state change, which
 * is what makes the window real: the trapped node stays composed and focused for [EXIT_MILLIS] after the close.
 *
 * Assertions are on the **returned grant**, not on where focus ends up. Recovery after the disposal picks
 * geometrically and this harness does not reproduce a device's pick, so a focus assertion alone would pass
 * whether or not the restore did anything. Fault injection, run against this fixture: reverting
 * `restoreTvOverlayFocus` to a single request makes `granted` false and fails the first test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOverlayExitTransitionFocusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `restore keeps offering across the exit transition and lands when the trap disposes`() {
        val fixture = fixture()
        fixture.openAndFocusOverlay()

        fixture.closeOverlay()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        assertEquals("The restore must land once the trap's exit transition finishes", true, fixture.granted)
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /**
     * The premise, as a unit fact: a focus trap refuses a request from outside it, so a single-shot restore aimed
     * into the exit transition cannot succeed however well timed. The window is not a race the caller can win, it
     * is a refusal for as long as the trap is composed.
     */
    @Test
    fun `a request from outside is refused while the trap is composed`() {
        val fixture = fixture()
        fixture.openAndFocusOverlay()

        assertFalse(
            "A request aimed past a tvExitFocusGroup must be cancelled, not granted",
            fixture.requestContentFocusOnce(),
        )
        composeTestRule.onNodeWithTag(CONTENT).assertIsNotFocused()
    }

    private fun fixture(): Fixture {
        val fixture = Fixture()
        composeTestRule.setContent {
            val content = remember { FocusRequester() }
            fixture.contentFocus = content
            BingeTvTheme {
                LaunchedEffect(fixture.restoring) {
                    if (fixture.restoring) fixture.granted = restoreTvOverlayFocus(content)
                }
                Row {
                    // A focusable elsewhere, so the disposal has somewhere to recover to other than the content.
                    Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                    Box(
                        Modifier
                            .testTag(CONTENT)
                            .size(80.dp)
                            .focusRequester(content)
                            .focusable(),
                    )
                    AnimatedVisibility(
                        visible = fixture.overlayOpen,
                        enter = fadeIn(tween(EXIT_MILLIS)),
                        exit = fadeOut(tween(EXIT_MILLIS)),
                    ) {
                        Box(Modifier.size(80.dp).tvExitFocusGroup()) {
                            Box(Modifier.testTag(OVERLAY).size(40.dp).focusable())
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        return fixture
    }

    private inner class Fixture {
        var overlayOpen by mutableStateOf(false)
        var restoring by mutableStateOf(false)
        var granted: Boolean? = null
        lateinit var contentFocus: FocusRequester

        fun openAndFocusOverlay() {
            composeTestRule.runOnIdle { overlayOpen = true }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(OVERLAY).requestFocus()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(OVERLAY).assertIsFocused()
        }

        /** Both in one frame, as a pop does: the stack empties and the host's restore effect arms together. */
        fun closeOverlay() {
            composeTestRule.runOnIdle {
                overlayOpen = false
                restoring = true
            }
        }

        fun requestContentFocusOnce(): Boolean = composeTestRule.runOnIdle { contentFocus.requestFocus() }
    }

    private companion object {
        const val RAIL = "rail"
        const val CONTENT = "content"
        const val OVERLAY = "overlay"

        /** Stands in for `tvContentSwap`'s crossfade: long enough that a single request has spent itself inside it. */
        const val EXIT_MILLIS = 300
    }
}
