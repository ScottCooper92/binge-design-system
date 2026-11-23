package com.binge.designsystem.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Which tab a [TabShell] shows, and the back stack each tab keeps for itself.
 *
 * Tabs keep their own stacks, so leaving a tab and coming back finds the page the user left. [select] on the tab that is
 * already showing pops that tab to its root and reports it as a reselect. [back] returns to [startTab] and reports whether
 * it moved. Showing a tab pops nothing.
 *
 * @property tabs the tabs, in order. The order must be the same from one launch to the next: the selected tab and each tab's
 * composition state are saved by index.
 * @property startTab the tab back returns to, and the one a restart shows.
 *
 * @need hold which tab is showing and each tab's own back stack
 * @group bars
 * @replaces a hand-built selected-tab index with one back stack per tab
 */
@Stable
class TabShellState<T : Any>(
    val tabs: List<T>,
    private val selectedIndex: MutableIntState,
    private val stacks: Map<T, NavBackStack<NavKey>>,
    val startTab: T = tabs.first(),
) {
    init {
        require(tabs.isNotEmpty() && tabs.all { it in stacks }) { "every tab needs a stack" }
        require(startTab in tabs) { "the start tab must be one of the tabs" }
    }

    /** The tab showing now. */
    val selected: T get() = tabs[selectedIndex.intValue]

    /** [tab]'s own back stack. */
    fun stack(tab: T): NavBackStack<NavKey> = stacks.getValue(tab)

    /** Shows [tab]. Nothing is popped, on this tab or any other. */
    fun show(tab: T) {
        selectedIndex.intValue = tabs.indexOf(tab).coerceAtLeast(0)
    }

    /** Returns to [startTab] and answers true, or answers false when it is already showing, so the caller can leave the app. */
    fun back(): Boolean {
        if (selected == startTab) return false
        show(startTab)
        return true
    }

    /**
     * The user chose [tab]. Another tab is shown as it was left, and false is returned. The tab already showing is popped to
     * its root and true is returned: a reselect.
     */
    fun select(tab: T): Boolean {
        if (tab != selected) {
            show(tab)
            return false
        }
        stack(tab).popToRoot()
        return true
    }
}

/**
 * Starts over: pops [appStack] and every tab to its root and shows [TabShellState.startTab]. [appStack] is the stack the app
 * keeps above the shell.
 *
 * @need return to the first tab with nothing open, such as when a session ends
 * @group bars
 */
fun <T : Any> TabShellState<T>.restart(appStack: NavBackStack<NavKey>) {
    appStack.popToRoot()
    tabs.forEach { stack(it).popToRoot() }
    show(startTab)
}

/**
 * Takes every tab to its root and shows [tab]. The app's own stack is not touched.
 *
 * @need reset every tab and show one of them
 * @group bars
 */
fun <T : Any> TabShellState<T>.settleOn(tab: T) {
    tabs.forEach { stack(it).popToRoot() }
    show(tab)
}

/**
 * Remembers a [TabShellState] over [tabs] and their [stacks], starting on [initial]. The selected index survives recreation.
 * [tabs] must keep the same order from one launch to the next.
 *
 * @need create the state a tab shell reads
 * @group bars
 */
@Composable
fun <T : Any> rememberTabShellState(
    tabs: List<T>,
    initial: T,
    stacks: Map<T, NavBackStack<NavKey>>,
): TabShellState<T> {
    val index = rememberSaveable { mutableIntStateOf(tabs.indexOf(initial).coerceAtLeast(0)) }
    return remember(tabs, stacks, index, initial) { TabShellState(tabs, index, stacks, startTab = initial) }
}
