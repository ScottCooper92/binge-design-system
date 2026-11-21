package com.binge.designsystem.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TITLE = "Users"
private const val LIST = "list"

/**
 * A pinned small bar stays where it is while the body scrolls, and its scrim still fades in as rows pass under it; an unpinned one slides away,
 * as it always has.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h640dp")
// Native graphics, because the scrim is read back from pixels.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeScreenScaffoldPinnedBarTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var page = Color.Unspecified

    private fun show(pinned: Boolean, barScrim: Boolean = true) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                page = MaterialTheme.colorScheme.background
                Screen(pinned, barScrim)
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun Screen(pinned: Boolean, barScrim: Boolean) {
        BingeScreenScaffold(title = TITLE, bar = ScreenBar.Small, pinned = pinned, barScrim = barScrim) { padding ->
            LazyColumn(Modifier.testTag(LIST), contentPadding = padding) {
                items((0 until ROWS).toList()) { Box(Modifier.fillMaxWidth().height(ROW_HEIGHT).background(Color.Red)) }
            }
        }
    }

    private fun titleTop() = rule.onNodeWithText(TITLE).getUnclippedBoundsInRoot().top

    private fun scroll() {
        repeat(3) { rule.onNodeWithTag(LIST).performTouchInput { swipeUp() } }
        rule.waitForIdle()
    }

    /** A pixel in the bar's row, at its end side, clear of the title and the back arrow. */
    private fun barPixel(): Int {
        val image = rule.onRoot().captureToImage()
        return image.asAndroidBitmap().getPixel(image.width - PIXEL_INSET_PX, BAR_PIXEL_Y_PX)
    }

    @Test
    fun `a pinned bar does not move after the page scrolls`() {
        show(pinned = true)
        val before = titleTop()
        scroll()
        assertEquals(before, titleTop())
    }

    @Test
    fun `an unpinned bar slides away as the page scrolls`() {
        show(pinned = false)
        val before = titleTop()
        scroll()
        assertTrue("the bar moved up and away", titleTop() < before)
    }

    @Test
    fun `a pinned bar's scrim is up once content passes under it`() {
        show(pinned = true)
        assertEquals("nothing under the bar at rest, so it shows the page", page.toArgb(), barPixel())
        scroll()
        assertEquals("rows pass under the bar and the scrim hides them, so it still shows the page", page.toArgb(), barPixel())
    }

    @Test
    fun `without the scrim the same rows show through under a pinned bar`() {
        show(pinned = true, barScrim = false)
        scroll()
        assertEquals("a row is under the bar, so the scrim is what hid it above", Color.Red.toArgb(), barPixel())
    }

    private companion object {
        const val ROWS = 60
        val ROW_HEIGHT = 64.dp
        const val PIXEL_INSET_PX = 4
        const val BAR_PIXEL_Y_PX = 30
    }
}
