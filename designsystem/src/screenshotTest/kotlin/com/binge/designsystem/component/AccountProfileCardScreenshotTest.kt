package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.AccountProfileCardLayout
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewAvatarImage

/** Gradient profile header — avatar (image or initials), name/secondary line, optional trailing action. */
class AccountProfileCardScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithImage() {
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

    /** Square, vertically-stacked form for the expanded two-pane left column. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Square() {
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
}
