package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.IconButtonTone
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

private const val FAVOURITE_LABEL = "Favourite"

/**
 * Public samples for [ExpressiveIconButton] — the four container tones plus the disabled state.
 * See the full catalog convention KDoc on [MediaCardRatedSample]; group these under `"Buttons"`.
 *
 * The four tones render side by side so the catalog cell shows the container-colour scale in one
 * shot; each tone supplies its own background (`Glass` paints a scrim), so the row is self-contained.
 */
@Composable
fun IconButtonToneSample() {
    ScreenshotTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = FAVOURITE_LABEL,
                tone = IconButtonTone.Default,
            )
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = FAVOURITE_LABEL,
                tone = IconButtonTone.Tonal,
            )
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = FAVOURITE_LABEL,
                tone = IconButtonTone.Accent,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            ExpressiveIconButton(
                onClick = {},
                icon = Icons.Filled.Favorite,
                contentDescription = FAVOURITE_LABEL,
                tone = IconButtonTone.Glass,
                tint = BingeTheme.colors.onScrim,
            )
        }
    }
}

/** Disabled icon button — the tonal container at reduced emphasis with taps suppressed. */
@Composable
fun IconButtonDisabledSample() {
    ScreenshotTheme {
        ExpressiveIconButton(
            onClick = {},
            icon = Icons.Filled.Favorite,
            contentDescription = FAVOURITE_LABEL,
            tone = IconButtonTone.Tonal,
            enabled = false,
        )
    }
}
