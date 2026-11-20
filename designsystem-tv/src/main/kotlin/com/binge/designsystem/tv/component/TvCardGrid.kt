package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

/**
 * Columns of posters across the content pane.
 *
 */
internal const val TV_MEDIA_GRID_COLUMNS = 5

/**
 * Rows for the loading skeleton — two of them, so the plates fill the pane without pretending to know how
 * many results are coming. Multiplied by the caller's column count rather than baked to a cell total, so a
 * grid at its own width (the gallery's per-format columns) still reserves two rows rather than a ragged
 * fraction of them.
 */
private const val SKELETON_ROWS = 2

/** A poster's 2:3, matching TvMediaCard so a plate and a resolved card occupy identical space. */
private const val TV_MEDIA_GRID_PLATE_ASPECT_RATIO = 2f / 3f

/**
 * A D-pad grid of posters: what a "see all" tile opens onto, and what the Discover destination is.
 *
 * @need lay out a d-pad grid of cells that remembers the cell last focused on a tv screen
 * @group tv
 * @replaces a hand-built tv LazyVerticalGrid with its own entry focus
 */
@Composable
fun TvCardGrid(
    itemCount: Int,
    itemKey: (Int) -> Any,
    columns: Int,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    initialFocusedIndex: Int? = null,
    contentPadding: PaddingValues = tvMediaGridPadding(),
    // The *index*, unlike TvMediaRow's equivalent, because the caller's job with it is arithmetic: a grid's
    // anchor row is `index / columns`, and there is no way back from an id to an index through a paged
    // accessor without scanning it.
    onItemFocused: (Int) -> Unit = {},
    entryFocusRequester: FocusRequester? = null,
    rememberedIndex: Int? = null,
    cell: @Composable (index: Int, isFocused: Boolean, onFocusChanged: (Boolean) -> Unit, cellModifier: Modifier) -> Unit,
) {
    // The cell the group's entry redirects to: the remembered one, or the **first** before anything has been
    // focused (#1727). Falling back to 0 rather than leaving the requester unattached is what makes a fresh
    // entry land on cell one — see the entry-cell note in the KDoc.
    val entryIndex = rememberedIndex ?: 0
    // Only meaningful while [entryIndex] names a cell that is currently composed, which is why the request is
    // wrapped — an absent cell falls through to the directional search.
    val rememberedCell = remember { FocusRequester() }
    // Nulled on blur so a grid that has lost focus does not keep a ring lit; the selected-but-unfocused
    // treatment is #1331's problem, not this component's. Seeded so a baseline can render a focused cell
    // *inside* the grid, which is the only way to prove the padding above protects the ring.
    var focusedIndex by remember { mutableStateOf(initialFocusedIndex) }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = state,
        modifier = modifier
            .fillMaxSize()
            // The group and its entry redirect are always present; only the *external* [entryFocusRequester] rides on when a caller
            // supplies one. `tvEntryFocusGroup`'s `onEnter` aims a rightward return from the rail at [rememberedCell] (not `tvFocusGroup`
            // — a restorer never saved anything here). The requester is the keeper's *request* target; a `LazyVerticalGrid` is none itself.
            .then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .tvEntryFocusGroup(rememberedCell),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_grid_row_gap)),
    ) {
        items(count = itemCount, key = itemKey) { index ->
            cell(
                index,
                focusedIndex == index,
                { focused ->
                    if (focused) {
                        focusedIndex = index
                        onItemFocused(index)
                    } else if (focusedIndex == index) {
                        focusedIndex = null
                    }
                },
                // Fills the cell, so the column count decides the size and the gutters stay even; the entry
                // requester rides the remembered cell, and must land on the *focusable* the slot builds.
                Modifier
                    .tvGridFirstCellAnchor(index == 0)
                    .fillMaxWidth()
                    .then(if (index == entryIndex) Modifier.focusRequester(rememberedCell) else Modifier),
            )
        }
    }
}

/**
 * The four-sided insets every TV grid shares.
 */
@Composable
fun tvMediaGridPadding(): PaddingValues =
    PaddingValues(
        start = tvContentGutterStart(),
        end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
        top = dimensionResource(TvR.dimen.tv_media_grid_row_gap),
        bottom = dimensionResource(TvR.dimen.tv_immersive_rows_bottom),
    )

/**
 * The grid's own loading state: plates in the geometry the results will occupy.
 *
 * @need show plates where a tv grid's cells will be while it loads
 * @group tv
 * @replaces a hand-built tv grid skeleton
 */
@Composable
fun TvMediaGridSkeleton(
    modifier: Modifier = Modifier,
    columns: Int = TV_MEDIA_GRID_COLUMNS,
    contentPadding: PaddingValues = tvMediaGridPadding(),
    // The plate itself, so a grid whose cell is not a 2:3 poster reserves *its* shape — the gallery's
    // format aspect, the cast list's circle over a two-line caption (#1682). Default is the poster plate,
    // which is what the poster grids want.
    plate: @Composable () -> Unit = { TvMediaGridPlate() },
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        // The **grid's own** insets, not this skeleton's idea of them ([tvMediaGridPadding] states why the
        // top must be the row gap); a skeleton that reserved the vertical overscan instead dropped every
        // plate 11dp below where its poster lands.
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_grid_row_gap)),
        // Nothing to scroll to: the plates are decoration and take no focus.
        userScrollEnabled = false,
    ) {
        items(count = columns * SKELETON_ROWS) { index ->
            Box(modifier = Modifier.tvGridFirstCellAnchor(index == 0)) { plate() }
        }
    }
}

/** One empty cell, at the poster shape the real card will take. *
 * @need draw one empty poster-shaped plate in a tv grid skeleton
 * @group tv
 */
@Composable
fun TvMediaGridPlate() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(TV_MEDIA_GRID_PLATE_ASPECT_RATIO)
            .clip(BingeShapes.MediaCard)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/**
 * Marks a media grid's first cell as the layout anchor its loading skeleton is measured against (#1598).
 *
 * The first cell is the only one worth anchoring: every grid inset the skeleton has to reproduce lands above
 * it, and the rest follow from the arrangement. It is the design system's grid anchor, so this grid, its skeleton
 * and [TvGridPane]'s cells cannot name it differently.
 */
fun Modifier.tvGridFirstCellAnchor(isFirst: Boolean): Modifier = if (isFirst) tvLayoutAnchor(TvLayoutAnchors.GRID_FIRST_CELL) else this
