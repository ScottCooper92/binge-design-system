package com.binge.designsystem.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Theme wrapper for previews and Compose screenshot tests.
 *
 * Deliberately takes no `darkTheme` parameter: [BingeExpressiveTheme] defaults to
 * `isSystemInDarkTheme()` so `@Preview(uiMode = …)` drives the light/dark axis, letting one
 * annotation expand into both colour variants. The [Surface] wraps (doesn't fill) so each screenshot
 * hugs the component; full-screen surfaces must pass an explicit height via [modifier].
 *
 * - `dynamicColor = false` keeps colours deterministic across machines (CI gate).
 * - `reduceMotion = true` disables animation so a single frame is stable.
 * - [Surface] paints the theme's `background`, the colour a full screen sits on in an app (a `Scaffold`
 *   paints it by default), so a screen frame shows its page as the app does. A Material 3 [Surface]
 *   defaults to `surface`, which differs in dark theme, so the colour is set here. TvScreenshotTheme
 *   paints the same canvas.
 */
@Composable
fun ScreenshotTheme(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BingeExpressiveTheme(dynamicColor = false, reduceMotion = true) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
