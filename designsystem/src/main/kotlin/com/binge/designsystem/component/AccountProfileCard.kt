package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.component.AccountProfileCardLayout
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * An account as a card: avatar, name, a secondary line and an optional country.
 *
 * With [onClick] the whole card is one control: its rounded surface takes the press, the ripple stays inside the
 * corners, and the trailing chevron is decoration. Without it the card is a plain header. [tag] is drawn beside the
 * name in a row card and under it in a column card, for a role such as Admin; pass a [BingeTag] to match elsewhere.
 */
@Composable
fun AccountProfileCard(
    name: String,
    secondaryLine: String,
    initialsName: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    country: String? = null,
    onClick: (() -> Unit)? = null,
    layout: AccountProfileCardLayout = AccountProfileCardLayout.Row,
    tag: (@Composable () -> Unit)? = null,
) {
    val surface =
        modifier
            .fillMaxWidth()
            .clip(BingeShapes.AccountCard)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ),
            ).then(
                if (onClick != null) {
                    // After the clip, so the ripple follows the corners; one control, so one focus stop.
                    Modifier.clickable(
                        onClickLabel = stringResource(R.string.cd_open_named, name),
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
    when (layout) {
        AccountProfileCardLayout.Row ->
            AccountProfileCardRow(
                modifier = surface.padding(dimensionResource(R.dimen.account_card_padding)),
                name = name,
                secondaryLine = secondaryLine,
                initialsName = initialsName,
                avatarUrl = avatarUrl,
                country = country,
                chevron = onClick != null,
                tag = tag,
            )

        AccountProfileCardLayout.Column ->
            AccountProfileCardColumn(
                modifier = surface.aspectRatio(1f).padding(dimensionResource(R.dimen.account_card_padding)),
                name = name,
                secondaryLine = secondaryLine,
                initialsName = initialsName,
                avatarUrl = avatarUrl,
                country = country,
                tag = tag,
            )
    }
}

@Composable
private fun AccountProfileCardRow(
    name: String,
    secondaryLine: String,
    initialsName: String,
    avatarUrl: String?,
    country: String?,
    modifier: Modifier = Modifier,
    chevron: Boolean = false,
    tag: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BingeInitialsAvatar(
            name = initialsName,
            avatarUrl = avatarUrl,
            size = dimensionResource(R.dimen.account_avatar_size),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.account_card_spacing)))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // Unfilled weight lets a short name sit beside the tag, and a long one ellipsise before it.
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (tag != null) {
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
                    tag()
                }
            }
            Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
            Text(
                text = secondaryLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (country != null) {
                Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
                Text(
                    text = country,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (chevron) {
            // Drawn like a tonal icon button, but not one: the card is the control, so this says only where it leads.
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.button_tonal_size))
                        .clip(BingeShapes.Pill)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AccountProfileCardColumn(
    name: String,
    secondaryLine: String,
    initialsName: String,
    avatarUrl: String?,
    country: String?,
    modifier: Modifier = Modifier,
    tag: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BingeInitialsAvatar(
            name = initialsName,
            avatarUrl = avatarUrl,
            size = dimensionResource(R.dimen.account_avatar_size_large),
        )
        Spacer(Modifier.size(dimensionResource(R.dimen.account_card_spacing)))
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (tag != null) {
            Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
            tag()
        }
        Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
        Text(
            text = secondaryLine,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (country != null) {
            Spacer(Modifier.size(dimensionResource(R.dimen.account_group_label_detail_spacing)))
            Text(
                text = country,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccountProfileCard() {
    BingeExpressiveTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "sam.rivera@binge.app · Member since 2024",
            initialsName = "Sam Rivera",
            country = "🇬🇧 United Kingdom",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAccountProfileCardNonInteractive() {
    BingeExpressiveTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "sam.rivera@binge.app",
            initialsName = "Sam Rivera",
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewAccountProfileCardColumn() {
    BingeExpressiveTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "@sam.rivera",
            initialsName = "Sam Rivera",
            country = "🇬🇧 United Kingdom",
            layout = AccountProfileCardLayout.Column,
        )
    }
}
