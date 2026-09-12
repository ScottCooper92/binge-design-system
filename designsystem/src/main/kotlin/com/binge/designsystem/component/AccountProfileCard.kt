package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.component.AccountProfileCardLayout
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.IconButtonTone
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

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
                onClick = onClick,
            )

        AccountProfileCardLayout.Column ->
            AccountProfileCardColumn(
                modifier = surface.aspectRatio(1f).padding(dimensionResource(R.dimen.account_card_padding)),
                name = name,
                secondaryLine = secondaryLine,
                initialsName = initialsName,
                avatarUrl = avatarUrl,
                country = country,
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
    onClick: (() -> Unit)? = null,
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
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
        if (onClick != null) {
            ExpressiveIconButton(
                onClick = onClick,
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tone = IconButtonTone.Tonal,
                tint = MaterialTheme.colorScheme.onSurface,
            )
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
