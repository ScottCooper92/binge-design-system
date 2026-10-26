package com.binge.designsystem.tv.component

import androidx.annotation.DimenRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.tv.theme.tvStyle
import com.binge.designsystem.tv.R as TvR

/**
 * The mark that means chosen — a service, a watch type, the current value of a setting.
 *
 * The accent colour carries focus on a TV surface, so selection needs a mark of its own rather than a
 * container treatment. A tick inside the item, not a border around it: it survives the focus fill landing on
 * the same item, and stays legible for a user who cannot separate the accent from grey.
 *
 * [tint] is required with no default. The colour that reads as "chosen" depends on what is behind the glyph,
 * and a default is how selection vanishes — the accent drawn over the accent focus fill. Pass the item's own
 * focused content colour where a focus fill can land under the tick, the primary colour where none can. Over
 * imagery, use [TvSelectedTickBadge], which needs no tint.
 *
 * The glyph is decoration and says nothing to a screen reader. Mark the row that holds it with
 * [Modifier.tvSelected], so the state is announced where the tick is drawn.
 */
@Composable
fun TvSelectedTick(tint: Color, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(dimensionResource(TvR.dimen.tv_selected_tick_size)),
    )
}

/**
 * The counterpart to [TvSelectedTick]: ruled out, for a value pressed past "chosen" into "not this".
 *
 * A crossed circle, not a minus: a minus at this size reads as a dash belonging to the label beside it, and
 * "excluded" must differ from "chosen" by shape rather than only by presence. [tint] is required for the
 * reason [TvSelectedTick]'s is.
 *
 * Deliberately not tinted with the error colour. Exclusion is a choice, not a fault, and a red glyph would
 * say something is wrong with this row.
 */
@Composable
fun TvExcludedMark(tint: Color, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.Block,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(dimensionResource(TvR.dimen.tv_selected_tick_size)),
    )
}

/**
 * [TvSelectedTick] on its own accent disc, for placing over imagery — a logo, a poster — where a bare glyph
 * would be lost against whatever is behind it.
 *
 * Takes no tint. The disc is the TV style's focus fill and the glyph its focus content colour, so the badge is the
 * accent under `TvStyle.Accent` and white on black under `TvStyle.Monochrome`. Either pair stays legible over any
 * artwork, so there is no decision to hand the caller.
 *
 * [size] is [TvSelectedTickBadgeSize.Standard] for a tile in a grid. Use [TvSelectedTickBadgeSize.Large] on a
 * large card that is read from across the room.
 */
@Composable
fun TvSelectedTickBadge(modifier: Modifier = Modifier, size: TvSelectedTickBadgeSize = TvSelectedTickBadgeSize.Standard) {
    Box(
        modifier = modifier
            .size(dimensionResource(size.discSize))
            .background(color = MaterialTheme.tvStyle.focusContainer, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.tvStyle.onFocusContainer,
            modifier = Modifier.size(dimensionResource(size.glyphSize)),
        )
    }
}

/** The sizes of [TvSelectedTickBadge]: the disc and the tick inside it. */
enum class TvSelectedTickBadgeSize(
    @param:DimenRes internal val discSize: Int,
    @param:DimenRes internal val glyphSize: Int,
) {
    /** The tile size, the same disc as [TvSelectedTick]. */
    Standard(TvR.dimen.tv_selected_tick_size, TvR.dimen.tv_selected_tick_glyph_size),

    /** For a large card, such as a full-height choice card, where the standard badge is too small to read. */
    Large(TvR.dimen.tv_selected_tick_badge_large_size, TvR.dimen.tv_selected_tick_badge_large_glyph_size),
}

/**
 * Marks a row as chosen or not for a screen reader, beside the [TvSelectedTick] that marks it for the eye. Put
 * it on the row that holds the tick, so the tick and the announced state cannot drift apart.
 */
fun Modifier.tvSelected(selected: Boolean): Modifier = semantics { this.selected = selected }
