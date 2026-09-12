package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeNavFloatingStyle
import com.binge.designsystem.component.BingeNavFloatingTone
import com.binge.designsystem.component.BingeNavPresentation

/** Standard tone over artwork — the low-contrast case reported from the device. */
@Composable
fun BingeNavFloatingBarArtworkStandardSample() {
    ArtworkSample(BingeNavFloatingTone.Standard)
}

/** Vibrant tone over artwork, with the indicator now paired to the container. */
@Composable
fun BingeNavFloatingBarArtworkVibrantSample() {
    ArtworkSample(BingeNavFloatingTone.Vibrant)
}

/** Inverted-surface tone over artwork — contrast that doesn't depend on what's behind it. */
@Composable
fun BingeNavFloatingBarArtworkHighContrastSample() {
    ArtworkSample(BingeNavFloatingTone.HighContrast)
}

/** Inverted-surface tone over the plain list, for the non-artwork case. */
@Composable
fun BingeNavFloatingBarHighContrastSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.HighContrast,
        showDiscover = false,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
    )
}

/** Standard tone over a bare background — the reported low-contrast case. */
@Composable
fun BingeNavFloatingBarEmptyStandardSample() {
    EmptyBackgroundSample(BingeNavFloatingTone.Standard)
}

/** Outlined tone over a bare background — lifted a tone step, with a hairline edge. */
@Composable
fun BingeNavFloatingBarEmptyOutlinedSample() {
    EmptyBackgroundSample(BingeNavFloatingTone.Outlined)
}

/** Always-dark tone over a bare background — dark pill in both themes, no inversion. */
@Composable
fun BingeNavFloatingBarEmptyAlwaysDarkSample() {
    EmptyBackgroundSample(BingeNavFloatingTone.AlwaysDark)
}

/** Always-dark tone over artwork. */
@Composable
fun BingeNavFloatingBarArtworkAlwaysDarkSample() {
    ArtworkSample(BingeNavFloatingTone.AlwaysDark)
}

/** Inverted-surface tone over a bare background. */
@Composable
fun BingeNavFloatingBarEmptyHighContrastSample() {
    EmptyBackgroundSample(BingeNavFloatingTone.HighContrast)
}

/**
 * The hard backdrop: with nothing but `background` behind the bar, the container's own tone is the
 * only separation there is — the case Account and the error screens actually hit.
 */
@Composable
private fun EmptyBackgroundSample(tone: BingeNavFloatingTone) {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        tone,
        showDiscover = false,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
        overEmptyBackground = true,
    )
}

/**
 * The easy backdrop: a poster grid gives the container something to sit against, so every tone
 * separates from it. Style is fixed at the shell's default so these vary on tone alone.
 */
@Composable
private fun ArtworkSample(tone: BingeNavFloatingTone) {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        tone,
        showDiscover = false,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
        overArtwork = true,
    )
}
