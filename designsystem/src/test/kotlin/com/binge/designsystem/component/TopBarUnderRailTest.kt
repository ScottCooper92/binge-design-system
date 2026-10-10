package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val TITLE = "Requests"
private val RailWidth = 96.dp

/**
 * A bar mounted under a custom rail that overlays its start edge, with no pane beside it, starts past the rail: the
 * title and the back button both, as the body below it does (#605).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class TopBarUnderRailTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(bar: @Composable () -> Unit) {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(start = RailWidth), content = bar)
            }
        }
        rule.waitForIdle()
    }

    private fun assertClearsRail() {
        val title = rule.onNodeWithText(TITLE).getBoundsInRoot().left
        val back =
            rule
                .onNodeWithContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_navigate_back))
                .getBoundsInRoot()
                .left
        assertTrue("The title starts at $title, under the $RailWidth rail", title >= RailWidth)
        assertTrue("The back button starts at $back, under the $RailWidth rail", back >= RailWidth)
    }

    @Test
    fun `the two-row bar clears the rail`() {
        show { BingeTopBar(title = TITLE, onBack = {}) }
        assertClearsRail()
    }

    @Test
    fun `the medium bar clears the rail`() {
        show { BingeMediumTopBar(title = TITLE, onBack = {}) }
        assertClearsRail()
    }

    @Test
    fun `the pane bar clears the rail with no pane beside it`() {
        show { BingePaneTopBar(title = TITLE, onBack = {}) }
        assertClearsRail()
    }

    @Test
    fun `the large bar clears the rail`() {
        show { BingeLargeTopBar(title = TITLE, onBack = {}) }
        assertClearsRail()
    }
}
