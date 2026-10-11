package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.WatchProviderGrid
import com.binge.designsystem.component.WatchProviderGridRow
import com.binge.designsystem.component.WatchProviderGridSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * The provider grid with two services selected, which also shows the tile in both its states. Tapping
 * a tile toggles it, so the catalog app can try the selection for real; the first frame, which is all
 * a screenshot sees, always starts from the same two.
 */
@Composable
fun WatchProviderGridSample() {
    var selected by remember { mutableStateOf(catalogSelectedWatchProviderIds()) }
    ScreenshotTheme {
        WatchProviderGrid(
            providers = catalogSampleWatchProviders(),
            selectedWatchProviderIds = selected,
            onToggle = { id -> selected = if (id in selected) selected - id else selected + id },
        )
    }
}

/** The provider grid while the services load: a grid of tile placeholders. */
@Composable
fun WatchProviderGridSkeletonSample() {
    ScreenshotTheme {
        WatchProviderGridSkeleton()
    }
}

private const val PARTIAL_ROW_COLUMNS = 4

/**
 * A short last row: two tiles in a four-column row, one selected. The trailing spacers keep its tiles the width of a
 * full row's. Tapping a tile toggles it; the first frame always starts from the same one.
 */
@Composable
fun WatchProviderGridPartialRowSample() {
    var selected by remember { mutableStateOf(catalogPartialRowSelectedIds()) }
    ScreenshotTheme {
        WatchProviderGridRow(
            providers = catalogPartialRowWatchProviders(),
            columns = PARTIAL_ROW_COLUMNS,
            selectedWatchProviderIds = selected,
            onToggle = { id -> selected = if (id in selected) selected - id else selected + id },
        )
    }
}
