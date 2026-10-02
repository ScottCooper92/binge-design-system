package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.tonalContainer

/**
 * A screen's standing guidance — how to use it, or what it affects — on a primary-tinted surface, so it
 * reads as help rather than as one more setting. The icon is decorative; [text] carries the meaning.
 *
 * Not dismissible by default: it states something true for as long as the screen is open. Pass
 * [onDismiss] to add a close control — [HintCard] only reports the tap; the caller decides what
 * "dismissed" means and whether to stop rendering the card at all.
 *
 * Pass [actionLabel] and [onAction] to add a centred text button beneath the text, for guidance that
 * asks the user to go somewhere. [actionIcon] trails the label, such as an open-in-new glyph for a
 * destination outside the app.
 */
@Composable
fun HintCard(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Lightbulb,
    onDismiss: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
) {
    val hasAction = actionLabel != null && onAction != null
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(BingeShapes.Large)
                .background(MaterialTheme.colorScheme.primary.tonalContainer()),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(R.dimen.settings_group_row_padding_h),
                            top = dimensionResource(R.dimen.settings_group_row_padding_v),
                            end = if (onDismiss != null) {
                                dimensionResource(R.dimen.hint_card_dismiss_end_inset)
                            } else {
                                dimensionResource(R.dimen.settings_group_row_padding_h)
                            },
                        ).then(
                            if (hasAction) {
                                Modifier
                            } else {
                                Modifier.padding(bottom = dimensionResource(R.dimen.settings_group_row_padding_v))
                            },
                        ),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.icon_size_l)),
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (actionLabel != null && onAction != null) {
                HintCardAction(label = actionLabel, onClick = onAction, icon = actionIcon)
            }
        }
        if (onDismiss != null) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.hint_card_dismiss),
                )
            }
        }
    }
}

@Composable
private fun HintCardAction(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector?,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minHeight = dimensionResource(R.dimen.button_filled_height)),
        shape = BingeShapes.Medium,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        if (icon != null) {
            Spacer(Modifier.width(dimensionResource(R.dimen.button_filled_icon_spacing)))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.button_filled_icon_size)),
            )
        }
    }
}
