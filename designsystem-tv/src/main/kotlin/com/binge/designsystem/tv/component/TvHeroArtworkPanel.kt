package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.startHorizontalGradient
import com.binge.designsystem.theme.BingeTheme

/**
 * The backdrop is not full-bleed: it fills the right [BACKDROP_WIDTH_FRACTION] of its band, end-aligned, so
 * the copy sits on clean surface and the still reads as an intentional panel rather than a dim wash behind
 * the whole band. Shared by every hero band, so they cannot drift.
 */
internal const val BACKDROP_WIDTH_FRACTION = 0.62f

/**
 * The panel's horizontal veil, across its own width: fully opaque at its left edge so it melts into the
 * surface beside it, held near-opaque only while the copy overlaps (to [PANEL_HOLD_FRACTION], roughly where
 * the text ends), then eased open across the rest in even steps so the still fades in gradually. The hold
 * must outlast the copy — a veil that thinned across the text would lose it over a bright still — but past
 * the copy the ramp is deliberately gentle (the extra [PANEL_MID_FRACTION] stop) so a bright still does not
 * cut off abruptly just past the text edge.
 */
private const val PANEL_START_ALPHA = 1f
private const val PANEL_HOLD_FRACTION = 0.32f
private const val PANEL_HOLD_ALPHA = 0.85f
private const val PANEL_MID_FRACTION = 0.58f
private const val PANEL_MID_ALPHA = 0.55f
private const val PANEL_OPEN_FRACTION = 0.8f
private const val PANEL_OPEN_ALPHA = 0.28f
private const val PANEL_END_ALPHA = 0.06f

/**
 * Where the foot veil begins. Must reach full opacity at the panel's foot: below the detail band is flat
 * `background`, so anything short of opaque there leaves a seam where the artwork stops.
 */
private const val PANEL_FOOT_START_FRACTION = 0.55f

/**
 * Alphas for the optional [richBackdrop] mesh wash. Kept low on purpose: the wash is a tonal enrichment
 * drawn **under** the legibility veil, so it must never darken the still enough to compete with the still
 * itself — the veil above it, unchanged, is what still guarantees the copy's contrast. The accents sit on
 * the open side — right under LTR, left under RTL, see [TvHeroBackdropMeshWash] below — where the still
 * shows through and glow in the upper band, fading to transparent before the foot so they never pool under
 * the bed-foot veil that darkens the band's bottom edge.
 */
private const val MESH_ACCENT_ALPHA = 0.22f
private const val MESH_ANCHOR_ALPHA = 0.10f

/**
 * The end-aligned artwork panel shared by every hero band: [artwork] filling the right
 * [BACKDROP_WIDTH_FRACTION] of its band, veiled by a horizontal scrim that melts into [veilColor] at its
 * left edge, holds past the copy, then opens to near-clear at the right. The scrim lives **inside** the
 * panel so it veils the still, not the whole band.
 *
 * [veilColor] is the surface the panel sits on, so the veil dissolves into it seamlessly rather than showing
 * a two-tone edge: the page `background` for all three callers — the detail hero's unbounded band over the
 * detail screen's opaque base, the season board's band above the tabs, and the hub hero's bounded card,
 * filled with that same `background` so the card melts into the page rather than reading as a lighter box.
 *
 * [bedFoot] adds a vertical veil landing full [veilColor] at the foot. The detail hero and the season board
 * need it — below their bands is flat `background`, so a hard artwork edge there would seam; the hub's panel
 * fills a self-contained card with nothing below it, so it passes false.
 *
 * [artwork] is a slot because a screenshot has no network — a default-only panel would only bake its
 * fallback plate. The season board also crossfades inside that slot, so its still swaps under a still veil.
 *
 * [richBackdrop] opts the panel into a `MeshGradientPainter` tonal wash (Compose 1.12) drawn between the
 * still and the veil — a soft, non-linear pull of two accent tones into the still, richer than the flat
 * veil alone. Only the detail hero asks for it; the hub, season board and episode still keep the plain
 * veil so their renders are unchanged. It sits under the veil, so legibility is untouched.
 */
@Composable
fun BoxScope.TvHeroArtworkPanel(
    veilColor: Color,
    bedFoot: Boolean,
    richBackdrop: Boolean = false,
    artwork: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .fillMaxWidth(BACKDROP_WIDTH_FRACTION),
    ) {
        artwork()
        if (richBackdrop) {
            TvHeroBackdropMeshWash(veilColor = veilColor)
        }
        TvHeroArtworkScrim(veilColor = veilColor, bedFoot = bedFoot)
    }
}

/**
 * The mesh wash: a 3-row grid whose open-side accents glow in the upper band — accentPurple at the top,
 * primary at the mid — and fall to transparent by the foot, while the copy-side column stays near-clear so
 * the enrichment lives where the still shows and dissolves into the panel edge behind the text. Clearing
 * before the foot keeps the accent from pooling under the bed-foot veil below the band. Colours are theme
 * tokens, fixed per composition, so a screenshot bakes the same wash every run.
 *
 * **Which physical column is the copy side flips under RTL.** The panel is end-aligned, so it mirrors to the
 * screen's other edge and the copy that sat to its left now sits to its right — while these vertices are
 * absolute UV, `0f` being the physical left whatever the direction. Left unmirrored, the glow meant to sit
 * *away* from the copy lands behind it, and the wash disagrees with the veil drawn over it, which
 * [startHorizontalGradient] already mirrors.
 *
 * The colours swap rather than the coordinates: a mesh patch is defined by its vertex winding, and mirroring
 * x would invert it rather than reflect it.
 *
 * Clipped to bounds for the same reason as the phone hero's mesh wash: a mesh patch's edges are cubic beziers that
 * bow outside the box, and `paint` does not clip. The transparent bottom row means nothing can pool past the
 * foot, but the top row's accent would otherwise bleed above the panel.
 */
@Composable
private fun TvHeroBackdropMeshWash(veilColor: Color) {
    val accentTop = BingeTheme.colors.accentPurple.copy(alpha = MESH_ACCENT_ALPHA)
    val accentMid = MaterialTheme.colorScheme.primary.copy(alpha = MESH_ACCENT_ALPHA)
    val anchor = veilColor.copy(alpha = MESH_ANCHOR_ALPHA)
    val copyOnTheRight = LocalLayoutDirection.current == LayoutDirection.Rtl
    val painter = remember(accentTop, accentMid, anchor, copyOnTheRight) {
        val leftTop = if (copyOnTheRight) accentTop else anchor
        val rightTop = if (copyOnTheRight) anchor else accentTop
        val leftMid = if (copyOnTheRight) accentMid else Color.Transparent
        val rightMid = if (copyOnTheRight) Color.Transparent else accentMid
        MeshGradientPainter(rows = 3, columns = 2) {
            setVertex(0, 0, Offset(0f, 0f), leftTop)
            setVertex(0, 1, Offset(1f, 0f), rightTop)
            setVertex(1, 0, Offset(0f, 0.5f), leftMid)
            setVertex(1, 1, Offset(1f, 0.5f), rightMid)
            setVertex(2, 0, Offset(0f, 1f), Color.Transparent)
            setVertex(2, 1, Offset(1f, 1f), Color.Transparent)
        }
    }
    Box(modifier = Modifier.fillMaxSize().clipToBounds().paint(painter))
}

/** The veil that beds the panel's copy, ramping from the artwork's start edge. */
@Composable
private fun TvHeroArtworkScrim(veilColor: Color, bedFoot: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                startHorizontalGradient(
                    0f to veilColor.copy(alpha = PANEL_START_ALPHA),
                    PANEL_HOLD_FRACTION to veilColor.copy(alpha = PANEL_HOLD_ALPHA),
                    PANEL_MID_FRACTION to veilColor.copy(alpha = PANEL_MID_ALPHA),
                    PANEL_OPEN_FRACTION to veilColor.copy(alpha = PANEL_OPEN_ALPHA),
                    1f to veilColor.copy(alpha = PANEL_END_ALPHA),
                ),
            ),
    )
    if (bedFoot) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            PANEL_FOOT_START_FRACTION to veilColor.copy(alpha = 0f),
                            1f to veilColor,
                        ),
                    ),
                ),
        )
    }
}
