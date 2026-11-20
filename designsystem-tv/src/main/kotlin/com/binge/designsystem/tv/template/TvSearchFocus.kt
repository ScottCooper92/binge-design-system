package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * A control's own focus flag, seedable so a screenshot can render it focused.
 *
 * Focus is a parameter rather than a runtime state across the TV layer (ADR §4): a baseline renders one frame
 * and runs no coroutines, so nothing that waited on a real focus event would ever capture a lit control.
 * [initiallyFocused] is what production leaves false and a preview sets true.
 */
@Composable
internal fun rememberFocusFlag(initiallyFocused: Boolean = false): MutableState<Boolean> = remember { mutableStateOf(initiallyFocused) }

/** Focus positions a screenshot can seed, since a baseline renders one frame and runs no coroutines (ADR §4). */
data class TvSearchSeededFocus(
    val field: Boolean = false,
    val suggestionKey: String? = null,
    val cellKey: String? = null,
    val retry: Boolean = false,
)

/**
 * The text the field shows, written on every edit and re-seeded from [query] only when [revision] moves, i.e. when the app
 * replaced the query itself. A field fed from state that reaches it through another thread shows each keystroke a frame late,
 * and the IME's next edit then applies to stale text.
 */
@Composable
internal fun rememberFieldText(query: String, revision: Int): MutableState<String> {
    val text = rememberSaveable { mutableStateOf(query) }
    LaunchedEffect(revision) { if (text.value != query) text.value = query }
    return text
}
