package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.tv.component.TvSkeletonPlate
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.R as TvR

/** How wide each suggestion plate runs against the column, cycled so the stack reads as ragged text and not a block. */
private val SUGGESTION_PLATE_FRACTIONS = listOf(0.9f, 0.7f, 0.8f, 0.6f)

/** Plates in the suggestion column: more than fit, clipped by the column the way the scrolling list is. */
internal const val SUGGESTION_PLATE_COUNT = 12

/** The first grid cell's layout anchor, which the loading plates reserve. */
internal const val SEARCH_ANCHOR_FIRST_CELL = "search-first-cell"

/** The first suggestion's layout anchor, which the suggestion plates reserve. */
internal const val SEARCH_ANCHOR_FIRST_SUGGESTION = "search-first-suggestion"

/** The first suggestion's icon, so a plate drawn without one fails the geometry test. */
internal const val SEARCH_ANCHOR_FIRST_SUGGESTION_ICON = "search-first-suggestion-icon"

/**
 * A suggestion row's content as a plate: its icon, then one line of text, in the row's own padding. [TvSuggestionRow] draws it
 * inside the same focusable row the suggestion arrives in, so the plate focus is on becomes the suggestion.
 */
@Composable
internal fun TvSuggestionRowPlate(index: Int, iconModifier: Modifier = Modifier) {
    Box(
        iconModifier
            .size(dimensionResource(TvR.dimen.tv_search_suggestion_icon))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
    Spacer(Modifier.width(dimensionResource(TvR.dimen.tv_search_suggestion_gap)))
    TvSkeletonPlate(
        widthFraction = SUGGESTION_PLATE_FRACTIONS[index % SUGGESTION_PLATE_FRACTIONS.size],
        height = dimensionResource(TvR.dimen.tv_search_suggestion_line_height),
    )
}

/** Marks the grid's first cell, resolved or plate, as the anchor the skeleton is measured against. */
internal fun Modifier.tvSearchFirstCellAnchor(index: Int): Modifier =
    if (index == 0) tvLayoutAnchor(TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_CELL)) else this
