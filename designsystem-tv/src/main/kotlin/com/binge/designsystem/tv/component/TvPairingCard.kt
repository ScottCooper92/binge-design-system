package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
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
    ) {
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
