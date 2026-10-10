package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.nav.BingeTvNavRail
import com.binge.designsystem.tv.nav.TvNavRailItem
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ITEM_KEY = "item"
private const val RAIL_ITEM = "Home"

/** A message page with nothing to press, in a rail destination, still lets the key toward the rail open it (#368, #429). */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvMessagePageRailFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private fun setLoadingPageInRail(layoutDirection: LayoutDirection) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                BingeTvTheme {
                    BingeTvNavRail(
                        header = null,
                        items = listOf(TvNavRailItem(key = ITEM_KEY, label = RAIL_ITEM, icon = Icons.Filled.Home)),
                        footer = null,
                        selectedKey = ITEM_KEY,
                        onSelect = {},
                        expanded = true,
                    ) {
                        TvMessagePage(body = "Loading", loading = true, hosting = TvPageHosting.RailDestination)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `Left from a loading message page opens the rail`() {
        setLoadingPageInRail(LayoutDirection.Ltr)

        press(Key.DirectionLeft)

        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()
    }

    /** Under RTL the rail is drawn on the right, so Right is the key toward it (#429). */
    @Test
    fun `under RTL Right from a loading message page opens the rail`() {
        setLoadingPageInRail(LayoutDirection.Rtl)

        press(Key.DirectionRight)

        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()
    }
}
