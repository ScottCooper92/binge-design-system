package com.binge.designsystem.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Keeps the user's place when the condition that decides which tabs make sense changes, such as a server being added or
 * removed. [resolved] is that condition, or null while it is still being worked out.
 *
 * Only a change after the first resolved value is a toggle. If the first value is false, [offTab] is shown. A toggle takes
 * every tab's own stack to its root and shows [offTab]. The app's own stack is not touched.
 *
 * @need keep or reset the user's place when a tab-gating condition flips
 * @group bars
 */
@Composable
fun <T : Any> KeepPlaceOnGateChange(
    resolved: Boolean?,
    tabs: TabShellState<T>,
    offTab: T,
) {
    var shown by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(resolved) {
        val now = resolved ?: return@LaunchedEffect
        val before = shown
        shown = now
        when {
            before == null && !now -> tabs.show(offTab)
            before != null && before != now -> tabs.settleOn(offTab)
        }
    }
}
