package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ADDRESS = "Not encrypted · http://living-room-media-server.local:8096/jellyfin"

/** An option row's supporting line wraps when the caller allows it more lines, and stays on one by default. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// The line wraps, and legacy graphics measures text at one pixel per character and never wraps it.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvOptionRowSupportingLinesTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `a supporting line given two lines wraps, and one left at the default stays on one`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column(Modifier.width(ROW_WIDTH.dp)) {
                    TvOptionRow(label = "Wrapped", selected = false, onClick = {}, supportingText = ADDRESS, supportingLines = 2)
                    TvOptionRow(label = "Default", selected = false, onClick = {}, supportingText = "$ADDRESS ")
                }
            }
        }
        assertEquals(2, composeTestRule.lineCount(ADDRESS))
        assertEquals(1, composeTestRule.lineCount("$ADDRESS "))
    }

    private fun ComposeContentTestRule.lineCount(text: String): Int {
        val layouts = mutableListOf<TextLayoutResult>()
        onNodeWithText(text, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(layouts)
        return layouts.single().lineCount
    }

    private companion object {
        const val ROW_WIDTH = 320
    }
}
