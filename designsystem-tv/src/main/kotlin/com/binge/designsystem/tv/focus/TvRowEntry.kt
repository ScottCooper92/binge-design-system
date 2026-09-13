package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

/**
 * The remembered-entry-cell focus contract shared by the detail and hub rows (`TvCardRow`, `TvMediaRow`), the TV
 * detail/gallery/cast grids and search: returning to a container lands on the cell that last held focus, and a
 * fresh one opens on its first — never a geometric pick, never a trailing see-all tile (outside the item list, so
 * it never carries the requester). This is **the** TV focus-memory contract (#1820); its four invariants, each
 * the fix for a class re-broken per surface, are pinned once in `TvRowEntryContractTest`:
 *
 * 1. The memory rides [rememberSaveable], keyed by a stable cell key — not composition (violated: #1574).
 * 2. It is **separate from the ring key**, which a blur nulls (violated: #1574 collapsed the two).
 * 3. The lazy container is **seeded** to [entryIndex] so the remembered cell is composed before entry arrives,
 *    or the requester binds to nothing (violated: #1610, #1730).
 * 4. Staleness is resolved against the current data — a too-large remembered index coerces in (violated: #1757).
 *
 * Three surfaces in the host app keep their **own** memory rather than this seam, and correctly so: a paged
 * poster grid needs `Items` staleness plus a backdrop cell; an immersive hub steers entry across many rows at
 * once; a screen returning from a child by item *type* rather than by index cannot use an index. Each holds the
 * four invariants above in its own type, so its own memory test is the same contract by another shape.
 *
 * **The memory must stay separate from whatever draws the ring.** A ring key is nulled on blur so a container
 * that lost focus keeps none lit — and that blur is exactly the ↑ away that starts the round trip this serves;
 * collapse the two and no cell carries the requester when entry needs it. `TvCardRow` and the gallery grid keep
 * a `focusedKey` beside their entry for that reason.
 *
 * The remembered index rides [rememberSaveable], which the enclosing `LazyColumn` item's `SaveableStateProvider`
 * preserves across the scroll-out disposal that wipes a plain `remember` (the failure `TvMediaRow` first hit). So
 * the memory is **caller-transparent**: a row inside a lazy column restores itself with no id threaded through
 * the screen. A caller that must steer entry across containers — the hub, entered from the nav rail rather than
 * by scrolling — passes [rememberTvRowEntry]'s `overrideIndex` instead.
 *
 * Three lines of wiring, all required (a requester on a not-yet-laid-out node throws, so seeding is not
 * optional): put `tvEntryFocusGroup(entryFocus)` on the container, seed its `initialFirstVisibleItemIndex` to
 * [entryIndex] so the remembered cell is composed before entry arrives, and hang [entryModifier] on each cell.
 * Report focus **gains** through [rememberFocused]; do not clear on blur, since the memory must outlive the blur
 * that nulls the ring.
 */
class TvRowEntry internal constructor(
    val entryFocus: FocusRequester,
    val entryIndex: Int,
    private val onRemember: (Int) -> Unit,
) {
    /** Record [index] as the cell entry returns to. Call on focus gain only, never on blur. */
    fun rememberFocused(index: Int) {
        onRemember(index)
    }

    /** The entry requester, attached to the remembered cell only, so `onEnter` resolves to it and no other. */
    fun entryModifier(index: Int): Modifier = if (index == entryIndex) Modifier.focusRequester(entryFocus) else Modifier
}

/**
 * Remembers the entry cell for a lazy container of [itemCount] cells. Leave [overrideIndex] null for the
 * caller-transparent default; pass it to steer entry from outside (the hub's cross-container memory). The
 * result is a fresh lightweight holder each recomposition — do not hoist it. See [TvRowEntry].
 *
 * [resetKey] names the data the memory belongs to (a query, a tab, a list id — the same idea `TvGridPane` takes
 * as its `queryKey`). Leave it null and the memory persists for the composition's life; pass it and a change
 * recreates the remembered index, so a new result set enters at the top (or at [overrideIndex]) rather than
 * restoring focus into an unrelated set. The reset rides `rememberSaveable(resetKey)`, so it survives disposal
 * within a key but starts fresh across one — the search rows' per-query reset (#1587).
 */
@Composable
fun rememberTvRowEntry(
    itemCount: Int,
    overrideIndex: Int? = null,
    resetKey: Any? = null,
): TvRowEntry {
    var remembered by rememberSaveable(resetKey) { mutableIntStateOf(0) }
    val entryIndex = (overrideIndex ?: remembered).coerceIn(0, (itemCount - 1).coerceAtLeast(0))
    val entryFocus = remember { FocusRequester() }
    return TvRowEntry(entryFocus, entryIndex, onRemember = { remembered = it })
}
