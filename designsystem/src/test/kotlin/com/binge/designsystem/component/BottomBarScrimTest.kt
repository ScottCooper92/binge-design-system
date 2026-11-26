package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import kotlin.math.roundToInt

/** The strongest alpha of the shared ramp, which the bottom scrim reaches at its bottom edge. */
private const val STRONGEST_ALPHA = 0.92f

/** How far a channel may sit from the expected blend: the gradient is sampled a pixel in from each edge. */
private const val TOLERANCE = 14

/**
 * [BottomBarScrim] is the top scrim's ramp mirrored: transparent at its top edge and at full strength at its bottom edge, in the theme's background.
 * Read back from the pixels it draws over a red page, with the scrim in a box at the page's bottom and its tail reaching up from the box.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h640dp")
// Native graphics, because the scrim is read back from pixels.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BottomBarScrimTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var page = Color.Unspecified
    private var density = 1f

    @Composable
    private fun Page(fraction: Float) {
        BingeExpressiveTheme(dynamicColor = false) {
            page = MaterialTheme.colorScheme.background
            density = LocalDensity.current.density
            Box(
                Modifier
                    .width(PAGE_WIDTH)
                    .height(PAGE_HEIGHT)
                    .background(Color.Red)
                    .testTag("page"),
            ) {
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(BAR_HEIGHT)) {
                    BottomBarScrim(fraction = fraction, tailHeight = TAIL)
                }
            }
        }
    }

    private fun pixels(fraction: Float, vararg ys: Float): List<Int> {
        rule.setContent { Page(fraction) }
        rule.waitForIdle()
        val bitmap = rule.onNodeWithTag("page").captureToImage().asAndroidBitmap()
        return ys.map { bitmap.getPixel((PIXEL_X * density).roundToInt(), (it * density).roundToInt().coerceAtMost(bitmap.height - 1)) }
    }

    private fun blend(alpha: Float): Int {
        val bg = page.toArgb()
        val red = Color.Red.toArgb()

        fun channel(shift: Int) = (((bg shr shift) and 0xFF) * alpha + ((red shr shift) and 0xFF) * (1f - alpha)).roundToInt()
        return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    private fun near(expected: Int, actual: Int): Boolean =
        listOf(16, 8, 0).all { abs(((expected shr it) and 0xFF) - ((actual shr it) and 0xFF)) <= TOLERANCE }

    @Test
    fun `it is transparent at its top edge and at full strength at the bottom`() {
        val (top, bottom) = pixels(1f, TOP_EDGE_Y, PAGE_HEIGHT.value)
        assertTrue("the top edge shows the page untouched", near(Color.Red.toArgb(), top))
        assertTrue("the bottom edge is the strongest stop over the page", near(blend(STRONGEST_ALPHA), bottom))
    }

    @Test
    fun `it thins out above the element, getting stronger toward the bottom`() {
        val (high, middle, low) = pixels(1f, TOP_EDGE_Y + 10f, (TOP_EDGE_Y + PAGE_HEIGHT.value) / 2f, PAGE_HEIGHT.value - 2f)

        fun greenChannel(c: Int) = (c shr 8) and 0xFF
        assertTrue("stronger lower down", greenChannel(high) <= greenChannel(middle) && greenChannel(middle) <= greenChannel(low))
    }

    @Test
    fun `it eases in, so a tenth of the way down it has barely started`() {
        val (early) = pixels(1f, TOP_EDGE_Y + (PAGE_HEIGHT.value - TOP_EDGE_Y) * 0.1f)
        assertTrue("the first tenth is still almost the page untouched, not a band", near(Color.Red.toArgb(), early))
    }

    @Test
    fun `a fraction of zero draws nothing`() {
        val (none) = pixels(0f, PAGE_HEIGHT.value)
        assertTrue("nothing drawn at 0", near(Color.Red.toArgb(), none))
    }

    @Test
    fun `a fraction of a half scales the whole ramp`() {
        val (bottom) = pixels(0.5f, PAGE_HEIGHT.value)
        assertTrue("half the strongest stop at the bottom", near(blend(STRONGEST_ALPHA * 0.5f), bottom))
    }

    private companion object {
        val PAGE_WIDTH = 300.dp
        val PAGE_HEIGHT = 200.dp
        val BAR_HEIGHT = 40.dp
        val TAIL = 60.dp
        const val PIXEL_X = 150f

        /** The scrim's paint area starts one tail above the bar's box. */
        val TOP_EDGE_Y = PAGE_HEIGHT.value - BAR_HEIGHT.value - TAIL.value + 1f
    }
}
