package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROWS = 20
private const val TOLERANCE = 0.5f

/** A page keeps its scroll while another is open over it, and forgets it once gone back from. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackScrollTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private fun show() {
        rule.setContent {
            BingeTvTheme {
                TvSideSheetStack(root = "root", onDismissRequest = {}, toBottomEdge = true) { nav ->
                    val prefix = if (nav.page == "root") "Row" else "Item"
                    TvSideSheetTitle(if (nav.page == "root") "Settings" else "Sub-page")
                    TvSideSheetRows {
                        for (index in 1..ROWS) {
                            TvSideSheetRow(
                                label = "$prefix $index",
                                onClick = { if (nav.page == "root") nav.push("sub", openedBy = "row$index") },
                                modifier = nav.row("row$index"),
                            )
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun press(key: Key, times: Int = 1) {
        repeat(times) {
            rule.onRoot().performKeyInput { pressKey(key) }
            rule.waitForIdle()
        }
    }

    private fun top(label: String) =
        rule
            .onNodeWithText(label)
            .getUnclippedBoundsInRoot()
            .top.value

    @Test
    fun `back from a page returns the list to where it was scrolled, with focus on the row that opened it`() {
        show()
        press(Key.DirectionDown, times = ROWS - 1) // to the last row, scrolling the list to its end
        press(Key.DirectionUp, times = 6) // back up to Row 14, which the list leaves where it is
        rule.onNodeWithText("Row 14").assertIsFocused()
        val before = top("Row 14")

        press(Key.DirectionCenter)
        rule.onNodeWithText("Sub-page").assertExists()
        press(Key.DirectionLeft)

        rule.onNodeWithText("Row 14").assertIsFocused()
        assertEquals("the list jumped on going back", before, top("Row 14"), TOLERANCE)
    }

    @Test
    fun `a page gone back from opens again from its top`() {
        show()
        press(Key.DirectionCenter) // Row 1 opens the sub-page
        val firstTop = top("Item 1")
        press(Key.DirectionDown, times = ROWS - 1)
        press(Key.DirectionLeft) // back to the root
        rule.onNodeWithText("Row 1").assertIsFocused()

        press(Key.DirectionCenter)

        rule.onNodeWithText("Item 1").assertIsFocused()
        assertEquals("the page kept the scroll it was left with", firstTop, top("Item 1"), TOLERANCE)
    }
}
