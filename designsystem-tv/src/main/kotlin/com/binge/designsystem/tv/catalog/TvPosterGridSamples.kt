package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.component.TvPosterGrid
import com.binge.designsystem.tv.R as TvR

private const val SAMPLE_CELLS = 12

/** The poster grid resolved. The cells carry no art, so each draws its title in its place. */
@Composable
fun TvPosterGridSample() {
    PosterGrid(loading = false)
}

/** A focused cell: the white ring over its art. */
@Composable
fun TvPosterGridFocusedCellSample() {
    PosterGrid(loading = false, seededIndex = 5)
}

/** While the grid loads: the same cells as plates, which take no focus. */
@Composable
fun TvPosterGridLoadingSample() {
    PosterGrid(loading = true)
}

/** A grid that loads in place (`stablePlates`): a plate takes focus and shows the ring before its poster arrives. */
@Composable
fun TvPosterGridFocusedPlateSample() {
    PosterGrid(loading = true, seededIndex = 1, stablePlates = true)
}

@Composable
private fun PosterGrid(
    loading: Boolean,
    seededIndex: Int? = null,
    stablePlates: Boolean = false,
) {
    Box(Modifier.fillMaxSize().padding(start = dimensionResource(TvR.dimen.tv_overscan_horizontal))) {
        TvPosterGrid(
            count = SAMPLE_CELLS,
            posterAt = { TvPosterCell(imageUrl = null, title = "Title $it", onClick = {}) },
            loading = loading,
            entry = remember { FocusRequester() },
            entryIndex = 0,
            listKey = Unit,
            onCellFocused = {},
            seededIndex = seededIndex,
            stablePlates = stablePlates,
        )
    }
}
