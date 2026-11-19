package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

/** The halo around the dot: the same fill at low alpha, so a solid dot nests in a soft halo. */
private const val STATUS_DOT_HALO_ALPHA = 0.30f

/** The thin scrim behind a halo that sits on artwork: enough to keep the dot at 3:1 over a bright image. */
private const val STATUS_DOT_BACKDROP_ALPHA = 0.55f

/**
 * A status as a solid dot in a soft halo of the same [fill]. The one drawing of a status dot: [StatusChip], the status button, the
 * hero and the poster all use it, so a status reads the same wherever it is shown.
 *
 * Over artwork, pass [overArtwork] so a thin scrim sits behind the halo. The dot and halo are unchanged.
 */
@Composable
internal fun StatusDot(
    fill: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = dimensionResource(R.dimen.status_chip_dot_size),
    haloSize: Dp = dimensionResource(R.dimen.status_chip_dot_halo_size),
    overArtwork: Boolean = false,
) {
    Box(
        modifier =
            modifier
                .size(haloSize)
                .then(
                    if (overArtwork) {
                        Modifier.background(BingeTheme.colors.scrim.copy(alpha = STATUS_DOT_BACKDROP_ALPHA), CircleShape)
                    } else {
                        Modifier
                    },
                ).background(fill.copy(alpha = STATUS_DOT_HALO_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            Modifier
                .size(dotSize)
                .background(fill, CircleShape),
        )
    }
}
