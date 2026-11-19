package com.binge.designsystem.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

/**
 * A disabled [BingeOutlinedButton] draws a muted outline (Material 3's: `onSurface` at 12%), not the full content colour its
 * enabled outline has, so the label and the outline grey together.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h640dp")
// Native graphics, because the border is read back from pixels the button drew.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeOutlinedButtonDisabledBorderTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var primary = Color.Unspecified
    private var page = Color.Unspecified

    @Composable
    private fun Page(content: @Composable () -> Unit) {
        BingeExpressiveTheme(dynamicColor = false) {
            primary = MaterialTheme.colorScheme.primary
            page = MaterialTheme.colorScheme.background
            Box(Modifier.background(page).width(200.dp)) { content() }
        }
    }

    private fun distance(a: Int, b: Int): Int {
        fun channel(c: Int, shift: Int) = (c shr shift) and 0xFF
        return listOf(16, 8, 0).sumOf { abs(channel(a, it) - channel(b, it)) }
    }

    /** The outline's colour: of the first few pixels in from the left edge at mid-height, the one furthest from the page. */
    private fun outlinePixel(enabled: Boolean): Int {
        rule.setContent {
            Page { BingeOutlinedButton("Label", onClick = {}, enabled = enabled, modifier = Modifier.testTag("button")) }
        }
        rule.waitForIdle()
        val bitmap: Bitmap = rule.onNodeWithTag("button").captureToImage().asAndroidBitmap()
        val y = bitmap.height / 2
        return (0..2).map { bitmap.getPixel(it, y) }.maxBy { distance(it, page.toArgb()) }
    }

    @Test
    fun `an enabled outline is the content colour`() {
        val outline = outlinePixel(enabled = true)

        assertTrue("the outline is close to primary", distance(outline, primary.toArgb()) < 60)
    }

    @Test
    fun `a disabled outline is muted, not the content colour`() {
        val outline = outlinePixel(enabled = false)

        assertTrue("the outline is not primary", distance(outline, primary.toArgb()) > 200)
        assertTrue("the outline is a faint line on the page", distance(outline, page.toArgb()) < 120)
    }
}
