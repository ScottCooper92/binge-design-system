package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.theme.BingeTvCorners
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The second-device hand-off on one card: a [TvQrCode] for [payload], an [instruction], a [code] to type
 * and an [address] to open by hand. A phone without a camera app still has the address, so it sits with
 * the code, legible from across a room. The code and the address are monospace because they are typed.
 *
 * The card is as wide as its widest line and no wider. [codeDescription] is what a screen reader says for
 * [code], for a code read better as words than as its characters.
 *
 * [qrSize] is the code's side where there is room for it. In a card whose height or width is bounded, the code
 * gives up size first and the lines around it keep theirs, so a card in a short pane fits it; measured with unbounded
 * room, as in a scrolling pane, it keeps the full [qrSize].
 */
@Composable
fun TvPairingCard(
    payload: String,
    qrDescription: String,
    modifier: Modifier = Modifier,
    instruction: String? = null,
    code: String? = null,
    codeDescription: String? = null,
    address: String? = null,
    qrSize: Dp = dimensionResource(TvR.dimen.tv_pairing_qr_size),
) {
    FittedPairingCard(modifier) { bounded ->
        TvQrCode(content = payload, contentDescription = qrDescription, modifier = fitSquare(qrSize, bounded))
        instruction?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        code?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = codeDescription?.let { description -> Modifier.semantics { contentDescription = description } } ?: Modifier,
            )
        }
        address?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                softWrap = false,
                maxLines = 1,
            )
        }
    }
}

/**
 * [TvPairingCard] before it has a code: the same card with a shimmering block where the code to scan goes, and a
 * [message] such as "Getting a code". A sign-in panel keeps the card in place while it asks for one. The block is
 * [qrSize] square where there is room and gives up size first where there is not, as the code does in [TvPairingCard];
 * the message wraps at [qrSize], so a long one is as tall as it is on the card that follows.
 */
@Composable
fun TvPairingCardLoading(
    message: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = dimensionResource(TvR.dimen.tv_pairing_qr_size),
) {
    FittedPairingCard(modifier) { bounded ->
        TvSkeletonBlock(modifier = fitSquare(qrSize, bounded))
        PairingMessage(message, qrSize)
    }
}

/**
 * [PairingCardSurface] that says whether its height is bounded. A column hands a weighted child no height when its own is
 * unbounded, so the code can only be weighted where [content] is told it is.
 */
@Composable
private fun FittedPairingCard(modifier: Modifier, content: @Composable ColumnScope.(bounded: Boolean) -> Unit) {
    BoxWithConstraints(modifier = modifier, propagateMinConstraints = true) {
        val bounded = constraints.hasBoundedHeight
        PairingCardSurface(Modifier) { content(bounded) }
    }
}

/**
 * A square no bigger than [max], shrunk where the card is [bounded] to the height it has left once its other lines are
 * placed, and to the width it is bounded to. Unbounded, it stays [max].
 */
private fun ColumnScope.fitSquare(max: Dp, bounded: Boolean): Modifier =
    (if (bounded) Modifier.weight(1f, fill = false) else Modifier).layout { measurable, constraints ->
        val limit = max.roundToPx()
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else limit
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else limit
        val side = minOf(limit, height, width)
        val placeable = measurable.measure(Constraints.fixed(side, side))
        layout(side, side) { placeable.place(0, 0) }
    }

/** The card's chrome, shared by every state so the card keeps its shape as it changes state. */
@Composable
private fun PairingCardSurface(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            modifier
                .width(IntrinsicSize.Max)
                .clip(BingeTvCorners.Small)
                .background(MaterialTheme.colorScheme.surface)
                .border(dimensionResource(TvR.dimen.tv_button_border_width), MaterialTheme.colorScheme.border, BingeTvCorners.Small)
                .padding(dimensionResource(DesR.dimen.padding_l)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        content = content,
    )
}

/** Wraps at [width], the code's, so a long failure does not widen the card past the loading state's. */
@Composable
private fun PairingMessage(text: String, width: Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = width),
    )
}
