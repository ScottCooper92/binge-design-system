package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NODE = "control"
private val Backdrop = Color(0xFF336699)

/**
 * A focused [tvClickable] draws nothing of its own (#839). TV components draw their own focus, so foundation's default
 * indication would only lay a translucent rectangle over the control's bounds, showing as square corners behind a pill.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvClickableIndicationTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `a focused tvClickable leaves its bounds unpainted`() {
        rule.setContent {
            BingeTvTheme {
                Box(Modifier.background(Backdrop)) {
                    Box(Modifier.size(48.dp).testTag(NODE).tvClickable(onFocusChanged = {}, onClick = {}))
                }
            }
        }
        rule.onNodeWithTag(NODE).requestFocus()
        rule.waitForIdle()

        val node = rule.onNodeWithTag(NODE).assertIsFocused()
        val pixels = node.captureToImage().toPixelMap()
        val painted = (0 until pixels.width).sumOf { x -> (0 until pixels.height).count { y -> pixels[x, y] != Backdrop } }
        assertEquals("pixels the focused control painted over its backdrop", 0, painted)
    }
}
