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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.R as TvR

/**
 * A compact, squarer button, the v2 look against [TvButton]'s tall pill. Same states (disabled stays focusable and
 * inert, as [TvButton]'s does); shorter, tighter, and small-radius corners. [loading] hides the label and shows a spinner in
 * its place, keeping the width, and ignores presses while keeping focus. [initiallyFocused] seeds the focused look for frames.
 */
@Composable
fun TvCompactButton(
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
    val palette = tvCompactButtonColors(style = style, enabled = enabled, isFocused = focused)
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .height(dimensionResource(TvR.dimen.tv_compact_button_height))
            .clip(shape)
            .background(palette.container)
            .border(dimensionResource(TvR.dimen.tv_button_border_width), palette.border, shape)
            // Always focusable: a disabled button that drops focus (mid-request, say) throws it out of the page. It is inert instead.
            .tvClickable(role = Role.Button, onFocusChanged = { focused = it }, onClick = { if (enabled && !loading) onClick() })
            .padding(horizontal = dimensionResource(TvR.dimen.tv_compact_button_padding_horizontal))
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
                style = MaterialTheme.typography.titleSmall,
                color = palette.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (loading) {
            TvSpinner(
                color = palette.content,
                diameter = dimensionResource(TvR.dimen.tv_button_icon),
                stroke = dimensionResource(TvR.dimen.tv_button_spinner_stroke),
            )
        }
    }
}

/**
 * The icon-only companion to [TvCompactButton]: a square of its height with the same corners and palette, named for a
 * screen reader by [label]. Unlike `TvIconButton` it does not expand into a labelled pill on focus: the v2 pages' icons
 * stay squares, and [label] is for accessibility only.
 */
@Composable
fun TvCompactIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val palette = tvCompactButtonColors(style = TvButtonStyle.Secondary, enabled = true, isFocused = focused)
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .size(dimensionResource(TvR.dimen.tv_compact_button_height))
            .clip(shape)
            .background(palette.container)
            .border(dimensionResource(TvR.dimen.tv_button_border_width), palette.border, shape)
            .tvClickable(role = Role.Button, onFocusChanged = { focused = it }, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.content,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
        )
    }
}
