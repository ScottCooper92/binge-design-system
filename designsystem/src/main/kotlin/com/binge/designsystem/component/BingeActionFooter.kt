package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * A primary full-width CTA pinned below a sheet's content, in a band of the sheet's own colour.
 *
 * The defaults are a [androidx.compose.material3.ModalBottomSheet]'s, which is what it was written for: [containerColor] matches the
 * sheet's container so the band reads flush against it, and the band is square and flat since the sheet's own chrome already carries
 * the rounded top corner this footer sits inside. A host that draws its own surface overrides [containerColor] to `Color.Transparent`
 * so that surface shows through rather than a lighter band with a hard edge, and [bottomPadding] to zero where it has already inset
 * the content.
 *
 * **For a page, use [com.binge.designsystem.template.ScreenFooter]**, as `ScreenFooter { BingeFilledButton(...) }`: it is the raised
 * bar with rounded top corners that clears the navigation bar and a side cutout and keeps the content's side inset, and every pinned
 * bar on a page sits the same. This footer is the sheet form and takes none of that.
 *
 * [destructive] gives the button the error tone, for a sheet whose one action removes or declines something.
 *
 * [failed] is the other use of that tone, and a different meaning: the sheet's primary send did not go through. The caller
 * passes the reason as [label] for as long as it wants it shown, and the reason is a few words ("Quota reached", "No permission",
 * "Couldn't request"): the full cause belongs in the dialog that [onClick], which stays the caller's, usually opens with a way
 * to retry (`docs/messages.md`). A longer reason wraps and the button grows rather than clip it, but a few words is what the
 * button is for. Beside the colour, a failed footer sets the `error` semantics with
 * the label, so a screen reader announces the reason as an error, which [destructive] does not do. A footer that is both gets the
 * same colours. It is for a failed send from a sheet's pinned primary; a page's footer reports a failure another way. Pass
 * [loading] or [failed], not both: a loading footer shows its spinner whatever [failed] says.
 *
 * [bottomPadding] is spacing, not a safe area. A sheet's own `contentWindowInsets` already applies
 * `safeDrawing` around its content — `BottomSheet` calls `windowInsetsPadding` with it — so this
 * never stood in for the gesture area, and a host that reads the inset itself is not reading it
 * twice. [horizontalPadding] defaults to the fixed spacing a sheet's own content already uses.
 *
 * [leadingIcon] matches the same action rendered as a [BingeOutlinedButton]/[BingeTextButton] tile
 * elsewhere — a caller whose action promotes to this footer only once it is the sheet's sole primary
 * CTA (dropping out of a tiered pool of two-or-more) should pass the same icon, so the action doesn't
 * lose its icon purely because of how many peers it currently has.
 *
 * @need pin a primary action to the bottom of a sheet
 * @group bars
 * @replaces a hand-built bottom Surface holding a full-width button
 */
@Composable
fun BingeActionFooter(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    bottomPadding: Dp = dimensionResource(R.dimen.padding_l),
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_m),
    destructive: Boolean = false,
    failed: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = RectangleShape,
    ) {
        BingeFilledButton(
            label = label,
            onClick = onClick,
            leadingIcon = leadingIcon,
            enabled = enabled,
            loading = loading,
            destructive = destructive || failed,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (failed) Modifier.semantics { error(label) } else Modifier)
                    .padding(horizontal = horizontalPadding)
                    .padding(top = dimensionResource(R.dimen.padding_sm), bottom = bottomPadding),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeActionFooter() {
    BingeExpressiveTheme {
        BingeActionFooter(label = "Show results", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeActionFooterWithIcon() {
    BingeExpressiveTheme {
        BingeActionFooter(
            label = "Open on your server",
            onClick = {},
            leadingIcon = Icons.AutoMirrored.Filled.OpenInNew,
        )
    }
}
