package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.component.TvCardRow
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.testing.ANCHOR_COALESCE_WAIT_MILLIS
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.settle
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val SYNOPSIS = "synopsis"
private const val ACTION = "action"
private const val SECTION = "cast"
private const val CARDS = 4

/**
 * A detail page routes arriving focus to the caller's action row, and back to the section the user browsed once they
 * have left the hero (#395). The hero opens with a focusable synopsis above the actions, so a geometric pick from the
 * rail would land there rather than on the row the page routes to.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailPageFocusTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    @Test
    fun `entry from the rail lands on the action row, not the first focusable in the hero`() {
        setPage()
        enterFromRail()

        composeTestRule.onNodeWithTag(ACTION).assertIsFocused()
    }

    @Test
    fun `re-entry from the rail lands on the section card last focused`() {
        setPage()
        enterFromRail()
        move(FocusDirection.Down)
        composeTestRule.onNodeWithTag("$SECTION-0").assertIsFocused()
        move(FocusDirection.Right)
        composeTestRule.onNodeWithTag("$SECTION-1").assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)
        enterFromRail()

        composeTestRule.onNodeWithTag("$SECTION-1").assertIsFocused()
    }

    @Test
    fun `a page seeded on a section takes entry there`() {
        setPage(initialFocusedSectionKey = SECTION)
        enterFromRail()

        composeTestRule.onNodeWithTag("$SECTION-0").assertIsFocused()
    }

    @Test
    fun `as an overlay the page claims first focus onto the action row`() {
        setPage(hosting = TvPageHosting.Overlay, withRail = false)
        composeTestRule.settle(ANCHOR_COALESCE_WAIT_MILLIS)

        composeTestRule.onNodeWithTag(ACTION).assertIsFocused()
    }

    private fun setPage(
        hosting: TvPageHosting = TvPageHosting.RailDestination,
        initialFocusedSectionKey: String? = null,
        withRail: Boolean = true,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                val actions = remember { FocusRequester() }
                Row {
                    if (withRail) Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvDetailPage(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        entryFocus = actions,
                        hosting = hosting,
                        initialFocusedSectionKey = initialFocusedSectionKey,
                    ) {
                        hero {
                            Column {
                                Box(Modifier.size(width = 320.dp, height = 48.dp).testTag(SYNOPSIS).focusable())
                                Box(
                                    Modifier
                                        .size(48.dp)
                                        .focusRequester(actions)
                                        .testTag(ACTION)
                                        .tvClickable(onFocusChanged = {}, onClick = {}),
                                )
                            }
                        }
                        section(SECTION) { onFocused ->
                            TvCardRow(
                                items = List(CARDS) { it },
                                key = { it },
                                cellWidth = 96.dp,
                                heading = "Cast",
                                onCellFocused = onFocused,
                            ) { item, _, onFocusChanged, cellModifier ->
                                Box(
                                    cellModifier
                                        .aspectRatio(2f / 3f)
                                        .testTag("$SECTION-$item")
                                        .tvClickable(onFocusChanged = onFocusChanged, onClick = {}),
                                )
                            }
                        }
                    }
                }
            }
        }
        if (withRail) composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun enterFromRail() = move(FocusDirection.Right)

    private fun move(direction: FocusDirection) {
        composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
        composeTestRule.waitForIdle()
    }
}
