package com.binge.designsystem.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

private const val OVERVIEW_COLLAPSED_LINES = 3

/**
 * Body copy that collapses to [OVERVIEW_COLLAPSED_LINES] and offers a toggle once it has more to
 * show.
 *
 * Whether the copy overflows is decided in the same pass that places it: the text is measured at the width it is given, in the style it is drawn in,
 * so the first frame already has the toggle or not. Nothing below the overview moves a frame later, which an overflow learned from `onTextLayout`
 * and applied by a recomposition did.
 *
 * [initiallyOverflowing] forces the toggle on regardless of the measure. It is a preview and test seam from before the measure existed, kept so
 * existing callers compile; passing `true` for copy that does not overflow draws a toggle that expands nothing.
 *
 * @need show long body text that collapses with a toggle
 * @group rows
 * @replaces a hand-built Text with a show-more toggle
 */
@Composable
fun ExpandableOverview(
    text: String,
    modifier: Modifier = Modifier,
    initiallyOverflowing: Boolean = false,
    // Reported on expand only: collapsing again answers no question the expand has not already
    // answered. Defaulted, so the five callers that report nothing compose unchanged.
    onExpand: () -> Unit = {},
) {
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    val style = LocalTextStyle.current.merge(MaterialTheme.typography.bodyMedium)
    val measurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        // Measured at the width the text will be given, so the answer is the one the layout would report.
        val overflows =
            initiallyOverflowing ||
                (
                    constraints.hasBoundedWidth &&
                        measurer
                            .measure(
                                text = text,
                                style = style,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = OVERVIEW_COLLAPSED_LINES,
                                constraints = Constraints(maxWidth = constraints.maxWidth),
                            ).hasVisualOverflow
                )
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else OVERVIEW_COLLAPSED_LINES,
                overflow = TextOverflow.Ellipsis,
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
