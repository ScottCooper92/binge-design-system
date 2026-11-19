package com.binge.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.tatarka.google.material.hct.Hct
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AvatarTonesTest {
    private val primaries = listOf(
        "binge light" to LightColorScheme.primary,
        "binge dark" to DarkColorScheme.primary,
        "indigo" to Color(0xFF4F46E5),
        "teal" to Color(0xFF00695C),
        "wallpaper pink" to Color(0xFFB3005F),
    )

    @Test
    fun `there are eight tones, all different`() {
        primaries.forEach { (name, primary) ->
            listOf(false, true).forEach { dark ->
                val tones = avatarTones(primary, dark).tones
                assertEquals(8, tones.size, "$name dark=$dark")
                assertEquals(8, tones.map { it.toArgb() }.toSet().size, "$name dark=$dark: tones must be distinct")
            }
        }
    }

    @Test
    fun `every tone sits at the container tone of its theme`() {
        primaries.forEach { (name, primary) ->
            avatarTones(primary, isDark = false).tones.forEach {
                assertEquals(90.0, Hct.fromInt(it.toArgb()).tone, TONE_TOLERANCE, "$name light")
            }
            avatarTones(primary, isDark = true).tones.forEach {
                assertEquals(30.0, Hct.fromInt(it.toArgb()).tone, TONE_TOLERANCE, "$name dark")
            }
        }
    }

    @Test
    fun `the ink clears 4_5 to 1 on every tone`() {
        primaries.forEach { (name, primary) ->
            listOf(false, true).forEach { dark ->
                val avatar = avatarTones(primary, dark)
                avatar.tones.forEachIndexed { i, tone ->
                    val ratio = ratio(avatar.ink, tone)
                    assertTrue(ratio >= 4.5, "$name dark=$dark tone[$i]: ${"%.2f".format(ratio)}:1")
                }
            }
        }
    }

    @Test
    fun `the same primary always gives the same tones`() {
        assertEquals(avatarTones(Color(0xFF4F46E5), false).tones, avatarTones(Color(0xFF4F46E5), false).tones)
    }

    @Test
    fun `a different primary leans the set to its own accent`() {
        assertNotEquals(avatarTones(Color(0xFF4F46E5), false).tones, avatarTones(Color(0xFFB3005F), false).tones)
    }

    /** WCAG 2.1 contrast ratio from two opaque colours, computed here as [TokenContrastTest] does. */
    private fun ratio(a: Color, b: Color): Double {
        fun lum(c: Color): Double {
            fun ch(v: Float) = if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
            return 0.2126 * ch(c.red) + 0.7152 * ch(c.green) + 0.0722 * ch(c.blue)
        }
        val l1 = lum(a)
        val l2 = lum(b)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    private companion object {
        const val TONE_TOLERANCE = 1.5
    }
}
