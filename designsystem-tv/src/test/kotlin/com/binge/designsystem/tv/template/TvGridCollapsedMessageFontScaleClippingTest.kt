package com.binge.designsystem.tv.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.assertNoTvTextClipped
import com.binge.designsystem.tv.testing.atTvFontScale
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The collapsed grid message (a failure, an empty result with an action, an empty result without one) through
 * `assertNoTextClipped`: the TV font scale (1.3), which the shared helper pins, and 2.0 for the largest the system offers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvGridCollapsedMessageFontScaleClippingTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var state by mutableStateOf(STATES.first())

    @Test
    fun `every collapsed state is not clipped at the TV font scale`() {
        show { atTvFontScale() }
        STATES.forEach { current ->
            state = current
            composeTestRule.assertNoTvTextClipped("the ${current.name} state", MIN_CHECKED)
        }
    }

    @Test
    fun `every collapsed state is not clipped at double font scale`() {
        show { Density(density, fontScale = LARGEST_FONT_SCALE) }
        STATES.forEach { current ->
            state = current
            composeTestRule.waitForIdle()
            val checked = composeTestRule.assertNoTextClipped()
            assertTrue("only $checked text nodes were checked for the ${current.name} state", checked >= MIN_CHECKED)
        }
    }

    private fun show(density: Density.() -> Density) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDensity provides LocalDensity.current.density()) {
                BingeTvTheme(reduceMotion = true) {
                    TestGridPage(
                        heading = "Horror",
                        results = state.results,
                        onItemClick = {},
                        onRetry = {},
                        emptyMessage = "No films match these filters, so there is nothing to show here right now",
                        emptyAction = TvGridAction("Clear all of the filters") {}.takeIf { state.withAction },
                    )
                }
            }
        }
    }

    private class CollapsedState(
        val name: String,
        val results: TvGridResults<GridItem>,
        val withAction: Boolean,
    )

    private companion object {
        const val MIN_CHECKED = 2
        const val LARGEST_FONT_SCALE = 2f
        val STATES = listOf(
            CollapsedState("failure", TvGridResults.Error(ErrorKind.Network), withAction = false),
            CollapsedState("empty with an action", TvGridResults.Empty, withAction = true),
            CollapsedState("empty with no action", TvGridResults.Empty, withAction = false),
        )
    }
}
