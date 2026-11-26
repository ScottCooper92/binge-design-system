@file:CatalogGroup("Text entry")

package com.binge.designsystem.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.component.BingeSearchFieldButton
import com.binge.designsystem.component.BingeSearchFieldHeader
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

/** Transparent container: the pill drops its fill, for a field sitting on a surface that already supplies one. */
@Composable
fun SearchFieldTransparentContainerSample() {
    var query by remember { mutableStateOf("") }
    ScreenshotTheme {
        BingeSearchField(
            query = query,
            onQueryChange = { query = it },
            onClear = { query = "" },
            placeholder = SEARCH_PLACEHOLDER,
            containerColor = Color.Transparent,
        )
    }
}

/** Outlined: a hairline edge round the pill, for a field in a sheet whose own colour is too close to the pill's fill to show its edge. */
@Composable
fun SearchFieldOutlinedSample() {
    var query by remember { mutableStateOf("") }
    ScreenshotTheme {
        BingeSearchField(
            query = query,
            onQueryChange = { query = it },
            onClear = { query = "" },
            placeholder = SEARCH_PLACEHOLDER,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            outlined = true,
        )
    }
}

/**
 * Read-only form: the same pill as the field above with its placeholder, one tap target and no input. It sits in the header wrapper the Search screen
 * gives its field. Tapping it counts the taps below, which stands in for opening the search page.
 */
@Composable
fun SearchFieldButtonSample() {
    var taps by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        BingeSearchFieldHeader {
            BingeSearchFieldButton(
                placeholder = if (taps == 0) SEARCH_PLACEHOLDER else "$SEARCH_PLACEHOLDER ($taps taps)",
                onClick = { taps++ },
            )
        }
    }
}
