package com.binge.designsystem.tv.template

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.TvStripSeeAllCard
import com.binge.designsystem.tv.component.readAs
import com.binge.designsystem.tv.component.tvExpandingStepKeys
import com.binge.designsystem.tv.component.unreadWhen
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SLOT_CROSSFADE_MILLIS = 200
private const val TAIL_KEY = "tv-slot-carousel-tail"
private const val SEE_ALL_KEY = "tv-slot-carousel-see-all"

/** The carousels' 16:9 shape, for videos and backdrops. */
const val TV_DETAIL_WIDE_ASPECT = 16f / 9f

/**
 * The card that ends a [TvDetailSlotCarousel]: [label] on it, [peek] as the images fanned behind the label (the carousel's own
 * first items), and [onClick] to open the whole set. One step right of the last item, where the slot keeps that item and the
 * card in the strip takes the ring.
 */
class TvSlotCarouselSeeAll(
    val label: String,
    val peek: List<@Composable BoxScope.() -> Unit>,
    val onClick: () -> Unit,
)

/**
 * A detail tab's carousel in the hub rows' manner: the focused item is a large card at the row's start, ringed, with [caption]
 * beneath; ←/→ step through [items] while the smaller cards slide along beside it. One focus stop: OK calls [onClick] with the item's
 * position. Cards are [aspect] wide for their height, [image] fills each. [initialPosition] and [initialFocused] seed a frame.
 * With [seeAll] the strip ends in a See-all card, one step past the last item; the slot stays on that item while the card is ringed.
 *
 * @need show a detail tab's items as one large focused card with a strip of the rest, stepped with left and right
 * @group tv
 * @replaces a hand-built detail carousel with its own focus and step keys
 */
@Composable
fun <T : Any> TvDetailSlotCarousel(
    items: List<T>,
    key: (T) -> Any,
    aspect: Float,
    slotHeight: Dp,
    stripHeight: Dp,
    onClick: (index: Int) -> Unit,
    image: @Composable (T) -> Unit,
    caption: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    initialPosition: Int = 0,
    initialFocused: Boolean = false,
    seeAll: TvSlotCarouselSeeAll? = null,
) {
    if (items.isEmpty()) return
    var position by rememberSaveable { mutableIntStateOf(initialPosition) }
    val lastPosition = if (seeAll != null) items.size else items.lastIndex
    val current = position.coerceIn(0, lastPosition)
    val atSeeAll = seeAll != null && current == items.size
    // At the See-all card the slot keeps the last item, so the strip starts at the card.
    val shown = if (atSeeAll) items.lastIndex else current
    val item = items[shown]
    var focused by remember { mutableStateOf(initialFocused) }
    val gap = dimensionResource(TvR.dimen.tv_media_row_card_gap)
    // Starts one past the slot's item, so nothing shows twice.
    val strip = rememberLazyListState(initialFirstVisibleItemIndex = shown + 1)
    val reduceMotion = LocalReduceMotion.current
    LaunchedEffect(shown) {
        if (reduceMotion) strip.scrollToItem(shown + 1) else strip.animateScrollToItem(shown + 1)
    }
    // Centred in the space under the tabs rather than hung from its top.
    Column(
        modifier = modifier.fillMaxSize().padding(start = tvContentGutterStart()),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(slotHeight)
                .tvExpandingStepKeys(
                    enabled = true,
                    canStepRight = current < lastPosition,
                    canStepLeft = current > 0,
                    onStep = { delta -> position = current + delta },
                ).tvClickable(role = Role.Button, onFocusChanged = {
                    focused = it
                }, onClick = { if (atSeeAll) seeAll?.onClick?.invoke() else onClick(current) })
                // On the card the carousel reads as it, not as the last item the slot keeps.
                .readAs(seeAll?.label.takeIf { atSeeAll }),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.unreadWhen(atSeeAll)) {
                Crossfade(
                    targetState = item,
                    animationSpec = if (reduceMotion) snap() else tween(SLOT_CROSSFADE_MILLIS),
                    label = "tvDetailSlot",
                ) { shownItem -> TvSlotCard(shownItem, slotHeight * aspect, slotHeight, ringed = focused && !atSeeAll, image = image) }
            }
            LazyRow(
                state = strip,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f).height(stripHeight).clipToBounds(),
                contentPadding = PaddingValues(end = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                itemsIndexed(items, key = { _, entry -> key(entry) }) { _, entry ->
                    TvSlotCard(entry, stripHeight * aspect, stripHeight, ringed = false, image = image)
                }
                seeAll?.let { card ->
                    item(key = SEE_ALL_KEY) {
                        TvStripSeeAllCard(
                            label = card.label,
                            isFocused = focused && atSeeAll,
                            onClick = card.onClick,
                            width = stripHeight * aspect,
                            height = stripHeight,
                            peek = card.peek,
                        )
                    }
                }
                // Room for the last items to slide all the way to the slot.
                item(key = TAIL_KEY) { Spacer(Modifier.fillParentMaxWidth()) }
            }
        }
        Column(
            Modifier
                .padding(top = dimensionResource(DesR.dimen.padding_m))
                .unreadWhen(atSeeAll),
        ) { caption(item) }
    }
}

@Composable
private fun <T : Any> TvSlotCard(
    item: T,
    width: Dp,
    height: Dp,
    ringed: Boolean,
    image: @Composable (T) -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val ring = if (ringed) {
        Modifier.border(
            dimensionResource(TvR.dimen.tv_focus_ring_width),
            MaterialTheme.colorScheme.onSurface,
            shape,
        )
    } else {
        Modifier
    }
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(ring),
    ) {
        Box(Modifier.fillMaxSize().clip(shape)) { image(item) }
    }
}

/**
 * A carousel caption: a title over a quieter line, both one line.
 *
 * @need caption a detail carousel item with a title over a quieter line
 * @group tv
 */
@Composable
fun TvDetailCaption(title: String, subtitle: String?) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Text(
        text = subtitle.orEmpty(),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
