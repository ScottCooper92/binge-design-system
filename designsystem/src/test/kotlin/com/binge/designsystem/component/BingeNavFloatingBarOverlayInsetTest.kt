package com.binge.designsystem.component

import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val StatusBar = 24.dp
private val NavigationBar = 48.dp
private const val TOLERANCE = 0.5f

/**
 * The strip the floating pill publishes counts the bottom inset and not the top one (#423). Its measured
 * correction once sat before the insets padding, so the status bar's height reached every list's bottom padding.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class BingeNavFloatingBarOverlayInsetTest {
    @get:Rule
    val rule = createComposeRule()

    private var overlay = PaddingValues()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Test
    fun `the overlay inset is the pill, its offset and the bottom inset, without the status bar`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                WithSystemBars(StatusBar, NavigationBar) {
                    BingeNavSuiteShell(
                        items = listOf(BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie)),
                        selectedKey = "movies",
                        onSelect = {},
                        presentation = BingeNavPresentation.FloatingBar,
                    ) {
                        overlay = LocalNavOverlayInsets.current
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            (FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset + NavigationBar).value,
            overlay.calculateBottomPadding().value,
            TOLERANCE,
        )
    }
}

/** A status bar [top] tall and a navigation bar [bottom] tall, answering every insets dispatch at the parent. */
@Composable
private fun WithSystemBars(
    top: Dp,
    bottom: Dp,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val insets =
        remember(density, top, bottom) {
            with(density) {
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, top.roundToPx(), 0, 0))
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, bottom.roundToPx()))
                    .build()
            }
        }
    // Read first, so Compose's own insets listener is installed on the view before the dispatch below.
    WindowInsets.systemBars
    DisposableEffect(view, insets) {
        val host = view.parent as View
        val platform = checkNotNull(insets.toWindowInsets())
        host.setOnApplyWindowInsetsListener { _, _ -> platform }
        host.dispatchApplyWindowInsets(platform)
        onDispose { host.setOnApplyWindowInsetsListener(null) }
    }
    content()
}
