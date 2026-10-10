package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.testing.WithWindowInsets
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
    private var keyboardHeight by mutableStateOf(0.dp)

    private fun render(
        keyboard: Dp,
        navigationBar: Dp = 0.dp,
        presentation: BingeNavPresentation = BingeNavPresentation.FloatingBar,
    ) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                WithKeyboard(if (keyboard > 0.dp) keyboard else keyboardHeight, navigationBar) {
                    BingeNavSuiteShell(
                        items = listOf(
                            BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie, testTag = MOVIES_TAG),
                        ),
                        selectedKey = "movies",
                        onSelect = {},
                        presentation = presentation,
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

    @Test
    fun `the rail does not shift when the keyboard opens over a navigation bar`() {
        val bar = 48.dp
        render(keyboard = 0.dp, navigationBar = bar, presentation = BingeNavPresentation.CustomRail)
        val closed = rule.onNodeWithTag(MOVIES_TAG).getUnclippedBoundsInRoot().top

        keyboardHeight = Keyboard
        rule.waitForIdle()
        val open = rule.onNodeWithTag(MOVIES_TAG).getUnclippedBoundsInRoot().top

        assertEquals(closed, open)
    }
}

/** Opens a keyboard [height] tall over the window, answering every insets dispatch at the parent. */
@Composable
private fun WithKeyboard(
    height: Dp,
    navigationBar: Dp,
    content: @Composable () -> Unit,
) = WithWindowInsets(
    {
        val px = height.roundToPx()
        val navPx = navigationBar.roundToPx()
        WindowInsetsCompat
            .Builder()
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navPx))
            .setVisible(WindowInsetsCompat.Type.navigationBars(), navPx > 0)
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, if (px > 0) maxOf(px, navPx) else 0))
            .setVisible(WindowInsetsCompat.Type.ime(), px > 0)
            .build()
    },
    content,
)
