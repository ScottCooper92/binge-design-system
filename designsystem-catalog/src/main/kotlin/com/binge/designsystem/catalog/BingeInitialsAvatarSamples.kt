package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeInitialsAvatar
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewAvatarImage
import com.binge.designsystem.theme.BingeBrand
import com.binge.designsystem.theme.DarkColorScheme
import com.binge.designsystem.theme.LightColorScheme

/**
 * Public sample for [BingeInitialsAvatar]. See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * Each name hashes to a deterministic tone, so the sample shows a row of distinct names to
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

/** One name for each of the eight tones, in the order the name's hash picks them, under Binge's own accent. */
@Composable
fun BingeInitialsAvatarTonesSample() {
    ScreenshotTheme {
        TonesRow()
    }
}

/**
 * The same eight names in an app whose accent is indigo: the tones lean to it, so the row reads as the app's own
 * containers and not as Binge's.
 */
@Composable
fun BingeInitialsAvatarBrandTonesSample() {
    ScreenshotTheme(brand = IndigoBrand) {
        TonesRow()
    }
}

/** The names that hash to tone 0 to 7, so the row shows every tone once. */
private val ToneNames = listOf("Pia", "Tess", "Eli", "Ben", "Ana", "Dev", "Fay", "Omar")

@Composable
private fun TonesRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs))) {
        ToneNames.forEach { BingeInitialsAvatar(it, size = dimensionResource(R.dimen.avatar_size_md)) }
    }
}

private val IndigoPrimary = Color(0xFF5C6BC0)
private val IndigoPrimaryLight = Color(0xFFC5CAE9)
private val IndigoInk = Color(0xFF0D1340)

/** A brand whose accent is indigo, with its fixed roles set alongside the primary as a [BingeBrand] requires. */
private val IndigoBrand = BingeBrand(
    light = LightColorScheme.indigo(),
    dark = DarkColorScheme.indigo(),
)

private fun ColorScheme.indigo() =
    copy(
        primary = IndigoPrimary,
        primaryFixed = IndigoPrimaryLight,
        primaryFixedDim = IndigoPrimary,
        onPrimaryFixed = IndigoInk,
        onPrimaryFixedVariant = IndigoInk,
    )
