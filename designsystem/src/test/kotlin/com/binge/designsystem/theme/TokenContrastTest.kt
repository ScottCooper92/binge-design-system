package com.binge.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.math.pow

/**
 * Guards Binge's hand-authored colour token pairs against WCAG contrast regressions — the palette
 * half of the accessibility story that no rendered test covers. Runs on the JVM (no emulator): it
 * computes the WCAG 2.1 relative-luminance ratio directly from the [Color] channels rather than
 * going through production's [contrastRatio], which reaches into android.graphics via ColorUtils and
 * so isn't callable from a plain unit test.
 *
 * Scope is the bespoke pairings the M3 scheme doesn't guarantee (success container, the avatar
 * ink-on-palette assumption [OnColors] flags as the dynamic-colour risk, scrim) plus the hand-tuned
 * M3 scheme text pairs themselves, at 4.5:1.
 *
 * Deliberately excluded, because contrast-vs-a-single-background is the wrong metric for them:
 * - the `ratingBucketGradient` ramp — the bars are read relative to each other and their heights, a
 *   data-viz whose bucket colours aren't standalone graphical objects that must clear 3:1 vs surface
 *   (yellow legitimately measures ~1.2:1 on the light container);
 * - `onSurfaceVariant`/`surfaceVariant` — a medium-emphasis M3 role, not a guaranteed 4.5:1 body pair;
 * - text over a low-alpha sentiment tint or over imagery — the composited background depends on the
 *   call-site alpha, so those need a rendered surface (out of scope for a JVM token test).
 */
class TokenContrastTest {
    /** WCAG 2.1 contrast ratio (1..21), order-independent, from two opaque sRGB colours. */
    private fun contrast(a: Color, b: Color): Double {
        fun channel(c: Float) = if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

        fun luminance(c: Color) = 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
        val l1 = luminance(a)
        val l2 = luminance(b)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private data class Pair(
        val name: String,
        val fg: Color,
        val bg: Color,
        val min: Double,
    )

    private fun pairsFor(
        theme: String,
        colors: BingeColors,
        scheme: androidx.compose.material3.ColorScheme,
    ): List<Pair> {
        val text = WCAG_CONTRAST_NORMAL
        val pairs = mutableListOf(
            Pair("$theme onSuccessContainer/successContainer", colors.onSuccessContainer, colors.successContainer, text),
            Pair("$theme onScrim/scrim", colors.onScrim, colors.scrim, text),
            // OnColors flags this as THE dynamic-colour risk: one ink assumed to read on every avatar tone.
            *colors.avatarPalette
                .mapIndexed { i, tone ->
                    Pair("$theme avatarInk/avatarPalette[$i]", colors.avatarInk, tone, text)
                }.toTypedArray(),
            // Hand-authored M3 scheme text pairs — catches a regression in the scheme itself.
            Pair("$theme onSurface/surface", scheme.onSurface, scheme.surface, text),
            Pair("$theme onPrimary/primary", scheme.onPrimary, scheme.primary, text),
            Pair("$theme onPrimaryContainer/primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer, text),
            Pair("$theme onSecondaryContainer/secondaryContainer", scheme.onSecondaryContainer, scheme.secondaryContainer, text),
            Pair("$theme onTertiaryContainer/tertiaryContainer", scheme.onTertiaryContainer, scheme.tertiaryContainer, text),
            Pair("$theme onBackground/background", scheme.onBackground, scheme.background, text),
            Pair("$theme onError/error", scheme.onError, scheme.error, text),
            Pair("$theme onErrorContainer/errorContainer", scheme.onErrorContainer, scheme.errorContainer, text),
        )
        return pairs
    }

    @TestFactory
    fun `every token pair clears its WCAG threshold in light and dark`(): List<DynamicTest> =
        (pairsFor("light", LightBingeColors, LightColorScheme) + pairsFor("dark", DarkBingeColors, DarkColorScheme))
            .map(::asTest)

    /**
     * A scheme that is not Binge's, held to the same thresholds.
     *
     * [BingeBrand] lets an app render the shared components in its own accent, and this is what says
     * the harness measures a scheme rather than Binge's constants: the pairs come from whatever
     * scheme is handed in. An indigo stands in for a consumer's brand because it is the case that
     * bites — the hue an app picks off its own icon is usually too dark to be `primary`, and the
     * lighter step of it that clears 4.5:1 is the one it has to ship.
     */
    @TestFactory
    fun `a consumer's own scheme is held to the same thresholds`(): List<DynamicTest> =
        (
            pairsFor("consumer light", LightBingeColors, consumerLight) +
                pairsFor("consumer dark", DarkBingeColors, consumerDark)
        ).map(::asTest)

    private val consumerLight = LightColorScheme.copy(primary = Color(0xFF4F46E5), onPrimary = Color.White)

    private val consumerDark = DarkColorScheme.copy(primary = Color(0xFF818CF8), onPrimary = Color(0xFF1E1B4B))

    private fun asTest(p: Pair): DynamicTest =
        DynamicTest.dynamicTest(p.name) {
            val ratio = contrast(p.fg, p.bg)
            assertTrue(
                ratio >= p.min,
                "${p.name}: contrast ${"%.2f".format(ratio)}:1 is below the ${p.min}:1 minimum",
            )
        }
}
