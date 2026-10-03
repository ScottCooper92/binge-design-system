package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)
private val hasNoStateDescription = SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription)

/** A screen reader jumps group to group by the title, and a row's tap is the row's, not the group's. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ItemGroupSemanticsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a group's title is a heading`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = "My library",
                    rows = listOf(ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist")),
                )
            }
        }
        // The group renders its title uppercased, so match what is actually on screen.
        composeTestRule.onNodeWithText("MY LIBRARY").assert(isHeading)
    }

    @Test
    fun `a clickable row takes its tap and a non-clickable one is a disabled button`() {
        var clicked = false
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist", onClick = { clicked = true }),
                        ListItem(icon = Icons.Filled.Bookmark, label = "Version", detail = "1.0", clickable = false),
                    ),
                )
            }
        }
        composeTestRule.onNode(hasText("Watchlist")).assertHasClickAction().performClick()
        // combinedClickable(enabled = false) keeps the role and reports the row disabled, which is
        // what a screen reader should say about a row that only shows a value.
        composeTestRule.onNode(hasText("Version")).assertIsNotEnabled()
        assertTrue(clicked)
    }

    @Test
    fun `a long-press row exposes its label to the screen reader`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(
                            icon = Icons.Filled.Bookmark,
                            label = "Ada",
                            onLongClick = {},
                            onLongClickLabel = "Select",
                        ),
                    ),
                )
            }
        }
        val hasSelectLabel = SemanticsMatcher("long-click label is Select") { node ->
            SemanticsActions.OnLongClick in node.config && node.config[SemanticsActions.OnLongClick].label == "Select"
        }
        composeTestRule.onNode(hasText("Ada")).assert(hasSelectLabel)
    }

    @Test
    fun `an external row announces that it opens in browser`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist"),
                        ListItem(
                            icon = Icons.Filled.Bookmark,
                            label = "Privacy policy",
                            destination = ListItemDestination.External,
                        ),
                    ),
                )
            }
        }
        composeTestRule.onNode(hasText("Watchlist")).assert(hasNoStateDescription)
        composeTestRule
            .onNode(hasText("Privacy policy"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Opens in browser"))
    }

    @Test
    fun `a loading row announces it is in progress and takes no tap`() {
        var clicked = false
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Block", loading = true, onClick = { clicked = true }),
                    ),
                )
            }
        }
        composeTestRule
            .onNode(hasText("Block"))
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "In progress"))
        assertTrue(!clicked)
    }

    @Test
    fun `a disabled row is inert and does not claim to be in progress`() {
        var clicked = false
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Report", disabled = true, onClick = { clicked = true }),
                    ),
                )
            }
        }
        composeTestRule.onNode(hasText("Report")).assertIsNotEnabled().assert(hasNoStateDescription)
        assertTrue(!clicked)
    }

    @Test
    fun `a connected row keeps its label and its tap`() {
        var clicked = false
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Parent"),
                        ListItem(
                            icon = Icons.Filled.Bookmark,
                            label = "Child",
                            connector = ListItemConnector.End,
                            onClick = { clicked = true },
                        ),
                    ),
                )
            }
        }
        composeTestRule.onNodeWithText("Child").assertHasClickAction().performClick()
        assertTrue(clicked)
    }

    @Test
    fun `a switch row is one switch node that exposes its on and off state`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Trust proxy", toggled = true),
                        ListItem(icon = Icons.Filled.Bookmark, label = "Force IPv4", toggled = false),
                    ),
                )
            }
        }
        val isSwitch = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)
        composeTestRule.onNode(hasText("Trust proxy")).assert(isSwitch).assertIsOn()
        composeTestRule.onNode(hasText("Force IPv4")).assert(isSwitch).assertIsOff()
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(2)
    }

    @Test
    fun `tapping a switch row calls onClick once and does not flip the state itself`() {
        var taps = 0
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "Trust proxy", toggled = false, onClick = { taps++ }),
                    ),
                )
            }
        }
        composeTestRule.onNode(hasText("Trust proxy")).performClick().assertIsOff()
        assertEquals(1, taps)
    }

    @Test
    fun `a disabled switch row is inert but still reports its state`() {
        var taps = 0
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(
                        ListItem(icon = Icons.Filled.Bookmark, label = "CSRF", toggled = true, disabled = true, onClick = { taps++ }),
                    ),
                )
            }
        }
        composeTestRule
            .onNode(hasText("CSRF"))
            .assertIsNotEnabled()
            .assertIsOn()
            .performClick()
        assertEquals(0, taps)
    }

    @Test
    fun `a plain row is still a button with no toggle state`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ItemGroup(
                    title = null,
                    rows = listOf(ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist")),
                )
            }
        }
        composeTestRule
            .onNode(hasText("Watchlist"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ToggleableState))
    }
}
