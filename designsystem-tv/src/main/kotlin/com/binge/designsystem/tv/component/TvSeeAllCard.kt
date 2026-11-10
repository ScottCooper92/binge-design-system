package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val MAX_PEEK = 3
private const val PEEK_TILT_DEGREES = 3f
private const val PEEK_SCALE = 0.8f

/** How far each layer sits from the middle one, as a share of the cell's own width and height. */
private const val PEEK_SPREAD = 0.04f
private const val PEEK_SCRIM_ALPHA = 0.6f

/**
 * The card that closes a row: press it to open the whole set behind the row.
 *
 * It fills the cell the caller gives it, so the caller's [modifier] sets the size and the shape of the row it ends:
 * a poster's 2:3, a cast portrait, a 16:9 gallery frame. [label] reads "All 24" when the caller has a count and
 * "See all" when it has none; the arrow sits beside it on the front layer.
 *
 * [peek] is up to [MAX_PEEK] image slots the caller fills (extra ones are ignored, and this module loads no images).
 * They fan out behind the label, the first slot nearest the front, wholly inside the cell, under a scrim that keeps the label legible.
 * With none, the card is a plain fill. Focus is the cell's own ring and the same press handling as any cell.
 */
@Composable
fun TvSeeAllCard(
    label: String,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    peek: List<@Composable BoxScope.() -> Unit> = emptyList(),
) {
    val slots = peek.take(MAX_PEEK)
    Box(
        modifier =
            modifier
                // Ring before clip: it draws outside the bounds, so an earlier clip cuts it off.
                .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                .clip(BingeShapes.MediaCard)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .tvClickable(role = Role.Button, onFocusChanged = onFocusChanged, onClick = onClick)
                .semantics { contentDescription = label },
    ) {
        // Drawn back to front: the last slot is furthest back. The layers are scaled to PEEK_SCALE and then fanned about the middle
        // one, by an offset and a tilt small enough that the corners stay inside the cell, so the clip never cuts the fan flat.
        slots.indices.reversed().forEach { depth ->
            val from = depth - (slots.size - 1) / 2f
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = PEEK_SCALE
                            scaleY = PEEK_SCALE
                            translationX = size.width * PEEK_SPREAD * from
                            translationY = -size.height * PEEK_SPREAD * from
                            rotationZ = PEEK_TILT_DEGREES * from
                        }.clip(BingeShapes.MediaCard),
            ) { slots[depth](this) }
        }
        if (slots.isNotEmpty()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = PEEK_SCRIM_ALPHA)))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(dimensionResource(DesR.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s), Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_see_all_icon)),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
