package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.TV_FOCUS_SINK_TAG
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val ENTRY = "entry"
private const val SECOND_ENTRY = "second-entry"
private const val LAST_ROW = "last-row"
private const val PINNED = "pinned"

/** Enough rows that the action pane overflows a 540dp panel, so the pinned action sits below a scroll. */
private const val OVERFLOW_ROWS = 12

/**
 * What the page templates promise about focus: each hosting mode places arrival the way
 * `docs/tv-foundation.md` says it must, a step change re-places it, and a pinned action stays reachable under
 * a scrolled action pane.
 *
 * A rail stand-in holds focus first in every fixture, so a template that claimed nothing would leave focus
 * there rather than have Compose recover it onto the page unaided.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTemplateFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `an overlay page claims arrival on its entry target`() {
        composeTestRule.setContent {
            Hosted { TwoPane(hosting = TvPageHosting.Overlay) }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(ENTRY).assertIsFocused()
    }

    @Test
    fun `a pre-shell page places first focus on its entry target`() {
        composeTestRule.setContent {
            Hosted { TwoPane(hosting = TvPageHosting.PreShell) }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(ENTRY).assertIsFocused()
    }

    @Test
    fun `a rail destination never claims focus, but routes entry from the rail to its target`() {
        composeTestRule.setContent {
            Hosted { TwoPane(hosting = TvPageHosting.RailDestination) }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ENTRY).assertIsNotFocused()

        composeTestRule.onNodeWithTag(RAIL).performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(ENTRY).assertIsFocused()
    }

    @Test
    fun `the pinned action is one press below the last row of a scrolled action pane`() {
        composeTestRule.setContent {
            Hosted { TwoPane(hosting = TvPageHosting.Overlay, rows = OVERFLOW_ROWS) }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(LAST_ROW).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(LAST_ROW).performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PINNED).assertIsFocused()
    }

    @Test
    fun `a step flow re-places focus on the new step's target when the step changes`() {
        var step by mutableIntStateOf(0)
        composeTestRule.setContent {
            Hosted {
                val first = remember { FocusRequester() }
                val second = remember { FocusRequester() }
                TvStepFlow(
                    stepCount = 2,
                    currentStep = step,
                    progressLabel = "Step ${step + 1} of 2",
                    entry = if (step == 0) first else second,
                ) {
                    if (step == 0) {
                        Target(ENTRY, first)
                    } else {
                        Target(SECOND_ENTRY, second)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ENTRY).assertIsFocused()

        step = 1
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SECOND_ENTRY).assertIsFocused()
    }

    @Test
    fun `a message page with an action lands arrival on the primary action`() {
        composeTestRule.setContent {
            Hosted {
                TvMessagePage(
                    body = "Body",
                    hosting = TvPageHosting.Overlay,
                    primary = TvPageAction("Retry") {},
                    secondary = TvPageAction("Close") {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Retry").assertIsFocused()
    }

    @Test
    fun `an overlay step flow re-places focus on the new step's target when the step changes`() {
        var step by mutableIntStateOf(0)
        composeTestRule.setContent {
            Hosted {
                val first = remember { FocusRequester() }
                val second = remember { FocusRequester() }
                TvStepFlow(
                    stepCount = 2,
                    currentStep = step,
                    progressLabel = "Step ${step + 1} of 2",
                    hosting = TvPageHosting.Overlay,
                    entry = if (step == 0) first else second,
                ) {
                    if (step == 0) {
                        Target(ENTRY, first)
                    } else {
                        Target(SECOND_ENTRY, second)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ENTRY).assertIsFocused()

        step = 1
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SECOND_ENTRY).assertIsFocused()
    }

    @Test
    fun `an overlay message page with no action claims focus on its sink`() {
        composeTestRule.setContent {
            Hosted { TvMessagePage(body = "Body", hosting = TvPageHosting.Overlay) }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()
    }

    @Test
    fun `a loading overlay message page claims focus on its sink`() {
        composeTestRule.setContent {
            Hosted {
                TvMessagePage(
                    body = "Body",
                    hosting = TvPageHosting.Overlay,
                    primary = TvPageAction("Retry") {},
                    loading = true,
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()
    }

    @Composable
    private fun Hosted(page: @Composable () -> Unit) {
        BingeTvTheme {
            Row {
                Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                Box(Modifier.weight(1f).fillMaxHeight()) { page() }
            }
        }
    }

    @Composable
    private fun TwoPane(hosting: TvPageHosting, rows: Int = 2) {
        val entry = remember { FocusRequester() }
        TvTwoPanePage(
            hosting = hosting,
            entry = entry,
            pinnedAction = { Box(Modifier.testTag(PINNED).size(80.dp).focusable()) },
            copy = { TvTwoPaneCopy(headline = "Headline", body = "Body") },
            action = {
                Target(ENTRY, entry)
                repeat(rows - 1) { index ->
                    val tag = if (index == rows - 2) LAST_ROW else "row-$index"
                    Box(Modifier.testTag(tag).size(80.dp).focusable())
                }
            },
        )
    }

    @Composable
    private fun Target(tag: String, requester: FocusRequester) {
        Box(
            Modifier
                .testTag(tag)
                .size(80.dp)
                .focusRequester(requester)
                .focusable(),
        )
    }
}
