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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.isExpandedPane
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeShapes

/** The cell a spread round action, or its plate, sits in, so a test can compare the loaded row's cells with the skeleton's. */
internal const val DETAIL_ACTION_CELL_TAG = "detail-action-cell"

/** From this many round actions the primary takes a row of its own, so its label keeps the room it needs at a large font. */
private const val STACK_FROM = 2

/**
 * One round, labelled action beside a detail page's primary: a Trailer, a Watchlist, a Report. [icon] is the glyph and [label] the
 * caption under it. [contentDescription] is what a screen reader announces in place of the [label], which a toggle uses to say what a
 * tap does now ("Remove from watchlist"); null announces the [label]. [trailingIcon] marks an action that leaves the app, such as a trailer
 * that opens a video: a small icon at the end of the label, as [LabelledIconButton] draws it, and [trailingContentDescription] says what the
 * mark means ("Opens outside the app"). With neither, the button is as it always was.
 */
@Immutable
class DetailRoundAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val contentDescription: String? = null,
    val trailingIcon: ImageVector? = null,
    val trailingContentDescription: String? = null,
)

/**
 * The actions under a detail page's stats: the [primary], there in every state, then a round [LabelledIconButton] for each of
 * [roundActions]. [primary] is a slot that is handed the modifier that sizes it (the row's remaining width, or the whole width when
 * stacked); the caller applies it to its outermost node and draws whatever button its page needs, so nothing here knows what the
 * primary means. On a phone, from two round actions up the primary takes a row of its own and the round buttons spread across a row
 * under it, so the primary's label keeps its room at a large font; at the expanded width, and with fewer round actions, they sit in one row
 * with the primary taking the rest, the round buttons side by side. With no [primary] the round actions spread alone across one row. Where they
 * spread, each takes an equal share of the row, its button centred in it, with `detail_action_row_spacing` between the shares, so two, three
 * or four buttons fill the row evenly. The row is inset by the screen's content inset (16, 24 or 32dp by width) on both sides. A page adds the
 * reading margin through `modifier.readingMargin()`.
 *
 * @need lay out a detail page's primary action beside its round secondary actions
 * @group detail
 * @replaces a hand-built Row of a primary button and labelled icon buttons
 * @sample DetailActionsSample
 */
@Composable
fun DetailActions(
    primary: (@Composable (Modifier) -> Unit)?,
    roundActions: List<DetailRoundAction>,
    modifier: Modifier = Modifier,
) {
    val gap = dimensionResource(R.dimen.padding_m)
    val cellSpacing = dimensionResource(R.dimen.detail_action_row_spacing)
    val edge = modifier.fillMaxWidth().padding(horizontal = resolvedContentInset())
    if (primary == null) {
        Row(modifier = edge, horizontalArrangement = Arrangement.spacedBy(cellSpacing), verticalAlignment = Alignment.Top) {
            RoundActionCells(roundActions)
        }
    } else if (roundActions.size >= STACK_FROM && !isExpandedPane()) {
        Column(modifier = edge, verticalArrangement = Arrangement.spacedBy(gap)) {
            primary(Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                verticalAlignment = Alignment.Top,
            ) {
                RoundActionCells(roundActions)
            }
        }
    } else {
        Row(modifier = edge, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.Top) {
            primary(Modifier.weight(1f))
            RoundActions(roundActions)
        }
    }
}

/** The round actions side by side at their own width, beside the primary in a single row. */
@Composable
private fun RowScope.RoundActions(actions: List<DetailRoundAction>) {
    actions.forEach { action -> RoundAction(action) }
}

/** The round actions spread across the row: each in an equal-weight cell, its button centred at the top of it. */
@Composable
private fun RowScope.RoundActionCells(actions: List<DetailRoundAction>) {
    actions.forEach { action ->
        Box(Modifier.weight(1f).testTag(DETAIL_ACTION_CELL_TAG), contentAlignment = Alignment.TopCenter) { RoundAction(action) }
    }
}

@Composable
private fun RoundAction(action: DetailRoundAction) {
    LabelledIconButton(
        icon = action.icon,
        label = action.label,
        onClick = action.onClick,
        contentDescription = action.contentDescription,
        trailingIcon = action.trailingIcon,
        trailingContentDescription = action.trailingContentDescription,
    )
}

/**
 * The action row of [DetailActions] while the page loads: a plate where the primary goes, and [buttons] round plates with a caption
 * line under each where the round actions go, laid out as the loaded row is (stacked from two up on a phone), so the row is the loaded
 * row's height. With no [buttons] it is the primary's plate alone, and with [primary] false it is the round plates alone, as [DetailActions] draws them with no primary.
 *
 * @need hold a detail page's action row at its loaded height while the page loads
 * @group detail
 * @replaces a hand-built shimmer plate for a row of actions
 */
@Composable
fun DetailActionsSkeleton(
    modifier: Modifier = Modifier,
    buttons: Int = 0,
    primary: Boolean = true,
) {
    val gap = dimensionResource(R.dimen.padding_m)
    val cellSpacing = dimensionResource(R.dimen.detail_action_row_spacing)
    val plate = Modifier.height(dimensionResource(R.dimen.button_filled_height))
    val edge = modifier.fillMaxWidth().padding(horizontal = resolvedContentInset())
    if (!primary) {
        Row(modifier = edge, horizontalArrangement = Arrangement.spacedBy(cellSpacing), verticalAlignment = Alignment.Top) {
            repeat(
                buttons,
            ) { Box(Modifier.weight(1f).testTag(DETAIL_ACTION_CELL_TAG), contentAlignment = Alignment.TopCenter) { RoundPlate() } }
        }
    } else if (buttons >= STACK_FROM && !isExpandedPane()) {
        Column(modifier = edge, verticalArrangement = Arrangement.spacedBy(gap)) {
            SkeletonPlate(plate.fillMaxWidth())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                verticalAlignment = Alignment.Top,
            ) {
                repeat(
                    buttons,
                ) { Box(Modifier.weight(1f).testTag(DETAIL_ACTION_CELL_TAG), contentAlignment = Alignment.TopCenter) { RoundPlate() } }
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
