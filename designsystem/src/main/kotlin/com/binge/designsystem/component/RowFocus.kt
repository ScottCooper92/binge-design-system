package com.binge.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.theme.tonalContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/** How long the row stays washed once it is in view. */
private const val FLASH_HOLD_MILLIS = 900L

/** How long a row is waited for, as a screen loads what its rows show, before the key is given up on. */
private const val FIND_TIMEOUT_MILLIS = 4_000L

/** Frames, and then milliseconds, a screen that has just loaded is given to settle its sizes before and after the row is scrolled to. */
private const val SETTLE_FRAMES = 3
private const val SETTLE_MILLIS = 120L

/** How much of the screen's height is asked for under the row, so the row ends up near the middle. */
private const val ROOM_BELOW_SHARE = 0.45f

/** The wash fading in and out, in milliseconds. Under reduce motion it snaps instead. */
private const val FLASH_FADE_MILLIS = 300

/** A wash strong enough to find the row by, soft enough to read through. */
private const val FLASH_PEAK_ALPHA = 0.9f

/** What a screen's [ListItem]s with a [ListItem.key] report to, so one of them can be found and shown. */
@Stable
internal class RowFocus {
    val targets = mutableMapOf<String, RowTarget>()
    var flashing by mutableStateOf<String?>(null)
}

/** A keyed row: what scrolls to it, and its size, which is how much room it asks for. */
internal class RowTarget(
    val requester: BringIntoViewRequester,
) {
    var size: IntSize = IntSize.Zero
}

internal val LocalRowFocus = compositionLocalOf<RowFocus?> { null }

/**
 * Brings the row whose [ListItem.key] is [key] into view and washes it once, for a screen that opens on a particular setting (a
 * search result, say). Wrap the screen in it; with a null [key] it does nothing.
 *
 * A screen that scrolls a plain column (a form, say) composes every row at once, so the row is scrolled to as soon as it reports
 * itself. A screen built on a lazy list passes that list's own [listState]: the row may belong to a group that is not composed yet,
 * because a lazy list only composes what is near the screen, so the list is stepped from the top, one item at a time, until the row
 * reports itself, and then the row is scrolled to. A row that does not appear within a few seconds (the screen does not have it, or the user may not see
 * it) leaves the list where it began. A screen that loads what its rows show is waited for. The wash is a tonal fade in and out over
 * about a second; under reduce motion it appears and goes without the fade. [showAtPeak] draws the wash on its row from the first
 * frame and never scrolls, for a screenshot of the state.
 */
@Composable
fun ProvideRowFocus(
    key: String?,
    modifier: Modifier = Modifier,
    listState: LazyListState? = null,
    showAtPeak: Boolean = false,
    content: @Composable () -> Unit,
) {
    val focus = remember { RowFocus() }
    val containerHeight = LocalWindowInfo.current.containerSize.height
    if (showAtPeak) focus.flashing = key
    LaunchedEffect(key, showAtPeak) {
        if (key == null || showAtPeak) return@LaunchedEffect
        val startIndex = listState?.firstVisibleItemIndex ?: 0
        val startOffset = listState?.firstVisibleItemScrollOffset ?: 0
        // The screen may still be loading what its rows show, and a lazy list composes only what is near the screen: so wait for the
        // row to report itself, stepping a lazy list from the top meanwhile (and again if its items change as the screen loads).
        val target =
            withTimeoutOrNull(FIND_TIMEOUT_MILLIS) {
                var scanned = 0
                var total = -1
                var found = focus.targets[key]
                while (found == null) {
                    val count = listState?.layoutInfo?.totalItemsCount ?: 0
                    if (count != total) {
                        total = count
                        scanned = 0
                    }
                    if (listState != null && scanned < count) listState.scrollToItem(scanned++)
                    // Composition of what the scroll brought near, and the row's registration, take a frame or two.
                    withFrameNanos { }
                    withFrameNanos { }
                    found = focus.targets[key]
                }
                found
            }
        if (target == null) {
            listState?.scrollToItem(startIndex, startOffset)
            return@LaunchedEffect
        }
        // A screen that has just loaded is still settling its sizes, so the scroll range may not reach the row yet: let a few frames
        // pass, scroll, and scroll once more when the layout has had a moment to finish.
        repeat(SETTLE_FRAMES) { withFrameNanos { } }
        target.showWithRoomBelow(containerHeight)
        delay(SETTLE_MILLIS)
        target.showWithRoomBelow(containerHeight)
        focus.flashing = key
        delay(FLASH_HOLD_MILLIS)
        focus.flashing = null
    }
    CompositionLocalProvider(LocalRowFocus provides focus) { content() }
}

/**
 * Scrolls to the row with some room below it. A plain "make it visible" scroll does the least it can, which leaves the row against the
 * edge of the screen where a pinned footer can cover it; asking for the row and a share of the screen under it puts the row in the
 * middle. A row near the end of a screen cannot go further than the end allows.
 */
private suspend fun RowTarget.showWithRoomBelow(containerHeight: Int) {
    val width = size.width.toFloat()
    val below = (containerHeight * ROOM_BELOW_SHARE).coerceAtLeast(size.height.toFloat())
    requester.bringIntoView(Rect(0f, 0f, width, size.height + below))
}

/**
 * Registers this row under [key] with the enclosing [ProvideRowFocus], so it can be scrolled to, and draws its wash while it is the
 * one being shown. Nothing happens without a [key] or without a [ProvideRowFocus] around the list.
 */
@Composable
internal fun Modifier.rowFocusTarget(key: String?): Modifier {
    val focus = LocalRowFocus.current
    if (focus == null || key == null) return this
    val target = remember { RowTarget(BringIntoViewRequester()) }
    DisposableEffect(focus, key, target) {
        focus.targets[key] = target
        onDispose { if (focus.targets[key] === target) focus.targets.remove(key) }
    }
    val alpha by animateFloatAsState(
        targetValue = if (focus.flashing == key) FLASH_PEAK_ALPHA else 0f,
        animationSpec = if (LocalReduceMotion.current) snap() else tween(FLASH_FADE_MILLIS),
        label = "rowFocusWash",
    )
    val wash = MaterialTheme.colorScheme.primary.tonalContainer()
    return this
        .bringIntoViewRequester(target.requester)
        .onSizeChanged { target.size = it }
        .drawBehind { if (alpha > 0f) drawRect(wash, alpha = alpha) }
}
