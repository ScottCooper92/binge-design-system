package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalog.FormFooterWorstCaseSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SECONDARY = "Report this on GitHub instead"
private const val PRIMARY = "Send"
private const val TOLERANCE_DP = 1f

/**
 * A footer's two buttons are the same height when their labels wrap differently (#840).
 *
 * Measured from the buttons' bounds against each other, and each label's against its own button's, rather than through
 * `hasVisualOverflow`: nothing here overflows, the shorter button is simply drawn short, with its label off-centre.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FormFooterHeightTest {
    @get:Rule
    val rule = createComposeRule()

    private fun check(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                Box(Modifier.width(412.dp)) { FormFooterWorstCaseSample() }
            }
        }
        rule.waitForIdle()

        val secondary = rule.onNode(hasClickAction() and hasText(SECONDARY)).getBoundsInRoot()
        val primary = rule.onNode(hasClickAction() and hasText(PRIMARY)).getBoundsInRoot()
        val secondaryHeight = secondary.bottom.value - secondary.top.value
        val primaryHeight = primary.bottom.value - primary.top.value
        assertEquals("the buttons are different heights", secondaryHeight, primaryHeight, TOLERANCE_DP)
        assertEquals("the buttons do not share a top edge", secondary.top.value, primary.top.value, TOLERANCE_DP)

        // The label must fit inside its button, centred on it, at whatever height the pair settles on.
        val label = rule.onNode(hasText(PRIMARY), useUnmergedTree = true).getBoundsInRoot()
        val labelCentre = (label.top.value + label.bottom.value) / 2
        val buttonCentre = (primary.top.value + primary.bottom.value) / 2
        assertEquals("the primary's label is off-centre", buttonCentre, labelCentre, TOLERANCE_DP)
        assertTrue("the primary's label is taller than its button", label.bottom.value - label.top.value <= primaryHeight)
    }

    @Test fun `the buttons match at 1_3`() = check(1.3f)

    @Test fun `the buttons match at 2_0`() = check(2.0f)
}
