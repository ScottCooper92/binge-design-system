package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * A round tonal icon button with a short [label] under it, for a row of secondary actions beside a primary one (a detail page's
 * "Trailer" and "Share" next to "Request"). The circle is [ExpressiveIconButton]'s tonal button: the same size and container.
 *
 * The button is a column of a fixed width, a little wider than the circle, so a short label has room and the label can never push the
 * circle off its middle. The label is one line, centred and ellipsized, at any font size. The caller owns the words, and the spacing
 * between buttons.
 *
 * The whole column is one click target: a tap on the label presses the button too. It is one accessibility item with the button role,
 * announcing [contentDescription], or the [label] when that is null, so the label is not a focus stop of its own. Pressing scales the
 * column as the carousel tiles do and ripples the circle. A button that is not [enabled] takes no taps, and its circle and label dim by
 * [DISABLED_ALPHA].
 */
@Composable
fun LabelledIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val announced = contentDescription ?: label
    Column(
        modifier = modifier
            .width(dimensionResource(R.dimen.labelled_icon_button_width))
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .pressScale(interactionSource)
            // The indication is drawn on the circle below, from this same source, so the ripple is the circle's and not the column's.
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ).clearAndSetSemantics {
                this.contentDescription = announced
                role = Role.Button
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.button_tonal_size))
                .clip(BingeShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .indication(interactionSource, ripple()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_button_glyph_size)),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_xs)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLabelledIconButton() {
    BingeExpressiveTheme {
        LabelledIconButton(icon = Icons.Filled.Share, label = "Share", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLabelledIconButtonDisabled() {
    BingeExpressiveTheme {
        LabelledIconButton(icon = Icons.Filled.Share, label = "Share", onClick = {}, enabled = false)
    }
}
