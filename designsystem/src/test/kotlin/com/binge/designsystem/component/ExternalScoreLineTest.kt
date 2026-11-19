package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * An [ExternalScoreLine] reads each score as one item ("Critics 97 percent"), and no score is cropped by its row or the
 * line at 1.3x or 2x font: the value sits inside its item, the item inside the line, and the value stays on one line.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExternalScoreLineTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val scores = listOf(
        ExternalScore(ExternalScoreKind.CriticsFresh, "97%", "Critics 97 percent"),
        ExternalScore(ExternalScoreKind.Audience, "95%", "Audience 95 percent"),
        ExternalScore(ExternalScoreKind.Imdb, "9.0", "IMDb 9.0"),
    )

    private fun show(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(200.dp).testTag("line")) { ExternalScoreLine(scores = scores) }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun valueLayout(value: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText(value, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        return results.first()
    }

    private fun assertNothingCropped(fontScale: Float) {
        show(fontScale)
        val line = rule.onNodeWithTag("line").getBoundsInRoot()
        scores.forEach { score ->
            val item = rule.onNodeWithContentDescription(score.contentDescription).getBoundsInRoot()
            val text = rule.onNodeWithText(score.value, useUnmergedTree = true).getBoundsInRoot()
            assertTrue("${score.value} sits inside its item", text.left >= item.left && text.right <= item.right)
            assertTrue("${score.value} sits inside its item", text.top >= item.top && text.bottom <= item.bottom)
            assertTrue("the item sits inside the line", item.left >= line.left && item.right <= line.right)
            assertTrue("${score.value} is on one line", valueLayout(score.value).lineCount == 1)
        }
    }

    @Test fun `no score is cropped at 1_0`() = assertNothingCropped(1.0f)

    @Test fun `no score is cropped at 1_3`() = assertNothingCropped(1.3f)

    @Test fun `no score is cropped at 2_0`() = assertNothingCropped(2.0f)

    @Test
    fun `each score is one item with its full description`() {
        show(1.0f)
        scores.forEach { rule.onNodeWithContentDescription(it.contentDescription).assertIsDisplayed() }
    }
}
