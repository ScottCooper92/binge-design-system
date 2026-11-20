package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The layout anchor on the pane's first category row, which the loading shell's first row reserves. It is the tag a host's test
 * finds the row by (`TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CATEGORY)`), so it is public for those tests and for nothing else.
 */
const val TV_FILTER_ANCHOR_FIRST_CATEGORY = "filter-first-category"

/**
 * The layout anchor on the results grid's first cell, which the loading grid's first plate reserves. It is the tag a host's test finds
 * the cell by (`TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CELL)`), so it is public for those tests and for nothing else.
 */
const val TV_FILTER_ANCHOR_FIRST_CELL = "filter-first-cell"

/** The anchor on the grid cell at [index]: the first only, in the resolved grid and its loading plates alike. */
internal fun tvFilterCellModifier(index: Int): Modifier =
    if (index == 0) Modifier.tvLayoutAnchor(TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CELL)) else Modifier

/** Enough category rows to run off the pane, as the real list does. */
private const val SKELETON_CATEGORY_ROWS = 10

/** How far each row's label and summary run, cycled so the stack reads as ragged text. */
private val LABEL_FRACTIONS = listOf(0.5f, 0.4f, 0.45f, 0.55f)
private val DETAIL_FRACTIONS = listOf(0.35f, 0.45f, 0.3f, 0.4f)

private const val HEADER_FRACTION = 0.4f

/**
 * The filter pane before the page has a state: the header line, then category rows as plates, each laid out with the real
 * row's padding, icon and two text styles, so the resolve fills the pane in place rather than drawing it from blank.
 */
@Composable
internal fun TvFilterPaneSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.clipToBounds()) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(DesR.dimen.padding_m), vertical = dimensionResource(DesR.dimen.padding_s)),
        ) {
            TextPlate(MaterialTheme.typography.titleMedium, HEADER_FRACTION)
        }
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs))) {
            repeat(SKELETON_CATEGORY_ROWS) { index ->
                Row(
                    modifier = Modifier
                        .then(
                            if (index == 0) Modifier.tvLayoutAnchor(TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CATEGORY)) else Modifier,
                        ).fillMaxWidth()
                        .padding(horizontal = dimensionResource(DesR.dimen.padding_m), vertical = dimensionResource(DesR.dimen.padding_s)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
                ) {
                    Box(
                        Modifier
                            .size(dimensionResource(TvR.dimen.tv_button_icon))
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Column(Modifier.weight(1f)) {
                        TextPlate(MaterialTheme.typography.titleSmall, LABEL_FRACTIONS[index % LABEL_FRACTIONS.size])
                        TextPlate(MaterialTheme.typography.bodySmall, DETAIL_FRACTIONS[index % DETAIL_FRACTIONS.size])
                    }
                }
            }
        }
    }
}

/** A line of [style]'s height, holding no text: a plate the width of [fraction], inset to the glyphs' own height. */
@Composable
private fun TextPlate(style: TextStyle, fraction: Float) {
    Box(Modifier.fillMaxWidth(fraction)) {
        Text(text = " ", style = style, modifier = Modifier.fillMaxWidth())
        Box(
            Modifier
                .matchParentSize()
                .padding(vertical = dimensionResource(DesR.dimen.padding_xxs))
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}
