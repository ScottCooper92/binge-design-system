package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "row"
private const val PLAIN = "plain"
private const val FOOTED = "footed"
private const val FOOTER = "Downloading"
private const val POSTER_LABEL = "Open title"
private const val NAME = "Past Lives"
private const val SUBTITLE = "Movie · 2023"

/** The footer and the poster tap [MediaResultRow] gained: both are off by default, and a poster tap is not a tap on the row. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
class MediaResultRowPosterTapTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var rowTaps = 0
    private var posterTaps = 0

    private fun show(
        kind: MediaResultKind = MediaResultKind.Poster,
        footer: Boolean = false,
        posterTap: Boolean = false,
    ) {
        rule.setContent {
            TestTheme {
                Box(Modifier.width(412.dp).testTag(ROW)) {
                    Row(kind, footer, posterTap)
                }
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun Row(
        kind: MediaResultKind,
        footer: Boolean,
        posterTap: Boolean,
    ) = MediaResultRow(
        result = MediaResult("m", kind, null, NAME, SUBTITLE),
        onClick = { rowTaps++ },
        footer = if (footer) ({ Text(FOOTER) }) else null,
        onPosterClick = if (posterTap) ({ posterTaps++ }) else null,
        posterClickLabel = if (posterTap) POSTER_LABEL else null,
    )

    private fun poster() =
        rule.onNode(
            SemanticsMatcher("click label is the poster's") { it.config.getOrNull(SemanticsActions.OnClick)?.label == POSTER_LABEL },
            useUnmergedTree = true,
        )

    private fun bounds(text: String) = rule.onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot()

    @Test
    fun `the footer is drawn under the picture and the text, inside the row`() {
        show(footer = true)

        assertTrue("under the subtitle", bounds(FOOTER).top >= bounds(SUBTITLE).bottom)
        assertTrue("inside the row", bounds(FOOTER).right <= rule.onNodeWithTag(ROW).getBoundsInRoot().right)
    }

    @Test
    fun `a row with no footer takes no room for one`() {
        rule.setContent {
            TestTheme {
                Column(Modifier.width(412.dp)) {
                    Box(Modifier.testTag(PLAIN)) { Row(MediaResultKind.Poster, footer = false, posterTap = false) }
                    Box(Modifier.testTag(FOOTED)) { Row(MediaResultKind.Poster, footer = true, posterTap = false) }
                }
            }
        }
        rule.waitForIdle()

        fun height(tag: String) = rule.onNodeWithTag(tag).getBoundsInRoot().let { it.bottom - it.top }
        rule.onAllNodesWithText(FOOTER, useUnmergedTree = true).assertCountEquals(1)
        assertTrue("a footer adds height", height(FOOTED) > height(PLAIN))
    }

    @Test
    fun `tapping the poster fires only the poster's callback`() {
        show(posterTap = true)

        poster().performClick()

        assertEquals(1, posterTaps)
        assertEquals(0, rowTaps)
    }

    @Test
    fun `tapping the text fires only the row's callback`() {
        show(posterTap = true)

        rule.onNodeWithText(NAME, useUnmergedTree = true).performClick()

        assertEquals(0, posterTaps)
        assertEquals(1, rowTaps)
    }

    @Test
    fun `the poster tap is a target of at least the minimum size, with its label`() {
        show(posterTap = true)

        poster().assertWidthIsAtLeast(MIN_TARGET).assertHeightIsAtLeast(MIN_TARGET)
    }

    @Test
    fun `a still is its own tap target too`() {
        show(kind = MediaResultKind.Still, posterTap = true)

        poster().performClick()

        assertEquals(1, posterTaps)
        assertEquals(0, rowTaps)
    }

    @Test
    fun `with no poster callback the row has only its own tap target`() {
        show()

        rule.onAllNodes(hasClickAction(), useUnmergedTree = true).assertCountEquals(1)
    }

    private companion object {
        val MIN_TARGET = 48.dp
    }
}
