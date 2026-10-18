package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.R as TvR

/**
 * A switch's state at the end of a row: a track with its thumb at the end it is set to, filled when [checked]. It is drawn,
 * not pressed: the row holds focus and toggles, and passes its own focus as [isFocused] so the switch reads on the row's
 * fill. The state is set on the switch's semantics, which the row's merged node announces; give the row `role = Role.Switch`
 * so a screen reader calls it a switch rather than a button.
 *
 * Match the row it sits in. A switch in a row that is not [enabled] dims with the row. One in a destructive row is
 * [destructive]: focused, the row fills with the error colour, and the switch inverts against that instead of the accent;
 * unfocused, a checked switch has the error colour for its track rather than the accent.
 */
@Composable
fun TvSwitch(
    checked: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    // On a focused row the fill is the accent, so the switch inverts against it. A destructive one reads in the error
    // colour whether or not it is focused, so a checked switch is never half destructive beside its red label.
    val fill = if (destructive) scheme.error else scheme.primary
    val onFill = if (destructive) scheme.onError else scheme.onPrimary
    val (track, thumb, outline) =
        when {
            !enabled && checked -> Triple(scheme.onSurface, scheme.surface, scheme.onSurface)
            !enabled -> Triple(Color.Transparent, scheme.onSurface, scheme.onSurface)
            isFocused && checked -> Triple(onFill, fill, onFill)
            isFocused -> Triple(Color.Transparent, onFill, onFill)
            checked -> Triple(fill, onFill, fill)
            else -> Triple(Color.Transparent, scheme.onSurfaceVariant, scheme.onSurfaceVariant)
        }
    Box(
        modifier =
            modifier
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
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
