package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

private const val GLASS_BACKGROUND_ALPHA = 0.4f

/**
 * [IconButtonTone.Glass] stays the always-black/white pair ([BingeTheme.colors.scrim]) rather than
 * following the theme: it exists specifically for an icon floating over *unpredictable* imagery — a
 * hero backdrop that could be any colour — where only a guaranteed-dark backing keeps the icon
 * legible regardless of what's directly behind it or which theme is active. A caller whose bar
 * brings in its own theme-following scrim over a *known* surface travels its own title/foreground
 * with that scrim instead; the icon's self-contained circle doesn't need to.
 *
 * [glassBackgroundAlpha] multiplies that backing's alpha — full by default, dialled toward 0 by a
 * caller whose bar brings in its own scrim as it collapses ([DetailOverlayTopBar]), so the per-icon
 * backing hands off to that rather than stacking two washes once both are visible. Ignored by every
 * other tone.
 */
@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    tone: IconButtonTone = IconButtonTone.Default,
    size: Dp = dimensionResource(R.dimen.button_tonal_size),
    glassBackgroundAlpha: Float = 1f,
) {
    val background = when (tone) {
        IconButtonTone.Tonal -> MaterialTheme.colorScheme.surfaceContainerHigh
        IconButtonTone.Accent -> MaterialTheme.colorScheme.secondaryContainer
        IconButtonTone.Glass -> BingeTheme.colors.scrim.copy(alpha = GLASS_BACKGROUND_ALPHA * glassBackgroundAlpha)
        IconButtonTone.Default -> Color.Transparent
    }
    val containerModifier = if (tone == IconButtonTone.Default) {
        modifier
    } else {
        modifier.size(size).clip(BingeShapes.Pill).background(background)
    }
    IconButton(
        onClick = onClick,
        modifier = containerModifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExpressiveIconButton() {
    BingeExpressiveTheme {
        ExpressiveIconButton(
            onClick = {},
            icon = Icons.Filled.Favorite,
            contentDescription = "Favourite",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExpressiveIconButtonTonal() {
    BingeExpressiveTheme {
        ExpressiveIconButton(
            onClick = {},
            icon = Icons.Filled.Favorite,
            contentDescription = "Favourite",
            tone = IconButtonTone.Tonal,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExpressiveIconButtonAccent() {
    BingeExpressiveTheme {
        ExpressiveIconButton(
            onClick = {},
            icon = Icons.Filled.Favorite,
            contentDescription = "Favourite",
            tone = IconButtonTone.Accent,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExpressiveIconButtonGlass() {
    BingeExpressiveTheme {
        ExpressiveIconButton(
            onClick = {},
            icon = Icons.Filled.Favorite,
            contentDescription = "Favourite",
            tone = IconButtonTone.Glass,
            tint = BingeTheme.colors.onScrim,
        )
    }
}
