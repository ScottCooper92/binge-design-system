package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.LayoutDirection
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvStepOutFocusGroup
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_OPENS = "opens"
private const val KEY_PREVIEWS = "previews"
private const val ROW_OPENS = "Streamly"
private const val ROW_PREVIEWS = "Cinebox"
private const val PREVIEW_ROW = "New this week"
private const val CLEAR_ALL = "Clear all"
private const val OUTSIDE = "Outside"
private const val INSIDE = "Inside"

/**
 * [TvPaneRow.onClick], [TvListPaneListPreview] and [tvStepOutFocusGroup] under a real D-pad: OK on an opening row runs
 * it, → on a row whose pane previews where it leads runs it too (and only on that row, never on Clear all), focus never
 * enters a preview, and ← at a stepped-in level's start edge steps back out.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvListPaneOpenFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private var opened = 0
    private var steppedOut = 0

    @Test
    fun `OK on a row with onClick runs it`() {
        setBoard(describedKey = KEY_OPENS, paneContent = false)
        focusRow(ROW_OPENS)

        press(Key.DirectionCenter)

        assertEquals(1, opened)
    }

    @Test
    fun `right on a row whose pane previews where it leads steps in`() {
        setBoard(describedKey = KEY_OPENS, paneContent = true)
        focusRow(ROW_OPENS)

        press(Key.DirectionRight)

        assertEquals(1, opened)
        row(ROW_OPENS).assertIsFocused()
    }

    @Test
    fun `right steps in under RTL too`() {
        setBoard(describedKey = KEY_OPENS, paneContent = true, layoutDirection = LayoutDirection.Rtl)
        focusRow(ROW_OPENS)

        press(Key.DirectionLeft)

        assertEquals(1, opened)
    }

    @Test
    fun `right on a row without onClick does not run anything`() {
        setBoard(describedKey = KEY_OPENS, paneContent = false)
        focusRow(ROW_OPENS)

        press(Key.DirectionRight)

        assertEquals(0, opened)
    }

    @Test
    fun `right on Clear all does not step into the described row`() {
        setBoard(describedKey = KEY_OPENS, paneContent = true, clearAll = true)
        focusClearAll()

        press(Key.DirectionRight)

        assertEquals(0, opened)
        composeTestRule.onNode(hasText(CLEAR_ALL) and isFocusable()).assertIsFocused()
    }

    @Test
    fun `right on the described row still steps in when there is a Clear all`() {
        setBoard(describedKey = KEY_OPENS, paneContent = true, clearAll = true)
        focusRow(ROW_OPENS)

        press(Key.DirectionRight)

        assertEquals(1, opened)
        row(ROW_OPENS).assertIsFocused()
    }

    @Test
    fun `right on Clear all under RTL does not step in either`() {
        setBoard(describedKey = KEY_OPENS, paneContent = true, clearAll = true, layoutDirection = LayoutDirection.Rtl)
        focusClearAll()

        press(Key.DirectionLeft)

        assertEquals(0, opened)
    }

    @Test
    fun `focus cannot enter a preview`() {
        setBoard(describedKey = KEY_PREVIEWS, paneContent = true)
        focusRow(ROW_PREVIEWS)

        press(Key.DirectionRight)

        row(ROW_PREVIEWS).assertIsFocused()
        composeTestRule.onNode(hasText(PREVIEW_ROW)).assertDoesNotExist()
    }

    @Test
    fun `left at the start edge of a stepped-in level steps out`() {
        setStepOut(LayoutDirection.Ltr)
        composeTestRule.onNode(hasText(INSIDE) and isFocusable()).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionLeft)

        assertEquals(1, steppedOut)
        composeTestRule.onNode(hasText(INSIDE) and isFocusable()).assertIsFocused()
    }

    @Test
    fun `the start edge is the right-hand one under RTL`() {
        setStepOut(LayoutDirection.Rtl)
        composeTestRule.onNode(hasText(INSIDE) and isFocusable()).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionRight)

        assertEquals(1, steppedOut)
    }

    @Test
    fun `the end edge leaves as usual`() {
        setStepOut(LayoutDirection.Ltr, outsideAtEnd = true)
        composeTestRule.onNode(hasText(INSIDE) and isFocusable()).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionRight)

        assertEquals(0, steppedOut)
        composeTestRule.onNode(hasText(OUTSIDE) and isFocusable()).assertIsFocused()
    }

    private fun setBoard(
        describedKey: String,
        paneContent: Boolean,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        clearAll: Boolean = false,
    ) {
        val preview = listOf(TvPaneGroup(title = "Rows", rows = listOf(TvPaneRow(key = "row", label = PREVIEW_ROW))))
        val opens =
            TvPaneRow(
                key = KEY_OPENS,
                label = ROW_OPENS,
                onClick = { opened++ },
                paneContent = if (paneContent) ({ TvListPaneListPreview(preview) }) else null,
            )
        val previews = TvPaneRow(key = KEY_PREVIEWS, label = ROW_PREVIEWS, paneContent = { TvListPaneListPreview(preview) })
        composeTestRule.setContent {
            var focusedKey by remember { mutableStateOf<String?>(describedKey) }
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                BingeTvTheme {
                    TvListPaneBoard(
                        title = "Apps",
                        groups =
                            listOf(
                                TvPaneGroup(
                                    title = "Installed",
                                    rows = if (clearAll) listOf(previews, opens) else listOf(opens, previews),
                                ),
                            ),
                        focusedKey = focusedKey,
                        onFocusRow = { focusedKey = it },
                        clearAll = if (clearAll) TvPaneAction(label = CLEAR_ALL, onClick = {}) else null,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun setStepOut(layoutDirection: LayoutDirection, outsideAtEnd: Boolean = false) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                BingeTvTheme {
                    Row {
                        if (!outsideAtEnd) Button(onClick = {}) { Text(OUTSIDE) }
                        Row(modifier = Modifier.tvStepOutFocusGroup { steppedOut++ }) {
                            Button(onClick = {}) { Text(INSIDE) }
                        }
                        if (outsideAtEnd) Button(onClick = {}) { Text(OUTSIDE) }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** Walks down from the stepping-in row, which [setBoard] puts last when there is a Clear all, so it stays the described one. */
    private fun focusClearAll() {
        focusRow(ROW_OPENS)
        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(CLEAR_ALL) and isFocusable()).assertIsFocused()
    }

    private fun focusRow(label: String) {
        row(label).requestFocus()
        composeTestRule.waitForIdle()
        row(label).assertIsFocused()
    }

    private fun row(label: String) = composeTestRule.onNode(hasText(label) and isFocusable())

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
