@file:CatalogGroup("Text entry")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.preview.ScreenshotTheme

private const val SEARCH_PLACEHOLDER = "Search services"

/**
 * Public samples for [BingeSearchField] — the pill search input, in the catalog's "Text entry" group. See the
 * convention KDoc on [MediaCardRatedSample].
 *
 * Empty shows the placeholder and no trailing affordance; with-query shows the clear button — the two
 * states that branch the field's trailing layout.
 */
@Composable
fun SearchFieldEmptySample() {
    var query by remember { mutableStateOf("") }
    ScreenshotTheme {
        BingeSearchField(
            query = query,
            onQueryChange = { query = it },
            onClear = { query = "" },
            placeholder = SEARCH_PLACEHOLDER,
        )
    }
}

/** Populated field — the trailing clear button appears once the query is non-empty. */
@Composable
fun SearchFieldWithQuerySample() {
    var query by remember { mutableStateOf("Netflix") }
    ScreenshotTheme {
        BingeSearchField(
            query = query,
            onQueryChange = { query = it },
            onClear = { query = "" },
            placeholder = SEARCH_PLACEHOLDER,
        )
    }
}
