package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LONG_LABEL = "Parcialmente disponible"

/**
 * A [BingeStatusButton] is tappable in every state, keeps its whole label inside the button at 1.3x and 2x font (a long label
 * wraps to a second line rather than being cropped or ellipsized), and is named by its label alone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeStatusButtonTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(fontScale: Float, content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) { content() }
            }
        }
        rule.waitForIdle()
    }

    private fun labelLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText(LONG_LABEL)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        return results.first()
    }

    private fun assertLabelWhole(fontScale: Float) {
        show(fontScale) {
            Box(Modifier.width(220.dp)) {
                BingeStatusButton(
                    label = LONG_LABEL,
                    onClick = {},
                    sentiment = BingeSentiment.Info,
                    modifier = Modifier.fillMaxWidth().testTag("button"),
                )
            }
        }

        val button = rule.onNodeWithTag("button").getBoundsInRoot()
        val label = rule.onNodeWithText(LONG_LABEL).getBoundsInRoot()
        assertTrue("the label sits inside the button", label.left >= button.left && label.right <= button.right)
        assertTrue("the label sits inside the button", label.top >= button.top && label.bottom <= button.bottom)
        val layout = labelLayout()
        assertTrue("the label is not ellipsized", (0 until layout.lineCount).none { layout.isLineEllipsized(it) })
        assertTrue("the button is at least as tall as its label", button.height >= label.height)
    }

    @Test fun `the whole label sits inside the button at 1_3`() = assertLabelWhole(1.3f)

    @Test fun `the whole label sits inside the button at 2_0`() = assertLabelWhole(2.0f)

    @Test
    fun `every state can be tapped`() {
        var taps = 0
        val states = listOf(
            "Request" to BingeSentiment.Neutral,
            "Requested" to BingeSentiment.Caution,
            "Processing" to BingeSentiment.Info,
            "Available" to BingeSentiment.Positive,
        )
        show(1f) {
            androidx.compose.foundation.layout.Column {
                states.forEachIndexed { index, (label, sentiment) ->
                    BingeStatusButton(
                        label = label,
                        onClick = { taps++ },
                        sentiment = sentiment,
                        emphasis = if (index == 0) BingeStatusButtonEmphasis.Filled else BingeStatusButtonEmphasis.Tonal,
                    )
                }
            }
        }

        states.forEach { (label, _) -> rule.onNodeWithText(label).assertHasClickAction().performClick() }

        assertEquals(states.size, taps)
    }

    @Test
    fun `the button is named by its label alone`() {
        show(1f) { BingeStatusButton("Available", onClick = {}, sentiment = BingeSentiment.Positive) }

        // The dot is decoration: the one text node is the label, and merging adds nothing else to read.
        val node = rule.onNodeWithText("Available").fetchSemanticsNode()
        val texts = node.config[androidx.compose.ui.semantics.SemanticsProperties.Text].map { it.text }
        assertEquals(listOf("Available"), texts)
    }
}
