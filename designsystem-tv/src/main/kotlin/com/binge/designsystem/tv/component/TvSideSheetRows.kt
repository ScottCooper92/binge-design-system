package com.binge.designsystem.tv.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Whether the [TvSideSheetPanel] around the rows runs to the screen's bottom edge, so the rows carry its overscan. */
internal val LocalTvSideSheetToBottomEdge = staticCompositionLocalOf { false }

/**
 * A sheet's rows: `padding_xs` apart, in a column that scrolls when they outgrow the panel, under the title and above
 * anything the sheet pins below them. On a panel that runs to the bottom edge the bottom overscan sits inside the scroll, so
 * the last row can scroll clear of the edge. Every sheet's rows go through it, so every sheet spaces them alike; a card the
 * sheet shows among them keeps its own spacing inside.
 */
@Composable
fun ColumnScope.TvSideSheetRows(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val toBottomEdge = LocalTvSideSheetToBottomEdge.current
    Column(
        modifier =
            modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .then(if (toBottomEdge) Modifier.padding(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical)) else Modifier),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
        content = content,
    )
}
