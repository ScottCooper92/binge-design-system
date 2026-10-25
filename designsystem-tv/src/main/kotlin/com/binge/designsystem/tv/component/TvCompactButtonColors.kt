package com.binge.designsystem.tv.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.tv.theme.TvButtonColors
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.theme.tvButtonColors

/** The primary rests in a faint white pill; focus fills white, as in the top bar and tab strip. */
private const val PRIMARY_REST_ALPHA = 0.2f

/** The compact button's palette: [tvButtonColors] with the v2 white focus fill, and a white-tinted resting primary. */
@Composable
internal fun tvCompactButtonColors(
    style: TvButtonStyle,
    enabled: Boolean,
    isFocused: Boolean,
): TvButtonColors {
    val base = tvButtonColors(style = style, enabled = enabled, isFocused = isFocused)
    if (!enabled || (style != TvButtonStyle.Primary && style != TvButtonStyle.Secondary)) return base
    val scheme = MaterialTheme.colorScheme
    return when {
        isFocused -> base.copy(container = scheme.onSurface, content = scheme.surface, border = scheme.onSurface)
        style == TvButtonStyle.Primary ->
            base.copy(container = scheme.onSurface.copy(alpha = PRIMARY_REST_ALPHA), content = scheme.onSurface, border = Color.Transparent)
        else -> base.copy(content = scheme.onSurface)
    }
}
