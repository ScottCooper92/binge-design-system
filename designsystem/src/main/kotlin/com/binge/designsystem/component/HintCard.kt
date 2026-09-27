package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.tonalContainer

/**
 * A screen's standing guidance — how to use it, or what it affects — on a primary-tinted surface, so it
 * reads as help rather than as one more setting. Not interactive and not dismissible: it states
 * something true for as long as the screen is open. The icon is decorative; [text] carries the meaning.
 */
@Composable
fun HintCard(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Lightbulb,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(BingeShapes.Large)
                .background(MaterialTheme.colorScheme.primary.tonalContainer())
                .padding(
                    horizontal = dimensionResource(R.dimen.settings_group_row_padding_h),
                    vertical = dimensionResource(R.dimen.settings_group_row_padding_v),
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
}
