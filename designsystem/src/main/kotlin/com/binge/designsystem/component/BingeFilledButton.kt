package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

@Composable
fun BingeFilledButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ),
) {
    Button(
        // Swallow taps while loading so the in-flight action can't be re-triggered,
        // without greying the button out (it stays filled with a spinner in place of the label).
        onClick = { if (!loading) onClick() },
        modifier = modifier.defaultMinSize(
            minWidth = ButtonDefaults.MinWidth,
            minHeight = dimensionResource(R.dimen.button_filled_height),
        ),
        shape = BingeShapes.Medium,
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.button_filled_padding_h),
            vertical = dimensionResource(R.dimen.zero),
        ),
        colors = colors,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
                color = colors.contentColor,
                strokeWidth = dimensionResource(R.dimen.progress_stroke_width),
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
                )
                Spacer(Modifier.width(dimensionResource(R.dimen.button_filled_icon_spacing)))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
