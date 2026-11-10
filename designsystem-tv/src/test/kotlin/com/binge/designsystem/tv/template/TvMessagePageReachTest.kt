package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.TvMessagePageWorstCaseSample
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A message page's actions are always on screen (#1134): with a long body at a large font the headline and body scroll above the
 * actions, which stay inside the window. Judged by bounds, at 1.3x and 2x, and never by `hasVisualOverflow`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap or overflow.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvMessagePageReachTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    private fun show(fontScale: Float, content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeTvTheme { Box(Modifier.fillMaxSize()) { content() } }
            }
        }
        rule.waitForIdle()
    }

    private fun assertActionsInsideWindow(fontScale: Float) {
        show(fontScale) { TvMessagePageWorstCaseSample() }
        val window = rule.onRoot().getUnclippedBoundsInRoot()
        for (label in listOf("Update", "Not now")) {
            rule.onNodeWithText(label).assertIsDisplayed()
            val bounds = rule.onNodeWithText(label).getUnclippedBoundsInRoot()
            assertTrue("$label starts above the window at ${bounds.top}", bounds.top.value >= -TOLERANCE)
            assertTrue(
                "$label ends at ${bounds.bottom}, below the window's ${window.bottom}",
                bounds.bottom.value <= window.bottom.value + TOLERANCE,
            )
        }
    }

    @Test
    fun `the actions are inside the window at 1_3x`() = assertActionsInsideWindow(1.3f)

    @Test
    fun `the actions are inside the window at 2x`() = assertActionsInsideWindow(2f)

    @Test
    fun `a plate in a container with no height to give still lays out, unscrolled`() {
        show(2f) {
            Box(Modifier.verticalScroll(rememberScrollState())) {
                TvMessagePlate(headline = "Headline", body = "Body text that is not very long.")
            }
        }
        rule.onNodeWithText("Headline").assertIsDisplayed()
    }

    private companion object {
        const val TOLERANCE = 0.5f
    }
}
