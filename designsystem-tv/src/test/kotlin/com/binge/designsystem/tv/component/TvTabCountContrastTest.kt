package com.binge.designsystem.tv.component

import androidx.compose.ui.graphics.Color
import com.binge.designsystem.tv.theme.BingeTvColorScheme
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.math.pow

/**
 * Guards the legibility of [TvTabRow]'s count sub-pill.
 *
 * The pill is grounded in a tint of its **own** content colour (`contentColor.copy(alpha = TAB_COUNT_SCRIM_ALPHA)`), so
 * the scrim moves the ground *towards* the text: it buys visual grounding by spending contrast. Nothing else asserts
 * that, so a palette change or a nudge to the alpha could drop the count below reading threshold unnoticed.
 *
 * How much margin that costs depends on the tab's own ground, so the test uses the real one. A resting tab is
 * transparent, so its ground is the page's `background`, where the count reads about 5.4:1. On the lighter `surface`
 * the same scrim lands lighter and the count falls to about 3.95:1, below AA. That is the constraint [TvTabRow]'s
 * KDoc passes on to callers, and the second test pins it as a fact rather than a sentence.
 *
 * WCAG 2.1 relative luminance computed straight from the channels, on the JVM, with no emulator. Scoped to
 * [BingeTvColorScheme]: the TV theme is dark-only, so the phone palettes are the wrong grounds to assert against.
 */
class TvTabCountContrastTest {
    /** WCAG 2.1 contrast ratio (1..21), order-independent, from two opaque sRGB colours. */
    private fun contrast(a: Color, b: Color): Double {
        fun channel(c: Float) = if (c <= LINEAR_CUTOFF) c / LINEAR_DIVISOR else ((c + GAMMA_OFFSET) / GAMMA_DIVISOR).pow(GAMMA)

        fun luminance(c: Color) = RED_WEIGHT * channel(c.red) + GREEN_WEIGHT * channel(c.green) + BLUE_WEIGHT * channel(c.blue)
        val lighter = maxOf(luminance(a), luminance(b))
        val darker = minOf(luminance(a), luminance(b))
        return (lighter + FLARE) / (darker + FLARE)
    }

    /** Source-over composite of [fg] at [alpha] onto opaque [bg]: what the scrim actually paints. */
    private fun over(
        fg: Color,
        alpha: Float,
        bg: Color,
    ): Color =
        Color(
            red = fg.red * alpha + bg.red * (1 - alpha),
            green = fg.green * alpha + bg.green * (1 - alpha),
            blue = fg.blue * alpha + bg.blue * (1 - alpha),
        )

    private data class Case(
        val name: String,
        val content: Color,
        val tabGround: Color,
    )

    @TestFactory
    fun `the count pill's text clears WCAG AA on every tab ground`(): List<DynamicTest> {
        val scheme = BingeTvColorScheme
        return listOf(
            Case("resting", scheme.onSurfaceVariant, scheme.background),
            Case("current", scheme.onPrimaryContainer, scheme.primaryContainer),
            Case("focused", scheme.onPrimary, scheme.primary),
        ).map { case ->
            DynamicTest.dynamicTest(case.name) {
                val pillGround = over(case.content, TAB_COUNT_SCRIM_ALPHA, case.tabGround)
                val ratio = contrast(case.content, pillGround)
                assertTrue(
                    ratio >= WCAG_AA_NORMAL,
                    "${case.name}: the count reads at ${"%.2f".format(ratio)}:1 on its own scrim, below the " +
                        "$WCAG_AA_NORMAL:1 floor. The palette moved, TAB_COUNT_SCRIM_ALPHA went up, or this ground " +
                        "got lighter: the scrim tints towards the text, so a lighter tab spends more of the margin.",
                )
            }
        }
    }

    /**
     * The caller constraint as an executed fact. If a palette change or a lower scrim alpha ever brings `surface` above
     * the floor, this goes red and says so: the constraint was relaxed by accident, and the KDoc that still states it is
     * the stale thing.
     */
    @Test
    fun `a surface-toned ground is the unsafe one the caller constraint is about`() {
        val scheme = BingeTvColorScheme
        val pillGround = over(scheme.onSurfaceVariant, TAB_COUNT_SCRIM_ALPHA, scheme.surface)
        val ratio = contrast(scheme.onSurfaceVariant, pillGround)
        assertTrue(
            ratio < WCAG_AA_NORMAL,
            "A resting tab on `surface` now reads ${"%.2f".format(ratio)}:1, at or above the $WCAG_AA_NORMAL:1 floor. " +
                "That is good news and a stale document: TvTabRow's KDoc still tells callers a lighter ground drops the " +
                "count below AA. Re-measure, relax the constraint there, and update this test.",
        )
    }
}

/** WCAG AA for normal text. The count is `labelMedium`, under the large-text exemption, so the body threshold applies. */
private const val WCAG_AA_NORMAL = 4.5
private const val LINEAR_CUTOFF = 0.03928
private const val LINEAR_DIVISOR = 12.92
private const val GAMMA_OFFSET = 0.055
private const val GAMMA_DIVISOR = 1.055
private const val GAMMA = 2.4
private const val RED_WEIGHT = 0.2126
private const val GREEN_WEIGHT = 0.7152
private const val BLUE_WEIGHT = 0.0722
private const val FLARE = 0.05
