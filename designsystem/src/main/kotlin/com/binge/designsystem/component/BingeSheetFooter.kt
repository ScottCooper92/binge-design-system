package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * A primary full-width CTA pinned below a surface's content, in a band of that surface's own colour.
 *
 * Written for [androidx.compose.material3.ModalBottomSheet], and now used on a page too, where both
 * defaults have to be overridden: [containerColor] to `Color.Transparent` so the host's surface shows
 * through instead of a lighter band with a hard edge, and [bottomPadding] to zero where the host has
 * already inset its own content.
 *
 * [bottomPadding] is spacing, not a safe area. A sheet's own `contentWindowInsets` already applies
 * `safeDrawing` around its content — `BottomSheet` calls `windowInsetsPadding` with it — so this
 * never stood in for the gesture area, and a host that reads the inset itself is not reading it
 * twice. The KDoc used to say safe-area, which is what made a page stack three bottom paddings.
 */
@Composable
fun BingeSheetFooter(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    bottomPadding: Dp = dimensionResource(R.dimen.padding_l),
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(containerColor)
                .padding(horizontal = dimensionResource(R.dimen.padding_m))
                .padding(top = dimensionResource(R.dimen.padding_sm), bottom = bottomPadding),
    ) {
        BingeFilledButton(
            label = label,
            onClick = onClick,
            enabled = enabled,
            loading = loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeSheetFooter() {
    BingeExpressiveTheme {
        BingeSheetFooter(label = "Show results", onClick = {})
    }
}
