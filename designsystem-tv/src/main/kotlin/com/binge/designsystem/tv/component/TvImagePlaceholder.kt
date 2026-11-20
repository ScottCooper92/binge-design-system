package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * What a TV image slot paints when there is no image: a type glyph over the [title]. TV cards are
 * poster-only (no caption), so a card with no artwork otherwise identifies nothing at all (#1497).
 */
@Composable
fun TvImagePlaceholder(
    kind: TvImageKind,
    title: String?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            // The card owns the content description (#1335), so a placeholder that published its own title
            // would make every artwork-less card announce twice — and would give two nodes the same title,
            // which broke TvGridPaneFocusMemoryTest's title-matching backdrop assertions.
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        val glyphSize = tvPlaceholderGlyphSize(minOf(maxWidth, maxHeight))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        ) {
            Icon(
                imageVector = kind.glyph(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = GLYPH_ALPHA),
                modifier = Modifier.size(glyphSize),
            )
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = TITLE_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(DesR.dimen.padding_s)),
                )
            }
        }
    }
}

/**
 * The glyph per kind. `Episode` shares `TvShow`'s screen glyph (the repo has no episode glyph) but stays a
 * distinct kind so it can diverge later without touching a call site.
 */
private fun TvImageKind.glyph(): ImageVector =
    when (this) {
        TvImageKind.Movie -> Icons.Filled.Movie
        TvImageKind.TvShow -> Icons.Filled.Tv
        TvImageKind.Person -> Icons.Filled.Person
        TvImageKind.Episode -> Icons.Filled.Tv
        TvImageKind.Photo -> Icons.Filled.Image
    }

/**
 * The glyph's size in a slot whose smaller side measures [slot]: a share of it, capped.
 *
 * Shared rather than inlined because one slot draws this glyph twice — [TvCastCardPlate] is the cast
 * avatar's loading state and [TvImagePlaceholder] its error state, and the two are meant to match. Holding
 * the rule in two places is how they stopped matching in the first place (#2558).
 */
@Composable
@ReadOnlyComposable
internal fun tvPlaceholderGlyphSize(slot: Dp): Dp =
    (slot * GLYPH_FRACTION).coerceAtMost(dimensionResource(TvR.dimen.tv_placeholder_glyph_max))

/**
 * Share of the slot's *smaller* side. Off the width alone, a 16:9 still got a glyph 89% of its own height and
 * the title was sliced by the cell edge; the smaller side is the one the content has to fit inside.
 */
private const val GLYPH_FRACTION = 0.28f

/**
 * Between the phone's 38% and the full-strength tint this carried. A glyph at full strength is the loudest
 * thing on an artwork-less row, and the phone's alpha alone is thin at three metres. Internal for the same
 * reason as [tvPlaceholderGlyphSize]: [TvCastCardPlate] tints its glyph off this one.
 */
internal const val GLYPH_ALPHA = 0.6f

/** Ellipsised at two: a third line in a 16:9 still is a line the cell has no room for. */
private const val TITLE_LINES = 2
