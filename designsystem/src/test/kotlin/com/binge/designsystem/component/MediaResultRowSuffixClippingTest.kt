package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "row"
private const val SEASONS = "Seasons 1, 2, 4–6, "
private const val TITLE = "The Lord of the Rings: The Fellowship of the Ring, Extended Edition"

// Long enough to overflow the row where text is measured coarsely, as the test runner does, so the two have to share the line.
private val LONG_SUFFIX = SEASONS.repeat(20)
private val LONG_NAME = TITLE.repeat(8)

/**
 * A name suffix has to leave the name a usable width however large the text is: a long suffix and a long name must both be seen, and neither may be
 * cropped to nothing. Sizes are compared with the row's bounds, since a text that overflows reports its box and not what is drawn.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
class MediaResultRowSuffixClippingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    Box(Modifier.width(412.dp).testTag(ROW)) {
                        MediaResultRow(
                            result = MediaResult("m", MediaResultKind.Poster, null, LONG_NAME, "Show", nameSuffix = LONG_SUFFIX),
                            onClick = {},
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun check(fontScale: Float) {
        show(fontScale)
        val row = rule.onNodeWithTag(ROW).getBoundsInRoot()
        val name = rule.onNodeWithText(LONG_NAME, useUnmergedTree = true).getBoundsInRoot()
        val suffix = rule.onNodeWithText(LONG_SUFFIX, useUnmergedTree = true).getBoundsInRoot()
        val rowWidth = row.right - row.left
        val nameWidth = name.right - name.left
        val suffixWidth = suffix.right - suffix.left
        assertTrue("the name keeps a usable width at $fontScale x: $nameWidth of $rowWidth", nameWidth >= rowWidth * MIN_NAME_SHARE)
        assertTrue("the suffix is not cropped to nothing at $fontScale x: $suffixWidth", suffixWidth >= MIN_SUFFIX_WIDTH)
        assertTrue("the suffix is inside the row at $fontScale x", suffix.right <= row.right && suffix.left >= name.right)
        assertTrue("the suffix is not taller than the name's box allows at $fontScale x", suffix.bottom - suffix.top > 0.dp)
    }

    @Test
    fun `a long name and a long suffix are both visible at 1x font`() = check(1f)

    @Test
    fun `a long name and a long suffix are both visible at 1_3x font`() = check(1.3f)

    @Test
    fun `a long name and a long suffix are both visible at 2x font`() = check(2f)

    private companion object {
        const val MIN_NAME_SHARE = 0.25f
        val MIN_SUFFIX_WIDTH = 40.dp
    }
}
