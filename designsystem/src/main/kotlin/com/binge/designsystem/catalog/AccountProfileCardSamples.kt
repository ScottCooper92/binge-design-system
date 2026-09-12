package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.AccountProfileCard
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [AccountProfileCard] (group `"Cards"`). See the convention KDoc on
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
