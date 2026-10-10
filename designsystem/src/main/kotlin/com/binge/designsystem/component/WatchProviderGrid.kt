package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.theme.BingeShapes

/**
 * The column count both halves of this pair default to.
 *
 * Not `provider_grid_columns`, which is keyed to the **screen** width: a caller that renders this grid
 * inside a half-width pane in landscape would get a screen-derived 6 or 8, sizing its tiles against a
 * measure they do not have. A caller whose grid really is full-width passes the
 * resource in.
 */
private const val DEFAULT_GRID_COLUMNS = 4

/** Skeleton rows shown while providers load — enough to fill the visible space without faking a count. */
private const val SKELETON_ROW_COUNT = 3

/**
 * Grid of [WatchProviderTile]s, [columns] wide. Rows are chunked from [providers] and
 * trailing slots are padded with `Spacer(weight 1f)` so the final row's tiles
 * stay the same width as full rows above. Caller controls scrolling — wrap in
 * a `verticalScroll` or `LazyColumn` item if the list could overflow.
 *
 * [columns] must match whatever [WatchProviderGridSkeleton] was given for the same surface, or the
 * skeleton reserves tiles a different size from the ones that arrive — see [DEFAULT_GRID_COLUMNS].
 */
@Composable
fun WatchProviderGrid(
    providers: List<WatchProviderUi>,
    selectedWatchProviderIds: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = DEFAULT_GRID_COLUMNS,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.provider_grid_spacing)),
    ) {
        providers.chunked(columns).forEachIndexed { rowIndex, row ->
            WatchProviderGridRow(
                providers = row,
                columns = columns,
                selectedWatchProviderIds = selectedWatchProviderIds,
                onToggle = onToggle,
                isFirstRow = rowIndex == 0,
            )
        }
    }
}

/**
 * One row of up to [columns] tiles, trailing slots padded with `Spacer(weight 1f)` so a short last row's
 * tiles stay the width of the full rows above.
 *
 * Public because the grid is not the only shape this row appears in: a screen that needs a `LazyColumn`
 * for a long list of tiles emits these rows as items rather than composing [WatchProviderGrid]. The spacing,
 * the weight, the trailing spacers and the anchor below all have to agree between the two, which they do by
 * construction rather than by review.
 *
 * [isFirstRow] puts the anchor `WatchProviderGridSkeleton`'s first plate carries on this row's first tile —
 * see [LayoutAnchors]. The grid is sized by its content, so anchoring the container would not say where the
 * tiles land.
 */
@Composable
fun WatchProviderGridRow(
    providers: List<WatchProviderUi>,
    columns: Int,
    selectedWatchProviderIds: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isFirstRow: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.provider_grid_spacing)),
    ) {
        providers.forEachIndexed { index, provider ->
            WatchProviderTile(
                provider = provider,
                selected = provider.id in selectedWatchProviderIds,
                onToggle = { onToggle(provider.id) },
                modifier = Modifier
                    .weight(1f)
                    .layoutAnchorIf(
                        isFirstRow && index == 0,
                        LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM),
                    ),
            )
        }
        repeat(columns - providers.size) { Spacer(Modifier.weight(1f)) }
    }
}

/**
 * Placeholder grid rendered while provider logos are loading — preserves the
 * heading + footer layout so the surface doesn't jump when the real grid arrives.
 *
 * [columns] has to be whatever the grid that replaces this one will use. A screen that builds its own
 * lazy grid from a screen-keyed column count would otherwise find this reserving a flat four against
 * its six or eight.
 */
@Composable
fun WatchProviderGridSkeleton(modifier: Modifier = Modifier, columns: Int = DEFAULT_GRID_COLUMNS) {
    val tileShape = BingeShapes.Large
    val spacing = dimensionResource(R.dimen.provider_grid_spacing)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(SKELETON_ROW_COUNT) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                repeat(columns) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .layoutAnchorIf(
                                rowIndex == 0 && index == 0,
                                LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM),
                            ).aspectRatio(1f)
                            .clip(tileShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                    )
                }
            }
        }
    }
}
