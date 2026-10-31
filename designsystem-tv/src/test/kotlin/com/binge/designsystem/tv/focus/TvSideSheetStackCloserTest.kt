package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetRows
import com.binge.designsystem.tv.component.TvSideSheetStack
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val OPENER = "opener"
private const val ROW = "row"

/**
 * A stacked sheet given its closer reports its disposal, so closing it returns focus once, in the frame it is gone, and the
 * closer never falls back to retrying. A stack not given it leaves [TvOverlayCloser.reportsDisposal] false.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackCloserTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private var open by mutableStateOf(false)
    private lateinit var closer: TvOverlayCloser

    @Test
    fun `a stack given the closer reports its disposal and focus returns to the opener`() {
        show(passCloser = true)
        open = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ROW).requestFocus()
        assertTrue(closer.reportsDisposal)

        composeTestRule.runOnIdle { closer.close() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(ROW).assertDoesNotExist()
        composeTestRule.onNodeWithTag(OPENER).assertIsFocused()
        assertFalse(closer.reportsDisposal)
    }

    @Test
    fun `a stack not given the closer does not report, which is what the closer's fallback is for`() {
        show(passCloser = false)
        open = true
        composeTestRule.waitForIdle()

        assertFalse(closer.reportsDisposal)
    }

    private fun show(passCloser: Boolean) {
        composeTestRule.setContent {
            BingeTvTheme {
                val opener = remember { FocusRequester() }
                closer = rememberTvOverlayCloser(restoreTo = opener) { open = false }
                Box(Modifier.fillMaxSize()) {
                    TvButton(label = "Open", onClick = {}, modifier = Modifier.testTag(OPENER).focusRequester(opener))
                    if (open) {
                        TvSideSheetStack(root = "root", onDismissRequest = closer::close, closer = closer.takeIf { passCloser }) {
                            TvSideSheetTitle("Sheet")
                            TvSideSheetRows { TvSideSheetRow(label = "Row", onClick = {}, modifier = Modifier.testTag(ROW)) }
                        }
                    }
                }
            }
        }
    }
}
