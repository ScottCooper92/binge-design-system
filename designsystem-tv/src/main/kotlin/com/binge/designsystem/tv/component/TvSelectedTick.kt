package com.binge.designsystem.tv.component

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
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
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
 * Takes no tint: the primary disc with its onPrimary glyph stays legible over any artwork and over a focus
 * fill, so there is no decision to hand the caller.
 */
@Composable
fun TvSelectedTickBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dimensionResource(TvR.dimen.tv_selected_tick_size))
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_selected_tick_glyph_size)),
        )
    }
}
