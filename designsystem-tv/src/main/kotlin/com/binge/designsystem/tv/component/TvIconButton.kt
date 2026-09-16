package com.binge.designsystem.tv.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntSize
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.theme.tvButtonColors
import com.binge.designsystem.tv.R as TvR

/**
 * A circular, icon-only 10-foot control that expands into a labelled pill on focus — the same
 * expand-on-focus idiom [com.binge.designsystem.tv.nav.BingeTvNavRail] uses to reveal its destinations'
 * labels, so a user who has already learnt the rail has learnt this too.
 *
 * [label] never renders as its own [Text] node while resting, so it is required rather than optional: it
 * is the control's accessible name the whole time, and the copy the reveal shows once focused.
 *
 * Coloured by [tvButtonColors] like [TvButton], so an icon button focuses amber (red for
 * [TvButtonStyle.Destructive]) exactly like every other control.
 *
 * [initiallyFocused] seeds the focus flag so a screenshot can show the revealed frame (a baseline runs no
 * coroutines, so no real focus event lands); production leaves it false. Reach for [TvIconButtonSurface]
 * when the caller owns the flag outright.
 */
@Composable
fun TvIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TvButtonStyle = TvButtonStyle.Secondary,
    enabled: Boolean = true,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    TvIconButtonSurface(
        icon = icon,
        label = label,
        style = style,
        enabled = enabled,
        isFocused = focused,
        modifier = modifier
            .tvClickable(enabled = enabled, onFocusChanged = { focused = it }, onClick = onClick),
    )
}

/**
 * The stateless surface behind [TvIconButton] — focus is a parameter, not owned here, so a preview or
 * screenshot can render the revealed frame without a real focus request landing by capture time.
 *
 * The surface is a fixed-diameter circle at rest and grows into a pill as [label] joins [icon], animated
 * by [Modifier.animateContentSize] rather than a hand-tracked target width — [label] is a caller-supplied
 * string of arbitrary length, so there is no one expanded width to animate towards. `animateContentSize`
 * settles at its target on the very first composition (nothing to animate from yet), so a screenshot
 * rendering a fixed [isFocused] needs no real animation frame to land on the right size.
 *
 * [label] is applied once, to the surface itself via [clearAndSetSemantics], rather than conditionally —
 * a screen reader gets a stable accessible name in both states, and the [Text] added on focus does not
 * double-announce it.
 */
@Composable
fun TvIconButtonSurface(
    icon: ImageVector,
    label: String,
    style: TvButtonStyle,
    enabled: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = tvButtonColors(style = style, enabled = enabled, isFocused = isFocused)
    val diameter = dimensionResource(TvR.dimen.tv_icon_button_diameter)
    val sizeMotion: FiniteAnimationSpec<IntSize> = if (LocalReduceMotion.current) snap() else spring()
    Box(
        modifier = modifier
            .heightIn(min = diameter)
            .widthIn(min = diameter)
            .animateContentSize(animationSpec = sizeMotion)
            .clip(BingeShapes.Pill)
            .background(palette.container)
            .border(dimensionResource(TvR.dimen.tv_button_border_width), palette.border, BingeShapes.Pill)
            .padding(horizontal = dimensionResource(TvR.dimen.tv_icon_button_padding_horizontal))
            .clearAndSetSemantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_button_icon_gap)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.content,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
            )
            if (isFocused) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.content,
                )
            }
        }
    }
}
