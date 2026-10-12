package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ONE_LINE = "alpha"
private const val THREE_LINES = "beta"
private const val FILLER = " words that run on well past the end of a narrow row so the line has to wrap onto the next one and the next"

/**
 * A row's second line is one line by default and takes more when the caller raises `supportingMaxLines`, on the
 * three rows that cap it: [CheckboxRow], [BingeChoice] in a sheet, and an [ItemGroup] row's detail.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// The second line wraps, and legacy graphics measures text at one pixel per character and never wraps it.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SupportingMaxLinesTest {
    @get:Rule
    val rule = createComposeRule()

    private fun heightOf(text: String) = rule.onNodeWithText(text, substring = true).getUnclippedBoundsInRoot().let { it.bottom - it.top }

    private fun assertRaisedLineIsTaller() {
        val one = heightOf(ONE_LINE)
        val three = heightOf(THREE_LINES)
        assertTrue("expected the raised line ($three) to be taller than the default ($one)", three > one)
    }

    @Test
    fun `a checkbox row's second line takes more lines when raised`() {
        rule.setContent {
            Column(Modifier.width(300.dp)) {
                CheckboxRow(label = "A", checked = false, onCheckedChange = {}, supportingText = ONE_LINE + FILLER)
                CheckboxRow(
                    label = "B",
                    checked = false,
                    onCheckedChange = {},
                    supportingText = THREE_LINES + FILLER,
                    supportingMaxLines = 3,
                )
            }
        }

        assertRaisedLineIsTaller()
    }

    @Test
    fun `a choice's supporting line takes more lines when raised`() {
        rule.setContent {
            Column(Modifier.width(300.dp)) {
                BingeChoiceSheetContent(
                    title = "Pick",
                    choices =
                        BingeChoiceList.Ready(
                            listOf(
                                BingeChoice("a", "A", supportingText = ONE_LINE + FILLER),
                                BingeChoice("b", "B", supportingText = THREE_LINES + FILLER, supportingMaxLines = 3),
                            ),
                        ),
                    selected = "a",
                    onSelect = {},
                )
            }
        }

        assertRaisedLineIsTaller()
    }

    @Test
    fun `an unmarked multi-select choice's supporting line takes more lines when raised`() {
        rule.setContent {
            Column(Modifier.width(300.dp)) {
                BingeMultiChoiceSheetContent(
                    title = "Pick",
                    choices =
                        BingeChoiceList.Ready(
                            listOf(
                                BingeChoice("a", "A", supportingText = ONE_LINE + FILLER),
                                BingeChoice("b", "B", supportingText = THREE_LINES + FILLER, supportingMaxLines = 3),
                            ),
                        ),
                    chosen = emptySet(),
                    onCheckedChange = { _, _ -> },
                    clearLabel = "Clear",
                    onClear = {},
                )
            }
        }

        assertRaisedLineIsTaller()
    }

    @Test
    fun `a list item's detail takes more lines when raised`() {
        rule.setContent {
            Column(Modifier.width(300.dp)) {
                ItemGroup(
                    title = "Group",
                    rows =
                        listOf(
                            ListItem(icon = Icons.Filled.Tune, label = "A", detail = ONE_LINE + FILLER),
                            ListItem(icon = Icons.Filled.Tune, label = "B", detail = THREE_LINES + FILLER, supportingMaxLines = 3),
                        ),
                )
            }
        }

        assertRaisedLineIsTaller()
    }
}
