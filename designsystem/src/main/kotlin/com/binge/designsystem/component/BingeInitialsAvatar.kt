package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.AVATAR_INITIALS_PADDING_FRACTION
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.contrastSafeOn
import com.binge.designsystem.toInitials

/**
 * A round user avatar. Renders [avatarUrl] cropped into the circle when present; otherwise (or while
 * loading / on error) falls back to up to two initials of [name] on a deterministic tone from the
 * theme's pastel avatar palette (same person always maps to the same colour). The initials autosize
 * to a fixed fraction of the circle, so they scale with [size].
 */
@Composable
fun BingeInitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    size: Dp = dimensionResource(R.dimen.avatar_size_sm),
) {
    val initials = @Composable { InitialsAvatar(name = name, size = size) }
    if (avatarUrl.isNullOrBlank()) {
        Box(modifier) { initials() }
        return
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(BingeShapes.Pill),
    ) {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { initials() },
            error = { initials() },
        )
    }
}

@Composable
private fun InitialsAvatar(name: String, size: Dp) {
    val palette = BingeTheme.colors.avatarPalette
    // Stable index from the name's hash; floor-mod keeps it in range for negative hashes.
    val tone = palette[((name.hashCode() % palette.size) + palette.size) % palette.size]
    val ink = contrastSafeOn(background = tone, preferred = BingeTheme.colors.avatarInk)
    Box(
        modifier = Modifier
            .size(size)
            .clip(BingeShapes.Pill)
            .background(tone),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.toInitials(),
            modifier = Modifier.padding(size * AVATAR_INITIALS_PADDING_FRACTION),
            style = MaterialTheme.typography.labelSmall,
            color = ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 40.sp),
        )
    }
}
