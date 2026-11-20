package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeShapes

/** From this many round actions the primary takes a row of its own, so its label keeps the room it needs at a large font. */
private const val STACK_FROM = 2

/**
 * One round, labelled action beside a detail page's primary: a Trailer, a Watchlist, a Report. [icon] is the glyph and [label] the
 * caption under it. [contentDescription] is what a screen reader announces in place of the [label], which a toggle uses to say what a
 * tap does now ("Remove from watchlist"); null announces the [label].
 */
@Immutable
class DetailRoundAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val contentDescription: String? = null,
)

/**
 * The actions under a detail page's stats: the [primary], there in every state, then a round [LabelledIconButton] for each of
 * [roundActions]. [primary] is a slot that is handed the modifier that sizes it (the row's remaining width, or the whole width when
 * stacked); the caller applies it to its outermost node and draws whatever button its page needs, so nothing here knows what the
 * primary means. On a phone, from two round actions up the primary takes a row of its own and the round buttons sit centred in a row
 * under it, so the primary's label keeps its room at a large font; at the expanded width, and with fewer round actions, they sit in one row
 * with the primary taking the rest. The row is inset by the screen's content inset (16, 24 or 32dp by width) on both sides, and the gaps between its buttons are `padding_m`. A page adds the reading margin through `modifier.readingMargin()`.
 *
 * @need lay out a detail page's primary action beside its round secondary actions
 * @group detail
 * @replaces a hand-built Row of a primary button and labelled icon buttons
 * @sample DetailActionsSample
 */
@Composable
fun DetailActions(
    primary: @Composable (Modifier) -> Unit,
    roundActions: List<DetailRoundAction>,
    modifier: Modifier = Modifier,
) {
    val gap = dimensionResource(R.dimen.padding_m)
    val edge = modifier.fillMaxWidth().padding(horizontal = resolvedContentInset())
    if (roundActions.size >= STACK_FROM && !isExpandedLayout()) {
        Column(modifier = edge, verticalArrangement = Arrangement.spacedBy(gap)) {
            primary(Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top,
            ) {
                RoundActions(roundActions)
            }
        }
    } else {
        Row(modifier = edge, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.Top) {
            primary(Modifier.weight(1f))
            RoundActions(roundActions)
        }
    }
}

@Composable
private fun RowScope.RoundActions(actions: List<DetailRoundAction>) {
    actions.forEach { action ->
        LabelledIconButton(
            icon = action.icon,
            label = action.label,
            onClick = action.onClick,
            contentDescription = action.contentDescription,
        )
    }
}

/**
 * The action row of [DetailActions] while the page loads: a plate where the primary goes, and [buttons] round plates with a caption
 * line under each where the round actions go, laid out as the loaded row is (stacked from two up on a phone), so the row is the loaded
 * row's height. With no [buttons] it is the primary's plate alone.
 *
 * @need hold a detail page's action row at its loaded height while the page loads
 * @group detail
 * @replaces a hand-built shimmer plate for a row of actions
 */
@Composable
fun DetailActionsSkeleton(modifier: Modifier = Modifier, buttons: Int = 0) {
    val gap = dimensionResource(R.dimen.padding_m)
    val plate = Modifier.height(dimensionResource(R.dimen.button_filled_height))
    val edge = modifier.fillMaxWidth().padding(horizontal = resolvedContentInset())
    if (buttons >= STACK_FROM && !isExpandedLayout()) {
        Column(modifier = edge, verticalArrangement = Arrangement.spacedBy(gap)) {
            SkeletonPlate(plate.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top,
            ) {
                repeat(buttons) { RoundPlate() }
            }
        }
    } else {
        Row(modifier = edge, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.Top) {
            SkeletonPlate(plate.weight(1f))
            repeat(buttons) { RoundPlate() }
        }
    }
}

/** A round button's stand-in: the circle's plate and a caption line under it, in the button's own width. */
@Composable
private fun RoundPlate() {
    Column(Modifier.width(dimensionResource(R.dimen.labelled_icon_button_width)), horizontalAlignment = Alignment.CenterHorizontally) {
        SkeletonPlate(Modifier.size(dimensionResource(R.dimen.button_tonal_size)), shape = BingeShapes.Pill)
        Box(Modifier.padding(top = dimensionResource(R.dimen.padding_xs)).fillMaxWidth()) {
            TextPlate(MaterialTheme.typography.labelMedium, Modifier.fillMaxWidth())
        }
    }
}
