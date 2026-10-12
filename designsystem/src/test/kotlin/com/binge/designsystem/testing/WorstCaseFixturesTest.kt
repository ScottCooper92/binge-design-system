package com.binge.designsystem.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalog.BingeChoiceWorstCaseSample
import com.binge.designsystem.catalog.CheckboxRowWorstCaseSample
import com.binge.designsystem.catalog.ItemGroupWorstCaseSample
import com.binge.designsystem.catalog.ListRowWorstCaseSample
import com.binge.designsystem.catalog.MediaCardWorstCaseSample
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The shared rows with every optional line filled and long text, at font scales 1.3 and 2.0, each through
 * [assertNoTextClipped]. A row that is fine on a short title and a small font is the usual way a clip ships.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorstCaseFixturesTest {
    @get:Rule
    val rule = createComposeRule()

    private fun check(fontScale: Float, sample: @Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                Box(Modifier.width(PHONE_WIDTH)) { sample() }
            }
        }
        rule.waitForIdle()
        val checked = rule.assertNoTextClipped()
        // A pass that checked nothing is no pass: scrolling content is skipped, and these rows must not hide in it.
        assertTrue("only $checked text nodes were checked", checked >= MIN_CHECKED)
    }

    @Test fun `a list row at 1_3`() = check(FONT_13) { ListRowWorstCaseSample() }

    @Test fun `a list row at 2_0`() = check(FONT_20) { ListRowWorstCaseSample() }

    @Test fun `an item group at 1_3`() = check(FONT_13) { ItemGroupWorstCaseSample() }

    @Test fun `an item group at 2_0`() = check(FONT_20) { ItemGroupWorstCaseSample() }

    @Test fun `a checkbox row at 1_3`() = check(FONT_13) { CheckboxRowWorstCaseSample() }

    @Test fun `a checkbox row at 2_0`() = check(FONT_20) { CheckboxRowWorstCaseSample() }

    @Test fun `a choice sheet at 1_3`() = check(FONT_13) { BingeChoiceWorstCaseSample() }

    @Test fun `a choice sheet at 2_0`() = check(FONT_20) { BingeChoiceWorstCaseSample() }

    @Test fun `a media card at 1_3`() = check(FONT_13) { MediaCardWorstCaseSample() }

    @Test fun `a media card at 2_0`() = check(FONT_20) { MediaCardWorstCaseSample() }

    private companion object {
        val PHONE_WIDTH = 412.dp
        const val MIN_CHECKED = 3
        const val FONT_13 = 1.3f
        const val FONT_20 = 2.0f
    }
}
