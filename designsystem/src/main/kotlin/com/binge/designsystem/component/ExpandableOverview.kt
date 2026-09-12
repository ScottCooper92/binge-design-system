package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

private const val OVERVIEW_COLLAPSED_LINES = 3

/**
 * Body copy that collapses to [OVERVIEW_COLLAPSED_LINES] and offers a toggle once it has more to
 * show.
 *
 * [initiallyOverflowing] seeds that toggle's visibility. Left to itself the component only learns it
 * overflowed from `onTextLayout`, which fires after the frame the preview screenshot lane captures —
 * so the collapsed-with-toggle appearance, the component's only interactive state, was unrenderable
 * there and no baseline held it (#2291). Seeding it makes the state a parameter, the same move that
 * makes TV focus screenshot-testable. Preview and test use only: at runtime the layout pass reports
 * the truth, and passing `true` for copy that does not overflow would draw a toggle that expands
 * nothing.
 */
@Composable
fun ExpandableOverview(
    text: String,
    modifier: Modifier = Modifier,
    initiallyOverflowing: Boolean = false,
    // Reported on expand only: collapsing again answers no question the expand has not already
    // answered (#2272). Defaulted, so the five callers that report nothing compose unchanged.
    onExpand: () -> Unit = {},
) {
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    var overflows by remember(text, initiallyOverflowing) { mutableStateOf(initiallyOverflowing) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else OVERVIEW_COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!overflows) overflows = it.hasVisualOverflow },
        )
        if (overflows || expanded) {
            TextButton(
                onClick = {
                    if (!expanded) onExpand()
                    expanded = !expanded
                },
                contentPadding = PaddingValues(dimensionResource(R.dimen.zero)),
            ) {
                Text(
                    text = stringResource(if (expanded) R.string.detail_show_less else R.string.detail_show_more),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExpandableOverviewShort() {
    BingeExpressiveTheme {
        ExpandableOverview(text = "A short overview that fits in three lines.")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExpandableOverviewLong() {
    BingeExpressiveTheme {
        ExpandableOverview(
            text = "A billionaire dons a bat costume and wages a one-man war on crime in Gotham City. " +
                "When a new criminal known as the Joker emerges, Batman must confront chaos itself. " +
                "The Dark Knight faces his greatest test as moral lines blur. " +
                "This long overview overflows the collapsed state to show the 'Show more' button.",
        )
    }
}
