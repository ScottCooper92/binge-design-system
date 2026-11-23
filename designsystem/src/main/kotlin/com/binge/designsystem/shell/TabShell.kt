package com.binge.designsystem.shell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.BingeNavPresentation
import com.binge.designsystem.component.BingeNavSuiteItem
import com.binge.designsystem.component.BingeNavSuiteShell
import com.binge.designsystem.component.LocalTabReselectSignal
import com.binge.designsystem.motion.rememberBingeNavMotion
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * The top-level tabs of an app: a [BingeNavSuiteShell] over [state], showing one tab's [content] at a time with the others'
 * state saved. Choosing the tab that is showing pops it to its root and tells the tab through [LocalTabReselectSignal], so
 * [com.binge.designsystem.component.OnTabReselect] and `ScrollToTopOnTabReselect` work inside [content].
 *
 * [items] are matched to tabs by key: an item's key is the tab itself. With [backToStart] the system Back returns to the
 * start tab before it leaves the app. [onReselect] runs after a reselect has popped the tab's own stack, so the app can close
 * what it opened above the shell for that tab, on its own stack.
 *
 * @need frame an app with tabs that each keep their own back stack
 * @group bars
 * @replaces a hand-built tab shell with a selected index and a stack per tab
 */
@Composable
fun <T : Any> TabShell(
    state: TabShellState<T>,
    items: List<BingeNavSuiteItem>,
    presentation: BingeNavPresentation,
    modifier: Modifier = Modifier,
    backToStart: Boolean = true,
    onReselect: (T) -> Unit = {},
    transition: ContentTransform = rememberBingeNavMotion().tabSwitch,
    content: @Composable (T) -> Unit,
) {
    val saveable = rememberSaveableStateHolder()
    val reselects = remember { MutableSharedFlow<Any>(extraBufferCapacity = 1) }
    BackHandler(enabled = backToStart && state.selected != state.startTab) { state.back() }
    BingeNavSuiteShell(
        items = items,
        selectedKey = state.selected,
        onSelect = { key ->
            val tab = state.tabs.firstOrNull { it == key }
            if (tab != null && state.select(tab)) {
                onReselect(tab)
                reselects.tryEmit(tab)
            }
        },
        modifier = modifier,
        presentation = presentation,
    ) {
        CompositionLocalProvider(LocalTabReselectSignal provides reselects) {
            AnimatedContent(targetState = state.selected, transitionSpec = { transition }, label = "tab") { tab ->
                saveable.SaveableStateProvider(state.tabs.indexOf(tab)) { content(tab) }
            }
        }
    }
}
