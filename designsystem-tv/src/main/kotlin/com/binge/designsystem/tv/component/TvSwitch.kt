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
import com.binge.designsystem.tv.theme.tvStyle
import com.binge.designsystem.tv.R as TvR

/**
 * A switch's state at the end of a row: a track with its thumb at the end it is set to, filled when [checked]. It is drawn,
 * not pressed: the row holds focus and toggles, and passes its own focus as [isFocused] so the switch reads on the row's
 * fill. The state is set on the switch's semantics, which the row's merged node announces; give the row `role = Role.Switch`
 * so a screen reader calls it a switch rather than a button.
 *
 * Match the row it sits in. A switch in a row that is not [enabled] dims with the row. One in a destructive row is
 * [destructive]: focused, the row fills with the error colour, and the switch inverts against that instead of the focus
 * fill; unfocused, a checked switch has the error colour for its track. The other colours are the style's: see `TvStyle`.
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
    val style = MaterialTheme.tvStyle
    // On a focused row the fill is the style's focus container, so the switch inverts against it: the style's switch
    // roles. A destructive one reads in the error colour whether or not it is focused, so a checked switch is never
    // half destructive beside its red label.
    val colors =
        if (destructive) {
            val fill = style.destructiveFocusContainer
            val onFill = style.onDestructiveFocusContainer
            style.switch.copy(
                checkedTrack = fill,
                checkedThumb = onFill,
                focusedCheckedTrack = onFill,
                focusedCheckedThumb = fill,
                focusedUncheckedThumb = onFill,
            )
        } else {
            style.switch
        }
    val (track, thumb) =
        when {
            !enabled && checked -> scheme.onSurface to scheme.surface
            !enabled -> Color.Transparent to scheme.onSurface
            isFocused && checked -> colors.focusedCheckedTrack to colors.focusedCheckedThumb
            isFocused -> colors.focusedUncheckedTrack to colors.focusedUncheckedThumb
            checked -> colors.checkedTrack to colors.checkedThumb
            else -> colors.uncheckedTrack to colors.uncheckedThumb
        }
    // The edge is the track when checked and the thumb when not, so an off switch still shows its shape.
    val outline = if (checked) track else thumb
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
