package com.binge.designsystem.tv.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The age rating, boxed. Bordered rather than filled: it is a classification, not a state of anything. `internal` so a hub's meta
 * line and a detail page's draw the same box.
 */
@Composable
internal fun TvMetaCertification(certification: String) {
    Text(
        text = certification,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .border(
                width = dimensionResource(DesR.dimen.hairline_thickness),
                color = MaterialTheme.colorScheme.border,
                shape = BingeShapes.Tag,
            ).padding(
                horizontal = dimensionResource(TvR.dimen.tv_detail_certification_padding_horizontal),
                vertical = dimensionResource(TvR.dimen.tv_detail_certification_padding_vertical),
            ),
    )
}
