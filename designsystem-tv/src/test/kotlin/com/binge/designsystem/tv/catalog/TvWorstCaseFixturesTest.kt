package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The shared TV rows with every optional line filled and long text, at font scales 1.3 and 2.0, each through
 * [assertNoTextClipped]. A label is held to one line with an ellipsis, so what this guards is the row's height keeping
 * up with a supporting line and an icon at a larger font.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvWorstCaseFixturesTest {
    @get:Rule
    val rule = createComposeRule()

    private fun check(fontScale: Float, sample: @Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeTvTheme {
                    androidx.compose.foundation.layout
                        .Box(Modifier.fillMaxSize()) { sample() }
                }
            }
        }
        rule.waitForIdle()
        val checked = rule.assertNoTextClipped()
        // A pass that checked nothing is no pass: scrolling content is skipped, and these rows must not hide in it.
        assertTrue("only $checked text nodes were checked", checked >= MIN_CHECKED)
    }

    @Test fun `an option row at 1_3`() = check(FONT_13) { TvOptionRowWorstCaseSample() }

    @Test fun `an option row at 2_0`() = check(FONT_20) { TvOptionRowWorstCaseSample() }

    @Test fun `a checkbox row at 1_3`() = check(FONT_13) { TvCheckboxRowWorstCaseSample() }

    @Test fun `a checkbox row at 2_0`() = check(FONT_20) { TvCheckboxRowWorstCaseSample() }

    @Test fun `a choice row at 1_3`() = check(FONT_13) { TvChoiceRowWorstCaseSample() }

    @Test fun `a choice row at 2_0`() = check(FONT_20) { TvChoiceRowWorstCaseSample() }

    @Test fun `a detail action row at 1_3`() = check(FONT_13) { TvDetailActionRowWorstCaseSample() }

    @Test fun `a detail action row at 2_0`() = check(FONT_20) { TvDetailActionRowWorstCaseSample() }

    @Test fun `a list pane board at 1_3`() = check(FONT_13) { TvListPaneBoardWorstCaseSample() }

    @Test fun `a list pane board at 2_0`() = check(FONT_20) { TvListPaneBoardWorstCaseSample() }

    private companion object {
        const val MIN_CHECKED = 3
        const val FONT_13 = 1.3f
        const val FONT_20 = 2.0f
    }
}
