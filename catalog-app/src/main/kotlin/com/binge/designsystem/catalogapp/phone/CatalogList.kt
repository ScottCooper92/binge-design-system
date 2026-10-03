package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.components
import com.binge.designsystem.catalogapp.registry.matching
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.component.HintCard
import com.binge.designsystem.paneSideInsets

/**
 * The search field over the grid. A query narrows each component to the variants it matches, and
 * selecting a component opens the first of those, so a search for a variant lands on it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogList(
    entries: List<CatalogEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (CatalogEntry) -> Unit,
    gridState: LazyGridState = rememberLazyGridState(),
    selectedGroup: String? = null,
) {
    val matches = remember(entries, query) { entries.matching(query).components() }
    // Edge to edge: no Scaffold padding. The search field takes the status bar's inset and the grid
    // pads its own content, so cards scroll under the navigation bar.
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            SearchField(query = query, onQueryChange = onQueryChange, count = matches.size)
            if (matches.isEmpty()) {
                HintCard(
                    text = stringResource(R.string.catalog_empty, query),
                    icon = Icons.Filled.SearchOff,
                    actionLabel = stringResource(R.string.catalog_search_clear),
                    onAction = { onQueryChange("") },
                    modifier = Modifier.padding(dimensionResource(R.dimen.catalog_padding)),
                )
            } else {
                CatalogGrid(
                    components = matches,
                    onSelect = { onSelect(it.variants.first()) },
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    selectedGroup = selectedGroup,
                )
            }
        }
    }
}

/**
 * The design system's own search field, so the catalog uses the component it lists; it brings its own
 * clear button. The count sits beneath it, as the field has no slot for one.
 */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    count: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .windowInsetsPadding(paneSideInsets())
            .padding(dimensionResource(R.dimen.catalog_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_padding_small)),
    ) {
        BingeSearchField(
            query = query,
            onQueryChange = onQueryChange,
            onClear = { onQueryChange("") },
            placeholder = stringResource(R.string.catalog_search_hint),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = pluralStringResource(R.plurals.catalog_component_count, count, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.catalog_padding_small)),
        )
    }
}
