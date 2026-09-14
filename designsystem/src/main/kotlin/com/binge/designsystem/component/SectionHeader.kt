package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * A section title with an optional "more" affordance. [startPadding] defaults to [horizontalPadding]
 * plus the nav overlay's start inset, so a header under an overlaying rail clears it without the
 * caller thinking about it; a caller that has already added the inset (a carousel padding its row to
 * match) passes its own value.
 *
 * The title carries `heading()` so TalkBack and Switch Access can jump section to section: this is
 * the app's only section-title primitive, so marking it here is what gives every carousel, detail
 * section and search group a landmark. The "see all" button describes itself with the section name,
 * since a hub carries one per carousel and they would otherwise all announce identically.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_m),
    startPadding: Dp = horizontalPadding + navOverlayStart(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = startPadding,
                end = horizontalPadding,
                top = dimensionResource(R.dimen.section_header_padding_v),
                bottom = dimensionResource(R.dimen.section_header_padding_v),
            )
            // Floored at the touch target, not just any height: M3 already expands the "More" TextButton to it, so
            // without the floor the same header renders 16dp shorter with no trailing action — adjacent rails on different
            // rhythms, and no one height a skeleton could reserve for both. The carousel and detail skeletons reserve it.
            .heightIn(min = dimensionResource(R.dimen.min_touch_target)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        when {
            trailingContent != null -> trailingContent()
            onMoreClick != null -> {
                val seeAllLabel = stringResource(R.string.action_see_all_in_section, title)
                TextButton(
                    onClick = onMoreClick,
                    shape = BingeShapes.Pill,
                    contentPadding = PaddingValues(
                        horizontal = dimensionResource(R.dimen.padding_s),
                        vertical = dimensionResource(R.dimen.padding_xs),
                    ),
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = dimensionResource(R.dimen.zero),
                            minHeight = dimensionResource(R.dimen.zero),
                        ).semantics { contentDescription = seeAllLabel },
                ) {
                    Text(
                        text = stringResource(R.string.action_more),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSectionHeader() {
    BingeExpressiveTheme {
        SectionHeader(title = "Trending Movies", onMoreClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSectionHeaderNoMore() {
    BingeExpressiveTheme {
        SectionHeader(title = "Cast")
    }
}
