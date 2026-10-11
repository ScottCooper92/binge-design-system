package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.theme.tvButtonColors
import com.binge.designsystem.tv.R as TvR

/**
 * The shared 10-foot text button: a pill coloured by [tvButtonColors], focus filling it with the accent (red for
 * [TvButtonStyle.Destructive], so danger survives being focused).
 *
 * Danger is a [style], not a `destructive` flag as on the phone's buttons: a TV control's role fixes its resting
 * fill, its border and its focus fill together, and the three roles share one colour model with [TvIconButton].
 *
 * A disabled button stays focusable but inert — a D-pad control that refuses focus reads as broken, not
 * "not ready". It is drawn as an outline, not filled: a `colors.surface` fill composites to nothing
 * on a panel, whereas a transparent container with a dimmed border keeps its shape on any background.
 *
 * [loading] draws a spinner over the content and swallows OK, as the phone's buttons do, so an in-flight action cannot
 * be sent twice. It is independent of [enabled]: the button keeps its colours, its size and focus, so the remote does
 * not lose its place, and [label] stays its accessible name.
 *
 * [initiallyFocused] seeds the focus flag so a screenshot can show the focused state (a baseline runs no
 * coroutines, so no real focus event lands); production leaves it false. Reach for [TvButtonSurface] when
 * the caller owns the flag outright.
 */
@Composable
fun TvButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TvButtonStyle = TvButtonStyle.Secondary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    TvButtonSurface(
        label = label,
        style = style,
        enabled = enabled,
        isFocused = focused,
        leadingIcon = leadingIcon,
        loading = loading,
        modifier = modifier
            .tvClickable(
                enabled = enabled,
                role = Role.Button,
                onFocusChanged = { focused = it },
                onClick = { if (!loading) onClick() },
            ),
    )
}

/**
 * The stateless surface behind [TvButton] — focus is a parameter, not owned here, so a preview or
 * screenshot can render the focused frame without a real focus request landing by capture time.
 *
 * An optional [leadingIcon] sits before the label at the button's content colour, so it inverts with the
 * label when focus fills the pill.
 *
 * [loading] hides the icon and label, keeping their room, and centres a spinner in their place, so the pill does not
 * change width. The hidden label still names the control to a screen reader, and the surface says it is busy.
 */
@Composable
fun TvButtonSurface(
    label: String,
    style: TvButtonStyle,
    enabled: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    loading: Boolean = false,
) {
    val palette = tvButtonColors(style = style, enabled = enabled, isFocused = isFocused)
    Box(
        modifier = modifier
            .height(dimensionResource(TvR.dimen.tv_button_height))
            .clip(BingeShapes.Pill)
            .background(palette.container)
            .border(dimensionResource(TvR.dimen.tv_button_border_width), palette.border, BingeShapes.Pill)
            .padding(horizontal = dimensionResource(TvR.dimen.tv_button_padding_horizontal))
            .semantics { if (loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = if (loading) Modifier.alpha(0f) else Modifier,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_button_icon_gap)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = palette.content,
                    modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = palette.content,
                textAlign = TextAlign.Center,
            )
        }
        if (loading) TvButtonSpinner(color = palette.content)
    }
}
