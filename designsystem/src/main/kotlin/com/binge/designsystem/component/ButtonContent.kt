package com.binge.designsystem.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R

/**
 * What goes inside a [BingeFilledButton], [BingeOutlinedButton] or [BingeTextButton]: a spinner in place of everything while [loading], else a [leading]
 * slot, a [leadingIcon], the [label] and a [trailingIcon], in that order with the icon spacing between them. One body, so the three cannot drift apart.
 *
 * With [showLabel] false the label is not drawn and [leadingIcon] carries it as its own description, since an icon alone has no other accessible name;
 * the trailing icon belongs to the label and goes with it. The spinner takes [contentColor], the button's own.
 */
@Composable
internal fun ButtonContent(
    label: String,
    loading: Boolean,
    contentColor: Color,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    showLabel: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
            color = contentColor,
            strokeWidth = dimensionResource(R.dimen.progress_stroke_width),
        )
        return
    }
    if (leading != null) {
        leading()
        Spacer(Modifier.width(dimensionResource(R.dimen.button_filled_icon_spacing)))
    }
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
        if (trailingIcon != null) {
            Spacer(Modifier.width(dimensionResource(R.dimen.button_filled_icon_spacing)))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
            )
        }
    }
}
