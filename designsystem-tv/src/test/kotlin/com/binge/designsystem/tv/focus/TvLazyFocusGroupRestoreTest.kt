package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
 * [tvLazyFocusGroup] is the restorer alone, for a lazy row that is already its own focus group: the first entry lands
 * on the fallback when there is one, and coming back by a directional move lands on the cell that last held focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvLazyFocusGroupRestoreTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    private fun show(withFallback: Boolean) =
        rule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                val fallback = remember { FocusRequester() }
                Row {
                    FocusCell(RAIL)
                    LazyRow(Modifier.tvLazyFocusGroup(fallback = fallback.takeIf { withFallback })) {
                        items(CELLS) {
                            FocusCell("cell$it", if (it == FALLBACK) Modifier.focusRequester(fallback) else Modifier)
                        }
                    }
                }
            }
        }

    private fun move(direction: FocusDirection) {
        rule.runOnIdle { focusManager.moveFocus(direction) }
        rule.waitForIdle()
    }

    private fun enterFromRail() {
        rule.onNodeWithTag(RAIL).requestFocus()
        rule.waitForIdle()
        move(FocusDirection.Right)
    }

    @Test
    fun `without a fallback the first entry from the side lands on the first cell`() {
        show(withFallback = false)

        enterFromRail()

        rule.onNodeWithTag("cell0").assertIsFocused()
    }

    @Test
    fun `with a fallback the first entry from the side lands on it`() {
        show(withFallback = true)

        enterFromRail()

        rule.onNodeWithTag("cell$FALLBACK").assertIsFocused()
    }

    @Test
    fun `coming back from the side lands on the cell that last held focus, not the fallback`() {
        show(withFallback = true)
        enterFromRail()
        move(FocusDirection.Right)
        rule.onNodeWithTag("cell2").assertIsFocused()

        // Leave from the third cell itself, so it is the one saved.
        rule.onNodeWithTag(RAIL).requestFocus()
        rule.waitForIdle()
        rule.onNodeWithTag(RAIL).assertIsFocused()

        move(FocusDirection.Right)

        rule.onNodeWithTag("cell2").assertIsFocused()
    }

    private companion object {
        const val RAIL = "rail"
        const val CELLS = 3
        const val FALLBACK = 1
    }
}

@Composable
private fun FocusCell(tag: String, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).testTag(tag).focusable())
}
