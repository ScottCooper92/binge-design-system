package com.binge.designsystem.component

import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MOVIES_TAG = "nav-movies"

private val Keyboard = 300.dp

/** The floating pill hides under the keyboard, as the docked bar does, and its overlay inset does not grow (#376). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class BingeNavFloatingBarKeyboardTest {
    @get:Rule
    val rule = createComposeRule()

    private var overlay = PaddingValues()

    private fun render(keyboard: Dp) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                WithKeyboard(keyboard) {
                    BingeNavSuiteShell(
                        items = listOf(
                            BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie, testTag = MOVIES_TAG),
                        ),
                        selectedKey = "movies",
                        onSelect = {},
                        presentation = BingeNavPresentation.FloatingBar,
                    ) {
                        overlay = LocalNavOverlayInsets.current
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `the pill shows while there is no keyboard`() {
        render(keyboard = 0.dp)

        rule.onNodeWithTag(MOVIES_TAG).assertExists()
    }

    @Test
    fun `the pill hides while the keyboard is up`() {
        render(keyboard = Keyboard)

        rule.onNodeWithTag(MOVIES_TAG).assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Test
    fun `the overlay inset does not grow with the keyboard`() {
        render(keyboard = Keyboard)

        assertEquals(FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset, overlay.calculateBottomPadding())
    }
}

/** Opens a keyboard [height] tall over the window, answering every insets dispatch at the parent. */
@Composable
private fun WithKeyboard(height: Dp, content: @Composable () -> Unit) {
    val view = LocalView.current
    val px = with(LocalDensity.current) { height.roundToPx() }
    val insets =
        remember(px) {
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, px))
                .setVisible(WindowInsetsCompat.Type.ime(), px > 0)
                .build()
        }
    // Read first, so Compose's own insets listener is installed on the view before the dispatch below.
    WindowInsets.ime
    DisposableEffect(view, insets) {
        val host = view.parent as View
        val platform = checkNotNull(insets.toWindowInsets())
        host.setOnApplyWindowInsetsListener { _, _ -> platform }
        host.dispatchApplyWindowInsets(platform)
        onDispose { host.setOnApplyWindowInsetsListener(null) }
    }
    content()
}
