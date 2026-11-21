package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.component.TvPosterGrid

/** The search results' poster grid: [cells] in the shared grid, or plates in the same cells while [loading]. */
@Composable
internal fun TvSearchGrid(
    cells: List<TvSearchCell>,
    loading: Boolean,
    entry: FocusRequester,
    entryKey: String?,
    listKey: String,
    seededKey: String?,
    onCellFocused: (index: Int) -> Unit,
    onEntryPlaced: () -> Unit = {},
) {
    val posters = remember(cells) { cells.map { TvPosterCell(imageUrl = it.imageUrl, title = it.title, onClick = it.onClick) } }
    val entryIndex = cells.indexOfFirst { it.key == entryKey }.coerceAtLeast(0)
    TvPosterGrid(
        count = cells.size,
        posterAt = posters::getOrNull,
        loading = loading,
        entry = entry,
        // The entry cell is the one last focused, else the first, so → from the left column always has somewhere to land.
        entryIndex = entryIndex,
        // A fresh scroll per set of cells: results arriving into the recommendations' state kept its offset, landing part-scrolled.
        listKey = listKey,
        onCellFocused = { if (it in cells.indices) onCellFocused(it) },
        keyOf = { cells[it].key },
        seededIndex = cells.indexOfFirst { it.key == seededKey }.takeIf { it >= 0 },
        cellModifier = { index ->
            val isEntry = index == entryIndex
            Modifier.tvSearchFirstCellAnchor(index).then(if (isEntry) Modifier.onGloballyPositioned { onEntryPlaced() } else Modifier)
        },
        stablePlates = true,
    )
}
