package com.binge.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R

/**
 * What an open [BingeExpandableGroup] shows beneath its header: its rows, or the loading and failed bodies of rows
 * read when the group opens.
 */
sealed interface BingeGroupContent {
    /** The rows are being read: a loading indicator. */
    data object Loading : BingeGroupContent

    /** The read failed: [message], and a [retryLabel] button that calls [onRetry]. */
    data class Failed(
        val message: String,
        val retryLabel: String,
        val onRetry: () -> Unit,
    ) : BingeGroupContent

    /**
     * The rows, joined to the header as its children. An empty list draws an open group with nothing under its header;
     * a read that returns nothing should be [Failed], or the caller's own copy.
     */
    data class Ready(
        val rows: List<ListItem>,
    ) : BingeGroupContent
}

/**
 * A group whose last row, [header], opens more rows beneath it in the same surface. Build the header with
 * [bingeExpandableItem]; its [ListItem.expanded] decides whether the group is open.
 *
 * A group whose rows are read from somewhere starts that read when it opens, from the header's `onExpandedChange`,
 * and passes [BingeGroupContent.Loading] until they arrive. A failure shows inline with its retry, so each row's own
 * sheet then opens with its data already there.
 *
 * [rowsAbove] are ordinary rows above the header, for a group the expandable entry is only part of.
 */
@Composable
fun BingeExpandableGroup(
    title: String?,
    header: ListItem,
    content: BingeGroupContent,
    modifier: Modifier = Modifier,
    rowsAbove: List<ListItem> = emptyList(),
) {
    ItemGroup(
        title = title,
        rows = rowsAbove + header,
        modifier = modifier,
        belowRows = {
            AnimatedVisibility(
                visible = header.expanded == true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(Modifier.fillMaxWidth().animateContentSize()) {
                    HorizontalDivider(
                        thickness = dimensionResource(R.dimen.hairline_thickness),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.item_group_row_padding_h)),
                    )
                    GroupContentBody(content)
                }
            }
        },
    )
}

/**
 * The header row of a [BingeExpandableGroup]: [title], a chevron, and [collapsedDetail] beneath the title while the
 * group is shut — a hint at what opening it offers. A tap calls [onExpandedChange] with the state it asks for.
 */
fun bingeExpandableItem(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    collapsedDetail: String? = null,
    disabled: Boolean = false,
): ListItem =
    ListItem(
        icon = icon,
        label = title,
        detail = collapsedDetail.takeUnless { expanded },
        expanded = expanded,
        disabled = disabled,
        onClick = { onExpandedChange(!expanded) },
    )

@Composable
private fun GroupContentBody(content: BingeGroupContent) {
    when (content) {
        BingeGroupContent.Loading ->
            BingeLoadingIndicator(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(dimensionResource(R.dimen.padding_m)),
            )
        is BingeGroupContent.Failed ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                Text(
                    text = content.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                BingeTextButton(label = content.retryLabel, onClick = content.onRetry)
            }
        is BingeGroupContent.Ready -> ItemRows(rows = content.rows.joinedToHeader())
    }
}

/** [this] as the header's children: each continues the connector line, and the last one ends it. */
internal fun List<ListItem>.joinedToHeader(): List<ListItem> =
    mapIndexed { index, row -> row.copy(connector = if (index == lastIndex) ListItemConnector.End else ListItemConnector.Continue) }
