package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.labelSmallEmphasis

/**
 * Theme-aware tint behind a tag's accent — paler in light (where the accents are darker for
 * contrast) so the label stays clear of WCAG AA. Mirrors RequestStateChip.
 */
private const val TAG_TINT_ALPHA_LIGHT = 0.12f
private const val TAG_TINT_ALPHA_DARK = 0.16f

/**
 * A compact category tag — a tinted rounded-rect with an optional leading [icon] and a label, for
 * fixed classifications like media type, issue type, or a user role (distinct from
 * [RequestStateChip], which is the rounded *status* pill with a dot).
 *
 * [tint] paints a same-hue wash behind a saturated label + icon (pass one of the AA-tuned
 * `BingeTheme.colors.status*` accents); a null [tint] is neutral — a `surfaceContainerHigh` wash
 * with `onSurfaceVariant` text. [uppercase] (default) matches the design's type tags (MOVIE,
 * VIDEO…); pass `false` for cased labels such as roles (Owner / Admin).
 */
@Composable
fun BingeTag(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
    fill: Color? = null,
    uppercase: Boolean = true,
) {
    val content = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
    // The icon is graphical (3:1), so it can run brighter than the label: callers pass a saturated
    // [fill] for it while the label + wash keep the AA [tint]. Defaults to [content] when unset.
    val iconColor = fill ?: content
    val container =
        if (tint == null) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            val alpha = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) TAG_TINT_ALPHA_DARK else TAG_TINT_ALPHA_LIGHT
            tint.copy(alpha = alpha)
        }
    Row(
        modifier = modifier
            .clip(BingeShapes.Tag)
            .background(container)
            .padding(
                horizontal = dimensionResource(R.dimen.padding_s),
                vertical = dimensionResource(R.dimen.tag_padding_v),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(dimensionResource(R.dimen.tag_icon_size)),
            )
        }
        Text(
            text = if (uppercase) label.uppercase() else label,
            style = MaterialTheme.typography.labelSmallEmphasis,
            color = content,
        )
    }
}
