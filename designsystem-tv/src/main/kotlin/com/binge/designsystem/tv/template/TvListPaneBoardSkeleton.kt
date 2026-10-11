package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvSkeletonBlock
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val GROUP_TITLE_WIDTH_FRACTION = 0.35f
private const val ROW_LABEL_WIDTH_FRACTION = 0.7f
private const val PANE_TITLE_WIDTH_FRACTION = 0.6f

/**
 * The loading state of a [TvListPaneBoard]: the board's own frame under [title], a column of row placeholders and a
 * pane with a title line and [options] option plates. Nothing in it is focusable.
 *
 * It is built on the same two-pane page as the board, so the title band, insets, split and divider cannot drift. The
 * column reserves what the board puts above its first row (the focus-ring bleed and the first group's title), so the
 * first of [rows] sits where the board's first row will and carries the same layout anchor. [hosting] is the board's.
 * [description] names the state for a screen reader.
 */
@Composable
fun TvListPaneBoardSkeleton(
    title: String?,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    rows: Int = 9,
    options: Int = 2,
    description: String? = null,
) {
    TvTwoPanePage(
        title = title,
        modifier = if (description == null) modifier else modifier.semantics { contentDescription = description },
        split = TvTwoPaneSplit.ListDetail,
        style = TvTwoPaneStyle.Board,
        hosting = hosting,
        actionScrolls = false,
        copy = { ListSkeleton(rows = rows) },
        action = { PaneSkeleton(options = options) },
    )
}

/** The column, with the board's vertical padding and row gap, so each placeholder sits on its row. */
@Composable
private fun ListSkeleton(rows: Int) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(vertical = dimensionResource(TvR.dimen.tv_focus_ring_bleed)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_list_pane_row_gap)),
    ) {
        GroupTitleSkeleton()
        repeat(rows) { index ->
            // A label bar in the row's full footprint rather than a plate filling it: the board's rows are transparent
            // until focused, so a filled plate would read as a dense block.
            Box(
                modifier =
                    Modifier
                        .tvListPaneFirstRowAnchor(index == 0)
                        .fillMaxWidth()
                        .height(dimensionResource(TvR.dimen.tv_list_item_height)),
                contentAlignment = Alignment.CenterStart,
            ) {
                TvSkeletonBlock(
                    Modifier
                        .padding(start = dimensionResource(TvR.dimen.tv_list_pane_row_padding_horizontal))
                        .fillMaxWidth(ROW_LABEL_WIDTH_FRACTION)
                        .height(dimensionResource(TvR.dimen.tv_list_pane_skeleton_line_height)),
                )
            }
        }
    }
}

/**
 * A group title's stand-in, with the title's own paddings. The empty line of the title's style holds the height the
 * title will take, so the rows under it do not move when the board arrives.
 */
@Composable
private fun GroupTitleSkeleton() {
    Box(
        modifier =
            Modifier.padding(
                top = dimensionResource(TvR.dimen.tv_list_pane_group_gap),
                bottom = dimensionResource(TvR.dimen.tv_list_pane_header_bottom_gap),
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = "", style = MaterialTheme.typography.labelMedium)
        TvSkeletonBlock(
            Modifier
                .padding(start = dimensionResource(TvR.dimen.tv_list_pane_row_padding_horizontal))
                .fillMaxWidth(GROUP_TITLE_WIDTH_FRACTION)
                .height(dimensionResource(TvR.dimen.tv_list_pane_group_header_height)),
        )
    }
}

/** The pane: a title line over option plates at the board's option width and shape. */
@Composable
private fun PaneSkeleton(options: Int) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(TvR.dimen.tv_focus_ring_bleed)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TvSkeletonBlock(
            Modifier
                .fillMaxWidth(PANE_TITLE_WIDTH_FRACTION)
                .height(dimensionResource(TvR.dimen.tv_list_pane_skeleton_line_height)),
        )
        repeat(options) {
            TvSkeletonBlock(
                modifier =
                    Modifier
                        .width(dimensionResource(TvR.dimen.tv_list_pane_option_width))
                        .height(dimensionResource(TvR.dimen.tv_list_item_height)),
                shape = BingeShapes.TvListItem,
            )
        }
    }
}
