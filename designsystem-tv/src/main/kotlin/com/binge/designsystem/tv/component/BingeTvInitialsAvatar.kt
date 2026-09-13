package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.contrastSafeOn
import com.binge.designsystem.toInitials

/** The initials fill a fixed fraction of the circle, so they scale with the avatar's size. */
private const val INITIALS_TEXT_FRACTION = 0.4f

/**
 * A round user avatar for TV. Renders [avatarUrl] cropped into the circle when present; otherwise (or while
 * loading / on error) falls back to up to two initials of [name] on a deterministic tone from the theme's
 * avatar palette (same person always maps to the same colour).
 *
 * The tv-material twin of the phone `BingeInitialsAvatar`: the two `MaterialTheme` trees don't cross, so the
 * initials read tv-material typography while both surfaces share the same `BingeColors` palette tokens.
 */
@Composable
fun BingeTvInitialsAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
) {
    val initials = @Composable { TvInitialsAvatar(name = name, size = size) }
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
private fun TvInitialsAvatar(name: String, size: Dp) {
    val palette = BingeTheme.colors.avatarPalette
    // Stable index from the name's hash; floor-mod keeps it in range for negative hashes.
    val tone = palette[((name.hashCode() % palette.size) + palette.size) % palette.size]
    val ink = contrastSafeOn(background = tone, preferred = BingeTheme.colors.avatarInk)
    val fontSize = with(LocalDensity.current) { (size * INITIALS_TEXT_FRACTION).toSp() }
    Box(
        modifier = Modifier
            .size(size)
            .clip(BingeShapes.Pill)
            .background(tone),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.toInitials(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = fontSize,
            color = ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
