package com.binge.designsystem.shell

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Parks and restores the chain an app opens above its root, as the user moves between tabs.
 *
 * Leaving [hostTab] parks the chain above [appStack]'s root on [hostTab]'s own stack, when its first entry
 * [opensBeside] the tabs. Showing [hostTab] restores it, with entries made anew, once [appStack] holds nothing or only a
 * stand-in ([isStandIn]). A stash belongs to the server it was opened on.
 *
 * @need keep a page opened beside the tabs while another tab is showing
 * @group bars
 */
fun <T : Any> TabShellState<T>.settleStash(
    appStack: NavBackStack<NavKey>,
    hostTab: T,
    opensBeside: (NavKey) -> Boolean,
    isStandIn: (NavKey) -> Boolean,
) {
    val stash = stack(hostTab)
    val above = appStack.drop(1)
    if (selected != hostTab) {
        if (above.firstOrNull()?.let(opensBeside) != true) return
        stash.replaceAll(stash.take(1) + above)
        appStack.replaceAll(appStack.take(1))
    } else if (stash.size > 1) {
        if (above.isEmpty() || (above.size == 1 && isStandIn(above.first()))) {
            appStack.replaceAll(appStack.take(1) + stash.drop(1))
        }
        stash.replaceAll(stash.take(1))
    }
}

/**
 * Whether [stack] holds nothing but its root, or its root and a stand-in. [isRoot] and [isStandIn] name those two.
 *
 * @need tell whether a back stack has nothing open
 * @group bars
 */
fun nothingOpenBeside(
    stack: List<NavKey>,
    isRoot: (NavKey) -> Boolean,
    isStandIn: (NavKey) -> Boolean,
): Boolean =
    (stack.size == 1 && isRoot(stack.first())) ||
        (stack.size == 2 && isRoot(stack.first()) && isStandIn(stack[1]))
