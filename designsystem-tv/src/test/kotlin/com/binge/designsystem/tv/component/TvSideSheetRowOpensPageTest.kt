package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.tv.material3.Text
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "row"
private const val PLAIN_ROW = "plain-row"
private const val VALUE = "value"
private const val PLAIN_VALUE = "plain-value"
private const val LONG_VALUE = "Every request since the server was set up"
private const val LONG_LABEL = "A label long enough to want the whole row"

/** A row that opens a page ends in a chevron after its value, and the value is held to its cap at a large font. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetRowOpensPageTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the value sits before the chevron, where a row that opens nothing ends in it`() {
        show(fontScale = 1f, value = "English")

        val chevronRoom = 24.dp
        assertTrue(
            "the value ends at ${right(VALUE)}, leaving no room for the chevron before ${right(ROW)}",
            right(VALUE) + chevronRoom <= right(ROW),
        )
        assertTrue("the chevron did not push the value in from ${right(PLAIN_VALUE)}", right(VALUE) + chevronRoom <= right(PLAIN_VALUE))
    }

    @Test
    fun `at 2x font scale a long value keeps to its cap, the label keeps room and the chevron stays in the row`() {
        show(fontScale = 2f, value = LONG_VALUE, valueWidth = 300.dp)

        val value = rule.onNodeWithTag(VALUE, useUnmergedTree = true).getUnclippedBoundsInRoot()
        val label = rule.onNodeWithText(LONG_LABEL, useUnmergedTree = true).getUnclippedBoundsInRoot()
        // The label's room is up to the value: its text node is only as wide as the text it ellipsizes to.
        val labelRoom = value.left - label.left
        assertTrue("the value is ${value.width} wide, past its cap", value.width <= 120.dp + 0.5.dp)
        assertTrue("the label was left $labelRoom", labelRoom >= 120.dp)
        assertTrue("the chevron was pushed out of the row", value.right + 24.dp <= right(ROW))
    }

    /** [valueWidth] asks for that much room for the value, wider than its cap, since a test font measures narrower than a TV's. */
    private fun show(
        fontScale: Float,
        value: String,
        valueWidth: Dp? = null,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeTvTheme {
                    Column(Modifier.width(348.dp)) {
                        TvSideSheetRow(label = LONG_LABEL, onClick = {}, opensPage = true, modifier = Modifier.testTag(ROW)) {
                            Text(
                                value,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag(VALUE).then(if (valueWidth != null) Modifier.width(valueWidth) else Modifier),
                            )
                        }
                        TvSideSheetRow(label = "Plain", onClick = {}, modifier = Modifier.testTag(PLAIN_ROW)) {
                            Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.testTag(PLAIN_VALUE))
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun right(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot().right
}
