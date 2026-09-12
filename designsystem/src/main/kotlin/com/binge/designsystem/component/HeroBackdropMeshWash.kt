package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.painter.Painter

/**
 * Alphas for the phone hero mesh wash. Deliberately low: the wash is a tonal enrichment drawn **under** the
 * legibility scrim, so it must not compete with the still — the scrim above it, unchanged, is what still holds
 * the copy's contrast. The top corners carry the accent tones; the bottom corners fade toward transparent so
 * the wash never doubles up with the deep foot of the vertical scrim under the copy.
 */
private const val MESH_TOP_ALPHA = 0.24f
private const val MESH_BOTTOM_ALPHA = 0.06f

/**
 * A `MeshGradientPainter` (Compose 1.12) tonal wash for the phone cinematic heroes: a 2×2 vertex grid pulling
 * two accent tones softly across the backdrop, richer than a straight vertical scrim. Layered between the
 * still and the legibility scrim, so it enriches the artwork without touching text contrast.
 *
 * [accentStart] and [accentEnd] are theme tokens resolved by the caller and fixed per composition, so a
 * screenshot bakes the same wash every run.
 *
 * Clipped to bounds: a mesh patch's edges are cubic beziers, so the bottom row bows *below* the box it is
 * given, and `paint` does not clip. Unclipped, the bottom vertices' accents bled past the hero's foot as a
 * curved amber-to-purple band over the page background beneath it.
 */
@Composable
fun HeroBackdropMeshWash(accentStart: Color, accentEnd: Color) {
    val painter = rememberHeroMeshPainter(accentStart, accentEnd)
    Box(modifier = Modifier.fillMaxSize().clipToBounds().paint(painter))
}

@Composable
private fun rememberHeroMeshPainter(accentStart: Color, accentEnd: Color): Painter {
    val topStart = accentStart.copy(alpha = MESH_TOP_ALPHA)
    val topEnd = accentEnd.copy(alpha = MESH_TOP_ALPHA)
    val bottomStart = accentStart.copy(alpha = MESH_BOTTOM_ALPHA)
    val bottomEnd = accentEnd.copy(alpha = MESH_BOTTOM_ALPHA)
    return remember(topStart, topEnd, bottomStart, bottomEnd) {
        MeshGradientPainter(rows = 2, columns = 2) {
            setVertex(0, 0, Offset(0f, 0f), topStart)
            setVertex(0, 1, Offset(1f, 0f), topEnd)
            setVertex(1, 0, Offset(0f, 1f), bottomStart)
            setVertex(1, 1, Offset(1f, 1f), bottomEnd)
        }
    }
}
