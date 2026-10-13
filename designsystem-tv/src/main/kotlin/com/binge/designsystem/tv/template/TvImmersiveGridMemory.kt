package com.binge.designsystem.tv.template

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import com.binge.designsystem.tv.focus.restoreTvOverlayFocus

/**
 * The cell a grid is built around, by index and by the key the content gives it. The key is what retires the memory
 * when the content behind the index changes. A null key was seeded before the content had keys, and is never stale.
 */
internal data class TvGridMemory(
    val index: Int,
    val key: Any?,
)

/** Saves the pair as a two-item list; an empty list is no memory, since a saver cannot save null itself. */
internal val TvGridMemorySaver: Saver<TvGridMemory?, Any> =
    listSaver(
        save = { memory -> if (memory == null) emptyList() else listOf(memory.index, memory.key) },
        restore = { saved -> if (saved.isEmpty()) null else TvGridMemory(saved[0] as Int, saved[1]) },
    )

/**
 * The remembered index if [content] can honour it, else null. A cell that is not loaded yet still honours it, so a
 * page in flight keeps the memory; only a loaded cell with a different key retires it.
 */
internal fun <T : Any> TvGridMemory?.resolveIn(content: TvImmersiveGridContent<T>): Int? {
    val memory = this ?: return null
    val items = content as? TvImmersiveGridContent.Items ?: return null
    if (memory.index !in 0 until items.count) return null
    return memory.index.takeUnless { isStaleIn(content) }
}

/**
 * Whether the memory is provably retired: a loaded cell sits at its index under a different key. Narrower than
 * [resolveIn] returning null on purpose, because only this case may send the grid back to the top.
 */
internal fun <T : Any> TvGridMemory?.isStaleIn(content: TvImmersiveGridContent<T>): Boolean {
    val memory = this ?: return false
    val remembered = memory.key ?: return false
    val items = content as? TvImmersiveGridContent.Items ?: return false
    if (memory.index !in 0 until items.count || items.at(memory.index) == null) return false
    return items.key(memory.index) != remembered
}

/** Sends a grid whose memory retired back to the top, so the next entry has a composed first cell to land on. */
@Composable
internal fun ResetScrollWhenStale(
    stale: Boolean,
    gridState: LazyGridState,
    reduceMotion: Boolean,
) {
    LaunchedEffect(stale) {
        if (!stale || gridState.firstVisibleItemIndex == 0) return@LaunchedEffect
        if (reduceMotion) gridState.scrollToItem(0) else gridState.animateScrollToItem(0)
    }
}

/**
 * Keeps focus in the grid's body when the body is swapped for another state, such as cells replaced by a message.
 * The old body leaves with the focus it held, so focus would otherwise fall to the rail. Returns the callback the
 * body reports its focus through.
 */
@Composable
internal fun rememberBodyFocusKeeper(bodyKind: Any, entryFocusRequester: FocusRequester?): (FocusState) -> Unit {
    var bodyHasFocus by remember { mutableStateOf(false) }
    var settled by remember { mutableStateOf(true) }
    val owed = remember(bodyKind) { bodyHasFocus || !settled }
    LaunchedEffect(bodyKind) {
        if (entryFocusRequester == null || !owed) return@LaunchedEffect
        settled = false
        // Only if nothing else got there first: requesting focus on a group that already holds it skips its entry
        // routing and lands on the first cell, off the one the user left.
        if (!bodyHasFocus) restoreTvOverlayFocus(entryFocusRequester)
        settled = true
    }
    return { state -> bodyHasFocus = state.hasFocus }
}

/** The body's identity for [rememberBodyFocusKeeper]: a swap between kinds, or between two messages, is a new body. */
internal fun TvImmersiveGridContent<*>.bodyKind(): Any =
    when (this) {
        TvImmersiveGridContent.Loading -> TvImmersiveGridContent.Loading
        is TvImmersiveGridContent.Message -> this
        is TvImmersiveGridContent.Items -> TvImmersiveGridContent.Items::class
    }
