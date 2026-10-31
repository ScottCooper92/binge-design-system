package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Moving between pages of a [TvSideSheetStack] must never show focus on a row it was not headed for: the page that opens
 * takes it on the row it arrives at, and the page gone back to takes it on the row that opened the page just left.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackPageChangeFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val focused = mutableStateListOf<String>()
    private var openerEnabled by mutableStateOf(true)

    private fun Modifier.record(label: String): Modifier = onFocusChanged { if (it.isFocused) focused.add(label) }

    private var disableOpener = false

    private fun setSheet() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheetStack(root = "root", onDismissRequest = {}) { nav ->
                    TvSideSheetTitle(nav.page)
                    (1..ROWS).forEach { index ->
                        val key = "${nav.page}-$index"
                        TvSideSheetRow(
                            label = "${nav.page} $index",
                            enabled = nav.page != "root" || index != OPENER || openerEnabled,
                            onClick = {
                                if (nav.page == "root") {
                                    if (disableOpener) openerEnabled = false
                                    nav.push("child", key)
                                }
                            },
                            modifier = nav.row(key).record(key),
                        )
                    }
                    if (nav.page == "child") nav.arriveAt("child-$ARRIVAL")
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key, on: String) {
        composeTestRule.onNodeWithText(on).requestFocus()
        composeTestRule.waitForIdle()
        focused.clear()
        composeTestRule.onNodeWithText(on).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a page that opens takes focus on its arrival row, never on its first`() {
        setSheet()

        press(Key.DirectionCenter, on = "root $OPENER")

        composeTestRule.onNodeWithText("child $ARRIVAL").assertIsFocused()
        assertFalse("the first row took focus on the way in: $focused", focused.contains("child-1"))
        assertEquals("focus order: $focused", listOf("child-$ARRIVAL"), focused.toList())
    }

    @Test
    fun `going back takes focus on the row that opened the page, never on the first`() {
        setSheet()
        press(Key.DirectionCenter, on = "root $OPENER")

        press(Key.DirectionLeft, on = "child $ARRIVAL")

        composeTestRule.onNodeWithText("root $OPENER").assertIsFocused()
        assertFalse("the first row took focus on the way back: $focused", focused.contains("root-1"))
        assertEquals("focus order: $focused", listOf("root-$OPENER"), focused.toList())
    }

    @Test
    fun `an opener that cannot take focus on the way back hands it to the first row, not to nothing`() {
        disableOpener = true
        setSheet()
        press(Key.DirectionCenter, on = "root $OPENER")

        press(Key.DirectionLeft, on = "child $ARRIVAL")

        composeTestRule.onNodeWithText("root 1").assertIsFocused()
        assertEquals("focus order: $focused", listOf("root-1"), focused.toList())
    }

    @Test
    fun `one Back pops a pushed page and focus lands on the row that opened it`() {
        setSheet()
        press(Key.DirectionCenter, on = "root $OPENER")

        press(Key.Back, on = "child $ARRIVAL")

        composeTestRule.onNodeWithText("root $OPENER").assertIsFocused()
        assertEquals("focus order: $focused", listOf("root-$OPENER"), focused.toList())
    }

    private fun showFocusEntry(arrival: String?): () -> Boolean {
        lateinit var focusEntry: () -> Boolean
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheetStack(root = "page", onDismissRequest = {}) { nav ->
                    focusEntry = nav::focusEntry
                    (1..ROWS).forEach { index ->
                        TvSideSheetRow(label = "row $index", onClick = {}, modifier = nav.row("page-$index"))
                    }
                    arrival?.let(nav::arriveAt)
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("row 5").requestFocus()
        composeTestRule.waitForIdle()
        return focusEntry
    }

    /** A page whose focused row has gone hands focus back to where the page opens: its arrival row. */
    @Test
    fun `focusEntry puts focus on the page's arrival row`() {
        val focusEntry = showFocusEntry(arrival = "page-3")

        composeTestRule.runOnUiThread { assertTrue(focusEntry()) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("row 3").assertIsFocused()
    }

    @Test
    fun `focusEntry puts focus on the page's first row when it names no arrival`() {
        val focusEntry = showFocusEntry(arrival = null)

        composeTestRule.runOnUiThread { assertTrue(focusEntry()) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("row 1").assertIsFocused()
    }

    private companion object {
        const val ROWS = 5
        const val OPENER = 4
        const val ARRIVAL = 3
    }
}
