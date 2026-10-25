package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvClickable
import kotlinx.coroutines.launch
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Posters across the v2 grid. */
const val TV_POSTER_GRID_COLUMNS = 4

/** Rows of plates drawn while the grid loads: enough to run past the bottom edge, the way a resolved grid does. */
private const val LOADING_ROWS = 3

/** One poster of a [TvPosterGrid]: its art, the name that stands in for it when there is none, and what OK does. */
class TvPosterCell(
    val imageUrl: String?,
    val title: String,
    val onClick: () -> Unit,
)

/**
 * The v2 poster grid (search's results, Discover's): [count] cells in [TV_POSTER_GRID_COLUMNS] columns, or the same number of
 * plates in the same cells while [loading]. Cells are read through [posterAt] by index; a null poster draws as a plate. [keyOf]
 * is each cell's key (default: its index). [entryIndex] is the cell that takes [entry]; [listKey] resets the scroll.
 * With [stablePlates] a plate takes focus (ring, OK does nothing) and a cell keeps its node as its poster arrives, so a grid
 * that loads in place keeps focus where it was; plates and posters are then positional, never keyed by [keyOf].
 */
@Composable
fun TvPosterGrid(
    count: Int,
    posterAt: (Int) -> TvPosterCell?,
    loading: Boolean,
    entry: FocusRequester,
    entryIndex: Int,
    listKey: Any?,
    onCellFocused: (Int) -> Unit,
    modifier: Modifier = Modifier,
    keyOf: ((Int) -> Any)? = null,
    seededIndex: Int? = null,
    cellModifier: (Int) -> Modifier = { Modifier },
    stablePlates: Boolean = false,
) {
    // Saved, so the grid comes back where it was when the screen is disposed under a page and restored (the retained entry).
    val state = rememberSaveable(listKey, saver = LazyGridState.Saver) { LazyGridState() }
    val scope = rememberCoroutineScope()
    val gap = dimensionResource(DesR.dimen.padding_m)
    val cells = if (loading) LOADING_ROWS * TV_POSTER_GRID_COLUMNS else count
    val entryCell = entryIndex.coerceIn(0, (cells - 1).coerceAtLeast(0))
    TvStableFocusScroll {
        LazyVerticalGrid(
            columns = GridCells.Fixed(TV_POSTER_GRID_COLUMNS),
            state = state,
            // No top padding: the pinned row sits at the grid's top edge, so the rows above it are clipped clean away.
            contentPadding = PaddingValues(
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
            ),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
            modifier = modifier.fillMaxSize(),
        ) {
            items(cells, key = cellKeys(stablePlates, loading, keyOf)) { index ->
                val poster = if (loading || index >= count) null else posterAt(index)
                TvPosterGridCell(
                    poster = poster,
                    initiallyFocused = index == seededIndex,
                    focusablePlate = stablePlates,
                    onFocused = {
                        if (reportsFocus(poster != null, stablePlates)) onCellFocused(index)
                        scope.launch { state.animateScrollToItem(index / TV_POSTER_GRID_COLUMNS * TV_POSTER_GRID_COLUMNS) }
                    },
                    modifier = cellModifier(index).then(entryModifier(entry, index == entryCell && (stablePlates || !loading))),
                )
            }
        }
    }
}

/** Keyed cells keep their node across a refresh; plates and the stable-plate grid are positional, so a poster arriving is the same node. */
private fun cellKeys(
    stablePlates: Boolean,
    loading: Boolean,
    keyOf: ((Int) -> Any)?,
): ((Int) -> Any)? = keyOf.takeUnless { stablePlates || loading }

/** A plate without a poster is not a result: it reports focus only on a stable-plate-less grid. */
private fun reportsFocus(hasPoster: Boolean, stablePlates: Boolean): Boolean = hasPoster || !stablePlates

private fun entryModifier(entry: FocusRequester, isEntry: Boolean): Modifier = if (isEntry) Modifier.focusRequester(entry) else Modifier

/**
 * One poster's footprint, drawn by a resolved cell and by its loading plate alike, so the two cannot drift apart. A
 * resolved cell passes its art as [content] and its focus handling as [interaction]; the plate passes neither.
 */
@Composable
fun TvPosterPlate(
    modifier: Modifier = Modifier,
    interaction: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(BingeShapes.MediaCard)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(interaction),
        content = content,
    )
}

@Composable
private fun TvPosterGridCell(
    poster: TvPosterCell?,
    initiallyFocused: Boolean,
    focusablePlate: Boolean,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (poster == null && !focusablePlate) {
        TvPosterPlate(modifier = modifier)
        return
    }
    // One node chain for the plate and the poster, so focus on a plate stays on it when the poster arrives.
    var focused by remember { mutableStateOf(initiallyFocused) }
    // Focus does not change when the poster arrives in the cell it is on, so the cell is reported once it has one.
    val hasPoster = poster != null
    val reportFocused by rememberUpdatedState(onFocused)
    LaunchedEffect(hasPoster) { if (focusablePlate && focused && hasPoster) reportFocused() }
    TvPosterPlate(
        modifier = modifier,
        interaction = Modifier.tvClickable(
            onFocusChanged = {
                focused = it
                if (it) onFocused()
            },
            onClick = { poster?.onClick?.invoke() },
        ),
    ) {
        if (poster != null) {
            SubcomposeAsyncImage(
                model = poster.imageUrl,
                contentDescription = poster.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) },
                // No poster: the name stands in, so a title without art is still something to pick.
                error = {
                    Box(Modifier.fillMaxSize().padding(dimensionResource(DesR.dimen.padding_s)), contentAlignment = Alignment.Center) {
                        Text(text = poster.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
            )
        }
        // The v2 focus: a white ring over the art, as the hub's rows draw it, not the yellow indicator.
        if (focused) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(dimensionResource(TvR.dimen.tv_focus_ring_width), MaterialTheme.colorScheme.onSurface, BingeShapes.MediaCard),
            )
        }
    }
}
