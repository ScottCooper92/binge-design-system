package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.focus.tvFocusTarget
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * Facts as one tile: an optional [icon] at the top start, and a [title] over a column of [lines]. There is nothing to press,
 * but it takes focus, so the remote can scroll it into view in a sheet or a page; focused, it shows the primary ring a focused
 * button does, never a fill. A screen reader reads the title and lines together, as one item. [initiallyFocused] draws the
 * focused state, for a frame.
 */
@Composable
fun TvInfoCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .tvFocusIndicator(isFocused = focused, shape = BingeShapes.TvListItem)
                // Prototype: on a monochrome panel the card's fill matches the sheet, so it carries a faint outline at rest.
                .then(
                    if (!focused && com.binge.designsystem.tv.theme.LocalTvStyle.current == com.binge.designsystem.tv.theme.TvStyle.Monochrome) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f), BingeShapes.TvListItem)
                    } else {
                        Modifier
                    },
                )
                .clip(BingeShapes.TvListItem)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .tvFocusTarget { focused = it }
                .semantics(mergeDescendants = true) {}
                .padding(dimensionResource(DesR.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs))) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            lines.forEach { line ->
                Text(text = line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
