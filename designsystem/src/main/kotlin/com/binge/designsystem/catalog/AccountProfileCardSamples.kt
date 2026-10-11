package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.AccountProfileCard
import com.binge.designsystem.component.AccountProfileCardLayout
import com.binge.designsystem.component.BingeTag
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewAvatarImage
import com.binge.designsystem.theme.BingeTheme

/**
 * Public samples for [AccountProfileCard]. See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * The card has two forms: tappable (an `onClick` is wired, so it shows the trailing chevron)
 * and static (no `onClick`, used as a plain header).
 */
@Composable
fun AccountProfileCardInteractiveSample() {
    ScreenshotTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "sam.rivera@binge.app · Member since 2024",
            initialsName = "Sam Rivera",
            country = "🇬🇧 United Kingdom",
            onClick = {},
        )
    }
}

/** Static header form — no `onClick`, so the trailing chevron is omitted. */
@Composable
fun AccountProfileCardStaticSample() {
    ScreenshotTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "sam.rivera@binge.app",
            initialsName = "Sam Rivera",
        )
    }
}

/** Square, vertically stacked form for the left column of an expanded two-pane screen. */
@Composable
fun AccountProfileCardColumnSample() {
    ScreenshotTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "@sam.rivera",
            initialsName = "Sam Rivera",
            country = "🇬🇧 United Kingdom",
            layout = AccountProfileCardLayout.Column,
        )
    }
}

/** A card carrying a role tag beside the name, as a server's account list shows Admin, Owner or User. */
@Composable
fun AccountProfileCardTaggedSample() {
    ScreenshotTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "sam.rivera@binge.app",
            initialsName = "Sam Rivera",
            onClick = {},
            tag = {
                BingeTag(label = "Admin", icon = Icons.Filled.Shield, tint = BingeTheme.colors.info, uppercase = false)
            },
        )
    }
}

/** The square column form with a role tag stacked under the name. */
@Composable
fun AccountProfileCardColumnTaggedSample() {
    ScreenshotTheme {
        AccountProfileCard(
            name = "Sam Rivera",
            secondaryLine = "@sam.rivera",
            initialsName = "Sam Rivera",
            country = "🇬🇧 United Kingdom",
            layout = AccountProfileCardLayout.Column,
            tag = {
                BingeTag(label = "Admin", icon = Icons.Filled.Shield, tint = BingeTheme.colors.info, uppercase = false)
            },
        )
    }
}

/** The avatar drawn from an image rather than initials; the preview image stands in for the loaded URL. */
@Composable
fun AccountProfileCardWithImageSample() {
    ScreenshotTheme {
        WithPreviewAvatarImage {
            AccountProfileCard(
                name = "Sam Rivera",
                secondaryLine = "sam.rivera@binge.app · Member since 2024",
                initialsName = "Sam Rivera",
                avatarUrl = "https://example.invalid/avatar.jpg",
                country = "🇬🇧 United Kingdom",
                onClick = {},
            )
        }
    }
}
