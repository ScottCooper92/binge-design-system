package com.binge.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter

/**
 * The signal a tab shell emits when the user taps the tab that is already selected, after it has popped what the tab opened back to its
 * root. It carries the tab's key, so a listener filters for its own tab: more than one tab may be composed at once, as during a
 * cross-fade. The key is whatever the app's own tab type is, compared by equality. Null when no shell provides one, as in a preview:
 * treat that as "no signal".
 */
val LocalTabReselectSignal = staticCompositionLocalOf<SharedFlow<Any>?> { null }

/**
 * Runs [action] each time the user taps [tab] while it is the selected one. Does nothing when no [LocalTabReselectSignal] is provided.
 *
 * The listener runs before any scroll the same reselect also triggers, so a screen that behaves differently by where it already is can
 * read its position here and get the answer for the moment of the tap.
 *
 * @need run something when the selected tab is tapped again
 * @group bars
 * @replaces a hand-built collector of a tab-bar reselect event
 */
@Composable
fun OnTabReselect(tab: Any, action: suspend () -> Unit) {
    val signal = LocalTabReselectSignal.current ?: return
    val currentAction by rememberUpdatedState(action)
    LaunchedEffect(signal, tab) {
        signal.filter { it == tab }.collect { currentAction() }
    }
}

/**
 * Scrolls [listState] back to the top each time the user taps [tab] while it is the selected one.
 *
 * @need scroll a list to the top when its tab is tapped again
 * @group bars
 * @replaces a hand-built scroll-to-top on a tab reselect
 */
@Composable
fun ScrollToTopOnTabReselect(tab: Any, listState: LazyListState) = OnTabReselect(tab) { listState.animateScrollToItem(0) }

/**
 * [ScrollToTopOnTabReselect] for a tab root that is a grid, such as search results or an idle browse page.
 *
 * @need scroll a grid to the top when its tab is tapped again
 * @group bars
 * @replaces a hand-built scroll-to-top on a tab reselect
 */
@Composable
fun ScrollToTopOnTabReselect(tab: Any, gridState: LazyGridState) = OnTabReselect(tab) { gridState.animateScrollToItem(0) }
