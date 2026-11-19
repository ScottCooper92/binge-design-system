package com.binge.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.binge.designsystem.theme.DarkBingeColors
import com.binge.designsystem.theme.LightBingeColors
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.pow

/**
 * The favourite heart over artwork holds a graphic's 3:1 against the scrim disc on the brightest poster, in both themes. It is the theme's
 * favourite red where that holds and `onScrim` where it does not, so this checks the colour the mark would actually draw. Computed here from the
 * channels, so the check is independent of the code under test; the rule itself is production's `favouriteTintFor`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class FavouriteMarkContrastTest {
    private fun contrast(a: Color, b: Color): Double {
        fun channel(c: Float) = if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

        fun lum(c: Color) = 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
        val hi = maxOf(lum(a), lum(b))
        val lo = minOf(lum(a), lum(b))
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun assertHolds(
        name: String,
        favourite: Color,
        scrim: Color,
        onScrim: Color,
    ) {
        val tint = favouriteTintFor(favourite, scrim, onScrim)
        val worst = scrim.copy(alpha = FAVOURITE_BACKDROP_ALPHA).compositeOver(Color.White)
        val ratio = contrast(tint, worst)
        assertTrue("$name heart is ${"%.2f".format(ratio)}:1 on the brightest poster", ratio >= 3.0)
    }

    @Test
    fun `the light theme's heart holds 3_1 on a white poster`() =
        assertHolds("light", LightBingeColors.favourite, LightBingeColors.scrim, LightBingeColors.onScrim)

    @Test
    fun `the dark theme's heart holds 3_1 on a white poster`() =
        assertHolds("dark", DarkBingeColors.favourite, DarkBingeColors.scrim, DarkBingeColors.onScrim)
}
