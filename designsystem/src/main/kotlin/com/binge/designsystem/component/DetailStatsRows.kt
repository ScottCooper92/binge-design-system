package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.template.readingMargin

private const val LARGE_FONT_SCALE = 1.5f

/**
 * How many stats a page puts on a row: all of them, or two at a large font. [DetailStatsRowsSkeleton] and the page lay their cells out by the
 * same rule, so a skeleton breaks into the rows the page does.
 *
 * @need decide how many stats go on one row at the current font scale
 * @group detail
 * @replaces a hand-built font-scale check before a row of stats
 */
@Composable
fun statsPerRow(): Int = if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) 2 else Int.MAX_VALUE

/**
 * The stats of a page in [DetailStatRow]s, each inset by the reading margin: one row of all of them, or two to a row from a font
 * scale of 1.5, where a label of four across breaks mid-word. The first row carries the [LayoutAnchors.Detail.STATS] anchor a page's
 * skeleton is checked against.
 *
 * @need show a title's stats (rating, year, runtime, certification) under its hero, breaking into two to a row at a large font
 * @group detail
 * @replaces a hand-built DetailStatRow with its own large-font chunking
 * @sample DetailStatsRowsSample
 */
@Composable
fun DetailStatsRows(stats: List<DetailStat>, modifier: Modifier = Modifier) {
    val perRow = statsPerRow()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
        stats.chunked(perRow).forEachIndexed { index, row ->
            val margin = Modifier.readingMargin()
            DetailStatRow(
                stats = row,
                modifier = if (index == 0) margin.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.STATS)) else margin,
            )
        }
    }
}
