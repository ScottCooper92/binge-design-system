package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Footer for [androidx.compose.material3.ModalBottomSheet] containing a primary
 * full-width CTA. The wrapper paints the sheet's container colour and applies safe-area
 * padding around a [BingeFilledButton].
 */
@Composable
fun BingeSheetFooter(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = dimensionResource(R.dimen.padding_m))
                .padding(
                    top = dimensionResource(R.dimen.padding_sm),
                    bottom = dimensionResource(R.dimen.padding_l),
                ),
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
