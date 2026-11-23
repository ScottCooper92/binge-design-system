package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.ShadowMeshSpecification
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
 * No label of a [DetailStatRow] is cropped at 1.3x or 2x font, on a phone and a foldable width: the label's text is as
 * tall as its laid-out box, and the box sits inside the row. Compares bounds and the text layout, never
 * `hasVisualOverflow`. The same holds for the row inside [DetailCinematicHeader], whose copy column grows with its copy at a large font.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w840dp-h8000dp",
)
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailStatRowClippingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val labels = listOf("24.0k votes", "Rating", "Runtime", "Released")
    private val scores = listOf(
        ExternalScore(ExternalScoreKind.CriticsFresh, "97%", "Critics 97 percent"),
        ExternalScore(ExternalScoreKind.Audience, "95%", "Audience 95 percent"),
        ExternalScore(ExternalScoreKind.Imdb, "9.0", "IMDb 9.0"),
    )
    private val stats = labels.mapIndexed { index, label -> DetailStat(Icons.Filled.Star, "v$index", label) }

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

    private fun assertNothingCropped(width: Dp, fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(width).testTag("row")) { DetailStatRow(stats = stats) }
                }
            }
        }
        rule.waitForIdle()
        val row = rule.onNodeWithTag("row").getBoundsInRoot()
        labels.forEach { label ->
            val bounds = rule.onNodeWithText(label, useUnmergedTree = true).getBoundsInRoot()
            val text = layout(label)
            assertTrue("$label sits inside the row", bounds.bottom <= row.bottom && bounds.top >= row.top)
            assertTrue(
                "$label is as tall as its text needs (${text.multiParagraph.height}px of ${text.size.height}px)",
                text.multiParagraph.height <= text.size.height + 0.5f,
            )
            assertTrue("$label is not cut by its box", !text.didOverflowHeight)
        }
    }

    @Test fun `no label is cropped on a phone at 1_3`() = assertNothingCropped(360.dp, 1.3f)

    @Test fun `no label is cropped on a phone at 2_0`() = assertNothingCropped(360.dp, 2.0f)

    @Test fun `no label is cropped on a foldable at 1_3`() = assertNothingCropped(840.dp, 1.3f)

    @Test fun `no label is cropped on a foldable at 2_0`() = assertNothingCropped(840.dp, 2.0f)

    private fun assertHeaderNothingCropped(width: Dp, fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(width).testTag("header")) {
                        DetailCinematicHeader(
                            title = "The Lord of the Rings: The Fellowship of the Ring",
                            genres = listOf("Adventure", "Fantasy", "Action", "Drama"),
                            synopsis = "When the menace known as the Joker wreaks havoc on Gotham, " +
                                "Batman must accept one of the greatest tests.",
                            tagline = "Why so serious?",
                            stats = stats,
                            backdropUrl = null,
                            posterUrl = null,
                            externalScores = scores,
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
        val header = rule.onNodeWithTag("header").getBoundsInRoot()
        labels.forEach { label ->
            val bounds = rule.onNodeWithText(label, useUnmergedTree = true).getBoundsInRoot()
            val text = layout(label)
            assertTrue("$label sits inside the header", bounds.bottom <= header.bottom)
            assertTrue("$label is not cut by its box", !text.didOverflowHeight)
            assertTrue(
                "$label is as tall as its text needs (${text.multiParagraph.height}px of ${text.size.height}px)",
                text.multiParagraph.height <= text.size.height + 0.5f,
            )
        }
    }

    @Test fun `no label of the cinematic header is cropped on a foldable at 1_3`() = assertHeaderNothingCropped(840.dp, 1.3f)

    @Test fun `no label of the cinematic header is cropped on a foldable at 2_0`() = assertHeaderNothingCropped(840.dp, 2.0f)
}
