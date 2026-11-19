package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.ListRowAvatar
import com.binge.designsystem.component.ListRowLogo
import com.binge.designsystem.component.ListRowPoster
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * A person row led by a [ListRowAvatar], under a title row led by a poster, so the two can be seen as tall as each
 * other. The image is a placeholder, because a frame cannot load one.
 */
@Composable
fun ListRowAvatarSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing))) {
            LeadingSampleRow(title = "Severance", subtitle = "TV show · 2022") {
                ListRowPoster(imageUrl = null, contentDescription = null)
            }
            LeadingSampleRow(title = "Adam Scott", subtitle = "Actor") { ListRowAvatar(imageUrl = null) }
        }
    }
}

/** A studio row led by a [ListRowLogo], under a title row led by a poster. The image is a placeholder. */
@Composable
fun ListRowLogoSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing))) {
            LeadingSampleRow(title = "Severance", subtitle = "TV show · 2022") {
                ListRowPoster(imageUrl = null, contentDescription = null)
            }
            LeadingSampleRow(title = "Warner Bros. Pictures", subtitle = "Studio") { ListRowLogo(logoUrl = null) }
        }
    }
}

@Composable
private fun LeadingSampleRow(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
) {
    ListRow(onClick = {}, leading = leading) { contentModifier ->
        Column(modifier = contentModifier) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
