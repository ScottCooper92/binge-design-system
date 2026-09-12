package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.preview.ScreenshotTheme

private const val SEARCH_PLACEHOLDER = "Search services"

/**
 * Public samples for [BingeSearchField] — the pill search input (group `"Inputs"`). See the
 * convention KDoc on [MediaCardRatedSample].
 *
 * Empty shows the placeholder and no trailing affordance; with-query shows the clear button — the two
 * states that branch the field's trailing layout.
 */
@Composable
fun SearchFieldEmptySample() {
    ScreenshotTheme {
        BingeSearchField(
            query = "",
            onQueryChange = {},
            onClear = {},
            placeholder = SEARCH_PLACEHOLDER,
        )
    }
}

/** Populated field — the trailing clear button appears once the query is non-empty. */
@Composable
fun SearchFieldWithQuerySample() {
    ScreenshotTheme {
        BingeSearchField(
            query = "Netflix",
            onQueryChange = {},
            onClear = {},
            placeholder = SEARCH_PLACEHOLDER,
        )
    }
}
