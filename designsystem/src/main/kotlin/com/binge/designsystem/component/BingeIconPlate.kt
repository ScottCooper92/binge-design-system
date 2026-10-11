package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.tonalContainer

/**
 * The tinted plate a row leads with: [content] drawn over a wash of [tint], which is `primary` unless the row
 * names its own. Settings rows and choice rows share it, so an icon reads the same wherever it leads.
 */
@Composable
internal fun BingeIconPlate(
    modifier: Modifier = Modifier,
    tint: Color? = null,
    content: @Composable (Color) -> Unit,
) {
    val accent = tint ?: MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.item_group_icon_size))
            .clip(BingeShapes.MoreCard)
            .background(accent.tonalContainer()),
        contentAlignment = Alignment.Center,
    ) {
        content(accent)
    }
}

/** [BingeIconPlate] around a single glyph. */
@Composable
internal fun BingeIconPlate(
    painter: Painter,
    modifier: Modifier = Modifier,
    tint: Color? = null,
) {
    BingeIconPlate(modifier = modifier, tint = tint) { accent ->
        Icon(
            painter = painter,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(dimensionResource(R.dimen.item_group_icon_glyph)),
        )
    }
}
