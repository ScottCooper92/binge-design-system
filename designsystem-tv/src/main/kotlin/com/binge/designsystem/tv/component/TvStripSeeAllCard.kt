package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.tv.R as TvR

/**
 * The See-all card as the last cell of a strip whose row is the one focus target: the cell is [width] by [height] like the cards
 * it follows, and the card sits inside it by the focus ring's reach, so the ring is drawn within the strip's clip rather than cut
 * off at its top and bottom. Not a focus stop and not a node of its own for accessibility: the row owns the key, the click and the
 * announcement, and [isFocused] says it is on the card.
 */
@Composable
internal fun TvStripSeeAllCard(
    label: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    width: Dp,
    height: Dp,
    peek: List<@Composable BoxScope.() -> Unit>,
) {
    val ring = dimensionResource(TvR.dimen.tv_focus_ring_offset) + dimensionResource(TvR.dimen.tv_focus_ring_width)
    Box(Modifier.size(width, height).clearAndSetSemantics {}.padding(ring)) {
        TvSeeAllCard(
            label = label,
            isFocused = isFocused,
            onFocusChanged = {},
            onClick = onClick,
            modifier = Modifier.fillMaxSize().focusProperties { canFocus = false },
            peek = peek,
        )
    }
}

/** Drops the node and its children from accessibility while [hidden]: what the row announces instead says it. */
internal fun Modifier.unreadWhen(hidden: Boolean): Modifier = if (hidden) clearAndSetSemantics {} else this

/** Names the node [label] for accessibility, or leaves it as it is when [label] is null. */
internal fun Modifier.readAs(label: String?): Modifier = if (label == null) this else semantics { contentDescription = label }
