package com.binge.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.binge.designsystem.R
import com.binge.designsystem.badgeCountLabel
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.tonalContainer

/**
 * A titled group of settings rows on one clipped surface, dividers between them. The title is
 * marked `heading()` so TalkBack can jump group to group rather than walking every row — a
 * settings screen runs to a dozen groups.
 */
@Composable
fun SettingsGroup(
    title: String?,
    rows: List<SettingsRow>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // 8dp inside the group renders 24dp from the screen edge, since the group sits
                // inside the screen's 16dp horizontal content padding.
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.padding_s))
                    .padding(bottom = dimensionResource(R.dimen.padding_s))
                    .semantics { heading() },
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BingeShapes.Large)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            rows.forEachIndexed { index, row ->
                SettingsRowView(row = row)
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        thickness = dimensionResource(R.dimen.hairline_thickness),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.settings_group_row_padding_h)),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsRowView(row: SettingsRow, modifier: Modifier = Modifier) {
    val external = row.clickable && row.trailingContent == null && row.destination == SettingsRowDestination.External
    val externalDescription = stringResource(R.string.cd_settings_row_external)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                selected = row.selected
                // Appended to the row's own merged node rather than a contentDescription on the trailing
                // icon, so a screen reader announces one node ("Watchlist, Opens in browser, Button")
                // instead of reading the icon as a second stop.
                if (external) stateDescription = externalDescription
            }
            // The wash sits outside the click, so the ripple draws over it rather than under it.
            .background(if (row.selected) MaterialTheme.colorScheme.primary.tonalContainer() else Color.Transparent)
            .combinedClickable(
                enabled = row.clickable,
                role = Role.Button,
                onClick = row.onClick,
                onLongClick = row.onLongClick,
            ).padding(
                horizontal = dimensionResource(R.dimen.settings_group_row_padding_h),
                vertical = dimensionResource(R.dimen.settings_group_row_padding_v),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.settings_group_icon_size))
                .clip(BingeShapes.MoreCard)
                .background(row.iconTint?.tonalContainer() ?: MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = row.iconPainter?.invoke() ?: rememberVectorPainter(row.icon),
                contentDescription = null,
                tint = row.iconTint ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.settings_group_icon_glyph)),
            )
        }
        Spacer(Modifier.width(dimensionResource(R.dimen.account_card_spacing)))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!row.detail.isNullOrBlank()) {
                Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
                Text(
                    text = row.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = row.detailColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (row.badgeCount != null && row.badgeCount > 0) {
                CountBadge(count = row.badgeCount, tint = row.badgeTint)
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
            }
            when {
                row.trailingContent != null -> row.trailingContent.invoke()
                external -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                row.clickable -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** The row's count: a tonal pill in [tint] when given (matching the row's sentiment), else the default badge. */
@Composable
private fun CountBadge(count: Int, tint: Color?) {
    if (tint == null) {
        Badge { Text(badgeCountLabel(count)) }
    } else {
        Text(
            text = badgeCountLabel(count),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            modifier = Modifier
                .clip(BingeShapes.Pill)
                .background(tint.tonalContainer())
                .padding(
                    horizontal = dimensionResource(R.dimen.padding_s),
                    vertical = dimensionResource(R.dimen.padding_xxs),
                ),
        )
    }
}
