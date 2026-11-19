package com.binge.designsystem.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
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
 * A bar fully collapsed over scrolled content has a solid surface behind its title and actions, so a row under it does
 * not read through; partway through the collapse the content still shows through the fade. Read back from the pixels
 * the bar draws over a bright row, in the lower half of the bar where the title sits.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h640dp")
// Native graphics, because the scrim is read back from pixels.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TopBarScrimSolidTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var page = Color.Unspecified

    @Composable
    private fun BarOverContent(fraction: Float) {
        BingeExpressiveTheme(dynamicColor = false) {
            page = MaterialTheme.colorScheme.background
            Box(
                Modifier
                    .width(300.dp)
                    .height(200.dp)
                    .background(Color.Red)
                    .testTag("page"),
            ) {
                BingeTopBar(title = "", scrimFraction = fraction, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    private fun distance(a: Int, b: Int): Int {
        fun channel(c: Int, shift: Int) = (c shr shift) and 0xFF
        return listOf(16, 8, 0).sumOf { abs(channel(a, it) - channel(b, it)) }
    }

    /** How far, in colour, the pixel in the lower half of the bar, clear of the title, is from the page at [fraction]. */
    private fun distanceFromPage(fraction: Float): Int {
        var density = 1f
        rule.setContent {
            density = LocalDensity.current.density
            BarOverContent(fraction)
        }
        rule.waitForIdle()
        val bitmap: Bitmap = rule.onNodeWithTag("page").captureToImage().asAndroidBitmap()
        val pixel = bitmap.getPixel((250 * density).toInt(), (44 * density).toInt())
        return distance(pixel, page.toArgb())
    }

    @Test
    fun `fully collapsed the content does not read through the bar`() {
        assertTrue("the bar is the page's colour", distanceFromPage(1f) < 12)
    }

    @Test
    fun `halfway the content still shows through the fade`() {
        assertTrue("the content shows", distanceFromPage(0.5f) > 100)
    }

    @Test
    fun `at the start of the solid ramp the content still shows`() {
        assertTrue("the content shows", distanceFromPage(0.75f) > 100)
    }
}
