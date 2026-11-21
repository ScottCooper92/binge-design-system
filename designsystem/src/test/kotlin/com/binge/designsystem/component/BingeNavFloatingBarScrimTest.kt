package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.math.roundToInt

private const val STRONGEST_ALPHA = 0.92f
private const val TOLERANCE = 14

/** The gap `BingeSnackbarHost` leaves above the overlay strip (`padding_s`). */
private const val SNACKBAR_GAP = 8f

/**
 * The floating pill draws a [BottomBarScrim] behind it, over the strip the pill and the navigation bar occupy plus a tail above it, so a list passing
 * under the pill fades out. It is a fade only: the strip content pads by does not change, so the last row still clears the pill. Read back from
 * the pixels over a red page.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
// Native graphics, because the scrim is read back from pixels.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeNavFloatingBarScrimTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var overlay = PaddingValues()
    private var page = Color.Unspecified
    private var density = 1f
    private var scrim by mutableStateOf(true)

    private fun show() {
        rule.setContent {
            TestTheme {
                page = MaterialTheme.colorScheme.background
                density = LocalDensity.current.density
                BingeNavSuiteShell(
                    items = listOf(BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie)),
                    selectedKey = "movies",
                    onSelect = {},
                    presentation = BingeNavPresentation.FloatingBar,
                    floatingScrim = scrim,
                ) {
                    overlay = LocalNavOverlayInsets.current
                    Box(Modifier.fillMaxSize().background(Color.Red))
                }
            }
        }
        rule.waitForIdle()
    }

    /** The pixel at the page's left edge, clear of the pill, [fromBottomDp] above the bottom of the screen. */
    private fun pixel(fromBottomDp: Float): Int {
        val image = rule.onRoot().captureToImage()
        val y = (image.height - fromBottomDp * density).roundToInt().coerceIn(0, image.height - 1)
        return image.asAndroidBitmap().getPixel(2, y)
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
    fun `the scrim runs to the bottom edge and leaves what sits just above the strip untouched`() {
        show()
        val strip = overlay.calculateBottomPadding().value
        assertTrue("the bottom edge is the strongest stop over the page", near(blend(STRONGEST_ALPHA), pixel(1f)))
        assertTrue("above the strip the page shows untouched", near(Color.Red.toArgb(), pixel(strip + 80f)))
        // A snackbar sits at the strip plus the small gap, and the scrim must not touch it.
        assertTrue("where a snackbar sits just above the strip", near(Color.Red.toArgb(), pixel(strip + SNACKBAR_GAP)))
    }

    @Test
    fun `without the scrim the page runs to the edge`() {
        scrim = false
        show()
        assertTrue("nothing drawn behind the pill", near(Color.Red.toArgb(), pixel(1f)))
    }

    @Test
    fun `the strip content pads by is the same with and without the scrim`() {
        show()
        val withScrim = overlay.calculateBottomPadding()
        scrim = false
        rule.waitForIdle()
        assertEquals(withScrim.value, overlay.calculateBottomPadding().value, 0.01f)
    }
}
