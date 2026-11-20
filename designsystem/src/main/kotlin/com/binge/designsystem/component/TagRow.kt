package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * A wrapping row of neutral [BingeTag]s, one per entry of [tags]: genres, keywords, formats. The tags keep the order given and wrap onto
 * further lines, so the row is safe in a card sized from intrinsic height (it is a [WrappingRow]).
 *
 * With [compact] the tags use [BingeTag]'s smaller form and the gaps between them close up, for a long run of keywords under a title.
 * The labels are shown as given, not upper-cased, since they are free text.
 *
 * With [onTagClick] set, each tag is its own button: one focus and click target, announced by its label, and the callback gets the tag's
 * index in [tags]. Each tappable tag keeps its visual size but is at least [R.dimen.min_touch_target] square to touch, as Material's own
 * controls are, so its row is that tall. Without it the tags are plain text, take no taps, and are as small as they look.
 *
 * @param tags the labels, in display order.
 * @param compact the smaller tag and tighter gaps.
 * @param onTagClick called with the tapped tag's index; null makes the tags non-interactive.
 *
 * @need show a run of short neutral labels that may wrap, and optionally let each be tapped
 * @group cards
 * @replaces a hand-built WrappingRow or FlowRow of BingeTags
 */
@Composable
fun TagRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onTagClick: ((index: Int) -> Unit)? = null,
) {
    val gap = dimensionResource(if (compact) R.dimen.padding_xxs else R.dimen.padding_xs)
    WrappingRow(modifier = modifier, horizontalSpacing = gap, verticalSpacing = gap) {
        tags.forEachIndexed { index, label ->
            BingeTag(
                label = label,
                uppercase = false,
                compact = compact,
                modifier =
                    if (onTagClick == null) {
                        Modifier
                    } else {
                        Modifier
                            .minimumInteractiveComponentSize()
                            .clip(BingeShapes.Tag)
                            .clickable(role = Role.Button) { onTagClick(index) }
                    },
            )
        }
    }
}
