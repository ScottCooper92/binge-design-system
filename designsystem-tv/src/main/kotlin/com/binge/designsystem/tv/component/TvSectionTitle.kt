package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

/**
 * The one in-content section title for TV, at [MaterialTheme.typography.titleLarge] — the single source every
 * carousel/section heading resolves to, so titles can't drift to different sizes (#1496).
 *
 * The inset is the row family's pair — start gutter, end overscan — so a title sits above the first card of
 * the [TvMediaRow] / [TvCardRow] it heads rather than nudged out of line.
 */
@Composable
fun TvSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(
            PaddingValues(
                start = tvContentGutterStart(),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
            ),
        ),
    )
}
