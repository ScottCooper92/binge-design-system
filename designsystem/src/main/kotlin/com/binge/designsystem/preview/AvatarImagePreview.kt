package com.binge.designsystem.preview

import androidx.compose.runtime.Composable

/**
 * [WithPreviewImage] at avatar size and colour: a square teal stand-in for a profile photo. Use a null or
 * blank URL inside it to exercise the initials/placeholder branch instead.
 */
@Composable
fun WithPreviewAvatarImage(content: @Composable () -> Unit) {
    WithPreviewImage(
        widthPx = PREVIEW_AVATAR_PX,
        heightPx = PREVIEW_AVATAR_PX,
        color = PREVIEW_AVATAR_COLOR,
        content = content,
    )
}

/** Opaque teal — a fixed, recognisable stand-in for a real profile photo in screenshots. */
private const val PREVIEW_AVATAR_COLOR = 0xFF1E7F7F.toInt()

/** A square intrinsic size so the cropped image stays circular rather than stretching to fill width. */
private const val PREVIEW_AVATAR_PX = 128
