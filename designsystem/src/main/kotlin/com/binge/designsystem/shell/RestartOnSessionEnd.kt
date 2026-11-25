package com.binge.designsystem.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Starts the shell over when the session ends: [connected] false restarts [tabs] and [appStack]. Null means the session is
 * still being worked out, so a restored stack is left alone. Place it after [KeepPlaceOnGateChange].
 *
 * @need start over from the first tab when the session ends
 * @group bars
 */
@Composable
fun <T : Any> RestartOnSessionEnd(
    connected: Boolean?,
    tabs: TabShellState<T>,
    appStack: NavBackStack<NavKey>,
) {
    LaunchedEffect(connected) {
        if (connected == false) tabs.restart(appStack)
    }
}
