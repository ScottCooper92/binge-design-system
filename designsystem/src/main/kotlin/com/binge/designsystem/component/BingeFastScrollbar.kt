package com.binge.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A fast scrollbar for a lazy grid: a thin track down the end edge with a thumb that follows the scroll and can be dragged to jump
 * through the whole list. It appears while the grid scrolls or is dragged, and fades after a moment of rest. Overlay it on the grid, in the
 * same box, aligned to the end.
 *
 * While the thumb is dragged, a bubble beside it shows [bubbleText] for the first visible item, such as the name of the section it is in. It
 * is one line and never wider than a fixed width, so it ends in an ellipsis rather than clip at a large font. Return null for an item that
 * has nothing worth showing and no bubble is drawn.
 *
 * For a screen reader the scrollbar is one adjustable control named [label], with the current position as its value and [bubbleText] as its
 * state, so it can be set by the usual increase and decrease gestures, and the grid itself stays scrollable as before. It takes no focus, so
 * a keyboard or D-pad user's traversal is unchanged.
 *
 * @need let a person jump through a long grid by dragging
 * @group bars
 * @replaces a hand-built draggable thumb over a lazy grid
 * @sample BingeFastScrollbarSample
 */
@Composable
fun BingeFastScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    bubbleText: ((firstVisibleIndex: Int) -> String?)? = null,
    label: String = stringResource(R.string.fast_scrollbar_label),
) {
    val source = remember(state) { LazyGridScrollSource(state) }
    FastScrollbar(source = source, modifier = modifier, bubbleText = bubbleText, label = label)
}

/**
 * A fast scrollbar for a lazy list; see the overload for a [LazyGridState], which behaves the same.
 *
 * @need let a person jump through a long list by dragging
 * @group bars
 * @replaces a hand-built draggable thumb over a lazy list
 * @sample BingeFastScrollbarListSample
 */
@Composable
fun BingeFastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    bubbleText: ((firstVisibleIndex: Int) -> String?)? = null,
    label: String = stringResource(R.string.fast_scrollbar_label),
) {
    val source = remember(state) { LazyListScrollSource(state) }
    FastScrollbar(source = source, modifier = modifier, bubbleText = bubbleText, label = label)
}

/** What the scrollbar reads from a lazy grid or list, so one scrollbar serves both. */
internal interface FastScrollSource {
    val totalItems: Int
    val firstVisibleIndex: Int
    val visibleItems: Int
    val scrolling: Boolean

    suspend fun scrollToItem(index: Int)
}

private class LazyGridScrollSource(
    private val state: LazyGridState,
) : FastScrollSource {
    override val totalItems get() = state.layoutInfo.totalItemsCount
    override val firstVisibleIndex get() = state.firstVisibleItemIndex
    override val visibleItems get() = state.layoutInfo.visibleItemsInfo.size
    override val scrolling get() = state.isScrollInProgress

    override suspend fun scrollToItem(index: Int) = state.scrollToItem(index)
}

private class LazyListScrollSource(
    private val state: LazyListState,
) : FastScrollSource {
    override val totalItems get() = state.layoutInfo.totalItemsCount
    override val firstVisibleIndex get() = state.firstVisibleItemIndex
    override val visibleItems get() = state.layoutInfo.visibleItemsInfo.size
    override val scrolling get() = state.isScrollInProgress

    override suspend fun scrollToItem(index: Int) = state.scrollToItem(index)
}

@Composable
internal fun FastScrollbar(
    source: FastScrollSource,
    modifier: Modifier,
    bubbleText: ((Int) -> String?)?,
    label: String,
) {
    val total = source.totalItems
    val visible = source.visibleItems
    // Nothing to jump through when the whole list is on screen.
    if (total <= 0 || visible <= 0 || total <= visible) return
    val scope = rememberCoroutineScope()
    val first = source.firstVisibleIndex
    val restFraction = (first.toFloat() / (total - visible).coerceAtLeast(1)).coerceIn(0f, 1f)
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val dragging = dragFraction != null
    val scrolling = source.scrolling
    // Revealed while the grid moves or the thumb is held, and for a moment after.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(scrolling, dragging) {
        if (scrolling || dragging) {
            revealed = true
        } else {
            delay(FAST_SCROLLBAR_HIDE_DELAY_MILLIS)
            revealed = false
        }
    }
    val bubble = if (dragging) bubbleText?.invoke(first) else null
    val jumpTo = { fraction: Float ->
        // The same scale as restFraction: the furthest the list scrolls puts item total - visible first.
        val index = (fraction.coerceIn(0f, 1f) * (total - visible)).roundToInt()
        scope.launch { source.scrollToItem(index) }
        Unit
    }
    val latestJumpTo by rememberUpdatedState(jumpTo)
    FastScrollbarBar(
        fraction = dragFraction ?: restFraction,
        thumbShare = visible.toFloat() / total,
        revealed = revealed,
        dragging = dragging,
        bubble = bubble,
        label = label,
        stateText = bubbleText?.invoke(first),
        onDragStart = { dragFraction = restFraction },
        onDragTo = { fraction ->
            dragFraction = fraction
            latestJumpTo(fraction)
        },
        onDragEnd = { dragFraction = null },
        onSetProgress = { latestJumpTo(it) },
        modifier = modifier,
    )
}

/**
 * The scrollbar drawn from its state: the thumb at [fraction] of the track, [thumbShare] of the track long (never shorter than a finger), shown
 * while [revealed] or [dragging], with a [bubble] beside it while it is held. The catalog draws frames of it directly, since a frame cannot hold a
 * drag.
 */
@Composable
internal fun FastScrollbarBar(
    fraction: Float,
    thumbShare: Float,
    revealed: Boolean,
    dragging: Boolean,
    bubble: String?,
    label: String,
    stateText: String?,
    onDragStart: () -> Unit,
    onDragTo: (fraction: Float) -> Unit,
    onDragEnd: () -> Unit,
    onSetProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shown by animateFloatAsState(if (revealed || dragging) 1f else 0f, label = "fast-scrollbar-alpha")
    val touchWidth = dimensionResource(R.dimen.fast_scrollbar_touch_width)
    val trackWidth = dimensionResource(R.dimen.fast_scrollbar_track_width)
    val minThumb = dimensionResource(R.dimen.fast_scrollbar_min_thumb_height)
    val bubbleGap = dimensionResource(R.dimen.fast_scrollbar_bubble_gap)
    val latestOnDragStart by rememberUpdatedState(onDragStart)
    val latestOnDragTo by rememberUpdatedState(onDragTo)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val trackPx = constraints.maxHeight.toFloat()
        val minThumbPx = with(LocalDensity.current) { minThumb.toPx() }
        val thumbPx = (trackPx * thumbShare).coerceIn(minThumbPx.coerceAtMost(trackPx), trackPx)
        val thumbTop = ((trackPx - thumbPx) * fraction.coerceIn(0f, 1f)).roundToInt()
        // Read through state, not a pointer-input key: the thumb's length changes as the list scrolls, and a key that changed mid-drag would restart
        // the gesture and let go of the thumb.
        val latestTravel by rememberUpdatedState((trackPx - thumbPx).coerceAtLeast(1f))
        val latestThumbTop by rememberUpdatedState(thumbTop)
        val grabOffset = remember { FloatArray(1) }
        // For a screen reader the end edge is one adjustable control. It has no pointer handling, so it never takes a touch from the grid.
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(touchWidth)
                .semantics {
                    contentDescription = label
                    stateText?.let { stateDescription = it }
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction.coerceIn(0f, 1f), 0f..1f)
                    setProgress { value ->
                        onSetProgress(value)
                        true
                    }
                },
        )
        if (shown > 0f) {
            val ink = MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(trackWidth)
                    .alpha(shown)
                    .clip(BingeShapes.Pill)
                    .background(ink.copy(alpha = FAST_SCROLLBAR_TRACK_ALPHA)),
            )
            // Only the thumb takes touches, so a tap on the grid under the track still lands on the grid. It is a touch target wide enough
            // for a finger, with the thin thumb drawn at its end.
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, thumbTop) }
                    .width(touchWidth)
                    .height(with(LocalDensity.current) { thumbPx.toDp() })
                    .testTag(FAST_SCROLLBAR_THUMB_TAG)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                grabOffset[0] = offset.y
                                latestOnDragStart()
                            },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                // The thumb moves under the finger as the list scrolls, so its own coordinates would shrink the drag. The
                                // finger's place in the track is steady: its place in the thumb plus where the thumb now is.
                                val inTrack = change.position.y + latestThumbTop - grabOffset[0]
                                latestOnDragTo((inTrack / latestTravel).coerceIn(0f, 1f))
                            },
                            onDragEnd = { latestOnDragEnd() },
                            onDragCancel = { latestOnDragEnd() },
                        )
                    },
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(trackWidth)
                        .alpha(shown)
                        .clip(BingeShapes.Pill)
                        .background(if (dragging) MaterialTheme.colorScheme.primary else ink.copy(alpha = FAST_SCROLLBAR_THUMB_ALPHA)),
                )
            }
        }
        if (bubble != null) {
            // Beside the thumb, level with its middle and kept inside the track, clear of the touch strip.
            val centre = thumbTop + (thumbPx / 2).roundToInt()
            Layout(
                content = { FastScrollBubble(text = bubble) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight()
                    .padding(end = touchWidth + bubbleGap),
            ) { measurables, bubbleConstraints ->
                val placeable = measurables.first().measure(bubbleConstraints.copy(minWidth = 0, minHeight = 0))
                val top = (centre - placeable.height / 2).coerceIn(0, (bubbleConstraints.maxHeight - placeable.height).coerceAtLeast(0))
                layout(placeable.width, bubbleConstraints.maxHeight) { placeable.place(0, top) }
            }
        }
    }
}

@Composable
private fun FastScrollBubble(text: String) {
    Box(
        Modifier
            .clip(BingeShapes.Medium)
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(
                horizontal = dimensionResource(R.dimen.fast_scrollbar_bubble_padding_h),
                vertical = dimensionResource(R.dimen.fast_scrollbar_bubble_padding_v),
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = dimensionResource(R.dimen.fast_scrollbar_bubble_max_width)),
        )
    }
}

/** The thumb's touch target, for tests to find. */
internal const val FAST_SCROLLBAR_THUMB_TAG = "fast-scrollbar-thumb"

/** How long the scrollbar stays after the grid stops moving. */
private const val FAST_SCROLLBAR_HIDE_DELAY_MILLIS = 1_500L
private const val FAST_SCROLLBAR_TRACK_ALPHA = 0.12f
private const val FAST_SCROLLBAR_THUMB_ALPHA = 0.6f
