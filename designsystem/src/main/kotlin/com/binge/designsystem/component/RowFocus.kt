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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.theme.tonalContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/** How long a row is waited for, as a screen loads what its rows show, before the key is given up on. */
private const val FIND_TIMEOUT_MILLIS = 4_000L

/** Frames, and then milliseconds, a screen that has just loaded is given to settle its sizes before and after the row is scrolled to. */
private const val SETTLE_FRAMES = 3
private const val SETTLE_MILLIS = 120L

/** How much of the screen's height is asked for under the row, so the row ends up near the middle. */
private const val ROOM_BELOW_SHARE = 0.45f

/** The wash fading between levels, in milliseconds. Under reduce motion it snaps instead. */
private const val FLASH_FADE_MILLIS = 220

/** One pulse is this long up and this long down; the row pulses [PULSES] times and then stays at [STEADY_ALPHA]. */
private const val PULSE_HALF_MILLIS = 220L
private const val PULSES = 2

/** The wash at a pulse's peak. It is the tonal container colour at full strength; the softness comes from that colour, not from this alpha. */
private const val PEAK_ALPHA = 1f

/** The wash the row keeps once it has pulsed, until the screen is left or another row is asked for. */
private const val STEADY_ALPHA = 0.8f

/** What a screen's [ListItem]s with a [ListItem.key] report to, so one of them can be found and shown. */
@Stable
internal class RowFocus {
    val targets = mutableMapOf<String, RowTarget>()

    /** The row washed now, and how strongly. */
    var shown by mutableStateOf<String?>(null)
    var level by mutableStateOf(0f)

    /** True while [ProvideRowFocus] is scrolling to a row, as opposed to the user scrolling. A top bar does not follow that scroll. */
    var scrolling by mutableStateOf(false)
}

/**
 * This connection, except that it takes no part in a scroll that [focus] is making to bring a row into view. A top bar's scroll behaviour reads the
 * body's scroll to collapse or hide the bar, and that is right for the user's own scroll; a screen that opens on a row scrolls on its own, and the bar
 * must still be there when it arrives. A null [focus] changes nothing.
 */
internal fun NestedScrollConnection.ignoringRowFocusScroll(focus: RowFocus?): NestedScrollConnection {
    if (focus == null) return this
    val delegate = this
    return object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (focus.scrolling) Offset.Zero else delegate.onPreScroll(available, source)

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset = if (focus.scrolling) Offset.Zero else delegate.onPostScroll(consumed, available, source)

        override suspend fun onPreFling(available: Velocity): Velocity = delegate.onPreFling(available)

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = delegate.onPostFling(consumed, available)
    }
}

/** A keyed row: what scrolls to it, and its size, which is how much room it asks for. */
internal class RowTarget(
    val requester: BringIntoViewRequester,
) {
    var size: IntSize = IntSize.Zero
}

internal val LocalRowFocus = compositionLocalOf<RowFocus?> { null }

/**
 * Brings the row whose [ListItem.key] is [key] into view and pulses it twice, then leaves it highlighted, for a screen that opens on a particular setting (a
 * search result, say). Wrap the screen in it; with a null [key] it does nothing.
 *
 * A screen that scrolls a plain column (a form, say) composes every row at once, so the row is scrolled to as soon as it reports
 * itself. A screen built on a lazy list passes that list's own [listState]: the row may belong to a group that is not composed yet,
 * because a lazy list only composes what is near the screen, so the list is stepped from the top, one item at a time, until the row
 * reports itself, and then the row is scrolled to. A row that does not appear within a few seconds (the screen does not have it, or the user may not see
 * it) leaves the list where it began. A screen that loads what its rows show is waited for. The wash is a tonal one that pulses twice and then settles to a
 * steady highlight, which stays until the screen is left or another row is asked for, as Android's Settings does; under reduce motion
 * it changes level without the fade. [showAtPeak] draws the wash on its row from the first
 * frame and never scrolls, for a screenshot of the state; it is not for an app's own screens.
 *
 * The screen's top bar does not follow this scroll: only the user's own scroll collapses or hides it. For that, wrap the scaffold in it, as the lazy and
 * plain screens here do, so the scaffold can see it.
 *
 * A row is looked for again only when [key] changes. To show the same row again, set the key to null, let a frame pass (composition has to
 * see the null), and then set it back.
 */
@Composable
fun ProvideRowFocus(
    key: String?,
    listState: LazyListState? = null,
    showAtPeak: Boolean = false,
    content: @Composable () -> Unit,
) {
    val focus = remember { RowFocus() }
    val containerHeight = LocalWindowInfo.current.containerSize.height
    if (showAtPeak) {
        focus.shown = key
        focus.level = PEAK_ALPHA
    }
    LaunchedEffect(key, showAtPeak) {
        if (showAtPeak) return@LaunchedEffect
        // A row washed for an earlier key lets go as the next is looked for.
        focus.shown = null
        if (key == null) return@LaunchedEffect
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
        focus.scrollTo(target, containerHeight)
        delay(SETTLE_MILLIS)
        focus.scrollTo(target, containerHeight)
        focus.shown = key
        repeat(PULSES) {
            focus.level = PEAK_ALPHA
            delay(PULSE_HALF_MILLIS)
            focus.level = STEADY_ALPHA
            delay(PULSE_HALF_MILLIS)
        }
    }
    CompositionLocalProvider(LocalRowFocus provides focus) { content() }
}

/** Scrolls to [target] while marking it as this focus's own scroll, which a top bar's scroll behaviour ignores. */
private suspend fun RowFocus.scrollTo(target: RowTarget, containerHeight: Int) {
    scrolling = true
    try {
        target.showWithRoomBelow(containerHeight)
    } finally {
        scrolling = false
    }
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
        targetValue = if (focus.shown == key) focus.level else 0f,
        animationSpec = if (LocalReduceMotion.current) snap() else tween(FLASH_FADE_MILLIS),
        label = "rowFocusWash",
    )
    val wash = MaterialTheme.colorScheme.primary.tonalContainer()
    return this
        .bringIntoViewRequester(target.requester)
        .onSizeChanged { target.size = it }
        .drawBehind { if (alpha > 0f) drawRect(wash, alpha = alpha) }
}
