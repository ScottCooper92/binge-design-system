package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.HeroItem

/**
 * The hub as the moved tests call it: items already are [TvHubItem]s, so the adapter is the identity. A fixture only; no test
 * asserts anything about the adapter. A row click defaults to reporting the title's number, as the hub did before it was generic.
 */
@Composable
fun TvExpandingRowsHub(
    rows: List<TvHubRow<TvHubItem>>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onRowVisible: (key: String) -> Unit = {},
    onItemTapped: (key: String, index: Int) -> Unit = { _, _ -> },
    titleExtras: suspend (TvHubItem) -> TvTitleExtras? = { null },
    heroExtras: suspend (HeroItem) -> TvTitleExtras? = { null },
    rowPages: @Composable (rowKey: String) -> TvRowPages<TvHubItem>? = { null },
    heroItems: List<HeroItem> = emptyList(),
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    loading: Boolean = false,
    takeFocusOnEntry: Boolean = false,
    onRowItemClick: (rowKey: String, item: TvHubItem) -> Unit = { _, item -> onItemClick(item.key.removePrefix("movie_").toInt()) },
    overlayOpen: Boolean = false,
    initialFocusedKey: String? = null,
) {
    TvExpandingRowsHub(
        rows = rows,
        item = { it },
        onItemClick = onItemClick,
        onRowItemClick = onRowItemClick,
        modifier = modifier,
        onRowVisible = onRowVisible,
        onItemTapped = onItemTapped,
        titleExtras = titleExtras,
        heroExtras = heroExtras,
        rowPages = rowPages,
        heroItems = heroItems,
        onVisitEnded = onVisitEnded,
        loading = loading,
        takeFocusOnEntry = takeFocusOnEntry,
        overlayOpen = overlayOpen,
        initialFocusedKey = initialFocusedKey,
    )
}
