package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.R as TvR

/**
 * A switch's state at the end of a row: a track with its thumb at the end it is set to, filled when [checked]. It is drawn,
 * not pressed: the row holds focus and toggles, and passes its own focus as [isFocused] so the switch reads on the row's
 * fill. The state is set on the switch's semantics, which the row's merged node announces; give the row `role = Role.Switch`
 * so a screen reader calls it a switch rather than a button.
 */
@Composable
fun TvSwitch(
    checked: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    // On a focused row the fill is the accent, so the switch inverts against it.
    val (track, thumb, outline) =
        when {
            isFocused && checked -> Triple(scheme.onPrimary, scheme.primary, scheme.onPrimary)
            isFocused -> Triple(Color.Transparent, scheme.onPrimary, scheme.onPrimary)
            checked -> Triple(scheme.primary, scheme.onPrimary, scheme.primary)
            else -> Triple(Color.Transparent, scheme.onSurfaceVariant, scheme.onSurfaceVariant)
        }
    Box(
        modifier =
            modifier
                .semantics { toggleableState = ToggleableState(checked) }
                .size(
                    width = dimensionResource(TvR.dimen.tv_switch_track_width),
                    height = dimensionResource(TvR.dimen.tv_switch_track_height),
                ).clip(BingeShapes.Pill)
                .background(track)
                .border(dimensionResource(TvR.dimen.tv_switch_outline_width), outline, BingeShapes.Pill)
                .padding(dimensionResource(TvR.dimen.tv_switch_thumb_inset)),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier
                    .size(dimensionResource(TvR.dimen.tv_switch_thumb))
                    .clip(BingeShapes.Pill)
                    .background(thumb),
        )
    }
}
