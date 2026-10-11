package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.height
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val WITH_LINE = "with"
private const val RESERVED = "reserved"
private const val PLAIN = "plain"

/** A row that reserves its supporting line stands as tall as one that has the line, and a row that does neither is shorter. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetRowReservedLineTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a reserved line keeps the row at its neighbour's height`() {
        rule.setContent {
            BingeTvTheme {
                Column {
                    TvSideSheetRow(label = "Pending", supportingText = "12 requests", onClick = {}, modifier = Modifier.testTag(WITH_LINE))
                    TvSideSheetRow(label = "All", onClick = {}, reserveSupportingLine = true, modifier = Modifier.testTag(RESERVED))
                    TvSideSheetRow(label = "All", onClick = {}, modifier = Modifier.testTag(PLAIN))
                }
            }
        }

        val withLine = height(WITH_LINE)
        val reserved = height(RESERVED)
        val plain = height(PLAIN)
        assertEquals(withLine.value, reserved.value, 0.01f)
        assertTrue("a plain row ($plain) is shorter than one with a line ($withLine)", plain < withLine)
    }

    private fun height(tag: String) = rule.onNodeWithTag(tag).getBoundsInRoot().height
}
