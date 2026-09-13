package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.tv.focus.TvInheritedFocusScroll
import com.binge.designsystem.tv.focus.rememberTvRowEntry
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

/** The trailing (see-all) cell's focus key. A String so it can never collide with a caller's item key. */
private const val TRAILING_KEY = "tv-card-row-trailing"

/**
 * The standard TV detail row: an optional heading over a horizontal run of fixed-width cards, with the focus
 * contract every such row needs **baked in** so a caller cannot forget it.
 *
 * A click-to-open row must route directional entry to its first cell, or ↓ from a control above (a *centred*
 * action row especially) falls through to whatever card sits under the beam. By hand that is three steps — a
 * [FocusRequester], [tvEntryFocusGroup] on the `LazyRow`, and `.focusRequester(entry)` on the first cell — and
 * `tvFocusGroup`, the helper reached for by name, is a restorer that silently does not do it (see its KDoc), so
 * hand-rolled rows kept getting it wrong. This wrapper owns all three plus per-cell focus tracking.
 *
 * Entering the row for the **first** time lands on its first cell — never the trailing see-all tile (#1500),
 * which is not in [items] and so never carries the entry requester. Returning lands on the cell **last
 * focused**. Both come from the shared [rememberTvRowEntry] contract — see [TvRowEntry] for how the memory survives
 * the `LazyColumn` scroll-out disposal.
 *
 * Where a host app's own poster row is bound to its media-item model, this is generic over the cell: the
 * detail page's cast circles, season posters and stills are not media items, and hand-rolled their own rows
 * before.
 *
 * The [cell] slot receives the item, whether it is focused, an `onFocusChanged`, and a `cellModifier` (fixed
 * width plus, for the first cell, the entry requester) to apply to its **focusable** element. [trailing] is an
 * optional see-all tile, keyed apart so it tracks focus like any other cell. [heading] is optional — a row under
 * its own section chrome passes none.
 *
 * Every cell must be focusable: the row's horizontal scroll is driven entirely by cell focus, so inert cells
 * would strand anything past the panel edge. A read-out of non-focusable tiles wants a wrapping layout
 * instead.
 */
@Composable
fun <T> TvCardRow(
    items: List<T>,
    key: (T) -> Any,
    cellWidth: Dp,
    modifier: Modifier = Modifier,
    heading: String? = null,
    // A target the caller aims focus at to land it inside the row; the entry group resolves the request into the
    // remembered (or first) cell. The row never fires it - a destination does not request focus, see
    // docs/tv-foundation.md - and it rides the LazyRow rather than a cell
    // because a cell is disposed when it scrolls out.
    entryFocusRequester: FocusRequester? = null,
    // Reports each focus gain with the cell's key — the hook a detail page's sections chain their anchor
    // reporter into, so a sideways move inside the row re-asserts the page's rest position.
    onCellFocused: ((Any) -> Unit)? = null,
    // Seeds the ring onto one cell for a screenshot; production passes null. A static baseline runs no coroutines and
    // dispatches no focus events, so without it every consumer of this row (episode cards, guest cast, gallery strips) had
    // its most-used focus state uncoverable rather than merely uncovered. Same seed as TvButton, and the same
    // reason: focus is a parameter, see docs/tv-foundation.md.
    initiallyFocusedKey: Any? = null,
    trailing: (@Composable (isFocused: Boolean, onFocusChanged: (Boolean) -> Unit) -> Unit)? = null,
    cell: @Composable (item: T, isFocused: Boolean, onFocusChanged: (Boolean) -> Unit, cellModifier: Modifier) -> Unit,
) {
    if (items.isEmpty()) return
    // Which cell draws the ring — one hoisted key, nulled on blur, so a row that has lost focus keeps none lit.
    var focusedKey by remember { mutableStateOf(initiallyFocusedKey) }
    // The cell entry returns to — the shared remembered-cell contract (survives blur and the LazyColumn
    // scroll-out disposal). Seed the row state to it so it is laid out before entry arrives.
    val entry = rememberTvRowEntry(items.size)
    val rowState = rememberLazyListState(initialFirstVisibleItemIndex = entry.entryIndex)
    // Start is the in-pane content gutter (inset + gutter under the overlay rail), end the panel's overscan.
    // The row is full panel width, so cells scrolled off the front pass *under* the rail rather than clipping
    // at its edge, while the first cell rests on the gutter exactly as before.
    val rowPadding = PaddingValues(
        start = tvContentGutterStart(),
        end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        heading?.let { TvSectionTitle(text = it) }
        // The detail screen scopes its LazyColumn with TvStableFocusScroll so crossing rows does not re-pivot
        // it; that scope reaches in here too, and this row's own sideways scroll wants the ambient parking
        // back. Same split the immersive hub keeps between its column and its rows.
        TvInheritedFocusScroll {
            LazyRow(
                state = rowState,
                contentPadding = rowPadding,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
                // The whole point: ↓ entry lands on the remembered cell (the first on a fresh row), not a
                // geometric pick. See the KDoc. [entryFocusRequester] rides on as an outer target the caller
                // can request to land focus in the row (the entry group resolves it to the remembered cell).
                modifier = Modifier
                    .then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                    .tvEntryFocusGroup(entry.entryFocus),
            ) {
                itemsIndexed(items = items, key = { _, item -> key(item) }) { index, item ->
                    val itemKey = key(item)
                    cell(
                        item,
                        focusedKey == itemKey,
                        { focused ->
                            focusedKey = focusedKey.trackFocus(focused, itemKey)
                            if (focused) {
                                entry.rememberFocused(index)
                                onCellFocused?.invoke(itemKey)
                            }
                        },
                        Modifier
                            .width(cellWidth)
                            .then(entry.entryModifier(index)),
                    )
                }
                trailing?.let { drawTrailing ->
                    item(key = TRAILING_KEY) {
                        drawTrailing(
                            focusedKey == TRAILING_KEY,
                            { focused ->
                                focusedKey = focusedKey.trackFocus(focused, TRAILING_KEY)
                                if (focused) onCellFocused?.invoke(TRAILING_KEY)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Latch a row's single focused key: claim it on gain, release it only if this cell still holds it. The "only
 * if" matters — focus reaches the new cell before the old one reports its loss, so an unconditional clear on
 * blur would wipe the key the incoming cell just set and leave the row with no ring.
 */
private fun Any?.trackFocus(gained: Boolean, key: Any): Any? =
    when {
        gained -> key
        this == key -> null
        else -> this
    }
