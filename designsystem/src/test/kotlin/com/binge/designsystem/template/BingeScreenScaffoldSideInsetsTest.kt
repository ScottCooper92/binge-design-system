package com.binge.designsystem.template

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilterChipRow
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val BODY = "body"
private const val FIRST_CHIP = "All"
private const val TOLERANCE = 1f

/** A navigation bar down the start side, as three-button navigation sits in landscape. */
private val SideBar = 48.dp

/** What an expanded custom rail publishes as its overlay: its width plus the start inset it consumed. */
private val RailWidth = 96.dp

/** The scaffold reserves a side inset once, whoever reserved it first (#371, #372). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeScreenScaffoldSideInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a header's chip row does not add the side inset the scaffold already padded`() {
        rule.setContent {
            Theme {
                WithStartNavigationBar(SideBar) {
                    BingeScreenScaffold(
                        title = "Requests",
                        bar = ScreenBar.Small,
                        header = {
                            BingeFilterChipRow(
                                items = listOf(FilterChipItem(FIRST_CHIP, count = 1)),
                                selectedIndex = 0,
                                onSelect = {},
                            )
                        },
                    ) { Box(Modifier.fillMaxSize()) }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            (SideBar + contentInset()).value,
            rule
                .onNodeWithText(FIRST_CHIP)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }

    @Test
    fun `a start inset the rail already consumed is not reserved again`() {
        rule.setContent {
            Theme {
                WithStartNavigationBar(SideBar) {
                    // As BingeNavCustomRail does: it publishes its width plus the start inset, and consumes that inset.
                    CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(start = RailWidth + SideBar)) {
                        Box(Modifier.consumeWindowInsets(WindowInsets.navigationBars.only(WindowInsetsSides.Start))) {
                            BingeScreenScaffold(title = "Requests", bar = ScreenBar.Small) { padding ->
                                val start = padding.calculateStartPadding(LocalLayoutDirection.current)
                                Box(Modifier.padding(start = start).testTag(BODY).fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            (RailWidth + SideBar).value,
            rule
                .onNodeWithTag(BODY)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }

    private fun contentInset(): Dp {
        val resources = RuntimeEnvironment.getApplication().resources
        return (resources.getDimension(R.dimen.screen_content_inset) / resources.displayMetrics.density).dp
    }
}

@Composable
private fun Theme(content: @Composable () -> Unit) = BingeExpressiveTheme(dynamicColor = false, content = content)

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
