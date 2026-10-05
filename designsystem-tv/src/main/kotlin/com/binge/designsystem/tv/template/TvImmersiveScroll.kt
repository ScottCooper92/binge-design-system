package com.binge.designsystem.tv.template

import android.os.SystemClock
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.drawscope.clipRect
import kotlinx.coroutines.delay

/** How long focus must rest before the backdrop crossfade follows it; the scroll and clip never wait. */
private const val FOCUS_SETTLE_MILLIS = 90L

/** Leading + trailing coalesce window for the anchor scroll, so a held D-pad sweep anchors only the row it lands on. */
private const val ANCHOR_COALESCE_MILLIS = 200L

/** The topmost of the candidate row tops, or infinity (clip everything) when the target row is not laid out. */
internal fun immersiveClipTopPx(candidateTops: List<Int>): Float =
    candidateTops.minOrNull()?.coerceAtLeast(0)?.toFloat() ?: Float.POSITIVE_INFINITY

/** Paints only below [topPx], so the backdrop behind the list shows through where the rows above the focused one sit. */
internal fun Modifier.immersiveTopClip(enabled: Boolean, topPx: () -> Float): Modifier =
    if (!enabled) {
        this
    } else {
        drawWithContent {
            val top = topPx().coerceIn(0f, size.height)
            if (top <= 0f) {
                drawContent()
            } else {
                val content = this
                clipRect(top = top) { content.drawContent() }
            }
        }
    }

internal fun listTopClip(state: LazyListState, target: () -> Int): () -> Float =
    {
        val t = target()
        immersiveClipTopPx(
            state.layoutInfo.visibleItemsInfo
                .filter { it.index == t }
                .map { it.offset },
        )
    }

internal fun gridTopClip(state: LazyGridState, targetLead: () -> Int): () -> Float =
    {
        val lead = targetLead()
        val row = state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == lead }
            ?.row
        val tops =
            if (row == null) {
                emptyList()
            } else {
                state.layoutInfo.visibleItemsInfo
                    .filter { it.row == row }
                    .map { it.offset.y }
            }
        immersiveClipTopPx(tops)
    }

/** [live], lagging by a short settle so a held sweep does not restart the backdrop crossfade on every card. */
@Composable
internal fun <T> rememberSettledFocus(live: T, reduceMotion: Boolean): T {
    var settled by remember { mutableStateOf(live) }
    LaunchedEffect(live, reduceMotion) {
        if (!reduceMotion) delay(FOCUS_SETTLE_MILLIS)
        settled = live
    }
    return settled
}

/** True while [signal] is changing faster than the anchor scroll can follow. */
@Composable
internal fun rememberBursting(signal: Any?): Boolean {
    var bursting by remember { mutableStateOf(false) }
    val lastChangeUptime = remember { mutableLongStateOf(0L) }
    LaunchedEffect(signal) {
        if (SystemClock.uptimeMillis() - lastChangeUptime.longValue <= ANCHOR_COALESCE_MILLIS) bursting = true
        lastChangeUptime.longValue = SystemClock.uptimeMillis()
        delay(ANCHOR_COALESCE_MILLIS)
        bursting = false
    }
    return bursting
}

/**
 * The single writer for an immersive surface's anchor scroll: re-asserts [scrollToAnchor] whenever any of
 * [anchorKeys] changes and whenever focus returns to the body from outside it. Returns the `onFocusChanged`
 * callback the body reports through; [alsoReport] chains a second observer.
 */
@Composable
internal fun rememberAnchorKeeper(
    vararg anchorKeys: Any?,
    alsoReport: (FocusState) -> Unit = {},
    shouldScroll: () -> Boolean = { true },
    scrollToAnchor: suspend () -> Unit,
): (FocusState) -> Unit {
    var returns by remember { mutableIntStateOf(0) }
    var bodyHasFocus by remember { mutableStateOf(false) }
    val lastFireUptime = remember { mutableLongStateOf(0L) }
    val currentShouldScroll by rememberUpdatedState(shouldScroll)
    val currentScrollToAnchor by rememberUpdatedState(scrollToAnchor)
    LaunchedEffect(*anchorKeys, returns) {
        if (!currentShouldScroll()) return@LaunchedEffect
        if (SystemClock.uptimeMillis() - lastFireUptime.longValue <= ANCHOR_COALESCE_MILLIS) {
            delay(ANCHOR_COALESCE_MILLIS)
        }
        lastFireUptime.longValue = SystemClock.uptimeMillis()
        currentScrollToAnchor()
    }
    return { state ->
        alsoReport(state)
        if (state.hasFocus && !bodyHasFocus) returns++
        bodyHasFocus = state.hasFocus
    }
}
