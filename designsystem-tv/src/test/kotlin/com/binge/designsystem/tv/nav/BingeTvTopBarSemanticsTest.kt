package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The bar's items as a screen reader meets them: a destination is a tab, whose selection the accessibility bridge reports as
 * selected (a button's reads as checked), and an item in `actionKeys` is a button with no selection at all.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvTopBarSemanticsTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the destinations are tabs and an action is a button with no selection`() {
        composeTestRule.setContent {
            BingeTvTheme {
                BingeTvTopBar(
                    header = null,
                    items = listOf(
                        TvNavItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie),
                        TvNavItem(key = "tv", label = "TV shows", icon = Icons.Filled.Tv),
                    ),
                    footer = TvNavItem(key = "settings", label = "Settings", icon = Icons.Filled.Settings),
                    selectedKey = "movies",
                    onSelect = {},
                    itemFocusRequesters = emptyMap(),
                    down = remember { FocusRequester() },
                    onFocusedChange = {},
                    iconOnlyKeys = setOf("settings"),
                    actionKeys = setOf("settings"),
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Movies").assertIsSelected().assert(role(Role.Tab))
        composeTestRule.onNodeWithContentDescription("TV shows").assertIsNotSelected().assert(role(Role.Tab))
        composeTestRule
            .onNodeWithContentDescription("Settings")
            .assert(role(Role.Button))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }

    private fun role(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
}
