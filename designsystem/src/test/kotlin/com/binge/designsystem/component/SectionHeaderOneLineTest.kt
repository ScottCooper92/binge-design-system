package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A [SectionHeader]'s title is one line: a title too long for the row ellipsizes beside the trailing action instead of
 * wrapping to a second line. What a screen reader hears is the whole title, because the semantics text is the string and
 * not what is drawn, and the "see all" button names the whole section too.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap or ellipsize.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SectionHeaderOneLineTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(title: String, fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(412.dp)) { SectionHeader(title = title, onMoreClick = {}) }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun titleLayout(title: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText(title)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        return results.first()
    }

    private fun assertOneEllipsizedLine(fontScale: Float) {
        show(LONG_TITLE, fontScale)

        val layout = titleLayout(LONG_TITLE)
        assertEquals("the title should not wrap", 1, layout.lineCount)
        assertTrue("the title should be ellipsized", layout.isLineEllipsized(0))
        // A screen reader reads the whole title, not the part that is drawn.
        val text = rule
            .onNodeWithText(LONG_TITLE)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
        assertEquals(listOf(LONG_TITLE), text?.map { it.text })
        rule.onNodeWithContentDescription("See all in $LONG_TITLE").assertExists()
    }

    @Test fun `a long title is one ellipsized line at 1_0`() = assertOneEllipsizedLine(1.0f)

    @Test fun `a long title is one ellipsized line at 2_0`() = assertOneEllipsizedLine(2.0f)

    @Test
    fun `a short title is not ellipsized`() {
        show("Cast", 1.0f)

        val layout = titleLayout("Cast")
        assertEquals(1, layout.lineCount)
        assertFalse(layout.isLineEllipsized(0))
    }

    private companion object {
        const val LONG_TITLE = "Because you watched The Lord of the Rings: The Return of the King"
    }
}
