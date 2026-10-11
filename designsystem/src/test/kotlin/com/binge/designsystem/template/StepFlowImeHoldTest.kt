package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.testing.WithWindowInsets
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOLD_FIELD = "field"
private const val HOLD_TOLERANCE = 1f

/** A split step puts its content in the right-hand column, which starts past this; a stacked one starts well before. */
private val SplitContentStart = 300.dp

/** One frame of the keyboard: how much of the window it covers, and whether it reports itself visible. */
private data class KeyboardFrame(
    val height: Dp,
    val visible: Boolean,
)

/**
 * The held split, composed (#558). A portrait foldable's flow stacks, and a keyboard cutting its height below its
 * width would measure it as split, disposing the focused field. Each frame of the show and hide is dispatched in turn,
 * including the hide's first frames, where the keyboard no longer reports itself visible but its inset has not gone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w690dp-h840dp")
class StepFlowImeHoldTest {
    @get:Rule
    val rule = createComposeRule()

    private var keyboard by mutableStateOf(KeyboardFrame(0.dp, visible = false))

    /** The control: with the keyboard already up and no earlier decision to hold, the cut space splits. */
    @Test
    fun `the space the keyboard leaves measures as split`() {
        keyboard = KeyboardFrame(300.dp, visible = true)
        setFlow()

        assertTrue("a split step's content starts past $SplitContentStart", fieldStart() > SplitContentStart.value)
    }

    @Test
    fun `the stacked step and its focused field survive the keyboard showing and hiding`() {
        setFlow()
        val stacked = fieldStart()
        assertTrue("a stacked step's content starts before $SplitContentStart", stacked < SplitContentStart.value)
        rule.onNodeWithTag(HOLD_FIELD).requestFocus()

        val frames =
            listOf(
                KeyboardFrame(150.dp, visible = true),
                KeyboardFrame(300.dp, visible = true),
                KeyboardFrame(300.dp, visible = false),
                KeyboardFrame(150.dp, visible = false),
            )
        frames.forEach { frame ->
            keyboard = frame
            rule.waitForIdle()
            assertEquals("the step moved at $frame", stacked, fieldStart(), HOLD_TOLERANCE)
            rule.onNodeWithTag(HOLD_FIELD).assertIsFocused()
        }
    }

    private fun setFlow() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                WithWindowInsets({
                    WindowInsetsCompat
                        .Builder()
                        .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, keyboard.height.roundToPx()))
                        .setVisible(WindowInsetsCompat.Type.ime(), keyboard.visible)
                        .build()
                }) {
                    Box(Modifier.fillMaxSize().imePadding()) {
                        StepFlowScreen(stepCount = 2, currentStep = 0, heading = { Text("Server") }, footer = { Text("Next") }) {
                            var value by remember { mutableStateOf("") }
                            TextField(value = value, onValueChange = { value = it }, modifier = Modifier.testTag(HOLD_FIELD))
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun fieldStart(): Float =
        rule
            .onNodeWithTag(HOLD_FIELD)
            .getBoundsInRoot()
            .left.value
}
