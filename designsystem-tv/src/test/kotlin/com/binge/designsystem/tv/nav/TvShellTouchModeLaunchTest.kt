package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val CONTENT = "content"

/**
 * A launch by `am start`, an instrumentation or some launchers leaves the window in touch mode, where `clickable`
 * refuses focus. This rule starts in touch mode for exactly that reason: the shell must leave it, so its startup offer
 * still lands on a clickable destination.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvShellTouchModeLaunchTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a launch in touch mode still lands the startup offer on a clickable destination`() {
        var mode: InputMode? = null
        rule.setContent {
            mode = LocalInputModeManager.current.inputMode
            BingeTvTheme {
                TvShellScaffold(
                    header = null,
                    items = listOf(TvNavRailItem(key = HOME, label = "Home", icon = Icons.Filled.Home)),
                    footer = null,
                    selectedKey = HOME,
                    homeKey = HOME,
                    onSelect = {},
                ) { Box(Modifier.size(40.dp).testTag(CONTENT).clickable {}) }
            }
        }
        rule.waitForIdle()

        assertEquals(InputMode.Keyboard, mode)
        rule.onNodeWithTag(CONTENT).assertIsFocused()
    }
}
