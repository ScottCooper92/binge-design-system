package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.tv.R as TvR

/**
 * A single glyph presented as **art**: amber on a circular `surfaceVariant` plate.
 *
 * The TV settings pane established this treatment for the illustration above a setting's name, and it is what
 * makes a lone icon read as an illustration rather than as a disabled control — the accent model's accent is
 * doing the work.
 * `TvMessagePlate` now draws its empty/failure art the same way, so the two live here as one component
 * rather than as two copies of a circle and an icon that would drift.
 *
 * Sized from `tv_illustration_*`, named for the role rather than for the settings screen that happened to need
 * it first.
 */
@Composable
fun TvIllustration(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dimensionResource(TvR.dimen.tv_illustration_plate))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_illustration_icon)),
        )
    }
}
