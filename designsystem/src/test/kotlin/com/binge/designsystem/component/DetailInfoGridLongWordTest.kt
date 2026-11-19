package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A [DetailInfoGrid] line with a word wider than its column puts its facts one above the other at the full width, so the
 * word is not broken mid-word; a line whose words all fit keeps its two columns. At 1.0x on 360dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailInfoGridLongWordTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val longWord = "Supercalifragilisticexpialidocious"

    private fun show() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Box(Modifier.width(360.dp).testTag("grid")) {
                    DetailInfoGrid(
                        entries = listOf(
                            DetailInfoEntry(Icons.Filled.Person, "Studio", longWord),
                            DetailInfoEntry(Icons.Filled.Person, "Released", "2008"),
                            DetailInfoEntry(Icons.Filled.Person, "Status", "Completed"),
                            DetailInfoEntry(Icons.Filled.Person, "Budget", "185 million"),
                        ),
                        columns = 2,
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun layout(text: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText(text, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        return results.first()
    }

    @Test
    fun `a long single word is on one line, not broken mid-word`() {
        show()

        assertEquals(1, layout(longWord).lineCount)
    }

    @Test
    fun `a line with a word wider than its column stacks its facts`() {
        show()

        val studio = rule.onNodeWithText("Studio", useUnmergedTree = true).getBoundsInRoot()
        val released = rule.onNodeWithText("Released", useUnmergedTree = true).getBoundsInRoot()
        assertTrue("the second fact sits below the first", released.top >= studio.bottom)
        assertEquals("the stacked facts share a left edge", studio.left, released.left)
    }

    @Test
    fun `a line whose words all fit keeps its two columns`() {
        show()

        val status = rule.onNodeWithText("Status", useUnmergedTree = true).getBoundsInRoot()
        val budget = rule.onNodeWithText("Budget", useUnmergedTree = true).getBoundsInRoot()
        assertEquals("the two facts start at the same height", status.top, budget.top)
        assertTrue("the second fact is to the right of the first", budget.left > status.left)
    }
}
