package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SAVE = "Save"
private const val LAST_FIELD = "Last field"
private const val NEXT = "Next"
private const val TOLERANCE = 1f

private val Window = 800.dp
private val Keyboard = 300.dp
private val Overlay = 400.dp

/** A footer pinned under a form or a step rides above the keyboard rather than behind it (#375). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FooterAboveKeyboardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a form's footer sits above the keyboard`() {
        rule.setContent {
            TestTheme {
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
    fun `a form's primary action is pinned in the footer unless it asks for the top bar`() {
        rule.setContent {
            TestTheme {
                WithKeyboard(Keyboard) {
                    FormScreen(title = "Edit", primaryAction = FormAction(SAVE, onClick = {})) {
                        Box(Modifier.fillMaxWidth().height(48.dp))
                    }
                }
            }
        }

        assertAboveKeyboard(SAVE)
        assertPinnedNearKeyboard(SAVE)
    }

    @Test
    fun `a step's footer sits above the keyboard`() {
        rule.setContent {
            TestTheme {
                WithKeyboard(Keyboard) {
                    StepFlowScreen(stepCount = 2, currentStep = 0, footer = { Text(NEXT) }) {
                        Box(Modifier.fillMaxWidth().height(48.dp))
                    }
                }
            }
        }

        assertAboveKeyboard(NEXT)
    }

    @Test
    fun `a form without a footer still clears the nav overlay with the keyboard up`() {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(bottom = Overlay)) {
                    WithKeyboard(Keyboard) {
                        FormScreen(
                            title = "Edit",
                            primaryAction = FormAction(SAVE, onClick = {}),
                            placement = FormActionPlacement.TopBar,
                            scrolling = false,
                        ) { padding ->
                            Spacer(Modifier.weight(1f))
                            Text(LAST_FIELD, Modifier.fillMaxWidth().padding(padding).height(48.dp))
                        }
                    }
                }
            }
        }

        rule.waitForIdle()
        val body = rule.onNodeWithText(LAST_FIELD).getBoundsInRoot().bottom
        assertTrue(
            "the last field ends at $body, under the overlay from ${Window - Overlay}",
            body.value <= (Window - Overlay).value + TOLERANCE,
        )
    }

    /** A top-bar button also sits above the keyboard, so this is what tells the footer from the top bar. */
    private fun assertPinnedNearKeyboard(text: String) {
        val bottom = rule.onNodeWithText(text).getBoundsInRoot().bottom
        val keyboardTop = Window - Keyboard
        assertTrue(
            "$text ends at $bottom, in the top half of the space above the keyboard (top ${keyboardTop / 2}), so it is not in the footer",
            bottom.value > (keyboardTop / 2).value,
        )
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

/** Opens a keyboard [height] tall over the window, answering every insets dispatch at the parent. */
@Composable
private fun WithKeyboard(height: Dp, content: @Composable () -> Unit) =
    WithWindowInsets(
        {
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, height.roundToPx()))
                .setVisible(WindowInsetsCompat.Type.ime(), true)
                .build()
        },
        content,
    )
