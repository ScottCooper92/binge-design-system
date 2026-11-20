package com.binge.designsystem.tv.template

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.withFrameNanos
import kotlin.math.abs
import kotlin.math.exp

private const val FOLLOW_TIME_CONSTANT_MILLIS = 45f
private const val FOLLOW_SNAP_PX = 6
private const val FOLLOW_MAX_FRAMES = 90
private const val NANOS_PER_MILLI = 1_000_000f

/**
 * Scrolls until item [index] sits at the top, closing a time-based share of the remaining distance each frame. It re-reads the
 * item's position every frame, so rows resizing above it mid-move are absorbed rather than overshot; a far item is jumped to.
 */
internal suspend fun LazyListState.followToTop(index: Int) {
    var last = withFrameNanos { it }
    repeat(FOLLOW_MAX_FRAMES) {
        val offset = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.offset
        if (offset == null) {
            scrollToItem(index)
            return
        }
        if (abs(offset) <= FOLLOW_SNAP_PX) {
            scrollBy(offset.toFloat())
            return
        }
        val now = withFrameNanos { it }
        val elapsedMillis = (now - last) / NANOS_PER_MILLI
        last = now
        val share = 1f - exp(-elapsedMillis / FOLLOW_TIME_CONSTANT_MILLIS)
        scrollBy(offset * share)
    }
    scrollToItem(index)
}
