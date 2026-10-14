package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROWS = 24
private const val TOLERANCE = 0.5f

/** `padding_xs` and `tv_overscan_vertical`, as values, so a test fails when one moves rather than following it. */
private val ROW_GAP = 4.dp
private val OVERSCAN = 27.dp

/**
 * A sheet's rows sit `padding_xs` apart and scroll when they outgrow the panel. Scrolled to the end, the last row stops the
 * bottom overscan above the screen's edge, whether the panel keeps that margin itself or runs to the edge and leaves it to the rows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetRowsTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val scroll = ScrollState(0)

    @Test
    fun `rows sit padding_xs apart`() {
        show(toBottomEdge = false, count = 2)

        assertEquals(ROW_GAP.value, (top(1) - bottom(0)).value, TOLERANCE)
    }

    @Test
    fun `on a panel to the bottom edge the last row scrolls clear of the edge by the overscan`() {
        show(toBottomEdge = true, count = ROWS)

        scrollToEnd()

        assertEquals((screenBottom() - OVERSCAN).value, bottom(ROWS - 1).value, TOLERANCE)
    }

    @Test
    fun `on a panel that keeps its margin the last row ends at that margin`() {
        show(toBottomEdge = false, count = ROWS)

        scrollToEnd()

        assertEquals((screenBottom() - OVERSCAN).value, bottom(ROWS - 1).value, TOLERANCE)
    }

    /** More rows than fit: focus moving down to the last one scrolls it on screen rather than leaving it under the edge. */
    @Test
    fun `focusing a row below the fold scrolls it into view`() {
        show(toBottomEdge = true, count = ROWS)

        rule.onNodeWithTag(tag(ROWS - 1)).requestFocus()
        rule.waitForIdle()

        rule.onNodeWithTag(tag(ROWS - 1)).assertIsFocused()
        assertTrue("the focused row ends at ${bottom(ROWS - 1)}, past the screen", bottom(ROWS - 1) <= screenBottom() + TOLERANCE.dp)
        assertTrue("the rows did not scroll", scroll.value > 0)
    }

    private fun show(toBottomEdge: Boolean, count: Int) {
        rule.setContent {
            BingeTvTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    TvSideSheetPanel(toBottomEdge = toBottomEdge) {
                        TvSideSheetTitle("Maintenance")
                        TvSideSheetRows(scrollState = scroll) {
                            repeat(count) { TvSideSheetRow(label = "Job $it", onClick = {}, modifier = Modifier.testTag(tag(it))) }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun scrollToEnd() {
        runBlocking { scroll.scrollTo(scroll.maxValue) }
        rule.waitForIdle()
    }

    private fun tag(index: Int) = "row-$index"

    private fun top(index: Int): Dp = rule.onNodeWithTag(tag(index)).getUnclippedBoundsInRoot().top

    private fun bottom(index: Int): Dp = rule.onNodeWithTag(tag(index)).getUnclippedBoundsInRoot().bottom

    private fun screenBottom(): Dp = rule.onRoot().getUnclippedBoundsInRoot().bottom
}
