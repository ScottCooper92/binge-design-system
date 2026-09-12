package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [BingeInitialsAvatar][com.binge.designsystem.component.BingeInitialsAvatar] catalog sample (#750). */
class BingeInitialsAvatarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Avatars() {
        BingeInitialsAvatarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun AvatarImage() {
        BingeInitialsAvatarImageSample()
    }
}
