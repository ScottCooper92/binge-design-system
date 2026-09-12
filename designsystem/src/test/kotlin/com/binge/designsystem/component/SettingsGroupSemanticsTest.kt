package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

/** A screen reader jumps group to group by the title, and a row's tap is the row's, not the group's. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SettingsGroupSemanticsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a group's title is a heading`() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                SettingsGroup(
                    title = "My library",
                    rows = listOf(SettingsRow(icon = Icons.Filled.Bookmark, label = "Watchlist")),
                )
            }
        }
        // The group renders its title uppercased, so match what is actually on screen.
        composeTestRule.onNodeWithText("MY LIBRARY").assert(isHeading)
    }

    @Test
    fun `a clickable row is a button and a non-clickable one is not`() {
        var clicked = false
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                SettingsGroup(
                    title = null,
                    rows = listOf(
                        SettingsRow(icon = Icons.Filled.Bookmark, label = "Watchlist", onClick = { clicked = true }),
                        SettingsRow(icon = Icons.Filled.Bookmark, label = "Version", detail = "1.0", clickable = false),
                    ),
                )
            }
        }
        composeTestRule.onNode(hasText("Watchlist")).assertHasClickAction().performClick()
        composeTestRule.onNode(hasText("Version")).assertHasNoClickAction()
        assertTrue(clicked)
    }
}
