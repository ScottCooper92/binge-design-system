package com.binge.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * Outlined counterpart of [BingeFilledButton], sharing its loading contract.
 *
 * [destructive] names what a caller otherwise says by passing an error [contentColor]: this control
 * commits or opens something irreversible. Naming it keeps the colour in one place, and keeps the
 * next destructive button from quietly defaulting to `primary` — which is how a repository ends up
 * with an error-toned confirmation behind a neutral trigger.
 */
@Composable
fun BingeOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
    contentColor: Color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
) {
    OutlinedButton(
        // Swallow taps while loading so the in-flight action can't be re-triggered,
        // without greying the button out (the spinner takes the label's place).
        onClick = { if (!loading) onClick() },
        modifier = modifier.defaultMinSize(minHeight = dimensionResource(R.dimen.button_filled_height)),
        shape = BingeShapes.Medium,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = BorderStroke(dimensionResource(R.dimen.hairline_thickness), contentColor),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
                color = contentColor,
                strokeWidth = dimensionResource(R.dimen.progress_stroke_width),
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeOutlinedButton() {
    BingeExpressiveTheme {
        BingeOutlinedButton(label = "Cancel request", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeOutlinedButtonLoading() {
    BingeExpressiveTheme {
        BingeOutlinedButton(label = "Cancel request", onClick = {}, loading = true)
    }
}
