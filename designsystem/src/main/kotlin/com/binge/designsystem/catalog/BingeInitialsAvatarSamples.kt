package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeInitialsAvatar
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewAvatarImage

/**
 * Public sample for [BingeInitialsAvatar]. See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * Each name hashes to a deterministic pastel tone, so the sample shows a row of distinct names to
 * cover the tone derivation rather than a single avatar.
 */
@Composable
fun BingeInitialsAvatarSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeInitialsAvatar("Noah Kim")
            BingeInitialsAvatar("Sana Ito")
            BingeInitialsAvatar("Amy")
            BingeInitialsAvatar("Raj Patel")
        }
    }
}

/** The image branch: a non-blank `avatarUrl` renders the profile photo (a deterministic stand-in). */
@Composable
fun BingeInitialsAvatarImageSample() {
    ScreenshotTheme {
        Row {
            WithPreviewAvatarImage {
                BingeInitialsAvatar(name = "Noah Kim", avatarUrl = "https://example.invalid/avatar.jpg")
            }
        }
    }
}

/** One name at each avatar size the components use, from the small list avatar up to the cast avatar. */
@Composable
fun BingeInitialsAvatarSizesSample() {
    ScreenshotTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BingeInitialsAvatar("Noah Kim", size = dimensionResource(R.dimen.avatar_size_sm))
            BingeInitialsAvatar("Noah Kim", size = dimensionResource(R.dimen.avatar_size_md))
            BingeInitialsAvatar("Noah Kim", size = dimensionResource(R.dimen.avatar_size_lg))
            BingeInitialsAvatar("Noah Kim", size = dimensionResource(R.dimen.cast_avatar_size))
        }
    }
}
