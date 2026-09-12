package com.binge.designsystem

import android.text.format.DateUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * The display formatting the shared components render: a score, a vote count, a name's initials, a
 * badge count. They live with the components rather than with any consumer's data layer so that a
 * rating reads the same in every app that shows one.
 */
private const val VOTE_COUNT_THOUSAND = 1_000
private const val VOTE_COUNT_STEP = 1_000.0
private const val MAX_BADGE_COUNT = 99

/** Instants within this window of "now" read as a relative span ("6 days ago"); older ones as an absolute date. */
private const val RELATIVE_DATE_WINDOW_MILLIS = 30L * 24 * 60 * 60 * 1000

/**
 * Abbreviated units for [formatVoteCount] in ascending order, each [VOTE_COUNT_STEP]× the previous.
 * A Kotlin constant, not a string resource: these SI-style abbreviations are not translated.
 */
private val VOTE_COUNT_UNITS = listOf("", "k", "M", "B")

fun Int.formatVoteCount(): String {
    if (this < VOTE_COUNT_THOUSAND) return toString()
    var value = this / VOTE_COUNT_STEP
    var unitIndex = 1
    // Choose the unit after rounding: 999_950 rounds to 1000.0k, which should read as 1.0M.
    while (roundsToStep(value, VOTE_COUNT_STEP) && unitIndex < VOTE_COUNT_UNITS.lastIndex) {
        value /= VOTE_COUNT_STEP
        unitIndex++
    }
    return "%.1f%s".format(Locale.getDefault(), value, VOTE_COUNT_UNITS[unitIndex])
}

/**
 * Locale-aware separator (e.g. "7,3" under de), passed explicitly to satisfy Lint's DefaultLocale.
 * [roundsToStep] stays locale-invariant to avoid a parse crash.
 */
fun Float.formatRating(): String = "%.1f".format(Locale.getDefault(), this)

private fun roundsToStep(value: Double, step: Double): Boolean = BigDecimal(value).setScale(1, RoundingMode.HALF_UP).toDouble() >= step

/**
 * Up to two initials from a display name, split on whitespace and the separators commonly found in
 * usernames (".", "_", "-"). Returns [fallback] when no initials can be derived (empty or
 * delimiter-only strings).
 */
fun String.toInitials(fallback: String = take(2).uppercase()): String =
    split(" ", ".", "_", "-")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { fallback }

/** Caps a badge count at "99+" so the pill stays compact. Shared by nav, tab and row badges. */
fun badgeCountLabel(count: Int): String = if (count > MAX_BADGE_COUNT) "$MAX_BADGE_COUNT+" else count.toString()

/**
 * A conversational date for [timeMillis]: a relative span ("just now", "6 days ago", "3 weeks ago")
 * for recent instants, falling back to the absolute long date ("12 June 2026") once it ages past the
 * relative window or sits in the future. `null` when [timeMillis] is `null` so callers can drop the
 * line entirely. [now] is a parameter so frames pass a fixed instant rather than the drifting clock.
 * [locale] and [zone] govern the absolute date only: the relative span is the platform's own
 * `DateUtils` copy, which always follows the device locale.
 */
fun formatRelativeOrAbsolute(
    timeMillis: Long?,
    now: Long = System.currentTimeMillis(),
    locale: Locale = Locale.getDefault(),
    zone: ZoneId = ZoneId.systemDefault(),
): String? {
    if (timeMillis == null) return null
    val age = now - timeMillis
    return if (age in 0..RELATIVE_DATE_WINDOW_MILLIS) {
        DateUtils.getRelativeTimeSpanString(timeMillis, now, DateUtils.MINUTE_IN_MILLIS).toString()
    } else {
        Instant
            .ofEpochMilli(timeMillis)
            .atZone(zone)
            .toLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
    }
}
