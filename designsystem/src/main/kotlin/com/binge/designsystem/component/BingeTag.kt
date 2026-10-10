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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.labelSmallEmphasis
import com.binge.designsystem.theme.tonalContainer
import com.binge.designsystem.uppercaseLocalised

/**
 * A compact category tag — a tinted rounded-rect with an optional leading [icon] and a label, for
 * fixed classifications like media type, issue type, or a user role (distinct from
 * [StatusChip], which is the rounded *status* pill with a dot).
 *
 * [tint] paints a same-hue wash behind a saturated label + icon (pass a `BingeSentiment.accent()`
 * for [tint], and its `fill()` for [fill]); a null [tint] is neutral — a `surfaceContainerHigh`
 * wash with `onSurfaceVariant` text. [uppercase] (default) matches the design's type tags (MOVIE,
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
            tint.tonalContainer()
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
            text = if (uppercase) label.uppercaseLocalised() else label,
            style = MaterialTheme.typography.labelSmallEmphasis,
            color = content,
        )
    }
}
