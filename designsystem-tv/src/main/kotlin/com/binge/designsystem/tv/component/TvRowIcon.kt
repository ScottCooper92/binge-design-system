package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.theme.tvStyle
import com.binge.designsystem.tv.R as TvR

private const val PLATE_ALPHA = 0.16f
private const val FOCUSED_PLATE_ALPHA = 0.2f

/**
 * A row's leading icon on its tinted plate, the TV twin of the settings-row icon box: the glyph in [tint], on a
 * low-alpha wash of the same colour. [tint] defaults to the accent.
 *
 * Focus is a parameter, as for the rows that host it. On the focus fill the plate becomes a wash of [focusedTint] and
 * the glyph takes it, so the icon inverts with the row instead of fighting its container. It defaults to the style's
 * content on focus, the ink on the focus fill; a row that fills with another colour, such as a destructive row's `error`, passes that fill's ink.
 */
@Composable
fun TvRowIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    isFocused: Boolean = false,
    focusedTint: Color = MaterialTheme.tvStyle.onFocusContainer,
) {
    val glyph = if (isFocused) focusedTint else tint
    val plate = glyph.copy(alpha = if (isFocused) FOCUSED_PLATE_ALPHA else PLATE_ALPHA)
    Box(
        modifier =
            modifier
                .size(dimensionResource(TvR.dimen.tv_row_icon_plate))
                .clip(BingeShapes.TvListItem)
                .background(plate),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = glyph,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
        )
    }
}
