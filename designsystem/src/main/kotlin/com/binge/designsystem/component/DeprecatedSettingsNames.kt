package com.binge.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/**
 * The names [ListItem], [ItemGroup] and [ListItemDestination] had before the rename, kept so a
 * consumer that has not migrated still compiles. Delete this file once Binge has moved its call sites.
 */
@Deprecated("Renamed", ReplaceWith("ListItem", "com.binge.designsystem.component.ListItem"))
typealias SettingsRow = ListItem

@Deprecated(
    "Renamed",
    ReplaceWith("ListItemDestination", "com.binge.designsystem.component.ListItemDestination"),
)
typealias SettingsRowDestination = ListItemDestination

@Deprecated(
    "Renamed",
    ReplaceWith(
        "ItemGroup(title, rows, modifier, rowVerticalPadding, titleSpacing, belowRows)",
        "com.binge.designsystem.component.ItemGroup",
    ),
)
@Composable
fun SettingsGroup(
    title: String?,
    rows: List<ListItem>,
    modifier: Modifier = Modifier,
    rowVerticalPadding: Dp = dimensionResource(R.dimen.item_group_row_padding_v),
    titleSpacing: Dp = dimensionResource(R.dimen.padding_s),
    belowRows: (@Composable ColumnScope.() -> Unit)? = null,
) = ItemGroup(title, rows, modifier, rowVerticalPadding, titleSpacing, belowRows)
