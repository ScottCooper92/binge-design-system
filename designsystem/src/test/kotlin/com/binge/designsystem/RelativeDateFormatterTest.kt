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

    /** The relative span is `DateUtils`' own copy in the device locale; the locale parameter does not reach it. */
    @Test
    fun `a recent instant reads as a relative span`() {
        val label = formatRelativeOrAbsolute(NOW - 6 * DAY, now = NOW)
        assertTrue("was '$label'", label.orEmpty().endsWith("ago"))
    }

    /** Past a week the platform drops to its own short date unless asked for weeks (#219). */
    @Test
    fun `an instant weeks old inside the window reads in weeks`() {
        assertEquals("1 week ago", formatRelativeOrAbsolute(NOW - 7 * DAY, now = NOW))
        assertEquals("3 weeks ago", formatRelativeOrAbsolute(NOW - 21 * DAY, now = NOW))
        assertEquals("4 weeks ago", formatRelativeOrAbsolute(NOW - 30 * DAY, now = NOW))
    }

    /** DateUtils would say "0 minutes ago" for anything under a minute (#240). */
    @Test
    fun `an instant under a minute old reads as now`() {
        assertEquals("now", formatRelativeOrAbsolute(NOW, now = NOW))
        assertEquals("now", formatRelativeOrAbsolute(NOW - 59_000, now = NOW))
        assertEquals("1 minute ago", formatRelativeOrAbsolute(NOW - 60_000, now = NOW))
    }

    @Test
    fun `an instant days old reads in days`() {
        assertEquals("6 days ago", formatRelativeOrAbsolute(NOW - 6 * DAY, now = NOW))
    }

    @Test
    fun `an instant past the window reads as the absolute long date`() {
        val label = formatRelativeOrAbsolute(NOW - 120 * DAY, now = NOW, locale = Locale.UK, zone = ZoneOffset.UTC)
        assertEquals("12 February 2026", label)
    }

    /** A scheduled time reads the way a past one does, never as a negative span ("-3 hours ago"). */
    @Test
    fun `a future instant inside the window reads as a relative span ahead`() {
        assertEquals("now", formatRelativeOrAbsolute(NOW + 59_000, now = NOW))
        assertEquals("in 20 minutes", formatRelativeOrAbsolute(NOW + 20 * 60_000, now = NOW))
        assertEquals("in 3 hours", formatRelativeOrAbsolute(NOW + 3 * 60 * 60_000, now = NOW))
        assertEquals("tomorrow", formatRelativeOrAbsolute(NOW + DAY, now = NOW))
        assertEquals("in 5 days", formatRelativeOrAbsolute(NOW + 5 * DAY, now = NOW))
        assertEquals("in 2 weeks", formatRelativeOrAbsolute(NOW + 14 * DAY, now = NOW))
    }

    /** Days are calendar days in the device zone, as on the past side, not 24-hour blocks. */
    @Test
    fun `a future instant counts calendar days across midnight`() {
        val zone = java.util.TimeZone.getDefault()
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))
        try {
            val evening = NOW + 22 * 60 * 60_000
            assertEquals("in 2 days", formatRelativeOrAbsolute(evening + 26 * 60 * 60_000, now = evening))
            assertEquals("in 2 days", formatRelativeOrAbsolute(evening + 46 * 60 * 60_000, now = evening))
            assertEquals("tomorrow", formatRelativeOrAbsolute(NOW + 32 * 60 * 60_000, now = NOW + 2 * 60 * 60_000))
            assertEquals("in 3 hours", formatRelativeOrAbsolute(NOW + 3 * 60 * 60_000, now = NOW))
        } finally {
            java.util.TimeZone.setDefault(zone)
        }
    }

    @Test
    fun `a future instant past the window reads as the absolute long date`() {
        val label = formatRelativeOrAbsolute(NOW + 120 * DAY, now = NOW, locale = Locale.UK, zone = ZoneOffset.UTC)
        assertEquals("10 October 2026", label)
    }
}
