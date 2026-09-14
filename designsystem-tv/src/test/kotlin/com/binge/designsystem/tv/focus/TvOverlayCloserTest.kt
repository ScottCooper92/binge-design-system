package com.binge.designsystem.tv.focus

import android.app.Application
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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val GRID = "grid"
private const val OVERLAY = "overlay"

/**
 * The contract for [rememberTvOverlayCloser], the one closer the thirteen hand-copied `closing*` latches fold
 * into: [TvOverlayCloser.close] disposes the overlay and returns focus to the entry, and the latch resets so the
 * overlay can be opened and closed again; a latch left set is what breaks the second open.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOverlayCloserTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `close disposes the overlay and restores focus to the entry`() {
        val fixture = fixture()
        fixture.openAndFocusOverlay()

        composeTestRule.runOnIdle { fixture.closer.close() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(GRID).assertIsFocused()
    }

    /**
     * The latch resets after a close, so a re-opened overlay closes and restores again. Removing the `closing =
     * false` reset leaves the latch armed: the second close never fires and focus stays off the grid.
     */
    @Test
    fun `the closer re-arms across opens`() {
        val fixture = fixture()
        fixture.openAndFocusOverlay()
        composeTestRule.runOnIdle { fixture.closer.close() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(GRID).assertIsFocused()

        fixture.openAndFocusOverlay()
        composeTestRule.runOnIdle { fixture.closer.close() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(GRID).assertIsFocused()
    }

    /** Two `close()`s before the latch settles dispose the overlay once — a dismissal path cannot double-fire. */
    @Test
    fun `a second close before the latch settles is a no-op`() {
        val fixture = fixture()
        fixture.openAndFocusOverlay()

        composeTestRule.runOnIdle {
            fixture.closer.close()
            fixture.closer.close()
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(GRID).assertIsFocused()
        assertEquals("onClose must run exactly once for a double close", 1, fixture.closeCount)
    }

    /**
     * The close restores focus out of a **focus-trapped** overlay — the shape `TvSideSheet` and `TvFilterPanel`
     * actually ship, and the one the frame wait inside [restoreTvOverlayFocus] exists for.
     *
     * The other tests here open an untrapped overlay, where there is nothing to swallow a request and the wait is
     * therefore inert: both orderings pass, so none of them can fail if it is removed. Add [tvExitFocusGroup] and
     * the difference appears. Fault injection, run against this fixture: replacing `restoreTvOverlayFocus(entry)`
     * with a bare `entry.requestFocus()` on the close leaves the grid unfocused and fails this test alone.
     *
     * That contradicts a caveat this suite and `TvArrivalFocusTest` both carried — that the wait's necessity is a
     * device-timing property the JVM harness cannot reproduce. It reproduces; what it needed was the trap,
     * not a device.
     */
    @Test
    fun `close restores focus out of a focus-trapped overlay`() {
        val fixture = fixture(trapped = true)
        fixture.openAndFocusOverlay()

        composeTestRule.runOnIdle { fixture.closer.close() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(OVERLAY).assertDoesNotExist()
        composeTestRule.onNodeWithTag(GRID).assertIsFocused()
    }

    private fun fixture(trapped: Boolean = false): Fixture {
        val fixture = Fixture()
        composeTestRule.setContent {
            BingeTvTheme {
                val entry = remember { FocusRequester() }
                fixture.closer = rememberTvOverlayCloser(restoreTo = entry) {
                    fixture.closeCount++
                    fixture.overlayOpen = false
                }
                Row {
                    // The rail stand-in: a focusable elsewhere, so a failed restore has somewhere for focus to be
                    // other than the grid — otherwise Compose recovers onto the grid unaided and the test passes
                    // for the wrong reason.
                    Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                    Box(
                        Modifier
                            .testTag(GRID)
                            .size(80.dp)
                            .focusRequester(entry)
                            .focusable(),
                    )
                    if (fixture.overlayOpen) {
                        if (trapped) {
                            Box(Modifier.size(80.dp).tvExitFocusGroup()) {
                                Box(Modifier.testTag(OVERLAY).size(40.dp).focusable())
                            }
                        } else {
                            Box(Modifier.testTag(OVERLAY).size(80.dp).focusable())
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
        var closeCount = 0
        lateinit var closer: TvOverlayCloser

        /** Show the overlay without touching focus. */
        fun openOverlay() {
            composeTestRule.runOnIdle { overlayOpen = true }
            composeTestRule.waitForIdle()
        }

        /** Show the overlay and plant focus on it — the state a dismissal starts from. */
        fun openAndFocusOverlay() {
            openOverlay()
            composeTestRule.onNodeWithTag(OVERLAY).requestFocus()
            composeTestRule.onNodeWithTag(OVERLAY).assertIsFocused()
        }
    }
}
