package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Initials avatars — deterministic pastel tone per name, near-black ink. */
class BingeInitialsAvatarScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun AvatarSizes() {
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
}
