package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import com.binge.designsystem.tv.theme.TvButtonColors
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.theme.tvButtonColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The palette overload of [TvButtonSurface] paints the container and border it is given, and the style overload is it. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvButtonSurfacePaletteTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    private val palette = TvButtonColors(container = Color(0xFF123456), content = Color(0xFFFEDCBA), border = Color(0xFF123456))

    @Test
    fun `the palette overload fills the pill with the given container`() {
        rule.setContent {
            BingeTvTheme {
                TvButtonSurface(label = "Request", palette = palette, isFocused = false, modifier = Modifier.testTag(NODE))
            }
        }
        val pixels = rule.onNodeWithTag(NODE).captureToImage().toPixelMap()

        // The pill's vertical middle, a little in from the left edge: past the rounded end, before the label.
        assertEquals(palette.container, pixels[pixels.width / 20, pixels.height / 2])
    }

    @Test
    fun `the palette overload draws the label in the given content colour`() {
        rule.setContent {
            BingeTvTheme {
                TvButtonSurface(label = "Request", palette = palette, isFocused = false, modifier = Modifier.testTag(NODE))
            }
        }
        val pixels = rule.onNodeWithTag(NODE).captureToImage().toPixelMap()
        val inks = (0 until pixels.width).sumOf { x -> (0 until pixels.height).count { y -> pixels[x, y] == palette.content } }

        assertTrue("pixels drawn in the content colour: $inks", inks > 0)
    }

    @Test
    fun `the style overload draws what its resolved palette draws`() {
        val resolved = mutableListOf<TvButtonColors>()
        rule.setContent {
            BingeTvTheme {
                resolved += tvButtonColors(TvButtonStyle.Primary, enabled = true, isFocused = false)
                TvButtonSurface("Request", TvButtonStyle.Primary, enabled = true, isFocused = false, modifier = Modifier.testTag(NODE))
            }
        }
        val pixels = rule.onNodeWithTag(NODE).captureToImage().toPixelMap()

        assertEquals(resolved.single().container, pixels[pixels.width / 20, pixels.height / 2])
    }

    private companion object {
        const val NODE = "button"
    }
}
