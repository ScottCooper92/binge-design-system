package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val DEMO_TAG = "Demo"
private const val ACCOUNT_DETAIL = "Server: home.example"
private const val QUOTA = "12 of 20 requests used"

/**
 * What the tab scaffold takes beyond what Binge's shell uses, each a defaulted parameter: the avatar menu's detail line, tag and extra
 * content, and tabs the host has not yet resolved. With none of them set the scaffold is as the Binge tests prove it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTopBarScaffoldSlotsTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var pending by mutableStateOf(setOf<Any>("requests"))

    private val slots = TvTopBarSlots(
        header = TvNavItem(KEY_ACCOUNT, "Account", Icons.Filled.Person, displayName = "Glurt"),
        items = listOf(
            TvNavItem(KEY_SEARCH, "Search", Icons.Filled.Search),
            TvNavItem(KEY_MOVIES, "Movies", Icons.Filled.Movie),
            TvNavItem(KEY_TV, "TV shows", Icons.Filled.Tv),
            TvNavItem("requests", "Requests", Icons.Filled.Bookmark),
        ),
        footer = TvNavItem(KEY_SETTINGS, "Settings", Icons.Filled.Settings),
    )

    private fun show(accountOpen: Boolean = false) {
        composeTestRule.setContent {
            BingeTvTheme {
                val content = remember { FocusRequester() }
                TvTopBarScaffold(
                    slots = slots,
                    selectedKey = KEY_TV,
                    homeKey = KEY_MOVIES,
                    onSelect = {},
                    contentDepth = 0,
                    onGoHome = {},
                    overlayOpen = false,
                    contentFocusRequester = content,
                    accountRows = listOf(TvAccountMenuRow("account-row", "Account page", Icons.Filled.Person, onClick = {})),
                    accountDetail = ACCOUNT_DETAIL,
                    accountTag = DEMO_TAG,
                    accountExtraContent = { Text(QUOTA) },
                    accountInitiallyOpen = accountOpen,
                    pendingKeys = pending,
                ) {
                    Box(Modifier.fillMaxSize().focusRequester(content).focusable())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a pending tab is neither drawn nor focusable and the selection is kept`() {
        show()
        composeTestRule.onNodeWithContentDescription("Requests").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("TV shows").assertIsSelected()
    }

    @Test
    fun `a tab shows once it is resolved`() {
        show()
        pending = emptySet()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Requests").assertExists()
    }

    @Test
    fun `walking right from the last shown tab goes to the gear, not onto a hidden tab`() {
        show()
        composeTestRule.onNodeWithContentDescription("TV shows").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsFocused()
    }

    @Test
    fun `the menu shows its detail line, tag and extra content`() {
        pending = emptySet()
        show(accountOpen = true)
        composeTestRule.onNodeWithText(ACCOUNT_DETAIL).assertExists()
        composeTestRule.onNodeWithText(DEMO_TAG).assertExists()
        composeTestRule.onNodeWithText(QUOTA).assertExists()
    }
}
