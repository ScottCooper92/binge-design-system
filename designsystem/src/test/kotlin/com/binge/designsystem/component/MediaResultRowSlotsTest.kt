package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "row"
private const val BYLINE = "Greta Lee · 2h"
private const val END = "Edit"
private const val STATUS = "Available"
private const val SUBTITLE = "Movie · 2023"
private const val SUFFIX = "(2001)"

private const val LONG_NAME = "The Lord of the Rings: The Fellowship of the Ring, Extended Edition"

/** Long enough to overflow the row even where text is measured coarsely, so the suffix is what has to be protected. */
private val VERY_LONG_NAME = LONG_NAME.repeat(8)

/**
 * The slots and options [MediaResultRow] gained: a byline above the name, an end slot after the text, a suffix after the name, and a status that
 * shares a row with the subtitle. A result that sets none of them is the row it always was.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
class MediaResultRowSlotsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        result: MediaResult,
        fontScale: Float = 1f,
        withSlots: Boolean = false,
        status: Boolean = false,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    Box(Modifier.width(412.dp).testTag(ROW)) {
                        MediaResultRow(
                            result = result,
                            onClick = {},
                            trailing = if (status) ({ StatusChip(label = STATUS, sentiment = BingeSentiment.Positive) }) else null,
                            byline = if (withSlots) ({ Text(BYLINE) }) else null,
                            end = if (withSlots) ({ Text(END) }) else null,
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun suffixWidthPx() =
        rule
            .onNodeWithText(SUFFIX, useUnmergedTree = true)
            .fetchSemanticsNode()
            .size.width

    private fun bounds(text: String) = rule.onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot()

    private val film = MediaResult("m", MediaResultKind.Poster, null, "Past Lives", SUBTITLE, rating = 7.8f)

    @Test
    fun `the byline is above the name, which is above the status line, and the end slot is after the text`() {
        show(film, withSlots = true, status = true)

        assertTrue("byline above name", bounds(BYLINE).bottom <= bounds("Past Lives").top)
        assertTrue("name above the subtitle", bounds("Past Lives").bottom <= bounds(SUBTITLE).top)
        assertTrue("subtitle above the status line", bounds(SUBTITLE).bottom <= bounds(STATUS).top)
        assertTrue("end slot after the text", bounds(END).left >= bounds("Past Lives").right)
        assertTrue("end slot inside the row", bounds(END).right <= rule.onNodeWithTag(ROW).getBoundsInRoot().right)
    }

    @Test
    fun `a row with nothing new set is the row it was`() {
        show(film, status = true)

        assertEquals("no suffix and the status line apart by default", Pair(null, false), Pair(film.nameSuffix, film.statusInline))
        assertTrue("the subtitle keeps its own line above the status", bounds(SUBTITLE).bottom <= bounds(STATUS).top)
        assertTrue("the name starts where the poster ends, with no byline above it", bounds("Past Lives").top < bounds(SUBTITLE).top)
        rule.onAllNodesWithText(BYLINE, useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText(END, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `a name suffix stays whole when a long name ellipsizes, at 2x font`() {
        show(MediaResult("m", MediaResultKind.Poster, null, VERY_LONG_NAME, SUBTITLE, nameSuffix = SUFFIX), fontScale = 2f)

        rule.onNodeWithText(SUFFIX, useUnmergedTree = true).assertIsDisplayed()
        assertTrue("the suffix is inside the row", bounds(SUFFIX).right <= rule.onNodeWithTag(ROW).getBoundsInRoot().right)
        assertTrue("the suffix follows the name", bounds(SUFFIX).left >= bounds(VERY_LONG_NAME).right)
        assertTrue("the suffix keeps its own width and is not squeezed out", suffixWidthPx() > 0)
    }

    @Test
    fun `with the status inline the subtitle shares the status row and has no line of its own`() {
        show(MediaResult("m", MediaResultKind.Poster, null, "Past Lives", SUBTITLE, statusInline = true), status = true)

        assertTrue("the subtitle follows the status", bounds(SUBTITLE).left >= bounds(STATUS).right)
        assertTrue("on the same row", bounds(SUBTITLE).top < bounds(STATUS).bottom)
        assertTrue("under the name", bounds(STATUS).top >= bounds("Past Lives").bottom)
    }
}
