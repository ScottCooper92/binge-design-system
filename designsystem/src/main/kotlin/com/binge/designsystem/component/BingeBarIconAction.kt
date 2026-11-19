package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R

/**
 * An icon action in a screen's top bar, in the round button the bar draws beside Back. It reads the bar's tone
 * ([LocalTopBarActionTone]) and tint ([LocalTopBarActionTint]), so the same action is a plain icon on an opaque bar and
 * a scrim-backed circle on a transparent one, and it is [R.dimen.top_bar_icon_size] square either way.
 *
 * [glassBackgroundAlpha] is the bar's own fade: the value the bar hands its `actions` slot, passed through so the circle's
 * backing hands off to the bar's scrim as it fades in rather than stacking with it. [contentDescription] is required,
 * because an icon-only action has no other name and the consumer owns the words. A disabled action dims.
 *
 * @need add an icon action to a screen's top bar, beside Back
 * @group bars
 * @sample BingeBarIconActionSample
 * @replaces a hand-built ExpressiveIconButton wired to the bar's tone, tint and glass fade
 */
@Composable
fun BingeBarIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glassBackgroundAlpha: Float = 1f,
) {
    ExpressiveIconButton(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        enabled = enabled,
        tint = LocalTopBarActionTint.current,
        tone = LocalTopBarActionTone.current,
        size = dimensionResource(R.dimen.top_bar_icon_size),
        glassBackgroundAlpha = glassBackgroundAlpha,
    )
}
