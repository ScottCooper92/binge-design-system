package com.binge.designsystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.ZoneOffset
import java.util.Locale

private const val NOW = 1_781_222_400_000L // 12 June 2026 00:00 UTC
private const val DAY = 24L * 60 * 60 * 1000

/** Robolectric because the relative half is the framework's `DateUtils`; the absolute half is `java.time`. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RelativeDateFormatterTest {
    @Test
    fun `null stays null so a caller can drop the line`() {
        assertNull(formatRelativeOrAbsolute(null, now = NOW))
    }

    @Test
    fun `a recent instant reads as a relative span`() {
        val label = formatRelativeOrAbsolute(NOW - 6 * DAY, now = NOW, locale = Locale.UK, zone = ZoneOffset.UTC)
        assertTrue("was '$label'", label.orEmpty().endsWith("ago"))
    }

    @Test
    fun `an instant past the window reads as the absolute long date`() {
        val label = formatRelativeOrAbsolute(NOW - 120 * DAY, now = NOW, locale = Locale.UK, zone = ZoneOffset.UTC)
        assertEquals("12 February 2026", label)
    }

    @Test
    fun `a future instant reads as the absolute date rather than a negative span`() {
        val label = formatRelativeOrAbsolute(NOW + DAY, now = NOW, locale = Locale.UK, zone = ZoneOffset.UTC)
        assertEquals("13 June 2026", label)
    }
}
