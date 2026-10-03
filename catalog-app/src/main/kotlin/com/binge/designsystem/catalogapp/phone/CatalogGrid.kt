package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.registry.CatalogComponent
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.R as DesR

/**
 * The catalog as a grid of [R.integer.catalog_grid_columns] columns, whatever the width: a phone, and
 * the list pane beside a component on a tablet or an unfolded foldable, which is phone width itself.
 * One card per component, never per variant: the variants are chosen on the component's page.
 * Components with a live demo come first, under their own heading, since behaviour is what the app is
 * for.
 */
@Composable
fun CatalogGrid(
    components: List<CatalogComponent>,
    onSelect: (CatalogComponent) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    selectedGroup: String? = null,
) {
    val (withDemos, without) = remember(components) { components.partition { it.hasDemo } }
    val sections =
        listOf(
            stringResource(R.string.catalog_section_demos) to withDemos,
            stringResource(R.string.catalog_section_components) to without,
        ).filter { (_, members) -> members.isNotEmpty() }
    val gap = dimensionResource(R.dimen.catalog_grid_gap)
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(integerResource(R.integer.catalog_grid_columns)),
        modifier = modifier,
        // Edge to edge: the cards scroll under the navigation bar, and the last row clears it.
        contentPadding = edgeToEdgeContentPadding(dimensionResource(R.dimen.catalog_padding)),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        sections.forEach { (heading, members) ->
            item(key = "section-$heading", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = heading, horizontalPadding = dimensionResource(DesR.dimen.zero))
            }
            items(members, key = { it.group }) { component ->
                CatalogCell(component = component, onClick = { onSelect(component) }, selected = component.group == selectedGroup)
            }
        }
    }
}
