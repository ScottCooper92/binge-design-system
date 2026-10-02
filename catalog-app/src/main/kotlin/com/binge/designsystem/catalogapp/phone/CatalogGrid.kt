package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.byGroup

/**
 * The catalog as an adaptive grid: as many columns as fit [R.dimen.catalog_grid_cell_min_width], so a
 * phone gets two, a foldable or tablet more, and a rotation re-flows with no per-device layout. Each
 * group has a full-width heading, and demos come first under their own.
 */
@Composable
fun CatalogGrid(
    entries: List<CatalogEntry>,
    onSelect: (CatalogEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(entries) { entries.byGroup() }
    val gap = dimensionResource(R.dimen.catalog_grid_gap)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(dimensionResource(R.dimen.catalog_grid_cell_min_width)),
        modifier = modifier,
        contentPadding = PaddingValues(dimensionResource(R.dimen.catalog_padding)),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        groups.forEach { (group, members) ->
            item(key = "group-$group", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = group,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.catalog_padding_small)),
                )
            }
            items(members, key = { it.id }) { entry ->
                CatalogCell(entry = entry, onClick = { onSelect(entry) })
            }
        }
    }
}
