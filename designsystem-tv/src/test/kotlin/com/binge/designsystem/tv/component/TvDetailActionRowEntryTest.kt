package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.template.TvDetailPage
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val SYNOPSIS = "synopsis"

/**
 * Entry to a detail page lands on the first action of the real [TvDetailActionRow], not on the focusable synopsis above
 * it. The page's own focus test hands the page a hand-rolled box as its action row, so it never composes this one. The
 * row puts `entryFocus` on its outer node, and its focus group routes entry to the first action.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailActionRowEntryTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    @Test
    fun `entry from the rail lands on the row's first action, not the synopsis above it`() {
        setPage()

        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Right) }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Play") and isFocusable()).assertIsFocused()
    }

    @Test
    fun `the page's first focus as an overlay lands on the first action too`() {
        setPage(hosting = TvPageHosting.Overlay, withRail = false)

        composeTestRule.onNode(hasText("Play") and isFocusable()).assertIsFocused()
    }

    private fun setPage(hosting: TvPageHosting = TvPageHosting.Destination, withRail: Boolean = true) {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                val entry = remember { FocusRequester() }
                Row {
                    if (withRail) Box(Modifier.size(48.dp).testTag(RAIL).focusable())
                    TvDetailPage(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        entryFocus = entry,
                        hosting = hosting,
                    ) {
                        hero {
                            Column {
                                Box(Modifier.size(width = 320.dp, height = 48.dp).testTag(SYNOPSIS).focusable())
                                TvDetailActionRow(
                                    actions =
                                        listOf(
                                            TvDetailAction(label = "Play", icon = Icons.Filled.PlayArrow, onClick = {}, isPrimary = true),
                                            TvDetailAction(label = "Add", icon = Icons.Filled.Add, onClick = {}),
                                        ),
                                    entryFocus = entry,
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
}
