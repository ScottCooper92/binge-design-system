package com.binge.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R
import com.binge.designsystem.badgeCountLabel
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.tonalContainer

/**
 * A titled group of list items on one clipped surface, dividers between them. The title is
 * marked `heading()` so TalkBack can jump group to group rather than walking every row — a
 * screen of grouped items runs to a dozen groups.
 *
 * [rowVerticalPadding] is the breathing room above and below each row's content. It defaults to the
 * shared token so every group stays alike; a caller that wants roomier rows — a sheet whose
 * rows are its main content — passes its own. [titleSpacing] is the gap between the title and the
 * first row. [belowRows] draws inside the same surface after the last row, for a caller whose row
 * expands into more content; it owns its own divider.
 */
@Composable
fun ItemGroup(
    title: String?,
    rows: List<ListItem>,
    modifier: Modifier = Modifier,
    rowVerticalPadding: Dp = dimensionResource(R.dimen.item_group_row_padding_v),
    titleSpacing: Dp = dimensionResource(R.dimen.padding_s),
    belowRows: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // 8dp inside the group renders 24dp from the screen edge, since the group sits
                // inside the screen's 16dp horizontal content padding.
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_s))
                    .padding(bottom = titleSpacing)
                    .semantics { heading() },
            )
        }
        Column(
            modifier = Modifier
                .clip(BingeShapes.Large)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            ItemRows(rows = rows, rowVerticalPadding = rowVerticalPadding)
            belowRows?.invoke(this)
        }
    }
}

/** The rows of a [ItemGroup] with their dividers and no surface, for a card that supplies its own. */
@Composable
fun ItemRows(
    rows: List<ListItem>,
    modifier: Modifier = Modifier,
    rowVerticalPadding: Dp = dimensionResource(R.dimen.item_group_row_padding_v),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            ListItemView(row = row, verticalPadding = rowVerticalPadding)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.hairline_thickness),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.item_group_row_padding_h)),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListItemView(
    row: ListItem,
    verticalPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val interactive = row.clickable && !row.loading && !row.disabled
    val external = interactive && row.toggled == null && row.trailingContent == null && row.destination == ListItemDestination.External
    val externalDescription = stringResource(R.string.cd_list_item_external)
    val loadingDescription = stringResource(R.string.cd_list_item_loading)
    val expandedDescription = stringResource(if (row.expanded == true) R.string.cd_group_expanded else R.string.cd_group_collapsed)
    val connectorModifier = row.connector?.let { listItemConnector(it) } ?: Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (row.disabled) DISABLED_ALPHA else 1f)
            .semantics {
                selected = row.selected
                if (row.loading) stateDescription = loadingDescription
                // Appended to the row's own merged node rather than a contentDescription on the trailing
                // icon, so a screen reader announces one node ("Watchlist, Opens in browser, Button")
                // instead of reading the icon as a second stop.
                if (external) stateDescription = externalDescription
                // Deliberately replaces "In progress" on a loading header: the group shows its own loading body.
                if (row.expanded != null) {
                    stateDescription = expandedDescription
                    if (interactive) {
                        if (row.expanded) {
                            collapse {
                                row.onClick()
                                true
                            }
                        } else {
                            expand {
                                row.onClick()
                                true
                            }
                        }
                    }
                }
            }
            // The wash sits outside the click, so the ripple draws over it rather than under it.
            .background(if (row.selected) MaterialTheme.colorScheme.primary.tonalContainer() else Color.Transparent)
            .then(rowActionModifier(row, interactive))
            .then(connectorModifier)
            .padding(
                horizontal = dimensionResource(R.dimen.item_group_row_padding_h),
                vertical = verticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (row.connector != null) {
            // Just past the connector's curve: half the parent's icon to its axis, the curve, then a small gap.
            Spacer(
                Modifier.width(
                    dimensionResource(R.dimen.item_group_icon_size) / 2 +
                        dimensionResource(R.dimen.item_group_connector_radius) +
                        dimensionResource(R.dimen.padding_s),
                ),
            )
        }
        if (row.leadingContent != null) {
            row.leadingContent.invoke()
        } else {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.item_group_icon_size))
                    .clip(BingeShapes.MoreCard)
                    .background(row.iconTint?.tonalContainer() ?: MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = row.iconPainter?.invoke() ?: rememberVectorPainter(row.icon),
                    contentDescription = null,
                    tint = row.iconTint ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.item_group_icon_glyph)),
                )
            }
        }
        Spacer(Modifier.width(dimensionResource(R.dimen.account_card_spacing)))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (row.destination == ListItemDestination.Action && !row.disabled) {
                    row.iconTint ?: MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (!row.detail.isNullOrBlank()) {
                Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
                Text(
                    text = row.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = row.detailColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (row.badgeAlert) {
                Badge { Text(stringResource(R.string.badge_alert)) }
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            } else if (row.badgeCount != null && row.badgeCount > 0) {
                CountBadge(count = row.badgeCount, tint = row.badgeTint)
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            }
            when {
                row.loading -> BingeLoadingIndicator(
                    modifier = Modifier.size(dimensionResource(R.dimen.item_group_loading_size)),
                )
                row.trailingContent != null -> row.trailingContent.invoke()
                row.toggled != null -> Switch(
                    checked = row.toggled,
                    onCheckedChange = null,
                    enabled = !row.disabled,
                    colors = bingeSwitchColors(),
                )
                row.expanded != null -> {
                    Icon(
                        imageVector = if (row.expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                external -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                interactive && row.destination != ListItemDestination.Action -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * The row's tap. A switch row is one `Role.Switch` toggleable so the state reaches a screen reader; every
 * other row keeps the `Role.Button` click and its long-press.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun rowActionModifier(row: ListItem, interactive: Boolean): Modifier =
    if (row.toggled != null) {
        Modifier.toggleable(
            value = row.toggled,
            enabled = interactive,
            role = Role.Switch,
            onValueChange = { row.onClick() },
        )
    } else {
        Modifier.combinedClickable(
            enabled = interactive,
            role = Role.Button,
            onClick = row.onClick,
            onLongClickLabel = row.onLongClickLabel,
            onLongClick = row.onLongClick,
        )
    }

/** The row's count: a tonal pill in [tint] when given (matching the row's sentiment), else the default badge. */
@Composable
private fun CountBadge(count: Int, tint: Color?) {
    if (tint == null) {
        Badge { Text(badgeCountLabel(count)) }
    } else {
        Text(
            text = badgeCountLabel(count),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            modifier = Modifier
                .clip(BingeShapes.Pill)
                .background(tint.tonalContainer())
                .padding(
                    horizontal = dimensionResource(R.dimen.padding_s),
                    vertical = dimensionResource(R.dimen.padding_xxs),
                ),
        )
    }
}

/**
 * Draws [connector] across the row's full height, past its vertical padding and over the divider
 * below, so the line stays continuous down a run of rows. It sits on the parent icon's axis and
 * mirrors in right-to-left layouts.
 */
@Composable
private fun listItemConnector(connector: ListItemConnector): Modifier {
    val color = MaterialTheme.colorScheme.outline
    val strokeWidth = with(LocalDensity.current) { dimensionResource(R.dimen.hairline_thickness).toPx() }
    val axis = with(LocalDensity.current) {
        (dimensionResource(R.dimen.item_group_row_padding_h) + dimensionResource(R.dimen.item_group_icon_size) / 2).toPx()
    }
    // The curve ends where it turns; the child's icon starts a small gap after it.
    val reach = with(LocalDensity.current) { dimensionResource(R.dimen.item_group_connector_radius).toPx() }
    val radius = with(LocalDensity.current) { dimensionResource(R.dimen.item_group_connector_radius).toPx() }
    return Modifier.drawBehind {
        val rtl = layoutDirection == LayoutDirection.Rtl

        fun x(value: Float) = if (rtl) size.width - value else value
        val midY = size.height / 2
        val curve = Path().apply {
            moveTo(x(axis), 0f)
            lineTo(x(axis), midY - radius)
            quadraticTo(x(axis), midY, x(axis + radius), midY)
            lineTo(x(axis + reach), midY)
        }
        drawPath(curve, color, style = Stroke(width = strokeWidth))
        if (connector == ListItemConnector.Continue) {
            // The hairline divider sits just below the row; run the line through it.
            drawLine(color, Offset(x(axis), 0f), Offset(x(axis), size.height + strokeWidth), strokeWidth)
        }
    }
}
