package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [tvFocusGroup] is `focusRestorer().focusGroup()`: leaving a row and coming back by a directional move lands on
 * the cell that last held focus, not on the first. It saves nothing until the group has been left once, so the
 * first entry is plain geometry.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvFocusGroupRestoreTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    private fun show() =
        rule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    FocusCell(RAIL)
                    Row(Modifier.tvFocusGroup()) {
                        repeat(CELLS) { FocusCell("cell$it") }
                    }
                }
            }
        }

    private fun move(direction: FocusDirection) {
        rule.runOnIdle { focusManager.moveFocus(direction) }
        rule.waitForIdle()
    }

    @Test
    fun `the first entry from the side lands on the first cell`() {
        show()
        rule.onNodeWithTag(RAIL).requestFocus()
        rule.waitForIdle()

        move(FocusDirection.Right)

        rule.onNodeWithTag("cell0").assertIsFocused()
    }

    @Test
    fun `coming back from the side lands on the cell that last held focus`() {
        show()
        rule.onNodeWithTag(RAIL).requestFocus()
        rule.waitForIdle()
        move(FocusDirection.Right)
        move(FocusDirection.Right)
        move(FocusDirection.Right)
        rule.onNodeWithTag("cell2").assertIsFocused()

        // Leave from the third cell itself, not by stepping back across the others, which would make the first
        // cell the last one to hold focus.
        rule.onNodeWithTag(RAIL).requestFocus()
        rule.waitForIdle()
        rule.onNodeWithTag(RAIL).assertIsFocused()

        move(FocusDirection.Right)

        rule.onNodeWithTag("cell2").assertIsFocused()
    }

    private companion object {
        const val RAIL = "rail"
        const val CELLS = 3
    }
}

@Composable
private fun FocusCell(tag: String) {
    Box(Modifier.size(40.dp).testTag(tag).focusable())
}
