package com.binge.designsystem.tv.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.theme.BingeShapes
import io.nayuki.qrcodegen.QrCode
import kotlin.math.ceil

/**
 * The quiet zone the spec requires around a symbol, in modules. Without it a scanner cannot find the
 * symbol's edges against whatever is behind it, which on a TV is a dark app background.
 */
private const val QUIET_ZONE_MODULES = 4

/**
 * A QR code drawn with [Canvas] rather than rasterised — the second-device hand-off for what a TV cannot do
 * itself, such as a sign-in whose approval page has to open on a phone.
 *
 * `io.nayuki:qrcodegen` hands back booleans, so the symbol is drawn at display size and stays crisp on a
 * 1080p panel, where a phone-sized bitmap would upscale into scan-failing soft edges.
 *
 * Black on white is functional, not stylistic: ISO/IEC 18004 defines dark modules on a light background, and
 * real scanners are unreliable on an inverted symbol, so a themed token here would be a defect. The white
 * plate is also the quiet zone ([QUIET_ZONE_MODULES]); cropping it flush is the most common way to ship one
 * that will not scan.
 *
 * [contentDescription] is required rather than defaulted: a matrix of squares is opaque to a screen reader,
 * so the caller must say what scanning it does.
 */
@Composable
fun TvQrCode(
    content: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    // Keyed on the payload: encoding is real CPU work (Reed-Solomon over the whole payload), not something to
    // re-run per recomposition. MEDIUM ECC — HIGH only pays off against print damage, not a clean lit screen.
    val qr = remember(content) { QrCode.encodeText(content, QrCode.Ecc.MEDIUM) }
    Canvas(
        // Square by construction: letting a parent stretch it is another common way to ship one that will not scan.
        modifier = modifier
            .aspectRatio(1f)
            .clip(BingeShapes.AccountCard)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val modules = qr.size + QUIET_ZONE_MODULES * 2
        val moduleSize = size.minDimension / modules

        // The plate, quiet zone included — see the KDoc on why this is white and not a token.
        drawRect(color = Color.White, size = size)

        // Ceil the module size so adjacent modules overlap sub-pixel rather than leaving hairline plate seams
        // between them — a module boundary lands on a fractional pixel at nearly every size, and a scanner
        // reading a symbol shot through with light seams sees a different symbol.
        val drawn = Size(ceil(moduleSize), ceil(moduleSize))
        for (y in 0 until qr.size) {
            for (x in 0 until qr.size) {
                if (!qr.getModule(x, y)) continue
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(
                        x = (x + QUIET_ZONE_MODULES) * moduleSize,
                        y = (y + QUIET_ZONE_MODULES) * moduleSize,
                    ),
                    size = drawn,
                )
            }
        }
    }
}
