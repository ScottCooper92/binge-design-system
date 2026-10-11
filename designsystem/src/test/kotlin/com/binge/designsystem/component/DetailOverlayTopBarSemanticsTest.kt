package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The bar's title is drawn at alpha zero over the hero. A screen reader must not stop on it until it shows. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailOverlayTopBarSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    private val hidden = SemanticsMatcher.keyIsDefined(SemanticsProperties.HideFromAccessibility)

    /** Shows the bar scrolled by [scrolledPast] of the hero's height in pixels; `0` is the hero untouched. */
    private fun show(scrolledPast: (heroHeightPx: Float) -> Float) =
        rule.setContent {
            TestTheme {
                val heroPx = with(LocalDensity.current) { HERO_HEIGHT.toPx() }
                Box(Modifier.fillMaxSize()) {
                    DetailOverlayTopBar(
                        title = "Alpha",
                        scrollOffsetPx = { scrolledPast(heroPx) },
                        onBack = null,
                        heroHeight = HERO_HEIGHT,
                    )
                }
            }
        }

    @Test
    fun `the title is hidden from a screen reader while the hero is untouched`() {
        show { 0f }

        rule.onNodeWithText("Alpha").assert(hidden)
    }

    @Test
    fun `the title is announced once the fade has begun`() {
        show { heroPx -> heroPx - 1f }

        rule.onNodeWithText("Alpha").assert(!hidden)
    }

    @Test
    fun `the title is announced when the bar is fully faded in`() {
        show { heroPx -> heroPx * 2f }

        rule.onNodeWithText("Alpha").assert(!hidden)
    }

    /**
     * A hero that has grown past the floor the bar is given. Scrolled to where the floor's own fade would have begun,
     * the bar given the measured height is still clear, and the one left on the floor is not.
     */
    private fun showOverGrownHero(useMeasured: Boolean) =
        rule.setContent {
            TestTheme {
                val measured = rememberMeasuredHeroHeight()
                val scrolledPx = with(LocalDensity.current) { GROWN_SCROLL.toPx() }
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.measuredHeroHeight(measured).height(GROWN_HERO_HEIGHT))
                    DetailOverlayTopBar(
                        title = "Alpha",
                        scrollOffsetPx = { scrolledPx },
                        onBack = null,
                        heroHeight = if (useMeasured) measured.height(HERO_HEIGHT) else HERO_HEIGHT,
                    )
                }
            }
        }

    @Test
    fun `a bar given the measured height of a grown hero stays clear where the floor would fade it`() {
        showOverGrownHero(useMeasured = true)

        rule.onNodeWithText("Alpha").assert(hidden)
    }

    @Test
    fun `a bar left on the floor height fades early over a grown hero`() {
        showOverGrownHero(useMeasured = false)

        rule.onNodeWithText("Alpha").assert(!hidden)
    }

    private companion object {
        val HERO_HEIGHT = 300.dp
        val GROWN_HERO_HEIGHT = 400.dp
        val GROWN_SCROLL = 250.dp
    }
}
