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
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * **A focus trap refuses a request from outside it for as long as it is composed.**
 *
 * The overlay here leaves through an [AnimatedVisibility] exit rather than disposing on the state change, so the trapped node
 * stays composed and focused for [EXIT_MILLIS] after the close. A request aimed into that window is cancelled however well
 * timed, which is why an overlay must report its disposal and take the request after it is gone, not retry during the exit.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOverlayExitTransitionFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

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
        lateinit var contentFocus: FocusRequester

        fun openAndFocusOverlay() {
            composeTestRule.runOnIdle { overlayOpen = true }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(OVERLAY).requestFocus()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(OVERLAY).assertIsFocused()
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
