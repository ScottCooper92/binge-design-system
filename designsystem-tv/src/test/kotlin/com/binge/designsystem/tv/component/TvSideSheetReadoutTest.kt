package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ABOVE = "above"
private const val READOUT = "readout"
private const val BELOW = "below"
private const val ROW = "row"
private const val VERSION = "2.7.3"

/** A read-out is passed over by the D-pad, reads as one item with its value, and stands as tall as a row with the same lines. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetReadoutTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `down from the row above a read-out lands on the row below it`() {
        setSheet()
        rule.onNodeWithTag(ABOVE).requestFocus()
        rule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        rule.waitForIdle()

        rule.onNodeWithTag(BELOW).assertIsFocused()
    }

    @Test
    fun `a read-out is one item holding its value, with nothing to press`() {
        setSheet()

        rule
            .onNodeWithTag(READOUT)
            .assert(hasText("Version") and hasText(VERSION))
            .assertHasNoClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
    }

    @Test
    fun `a read-out stands as tall as a row with the same lines`() {
        setSheet()

        assertEquals(height(ROW).value, height(READOUT).value, 0.01f)
    }

    private fun setSheet() {
        rule.setContent {
            BingeTvTheme {
                Column {
                    TvSideSheetRow(label = "Scan", onClick = {}, modifier = Modifier.testTag(ABOVE))
                    TvSideSheetReadout(label = "Version", supportingText = VERSION, modifier = Modifier.testTag(READOUT))
                    TvSideSheetRow(label = "Restart", onClick = {}, modifier = Modifier.testTag(BELOW))
                    TvSideSheetRow(label = "Version", supportingText = VERSION, onClick = {}, modifier = Modifier.testTag(ROW))
                }
            }
        }
        rule.waitForIdle()
    }

    private fun height(tag: String) = rule.onNodeWithTag(tag).getBoundsInRoot().height
}
