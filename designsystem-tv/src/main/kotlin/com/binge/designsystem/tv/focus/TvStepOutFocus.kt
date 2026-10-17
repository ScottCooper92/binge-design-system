package com.binge.designsystem.tv.focus

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * One focus unit for a level stepped into from a list beside it, such as the settings a list row opened: the key
 * toward the start edge, pressed at the unit's start edge, runs [onStepOut] instead of leaving. The caller swaps the
 * list back in and places focus on the row that led here. Every other direction leaves as usual.
 *
 * The step back in is [TvPaneRow][com.binge.designsystem.tv.template.TvPaneRow]'s `onClick` with a `paneContent`.
 */
@Composable
fun Modifier.tvStepOutFocusGroup(onStepOut: () -> Unit): Modifier {
    val start = tvStartFocusDirection(LocalLayoutDirection.current)
    return this
        .focusProperties {
            onExit = {
                if (requestedFocusDirection == start) {
                    cancelFocusChange()
                    onStepOut()
                }
            }
        }.focusGroup()
}
