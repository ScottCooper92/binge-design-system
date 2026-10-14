package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "row"
private const val FILL = "fill"
private const val TOLERANCE = 0.5f

/**
 * A row's trailing slot is told whether the row is focused, so it can match the fill. A panel that runs to the bottom edge
 * keeps no bottom margin, so content that fills it reaches the screen's edge; the default keeps the overscan.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetEdgeAndTrailingTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `the trailing slot follows the row's focus`() {
        var trailingFocused = false
        rule.setContent {
            BingeTvTheme {
                TvSideSheetRow(label = "Notifications", onClick = {}, modifier = Modifier.testTag(ROW)) { isFocused ->
                    trailingFocused = isFocused
                    Box(Modifier.size(24.dp))
                }
            }
        }
        assertEquals(false, trailingFocused)

        rule.onNodeWithTag(ROW).requestFocus()
        rule.waitForIdle()

        assertEquals(true, trailingFocused)
    }

    @Test
    fun `a panel to the bottom edge lets its content reach the screen's edge`() {
        showPanel(toBottomEdge = true)

        assertEquals(screenBottom().value, fillBottom().value, TOLERANCE)
    }

    @Test
    fun `a panel by default keeps the bottom overscan`() {
        showPanel(toBottomEdge = false)

        assertTrue("the content reaches the edge it should keep clear of", fillBottom() < screenBottom())
    }

    private fun showPanel(toBottomEdge: Boolean) {
        rule.setContent {
            BingeTvTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    TvSideSheetPanel(toBottomEdge = toBottomEdge) {
                        Box(Modifier.fillMaxWidth().fillMaxHeight().testTag(FILL))
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun fillBottom() = rule.onNodeWithTag(FILL).getUnclippedBoundsInRoot().bottom

    private fun screenBottom() = rule.onRoot().getUnclippedBoundsInRoot().bottom
}
