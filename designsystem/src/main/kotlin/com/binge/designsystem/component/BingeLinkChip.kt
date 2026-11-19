package com.binge.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import com.binge.designsystem.R

/**
 * The glyph that ends a control that leaves the app: the open-in-new arrow, at the size of a chip's own icon and in the content colour
 * unless [tint] says otherwise. It is decoration, so it has no description of its own: the control it sits in says it opens outside the
 * app (see [BingeLinkChip]), once, for the whole control. Use it on any chip, tile or button that opens a browser, the media server or
 * another app, so they all look the same.
 *
 * @need mark a control as one that opens outside the app
 * @group inputs
 * @replaces a hand-sized open-in-new Icon on a link
 */
@Composable
fun ExternalLinkIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = null,
        modifier = modifier.size(dimensionResource(R.dimen.external_link_icon_size)),
        tint = tint,
    )
}

/**
 * An assist chip that opens something outside the app: a leading icon, the [label], and the [ExternalLinkIcon] at its end, so it does
 * not read as an action inside the app. A screen reader reads it as "[label], opens outside the app", then offers the tap.
 *
 * @need link to a website or another app with a chip
 * @group inputs
 * @replaces a hand-built AssistChip with a leading icon that opens a browser
 */
@Composable
fun BingeLinkChip(
    label: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.cd_link_chip_opens_outside, label)
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        trailingIcon = { ExternalLinkIcon() },
        modifier =
            modifier.clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick {
                    onClick()
                    true
                }
            },
    )
}
