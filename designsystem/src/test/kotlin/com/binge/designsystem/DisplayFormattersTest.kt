package com.binge.designsystem

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale
import java.util.stream.Stream

class DisplayFormattersTest {
    private val defaultLocale = Locale.getDefault()

    @AfterEach
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @ParameterizedTest
    @CsvSource(
        "500, 500",
        "1500, 1.5k",
        "2000000, 2.0M",
        "999949, 999.9k",
        "999950, 1.0M",
        "999999, 1.0M",
        "999999950, 1.0B",
    )
    fun `formatVoteCount abbreviates thousands and millions`(count: Int, expected: String) {
        Locale.setDefault(Locale.UK)
        assertEquals(expected, count.formatVoteCount())
    }

    @Test
    fun `formatVoteCount does not crash on a comma-decimal locale at the unit boundary`() {
        Locale.setDefault(Locale.GERMANY)
        assertEquals("1,0M", 999_950.formatVoteCount())
    }

    @ParameterizedTest
    @CsvSource("7.46, 7.5", "0.0, 0.0", "10.0, 10.0", "6.04, 6.0")
    fun `formatRating renders one decimal place`(rating: Float, expected: String) {
        Locale.setDefault(Locale.UK)
        assertEquals(expected, rating.formatRating())
    }

    @Test
    fun `formatRating uses the locale decimal separator without crashing`() {
        Locale.setDefault(Locale.GERMANY)
        assertEquals("7,5", 7.46f.formatRating())
    }

    @ParameterizedTest
    @MethodSource("toInitialsCases")
    fun `toInitials derives up to two initials with the default fallback`(input: String, expected: String) {
        assertEquals(expected, input.toInitials())
    }

    @Test
    fun `toInitials returns the explicit fallback when nothing can be derived`() {
        assertEquals("?", "".toInitials(fallback = "?"))
        assertEquals("?", "._-".toInitials(fallback = "?"))
    }

    @ParameterizedTest
    @CsvSource("0, 0", "1, 1", "99, 99", "100, 99+", "1500, 99+")
    fun `badgeCountLabel caps at 99+`(count: Int, expected: String) {
        assertEquals(expected, badgeCountLabel(count))
    }

    companion object {
        @JvmStatic
        fun toInitialsCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of("John Doe", "JD"),
                Arguments.of("John Ronald Reuel Tolkien", "JR"),
                Arguments.of("john doe", "JD"),
                Arguments.of("Madonna", "M"),
                Arguments.of("john.doe", "JD"),
                Arguments.of("john_doe", "JD"),
                Arguments.of("john-doe", "JD"),
                Arguments.of("  john   doe  ", "JD"),
            )
    }
}
