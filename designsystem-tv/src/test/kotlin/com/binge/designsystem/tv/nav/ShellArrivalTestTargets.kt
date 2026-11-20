package com.binge.designsystem.tv.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.binge.designsystem.tv.focus.tvShellArrivalTarget

/**
 * Makes a test destination the shell's arrival target, as every real destination is, while [enabled]: a pane still loading has no
 * target, so the arrival waits for it. Put it ahead of the `focusable` or `clickable` it routes to.
 */
@Composable
internal fun Modifier.shellArrivalTarget(enabled: Boolean = true): Modifier {
    val requester = remember { FocusRequester() }
    return if (enabled) tvShellArrivalTarget(requester) else this
}
