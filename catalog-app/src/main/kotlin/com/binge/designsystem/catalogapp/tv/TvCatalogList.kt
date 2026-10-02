package com.binge.designsystem.catalogapp.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.byGroup
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvSectionTitle
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.tv.R as TvR

/**
 * Every TV sample, grouped, reachable from the D-pad alone. Each row's requester is kept by id so the
 * shell can return focus to the row that opened a sample; the first row also takes first arrival.
 */
@Composable
fun TvCatalogList(
    entries: List<CatalogEntry>,
    rowRequesters: TvRowRequesters,
    arrival: TvArrivalFocus,
    onOpen: (CatalogEntry) -> Unit,
) {
    val groups = remember(entries) { entries.byGroup() }
    val firstId = entries.firstOrNull()?.id
    Column(Modifier.fillMaxSize()) {
        TvSectionTitle(
            text = stringResource(R.string.tv_catalog_title),
            modifier = Modifier.padding(top = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        )
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                vertical = dimensionResource(R.dimen.catalog_padding),
            ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_item_gap)),
        ) {
            groups.forEach { (group, members) ->
                item(key = "group-$group") {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.catalog_padding_small)),
                    )
                }
                items(members, key = { it.id }) { entry ->
                    var focused by remember { mutableStateOf(false) }
                    val requester = rowRequesters[entry.id]
                    TvSampleRow(
                        entry = entry,
                        isFocused = focused,
                        modifier = Modifier
                            .focusRequester(requester)
                            .then(if (entry.id == firstId) Modifier.tvArrivalTarget(arrival) else Modifier)
                            .tvClickable(onFocusChanged = { focused = it }, onClick = { onOpen(entry) }),
                    )
                }
            }
        }
    }
}

/** One sample's row. Focus is a parameter, so the focused look is a plain function of [isFocused]. */
@Composable
fun TvSampleRow(
    entry: CatalogEntry,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .tvFocusFill(isFocused = isFocused, shape = BingeShapes.Pill)
            .padding(
                horizontal = dimensionResource(R.dimen.catalog_tv_row_padding_horizontal),
                vertical = dimensionResource(R.dimen.catalog_tv_row_padding_vertical),
            ),
    ) {
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleMedium,
            color = tvFocusContentColor(isFocused, MaterialTheme.colorScheme.onSurface),
        )
        if (entry.description.isNotBlank()) {
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = tvFocusContentColor(isFocused, MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
    }
}
