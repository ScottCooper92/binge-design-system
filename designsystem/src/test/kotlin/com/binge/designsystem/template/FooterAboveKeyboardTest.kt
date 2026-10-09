package com.binge.designsystem.template

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SAVE = "Save"
private const val NEXT = "Next"
private const val TOLERANCE = 1f

private val Window = 800.dp
private val Keyboard = 300.dp

/** A footer pinned under a form or a step rides above the keyboard rather than behind it (#375). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FooterAboveKeyboardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a form's footer sits above the keyboard`() {
        rule.setContent {
            Theme {
                WithKeyboard(Keyboard) {
                    FormScreen(title = "Edit", primaryAction = FormAction(SAVE, onClick = {}), placement = FormActionPlacement.Footer) {
                        Box(Modifier.fillMaxWidth().height(48.dp))
                    }
                }
            }
        }

        assertAboveKeyboard(SAVE)
    }

    @Test
    fun `a step's footer sits above the keyboard`() {
        rule.setContent {
            Theme {
                WithKeyboard(Keyboard) {
                    StepFlowScreen(stepCount = 2, currentStep = 0, footer = { Text(NEXT) }) {
                        Box(Modifier.fillMaxWidth().height(48.dp))
                    }
                }
            }
        }

        assertAboveKeyboard(NEXT)
    }

    private fun assertAboveKeyboard(text: String) {
        rule.waitForIdle()
        val bottom = rule.onNodeWithText(text).getBoundsInRoot().bottom
        assertTrue(
            "$text ends at $bottom, under a keyboard from ${Window - Keyboard}",
            bottom.value <= (Window - Keyboard).value + TOLERANCE,
        )
    }
}

@Composable
private fun Theme(content: @Composable () -> Unit) = BingeExpressiveTheme(dynamicColor = false, content = content)

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
                .setVisible(WindowInsetsCompat.Type.ime(), true)
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
