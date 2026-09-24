package com.binge.designsystem.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * Borderless counterpart of [BingeOutlinedButton], sharing its loading contract.
 *
 * [destructive] names what a caller otherwise says by passing an error [contentColor]: this control
 * commits or opens something irreversible. Naming it keeps the colour in one place, and keeps the
 * next destructive button from quietly defaulting to `primary` — which is how a repository ends up
 * with an error-toned confirmation behind a neutral trigger.
 */
@Composable
fun BingeTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    showLabel: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
    contentColor: Color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
) {
    TextButton(
        // Swallow taps while loading so the in-flight action can't be re-triggered,
        // without greying the button out (the spinner takes the label's place).
        onClick = { if (!loading) onClick() },
        modifier = modifier.defaultMinSize(minHeight = dimensionResource(R.dimen.button_filled_height)),
        shape = BingeShapes.Medium,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
                color = contentColor,
                strokeWidth = dimensionResource(R.dimen.progress_stroke_width),
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    // The icon carries the accessible name itself once the label's Text isn't there to.
                    contentDescription = if (showLabel) null else label,
                    modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
                )
                if (showLabel) Spacer(Modifier.width(dimensionResource(R.dimen.button_filled_icon_spacing)))
            }
            if (showLabel) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeTextButton() {
    BingeExpressiveTheme {
        BingeTextButton(label = "Cancel request", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeTextButtonWithIcon() {
    BingeExpressiveTheme {
        BingeTextButton(
            label = "Block this title",
            onClick = {},
            leadingIcon = Icons.Filled.Block,
            contentColor = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeTextButtonIconOnly() {
    BingeExpressiveTheme {
        BingeTextButton(
            label = "Block this title",
            onClick = {},
            leadingIcon = Icons.Filled.Block,
            showLabel = false,
            contentColor = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeTextButtonError() {
    BingeExpressiveTheme {
        BingeTextButton(
            label = "Cancel request",
            onClick = {},
            contentColor = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeTextButtonLoading() {
    BingeExpressiveTheme {
        BingeTextButton(label = "Cancel request", onClick = {}, loading = true)
    }
}
