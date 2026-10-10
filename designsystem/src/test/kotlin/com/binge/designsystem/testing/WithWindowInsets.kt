package com.binge.designsystem.testing

import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowInsetsCompat

/**
 * Gives the window the insets [insets] builds, in pixels, answering every insets dispatch at the parent so the
 * test's own content only ever receives them. The insets are rebuilt on each composition and dispatched again only
 * when they change, so a test can move a keyboard or a bar by changing the state its builder reads.
 */
@Composable
internal fun WithWindowInsets(insets: Density.() -> WindowInsetsCompat, content: @Composable () -> Unit) {
    val view = LocalView.current
    val built = with(LocalDensity.current) { insets() }
    // Read first, so Compose's own insets listener is installed on the view before the dispatch below.
    WindowInsets.systemBars
    DisposableEffect(view, built) {
        val host = view.parent as View
        val platform = checkNotNull(built.toWindowInsets())
        host.setOnApplyWindowInsetsListener { _, _ -> platform }
        host.dispatchApplyWindowInsets(platform)
        onDispose { host.setOnApplyWindowInsetsListener(null) }
    }
    content()
}
