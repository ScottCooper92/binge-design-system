package com.binge.designsystem

import android.icu.text.RelativeDateTimeFormatter
import android.text.format.DateUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/**
 * The display formatting the shared components render: a score, a vote count, a name's initials, a
 * badge count. They live with the components rather than with any consumer's data layer so that a
 * rating reads the same in every app that shows one.
 */
private const val VOTE_COUNT_THOUSAND = 1_000
private const val VOTE_COUNT_STEP = 1_000.0
private const val MAX_BADGE_COUNT = 99
private const val MIN_COLLAPSED_RUN = 3

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
 * A conversational date for [timeMillis]: a relative span ("now", "6 days ago", "in 3 hours") for an
 * instant within the relative window either side of [now], falling back to the absolute long date
 * ("12 June 2026") beyond it. `null` when [timeMillis] is `null` so callers can drop the
 * line entirely. [now] is a parameter so frames pass a fixed instant rather than the drifting clock.
 * [locale] and [zone] govern the absolute date only: the relative span is the platform's own copy
 * (`DateUtils`, and ICU's "now" under a minute), which always follows the device locale.
 */
fun formatRelativeOrAbsolute(
    timeMillis: Long?,
    now: Long = System.currentTimeMillis(),
    locale: Locale = Locale.getDefault(),
    zone: ZoneId = ZoneId.systemDefault(),
): String? {
    if (timeMillis == null) return null
    val age = now - timeMillis
    // The future reads the same way as the past ("in 3 hours", "tomorrow"), so a scheduled time sits beside a past one.
    if (age < 0 && -age <= RELATIVE_DATE_WINDOW_MILLIS) return relativeFuture(-age)
    return if (age in 0 until DateUtils.MINUTE_IN_MILLIS) {
        // DateUtils counts whole minutes, so under one it says "0 minutes ago" (#240).
        RelativeDateTimeFormatter.getInstance().format(
            RelativeDateTimeFormatter.Direction.PLAIN,
            RelativeDateTimeFormatter.AbsoluteUnit.NOW,
        )
    } else if (age in 0..RELATIVE_DATE_WINDOW_MILLIS) {
        // DateUtils stops counting at a week unless asked for weeks: at minute resolution it falls back
        // to its own short absolute date, a second date style beside the long one below (#219).
        val resolution = if (age < DateUtils.WEEK_IN_MILLIS) DateUtils.MINUTE_IN_MILLIS else DateUtils.WEEK_IN_MILLIS
        DateUtils.getRelativeTimeSpanString(timeMillis, now, resolution).toString()
    } else {
        Instant
            .ofEpochMilli(timeMillis)
            .atZone(zone)
            .toLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))
    }
}

/**
 * [distance] ahead, inside the window: "now" under a minute, then "in 20 minutes", "in 3 hours", "tomorrow", "in 5
 * days", "in 2 weeks". ICU's formatter rather than `DateUtils`, which capitalises a future span for standing alone
 * ("In 3 hours") and so reads wrong inside a sentence ("Next run In 3 hours").
 */
private fun relativeFuture(distance: Long): String {
    val formatter = RelativeDateTimeFormatter.getInstance()
    val next = RelativeDateTimeFormatter.Direction.NEXT
    return when {
        distance < DateUtils.MINUTE_IN_MILLIS ->
            formatter.format(RelativeDateTimeFormatter.Direction.PLAIN, RelativeDateTimeFormatter.AbsoluteUnit.NOW)
        distance < DateUtils.HOUR_IN_MILLIS ->
            formatter.format((distance / DateUtils.MINUTE_IN_MILLIS).toDouble(), next, RelativeDateTimeFormatter.RelativeUnit.MINUTES)
        distance < DateUtils.DAY_IN_MILLIS ->
            formatter.format((distance / DateUtils.HOUR_IN_MILLIS).toDouble(), next, RelativeDateTimeFormatter.RelativeUnit.HOURS)
        distance < 2 * DateUtils.DAY_IN_MILLIS -> formatter.format(next, RelativeDateTimeFormatter.AbsoluteUnit.DAY)
        distance < DateUtils.WEEK_IN_MILLIS ->
            formatter.format((distance / DateUtils.DAY_IN_MILLIS).toDouble(), next, RelativeDateTimeFormatter.RelativeUnit.DAYS)
        else -> formatter.format((distance / DateUtils.WEEK_IN_MILLIS).toDouble(), next, RelativeDateTimeFormatter.RelativeUnit.WEEKS)
    }
}

/** A month's short name in [locale] ("Jan", "ene"), in the standalone form a picker cell shows. */
fun shortMonthName(month: Month, locale: Locale): String = month.getDisplayName(TextStyle.SHORT_STANDALONE, locale)

/** A month's full name in [locale] ("March", "marzo"), in the standalone form a picker headline shows. */
fun fullMonthName(month: Month, locale: Locale): String = month.getDisplayName(TextStyle.FULL_STANDALONE, locale)

/**
 * The numbers joined for display, with each run of [MIN_COLLAPSED_RUN] or more consecutive values
 * collapsed to a range: `1, 2, 3, 5, 6` reads "1-3, 5, 6". Sorted and de-duplicated first, so order
 * and repeats do not matter; empty input gives "". The numbers only, with no label, so the caller
 * keeps its own plural resource around the text. Zero is an ordinary number.
 */
fun Collection<Int>.formatRanges(): String {
    val runs = sorted().distinct().fold(mutableListOf<IntRange>()) { acc, n ->
        val last = acc.lastOrNull()
        if (last != null && last.last + 1 == n) acc[acc.lastIndex] = last.first..n else acc += n..n
        acc
    }
    return runs.joinToString(", ") { run ->
        val size = run.last - run.first + 1
        if (size >= MIN_COLLAPSED_RUN) "${run.first}-${run.last}" else run.joinToString(", ")
    }
}
