package com.binge.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.binge.designsystem.R
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
import org.robolectric.annotation.GraphicsMode

/**
 * A [MediaCard]'s status chip at the largest font scale: the label stays on one line, so it can never be broken inside a word, and
 * a label that does not fit is cut with an ellipsis rather than wrapped over the poster.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is broken.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaCardStatusFontScaleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun layoutOf(label: String, fontScale: Float): TextLayoutResult {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                    MediaCard(
                        posterUrl = null,
                        title = "Arrival",
                        rating = null,
                        onClick = {},
                        status = MediaCardStatus(label = label, sentiment = BingeSentiment.Positive),
                        modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                    )
                }
            }
        }
        rule.waitForIdle()
        val results = mutableListOf<TextLayoutResult>()
        val node = rule.onNodeWithText(label, useUnmergedTree = true).fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    @Test
    fun `a one-word label stays on one line at 2x`() {
        val layout = layoutOf("Available", fontScale = 2f)
        assertEquals("a word broken across lines", 1, layout.lineCount)
    }

    @Test
    fun `a two-word label stays on one line at 2x and is cut with an ellipsis`() {
        val layout = layoutOf("Partly available", fontScale = 2f)
        assertEquals(1, layout.lineCount)
        assertTrue("the label does not fit, so it should be cut", layout.isLineEllipsized(0))
    }

    @Test
    fun `a label that fits is left whole at the default scale`() {
        val layout = layoutOf("Available", fontScale = 1f)
        assertEquals(1, layout.lineCount)
        assertTrue(!layout.isLineEllipsized(0))
    }
}
