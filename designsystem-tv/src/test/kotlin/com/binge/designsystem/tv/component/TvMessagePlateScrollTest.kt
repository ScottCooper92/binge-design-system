package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LONG_BODY_SENTENCES = 40
private const val LARGE_FONT_SCALE = 2f
private const val MAX_PRESSES = 40
private const val TOLERANCE = 0.5f

/** A body taller than the plate is read with the D-pad: it scrolls under focus, then focus moves on. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvMessagePlateScrollTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    private val longBody = (1..LONG_BODY_SENTENCES).joinToString("\n") { "Line $it of a message that runs well past the plate." }
    private val shortBody = "Nothing to see here."

    @Composable
    private fun Frame(fontScale: Float, content: @Composable () -> Unit) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            BingeTvTheme {
                Box(Modifier.size(width = 720.dp, height = 320.dp)) { content() }
            }
        }
    }

    private fun show(
        body: String,
        withActions: Boolean,
        fontScale: Float = LARGE_FONT_SCALE,
    ) {
        rule.setContent {
            Frame(fontScale) {
                TvMessagePlate(
                    body = body,
                    headline = "Something happened",
                    actions = if (withActions) {
                        { TvButton(label = "Try again", onClick = {}) }
                    } else {
                        null
                    },
                )
            }
        }
        rule.waitForIdle()
    }

    private fun press(key: Key) {
        rule.onRoot().performKeyInput { pressKey(key) }
        rule.waitForIdle()
    }

    private fun bodyTop() =
        rule
            .onNode(hasText(longBody.take(BODY_PREFIX), substring = true))
            .getUnclippedBoundsInRoot()
            .top.value

    private fun bodyBottom() =
        rule
            .onNode(hasText(longBody.take(BODY_PREFIX), substring = true))
            .getUnclippedBoundsInRoot()
            .bottom.value

    @Test
    fun `with actions, Down scrolls the copy to its end and then focus moves to the action`() {
        show(longBody, withActions = true)
        rule.onNodeWithText("Try again").requestFocus()
        press(Key.DirectionUp)
        rule.onNodeWithText("Try again").assertIsNotFocused()
        val before = bodyTop()

        var presses = 0
        while (presses < MAX_PRESSES && !isFocused("Try again")) {
            press(Key.DirectionDown)
            presses++
        }

        rule.onNodeWithText("Try again").assertIsFocused()
        assertTrue("the copy did not scroll before focus left it", bodyTop() < before)
        // The whole body is still there for a screen reader, scrolled or not.
        rule.onNode(hasText(longBody.take(BODY_PREFIX), substring = true)).assertExists()
    }

    @Test
    fun `Up from the action moves focus into an overflowing copy`() {
        show(longBody, withActions = true)
        rule.onNodeWithText("Try again").requestFocus()

        press(Key.DirectionUp)

        rule.onNodeWithText("Try again").assertIsNotFocused()
    }

    @Test
    fun `without actions the copy is the one focus stop, and Down scrolls it to its end`() {
        show(longBody, withActions = false)
        press(Key.DirectionDown)
        val first = bodyTop()
        repeat(MAX_PRESSES) { press(Key.DirectionDown) }
        val last = bodyTop()

        assertTrue("Down did not scroll the copy", last < first)
        // At the end the copy has scrolled by as much as it can: another press moves nothing.
        val bottom = bodyBottom()
        press(Key.DirectionDown)
        assertEquals(bottom, bodyBottom(), TOLERANCE)
    }

    @Test
    fun `a body that fits adds no focus stop`() {
        show(shortBody, withActions = true, fontScale = 1f)
        rule.onNodeWithText("Try again").requestFocus()

        press(Key.DirectionUp)

        // Nothing above the action to take focus: it keeps it.
        rule.onNodeWithText("Try again").assertIsFocused()
    }

    private fun isFocused(text: String): Boolean = runCatching { rule.onNodeWithText(text).assertIsFocused() }.isSuccess

    private companion object {
        const val BODY_PREFIX = 6
    }
}
