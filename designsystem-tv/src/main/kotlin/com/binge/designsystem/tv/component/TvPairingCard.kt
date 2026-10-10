package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The second-device hand-off on one card: a [TvQrCode] for [payload], an [instruction], a [code] to type
 * and an [address] to open by hand. A phone without a camera app still has the address, so it sits with
 * the code, legible from across a room. The code and the address are monospace because they are typed.
 *
 * The card is as wide as its widest line and no wider. [codeDescription] is what a screen reader says for
 * [code], for a code read better as words than as its characters.
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
    PairingCardSurface(modifier) {
        TvQrCode(content = payload, contentDescription = qrDescription, modifier = Modifier.size(qrSize))
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
 * [message] such as "Getting a code". A sign-in panel keeps the card in place while it asks for one.
 */
@Composable
fun TvPairingCardLoading(
    message: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = dimensionResource(TvR.dimen.tv_pairing_qr_size),
) {
    PairingCardSurface(modifier) {
        TvSkeletonBlock(modifier = Modifier.size(qrSize))
        PairingMessage(message, qrSize)
    }
}

/**
 * [TvPairingCard] when asking for a code failed: the same card with the failure's glyph where the code to scan goes,
 * a [message], and [retryLabel] calling [onRetry]. [retryInitiallyFocused] takes focus as a parameter, so the focused
 * state can be screenshotted.
 *
 * The glyph's box is [qrSize] square while the card has the height, and gives up height first when it does not: in a
 * card whose height is bounded, a long message and the retry button keep their room and the box shrinks to fit, down
 * to the glyph itself.
 */
@Composable
fun TvPairingCardError(
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryInitiallyFocused: Boolean = false,
    qrSize: Dp = dimensionResource(TvR.dimen.tv_pairing_qr_size),
) {
    PairingCardSurface(modifier) {
        val iconSize = dimensionResource(TvR.dimen.tv_pairing_error_icon_size)
        Box(
            modifier = Modifier
                .width(qrSize)
                .weight(1f, fill = false)
                .heightIn(min = iconSize)
                .height(qrSize),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize),
            )
        }
        PairingMessage(message, qrSize)
        TvButton(label = retryLabel, onClick = onRetry, style = TvButtonStyle.Primary, initiallyFocused = retryInitiallyFocused)
    }
}

/** The card's chrome, shared by every state so the card keeps its shape as it changes state. */
@Composable
private fun PairingCardSurface(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            modifier
                .width(IntrinsicSize.Max)
                .clip(BingeShapes.AccountCard)
                .background(MaterialTheme.colorScheme.surface)
                .border(dimensionResource(TvR.dimen.tv_button_border_width), MaterialTheme.colorScheme.border, BingeShapes.AccountCard)
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
