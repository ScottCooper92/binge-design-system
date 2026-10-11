package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val TITLE = "Requests"
private val RailWidth = 96.dp
private val Cutout = 32.dp

/**
 * A bar mounted under a custom rail that overlays its start edge, with no pane beside it, starts past the rail: the
 * title and the back button both, as the body below it does (#605).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class TopBarUnderRailTest {
    @get:Rule
    val rule = createComposeRule()

    /** The rail's own contract: it publishes its width plus the start safe area, and consumes that area. */
    private fun showUnderRail(cutout: Dp, bar: @Composable () -> Unit) {
        rule.setContent {
            TestTheme {
                WithWindowInsets({ cutout.toCutoutInsets(this) }) {
                    CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(start = RailWidth + cutout)) {
                        Box(Modifier.consumeWindowInsets(PaddingValues(start = cutout))) { bar() }
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun assertClearsRail(rail: Dp) {
        val title = rule.onNodeWithText(TITLE).getBoundsInRoot().left
        val back =
            rule
                .onNodeWithContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_navigate_back))
                .getBoundsInRoot()
                .left
        assertTrue("The title starts at $title, under the $rail rail", title >= rail)
        assertTrue("The back button starts at $back, under the $rail rail", back >= rail)
    }

    @Test
    fun `the single-row bar clears the rail`() {
        showUnderRail(cutout = 0.dp) { BingeTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth)
    }

    @Test
    fun `the medium bar clears the rail`() {
        showUnderRail(cutout = 0.dp) { BingeMediumTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth)
    }

    @Test
    fun `the pane bar clears the rail with no pane beside it`() {
        showUnderRail(cutout = 0.dp) { BingePaneTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth)
    }

    @Test
    fun `the large bar clears the rail`() {
        showUnderRail(cutout = 0.dp) { BingeLargeTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth)
    }

    /** The cutout sits inside the rail, so a bar must still clear the whole rail, not fall short by the cutout's width. */
    @Test
    fun `the single-row bar clears the whole rail under a side cutout`() {
        showUnderRail(cutout = Cutout) { BingeTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth + Cutout)
    }

    @Test
    fun `the medium bar clears the whole rail under a side cutout`() {
        showUnderRail(cutout = Cutout) { BingeMediumTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth + Cutout)
    }

    @Test
    fun `the pane bar clears the whole rail under a side cutout`() {
        showUnderRail(cutout = Cutout) { BingePaneTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth + Cutout)
    }

    @Test
    fun `the large bar clears the whole rail under a side cutout`() {
        showUnderRail(cutout = Cutout) { BingeLargeTopBar(title = TITLE, onBack = {}) }
        assertClearsRail(RailWidth + Cutout)
    }

    private fun Dp.toCutoutInsets(density: Density): WindowInsetsCompat =
        WindowInsetsCompat
            .Builder()
            .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(with(density) { roundToPx() }, 0, 0, 0))
            .build()
}
