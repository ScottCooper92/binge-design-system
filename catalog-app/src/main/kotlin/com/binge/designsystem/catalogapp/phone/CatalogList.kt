package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.byGroup
import com.binge.designsystem.catalogapp.registry.matching

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogList(
    entries: List<CatalogEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (CatalogEntry) -> Unit,
) {
    val matches = remember(entries, query) { entries.matching(query) }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchField(query = query, onQueryChange = onQueryChange, count = matches.size)
            if (matches.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.catalog_empty, query), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                SampleList(entries = matches, onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    count: Int,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.catalog_padding)),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.catalog_search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isEmpty()) {
                Text(
                    stringResource(R.string.catalog_sample_count, count),
                    modifier = Modifier.padding(end = dimensionResource(R.dimen.catalog_padding)),
                )
            } else {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.catalog_search_clear))
                }
            }
        },
    )
}

@Composable
private fun SampleList(entries: List<CatalogEntry>, onSelect: (CatalogEntry) -> Unit) {
    val groups = remember(entries) { entries.byGroup() }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_item_gap))) {
        groups.forEach { (group, members) ->
            item(key = "group-$group") {
                Text(
                    text = group,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.catalog_padding),
                        vertical = dimensionResource(R.dimen.catalog_padding_small),
                    ),
                )
            }
            items(members, key = { it.id }) { entry ->
                ListItem(
                    headlineContent = { Text(entry.name) },
                    supportingContent = entry.description.takeIf { it.isNotBlank() }?.let { { Text(it) } },
                    modifier = Modifier.clickable { onSelect(entry) },
                )
            }
            item(key = "divider-$group") { HorizontalDivider() }
        }
    }
}
