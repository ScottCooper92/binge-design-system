package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * A primary full-width CTA pinned below its host's content, in a band of the host's own colour.
 *
 * The defaults are a [androidx.compose.material3.ModalBottomSheet]'s, which is what it was written
 * for: [containerColor] matches the sheet's container so the band reads flush against it, and
 * [shape]/[tonalElevation] stay off ([RectangleShape], none) since the sheet's own chrome already
 * carries the rounded top corner this footer sits inside. A page overrides [containerColor] to
 * `Color.Transparent` so its own surface shows through rather than a lighter band with a hard edge,
 * and [bottomPadding] to zero where its scaffold has already inset the content — or, for a page
 * whose primary action anchors in a `bottomBar` slot outside the scaffold's own body, reaches for
 * [BingeShapes.HeroTop] and [R.dimen.snackbar_elevation] instead, so it reads as a raised band of
 * its own rather than the page's background continuing under the button.
 *
 * [bottomPadding] is spacing, not a safe area. A sheet's own `contentWindowInsets` already applies
 * `safeDrawing` around its content — `BottomSheet` calls `windowInsetsPadding` with it — so this
 * never stood in for the gesture area, and a host that reads the inset itself is not reading it
 * twice. The KDoc used to say safe-area, which is what made a page stack three bottom paddings.
 *
 * [clearsNavigationBar] is the one case that does need the real inset: a `bottomBar` slot sits
 * outside a Scaffold's own body, so nothing insets it automatically. On, only the button clears the
 * gesture area — the [Surface] itself stays unpadded so its background still extends full-bleed
 * behind it, edge to edge like the rest of the window.
 *
 * [horizontalPadding] defaults to the fixed spacing a sheet's own content already uses. A page whose
 * body reads [resolvedContentInset] passes that instead, so the button's edges land under its own
 * content's rather than a narrower fixed one — the two would otherwise visibly disagree the moment a
 * window is wide enough for [resolvedContentInset] to ramp past this default.
 */
@Composable
fun BingeActionFooter(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape: Shape = RectangleShape,
    tonalElevation: Dp = dimensionResource(R.dimen.zero),
    bottomPadding: Dp = dimensionResource(R.dimen.padding_l),
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_m),
    clearsNavigationBar: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        shape = shape,
        tonalElevation = tonalElevation,
    ) {
        BingeFilledButton(
            label = label,
            onClick = onClick,
            enabled = enabled,
            loading = loading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (clearsNavigationBar) Modifier.navigationBarsPadding() else Modifier)
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
private fun PreviewBingeActionFooterElevated() {
    BingeExpressiveTheme {
        BingeActionFooter(
            label = "Add a slider",
            onClick = {},
            shape = BingeShapes.HeroTop,
            tonalElevation = dimensionResource(R.dimen.snackbar_elevation),
            horizontalPadding = resolvedContentInset(),
            clearsNavigationBar = true,
        )
    }
}
