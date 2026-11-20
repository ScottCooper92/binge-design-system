package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.binge.designsystem.tv.template.TvHubRow

/**
 * The rows to show, holding the list still under a user who has reached the rows. Once focus is on a row, a row that was not shown at
 * the last composition and would land above it (a "Continue watching" answering late) is held out, and a pending row that gives up
 * keeps its empty slot rather than collapsing, so nothing below it moves. Both last until the hub leaves composition, the next visit.
 * Whether focus is on a row is read from where [TvExpandingHubFocus.savedKey] is now: the index goes stale as rows come and go.
 */
@Composable
internal fun rememberTvExpandingLateRowGuard(rows: List<TvHubRow<TvHubItem>>, focus: TvExpandingHubFocus): List<TvHubRow<TvHubItem>> {
    val latch = remember { LateRowLatch() }
    val shownBefore = latch.shown
    val focusedAt = rows.indexOfFirst { it.key == focus.savedKey }
    val onARow = focus.focusedRowIndex > 0
    val late =
        if (shownBefore != null && onARow && focusedAt >= 0) {
            rows.take(focusedAt).map { it.key }.filterTo(HashSet()) { key -> shownBefore.none { it.key == key } }
        } else {
            emptySet()
        }
    val heldOut = latch.heldOut + late
    val incomingKeys = rows.mapTo(HashSet()) { it.key }
    val newlyKept =
        if (shownBefore != null && onARow) {
            shownBefore.filter { it.items.isEmpty() && it.isLoading && it.key !in incomingKeys }
        } else {
            emptyList()
        }
    val kept = latch.kept + newlyKept.associateBy { it.key }
    val shown = keepSlots(rows.filterNot { it.key in heldOut }, kept, shownBefore.orEmpty())
    SideEffect {
        latch.heldOut = heldOut
        latch.kept = kept
        latch.shown = shown
    }
    return shown
}

/**
 * [rows] with every [kept] slot drawn as the empty pending row it was, whatever has arrived for it since: where it is still listed, in
 * place; where it has gone, back before the row that followed it last time ([before]), so the rows under it stay where they were.
 */
private fun keepSlots(
    rows: List<TvHubRow<TvHubItem>>,
    kept: Map<String, TvHubRow<TvHubItem>>,
    before: List<TvHubRow<TvHubItem>>,
): List<TvHubRow<TvHubItem>> {
    if (kept.isEmpty()) return rows
    val result = rows.map { row -> kept[row.key] ?: row }.toMutableList()
    kept.values.filter { slot -> result.none { it.key == slot.key } }.forEach { slot ->
        val after = before.dropWhile { it.key != slot.key }.drop(1).map { it.key }
        val at = result.indexOfFirst { it.key in after }.takeIf { it >= 0 } ?: result.size
        result.add(at, slot)
    }
    return result
}

/** What the guard showed at the last composition, the keys it holds out and the slots it keeps; plain fields, read to spot changes. */
private class LateRowLatch {
    var shown: List<TvHubRow<TvHubItem>>? = null
    var heldOut: Set<String> = emptySet()
    var kept: Map<String, TvHubRow<TvHubItem>> = emptyMap()
}
