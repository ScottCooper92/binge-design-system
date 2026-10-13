package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val NavigationBar = 48.dp
private val ShortBar = 40.dp
private val TallBar = 240.dp
private const val TOLERANCE = 0.5f

/**
 * A screen with a pinned bottom bar, shown under the floating pill, keeps its body clear of both (#435). The body's
 * bottom padding is the larger of the bar and the pill's overlay; it once took the bar whenever the bar had height.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class BingeNavShellPinnedBarTest {
    @get:Rule
    val rule = createComposeRule()

    private var overlay = PaddingValues()
    private var body = PaddingValues()

    @Test
    fun `a bar shorter than the pill leaves the body clear of the pill`() {
        showUnderFloatingShell(barHeight = ShortBar)

        assertEquals(overlay.calculateBottomPadding().value, body.calculateBottomPadding().value, TOLERANCE)
    }

    @Test
    fun `a bar taller than the pill leaves the body clear of the bar`() {
        showUnderFloatingShell(barHeight = TallBar)

        assertEquals(TallBar.value, body.calculateBottomPadding().value, TOLERANCE)
    }

    private fun showUnderFloatingShell(barHeight: Dp) {
        rule.setContent {
            TestTheme {
                WithNavigationBar(NavigationBar) {
                    BingeNavSuiteShell(
                        items = listOf(BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie)),
                        selectedKey = "movies",
                        onSelect = {},
                        presentation = BingeNavPresentation.FloatingBar,
                    ) {
                        overlay = LocalNavOverlayInsets.current
                        BingeScreenScaffold(
                            title = "Requests",
                            bar = ScreenBar.Small,
                            bottomBar = { Box(Modifier.fillMaxWidth().height(barHeight)) },
                        ) { padding -> body = padding }
                    }
                }
            }
        }
        rule.waitForIdle()
        // The pill is taller than the short bar, or the first case would not tell the two policies apart.
        assertEquals(true, overlay.calculateBottomPadding() > ShortBar)
    }
}

/** A navigation bar [bottom] tall, answering every insets dispatch at the parent. */
@Composable
private fun WithNavigationBar(bottom: Dp, content: @Composable () -> Unit) =
    WithWindowInsets(
        {
            WindowInsetsCompat
                .Builder()
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, bottom.roundToPx()))
                .build()
        },
        content,
    )
