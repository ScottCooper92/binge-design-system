package com.binge.designsystem.template

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SECTION = "Section"
private const val TOLERANCE = 1f

/** A navigation bar down the start side, as three-button navigation sits in landscape. */
private val SideBar = 48.dp

/** A [HeroDetailScreen] page clears the side insets on its outer edges only, as a pane does (#373). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w1000dp-h800dp")
class HeroDetailScreenPaneInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun page(innerEdge: PaneEdge?) =
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                WithStartNavigationBar(SideBar) {
                    CompositionLocalProvider(LocalPaneInnerEdge provides innerEdge) {
                        HeroDetailScreen(
                            title = "Title",
                            onBack = null,
                            contentMaxWidth = Dp.Infinity,
                            hero = { Box(Modifier.height(200.dp)) },
                        ) {
                            Text(SECTION)
                        }
                    }
                }
            }
        }

    @Test
    fun `alone in the window, the page clears the start inset`() {
        page(innerEdge = null)

        assertEquals(
            SideBar.value,
            rule
                .onNodeWithText(SECTION)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }

    @Test
    fun `in the end pane, the page does not clear an inset at its shared edge`() {
        page(innerEdge = PaneEdge.Start)

        assertEquals(
            0f,
            rule
                .onNodeWithText(SECTION)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }
}

/** Gives the window a navigation bar [width] wide down the start side, answering every insets dispatch at the parent. */
@Composable
private fun WithStartNavigationBar(width: Dp, content: @Composable () -> Unit) {
    val view = LocalView.current
    val px = with(LocalDensity.current) { width.roundToPx() }
    val insets =
        remember(px) { WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(px, 0, 0, 0)).build() }
    // Read first, so Compose's own insets listener is installed on the view before the dispatch below.
    WindowInsets.navigationBars
    DisposableEffect(view, insets) {
        val host = view.parent as View
        val platform = checkNotNull(insets.toWindowInsets())
        host.setOnApplyWindowInsetsListener { _, _ -> platform }
        host.dispatchApplyWindowInsets(platform)
        onDispose { host.setOnApplyWindowInsetsListener(null) }
    }
    content()
}
