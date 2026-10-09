package com.binge.designsystem.template

import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "Saved"
private const val TOLERANCE = 1f

/** A camera cutout down the start side, as a phone held in landscape has. */
private val Cutout = 40.dp

/** The scaffold's snackbar clears a side cutout, as the body above it does (#402). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class ScaffoldSnackbarSideInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a start cutout moves the snackbar in by its width`() {
        var cutout by mutableStateOf(0.dp)
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                WithStartCutout(cutout) {
                    val snackbar = remember { SnackbarHostState() }
                    LaunchedEffect(Unit) { snackbar.showSnackbar(MESSAGE) }
                    BingeScreenScaffold(title = "Settings", snackbarHostState = snackbar) {}
                }
            }
        }
        rule.waitForIdle()
        val without = messageLeft()

        cutout = Cutout
        rule.waitForIdle()

        assertEquals(without + Cutout.value, messageLeft(), TOLERANCE)
    }

    private fun messageLeft() =
        rule
            .onNodeWithText(MESSAGE)
            .getBoundsInRoot()
            .left.value
}

/** Gives the window a display cutout [width] wide down the start side, answering every insets dispatch at the parent. */
@Composable
private fun WithStartCutout(width: Dp, content: @Composable () -> Unit) {
    val view = LocalView.current
    val px = with(LocalDensity.current) { width.roundToPx() }
    val insets =
        remember(px) { WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(px, 0, 0, 0)).build() }
    // Read first, so Compose's own insets listener is installed on the view before the dispatch below.
    WindowInsets.displayCutout
    DisposableEffect(view, insets) {
        val host = view.parent as View
        val platform = checkNotNull(insets.toWindowInsets())
        host.setOnApplyWindowInsetsListener { _, _ -> platform }
        host.dispatchApplyWindowInsets(platform)
        onDispose { host.setOnApplyWindowInsetsListener(null) }
    }
    content()
}
