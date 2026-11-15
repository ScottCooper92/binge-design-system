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
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.theme.tonalContainer
import kotlinx.coroutines.delay

/** How long the row stays washed once it is in view. */
private const val FLASH_HOLD_MILLIS = 900L

/** The wash fading in and out, in milliseconds. Under reduce motion it snaps instead. */
private const val FLASH_FADE_MILLIS = 300

/** A wash strong enough to find the row by, soft enough to read through. */
private const val FLASH_PEAK_ALPHA = 0.9f

/** What a screen's [ListItem]s with a [ListItem.key] report to, so one of them can be found and shown. */
@Stable
internal class RowFocus {
    val targets = mutableMapOf<String, BringIntoViewRequester>()
    var flashing by mutableStateOf<String?>(null)
}

internal val LocalRowFocus = compositionLocalOf<RowFocus?> { null }

/**
 * Brings the row whose [ListItem.key] is [key] into view and washes it once, for a screen that opens on a particular setting (a
 * search result, say). Wrap the screen's list in it and give [listState] the list's own state; with a null [key] it does nothing.
 *
 * The row may belong to a group that is not composed yet, because a lazy list only composes what is near the screen. So the list is
 * stepped from the top, one item at a time, until the row reports itself, and then the row is scrolled to. A row that never appears
 * (the page does not have it, or the user may not see it) leaves the list where it began. The wash is a tonal fade in and out over
 * about a second; under reduce motion it appears and goes without the fade. [showAtPeak] draws the wash on its row from the first
 * frame and never scrolls, for a screenshot of the state.
 */
@Composable
fun ProvideRowFocus(
    key: String?,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    showAtPeak: Boolean = false,
    content: @Composable () -> Unit,
) {
    val focus = remember { RowFocus() }
    if (showAtPeak) focus.flashing = key
    LaunchedEffect(key, showAtPeak) {
        if (key == null || showAtPeak) return@LaunchedEffect
        val startIndex = listState.firstVisibleItemIndex
        val startOffset = listState.firstVisibleItemScrollOffset
        var index = 0
        while (focus.targets[key] == null && index < listState.layoutInfo.totalItemsCount) {
            listState.scrollToItem(index++)
            // Composition of the item the scroll brought near, and the row's registration, take a frame or two.
            withFrameNanos { }
            withFrameNanos { }
        }
        val requester = focus.targets[key]
        if (requester == null) {
            listState.scrollToItem(startIndex, startOffset)
            return@LaunchedEffect
        }
        requester.bringIntoView()
        focus.flashing = key
        delay(FLASH_HOLD_MILLIS)
        focus.flashing = null
    }
    CompositionLocalProvider(LocalRowFocus provides focus) { content() }
}

/**
 * Registers this row under [key] with the enclosing [ProvideRowFocus], so it can be scrolled to, and draws its wash while it is the
 * one being shown. Nothing happens without a [key] or without a [ProvideRowFocus] around the list.
 */
@Composable
internal fun Modifier.rowFocusTarget(key: String?): Modifier {
    val focus = LocalRowFocus.current
    if (focus == null || key == null) return this
    val requester = remember { BringIntoViewRequester() }
    DisposableEffect(focus, key, requester) {
        focus.targets[key] = requester
        onDispose { if (focus.targets[key] === requester) focus.targets.remove(key) }
    }
    val alpha by animateFloatAsState(
        targetValue = if (focus.flashing == key) FLASH_PEAK_ALPHA else 0f,
        animationSpec = if (LocalReduceMotion.current) snap() else tween(FLASH_FADE_MILLIS),
        label = "rowFocusWash",
    )
    val wash = MaterialTheme.colorScheme.primary.tonalContainer()
    return this
        .bringIntoViewRequester(requester)
        .drawBehind { if (alpha > 0f) drawRect(wash, alpha = alpha) }
}
