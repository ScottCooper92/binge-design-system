package com.binge.designsystem.theme

import me.tatarka.google.material.contrast.Contrast
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Covers the pure tone arithmetic behind [contrastSafeOn] — [contrastSafeOnTone]. It touches only
 * `material-color-utilities` (pure JVM), so the achieved contrast can be computed and asserted here
 * without the Android colour engine (the `Color` wrappers themselves are exercised on-device via the
 * avatar composables, since `Color.toArgb` reaches `android.graphics` statics that stub out in
 * plain-JVM unit tests).
 *
 * The HCT tones below stand in for the deterministic avatar palette: light pastels (high tone),
 * mid-tones, and the dark surfaces a lightened ink would sit on.
 */
class OnColorsTest {
    /**
     * For every representative background tone, the derived on-tone clears the 4.5:1 target.
     * A tiny epsilon absorbs the `*Unsafe` clamp at mid-tones, where the exact ratio is unreachable
     * and the engine returns the best available — which still lands within rounding of the target.
     */
    @ParameterizedTest
    @ValueSource(doubles = [0.0, 12.0, 30.0, 45.0, 50.0, 55.0, 70.0, 85.0, 92.0, 100.0])
    fun `derived on-tone meets the AA target for body text`(backgroundTone: Double) {
        val onTone = contrastSafeOnTone(backgroundTone, WCAG_CONTRAST_NORMAL)
        val achieved = Contrast.ratioOfTones(onTone, backgroundTone)
        assertTrue(
            achieved >= WCAG_CONTRAST_NORMAL - RATIO_EPSILON,
            "tone $backgroundTone -> on-tone $onTone only reached $achieved:1 (target $WCAG_CONTRAST_NORMAL)",
        )
    }

    /** A relaxed 3:1 floor is also satisfied for the same backgrounds. */
    @ParameterizedTest
    @ValueSource(doubles = [0.0, 12.0, 30.0, 45.0, 50.0, 55.0, 70.0, 85.0, 92.0, 100.0])
    fun `derived on-tone meets a relaxed target`(backgroundTone: Double) {
        val onTone = contrastSafeOnTone(backgroundTone, RELAXED_RATIO)
        val achieved = Contrast.ratioOfTones(onTone, backgroundTone)
        assertTrue(
            achieved >= RELAXED_RATIO - RATIO_EPSILON,
            "tone $backgroundTone -> on-tone $onTone only reached $achieved:1 (target $RELAXED_RATIO)",
        )
    }

    @Test
    fun `dark backgrounds get a lighter ink, light backgrounds a darker one`() {
        val onDark = contrastSafeOnTone(backgroundTone = 10.0, minRatio = WCAG_CONTRAST_NORMAL)
        val onLight = contrastSafeOnTone(backgroundTone = 95.0, minRatio = WCAG_CONTRAST_NORMAL)
        assertTrue(onDark > 10.0, "expected a lighter ink over a dark tone, got $onDark")
        assertTrue(onLight < 95.0, "expected a darker ink over a light tone, got $onLight")
    }

    @Test
    fun `a stricter target pushes the ink further from a light background`() {
        val relaxed = contrastSafeOnTone(backgroundTone = 90.0, minRatio = RELAXED_RATIO)
        val strict = contrastSafeOnTone(backgroundTone = 90.0, minRatio = WCAG_CONTRAST_NORMAL)
        assertTrue(strict <= relaxed, "stricter ratio should darken further: strict=$strict relaxed=$relaxed")
    }

    private companion object {
        const val RATIO_EPSILON = 0.05

        /** A relaxed target below the 4.5:1 body-text default, used to check a stricter ratio darkens further. */
        const val RELAXED_RATIO = 3.0
    }
}
