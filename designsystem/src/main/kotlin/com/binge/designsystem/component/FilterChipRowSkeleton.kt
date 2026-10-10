package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/** All, plus the two categories a filtered screen most often has — enough to fill the measure, not overrun it. */
private const val SKELETON_CHIP_COUNT = 3

/**
 * Stands in for the [BingeFilterChipRow] a filtered screen puts above its grid, on the row's own insets
 * — literally, via the shared [filterChipRowPadding], so the plate cannot come to disagree with the row
 * about where the first chip starts.
 *
 * Both screens that pair a chip row with a grid — the gallery and the library — shipped skeletons that drew
 * only the grid. The row is 56dp, so on both the first tile arrived that far below where it was reserved and
 * the whole grid shunted down on resolve. This is the plate that reserves it, shared so the two
 * cannot drift apart again.
 *
 * The pill's height is a dimen rather than a derived value because a chip is a composed component with its
 * own padding and label, not a line of text; the geometry checks on both screens are what keep it honest.
 */
@Composable
fun FilterChipRowSkeleton(modifier: Modifier = Modifier) {
    var consumedInsets by remember { mutableStateOf(NoInsets) }
    Row(
        modifier = modifier
            .onConsumedWindowInsetsChanged { consumedInsets = it }
            .fillMaxWidth()
            .padding(filterChipRowPadding(consumedInsets)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        repeat(SKELETON_CHIP_COUNT) {
            SkeletonPlate(
                Modifier
                    .width(dimensionResource(R.dimen.skeleton_filter_chip_width))
                    .height(dimensionResource(R.dimen.skeleton_filter_chip_height)),
                shape = BingeShapes.Pill,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewFilterChipRowSkeleton() {
    BingeExpressiveTheme {
        FilterChipRowSkeleton()
    }
}
