package com.binge.designsystem.tv.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * The destination a TV shell's content shows, which may trail the navigation's selection while the selection is still
 * moving. [jumpTo] settles on a key at once, for a deep link or a Back to home that should not wait.
 */
@Stable
class TvShellSettle internal constructor(
    initialKey: Any,
) {
    /** The key the content shows. */
    var settledKey: Any by mutableStateOf(initialKey)
        internal set

    /** Settles on [key] now, skipping any delay. */
    fun jumpTo(key: Any) {
        settledKey = key
    }

    internal companion object {
        val Saver: Saver<TvShellSettle, Any> = Saver(save = { it.settledKey }, restore = { TvShellSettle(it) })
    }
}

/** A [TvShellSettle] starting on [initialKey], saved across recreation. The key must fit in a Bundle. */
@Composable
fun rememberTvShellSettle(initialKey: Any): TvShellSettle = rememberSaveable(saver = TvShellSettle.Saver) { TvShellSettle(initialKey) }
