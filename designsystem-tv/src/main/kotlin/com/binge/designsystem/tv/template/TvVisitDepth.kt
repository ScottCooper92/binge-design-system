package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * How far into a focus-driven page a visit got, reported once as the page leaves. Focus rather than the viewport,
 * because on a television the viewport is nearly the whole page: a visit that scrolls nowhere still sees most of it.
 */
internal class TvVisitDepth {
    var deepest: Int? = null
        private set

    /** Records focus arriving at [index]. A null index, for something not in the page, is ignored. */
    fun reach(index: Int?) {
        if (index != null && index > (deepest ?: -1)) deepest = index
    }
}

/** Remembers a [TvVisitDepth] and reports it to [onVisitEnded] as the page leaves composition. */
@Composable
internal fun rememberTvVisitDepth(onVisitEnded: (deepestIndex: Int?) -> Unit): TvVisitDepth {
    val depth = remember { TvVisitDepth() }
    // The report happens in onDispose, after any recomposition that replaced the lambda.
    val currentOnVisitEnded by rememberUpdatedState(onVisitEnded)
    DisposableEffect(Unit) {
        onDispose { currentOnVisitEnded(depth.deepest) }
    }
    return depth
}
