package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
private const val PEEK_TILT_DEGREES = 2f

/** A fanned layer's width and height, as shares of the cell's own. */
internal const val PEEK_WIDTH_SHARE = 0.44f
private const val PEEK_HEIGHT_SHARE = 0.8f

/** How far each back layer sits from the one in front of it, sideways, as a share of the cell's width: the strip of it that shows. */
internal const val PEEK_STEP_SHARE = 0.26f

/** How far a back layer is raised above the front one, as a share of the cell's height. */
private const val PEEK_RAISE_SHARE = 0.02f

/** The front layer sits under the label and carries the heavier scrim; a back layer shows its image, so its scrim is light. */
private const val PEEK_FRONT_SCRIM_ALPHA = 0.6f
private const val PEEK_BACK_SCRIM_ALPHA = 0.25f

/**
 * Where each fanned layer's middle sits sideways, from the cell's middle, as a share of the cell's width; the first is the front
 * layer. One layer is centred, two straddle the middle, and three put the front one in the middle with a back layer out to each
 * side, so every back layer shows [PEEK_STEP_SHARE] of its width beside the layers in front of it.
 */
internal fun seeAllFanOffsets(count: Int): List<Float> =
    when (count.coerceIn(0, MAX_PEEK)) {
        0 -> emptyList()
        1 -> listOf(0f)
        2 -> listOf(-PEEK_STEP_SHARE / 2, PEEK_STEP_SHARE / 2)
        else -> listOf(0f, PEEK_STEP_SHARE, -PEEK_STEP_SHARE)
    }

/**
 * The share of the cell's width each fanned layer shows, in the same order as [seeAllFanOffsets]: the layer's own span less what the
 * layers in front of it cover. The front layer shows all of its width; a back layer shows the strip beside the ones before it.
 */
internal fun seeAllFanVisibleShares(count: Int): List<Float> {
    val offsets = seeAllFanOffsets(count)
    return offsets.mapIndexed { index, offset ->
        val left = offset - PEEK_WIDTH_SHARE / 2
        val right = offset + PEEK_WIDTH_SHARE / 2
        val covered =
            offsets.take(index).map { front -> (front - PEEK_WIDTH_SHARE / 2) to (front + PEEK_WIDTH_SHARE / 2) }
        // The share of [left, right] not covered by any interval in front: walk the cuts in order.
        val cuts = (listOf(left, right) + covered.flatMap { listOf(it.first, it.second) }).filter { it in left..right }.distinct().sorted()
        cuts
            .zipWithNext()
            .filter { (from, to) ->
                covered.none {
                    it.first <= from && to <= it.second
                }
            }.sumOf { (from, to) -> (to - from).toDouble() }
            .toFloat()
    }
}

/**
 * The card that closes a row: press it to open the whole set behind the row.
 *
 * It fills the cell the caller gives it, so the caller's [modifier] sets the size and the shape of the row it ends:
 * a poster's 2:3, a cast portrait, a 16:9 gallery frame. [label] reads "All 24" when the caller has a count and
 * "See all" when it has none; the arrow sits beside it on the front layer.
 *
 * [peek] is up to [MAX_PEEK] image slots the caller fills (extra ones are ignored, and this module loads no images).
 * They fan out behind the label, the first slot nearest the front, wholly inside the cell: the front layer in the middle under a
 * scrim that keeps the label legible, and each further layer beside it, showing about a quarter of the cell's width of its own image
 * under a lighter scrim, so a portrait or a face still reads as one. With none, the card is a plain fill. Focus is the cell's own ring and the same press handling as any cell.
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
        // Drawn back to front: the last slot is furthest back. Each layer is a real, smaller box (so its image crops to it and is
        // not squeezed) moved sideways by its offset, with a tilt small enough that its corners stay inside the cell.
        val offsets = seeAllFanOffsets(slots.size)
        slots.indices.reversed().forEach { depth ->
            val offset = offsets[depth]
            Box(
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(PEEK_WIDTH_SHARE)
                        .fillMaxHeight(PEEK_HEIGHT_SHARE)
                        .graphicsLayer {
                            translationX = size.width / PEEK_WIDTH_SHARE * offset
                            translationY = if (depth == 0) 0f else -size.height / PEEK_HEIGHT_SHARE * PEEK_RAISE_SHARE
                            rotationZ = PEEK_TILT_DEGREES * offset.compareTo(0f)
                        }.clip(BingeShapes.MediaCard),
            ) {
                slots[depth](this)
                val scrim = if (depth == 0) PEEK_FRONT_SCRIM_ALPHA else PEEK_BACK_SCRIM_ALPHA
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = scrim)))
            }
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
