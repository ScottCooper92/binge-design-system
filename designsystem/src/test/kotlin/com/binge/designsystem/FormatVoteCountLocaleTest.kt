package com.binge.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

/**
 * [formatVoteCount] across languages, at 999, 1,000, 24,000 and 1.2M. English keeps its "k" and "M" with one decimal;
 * every other language takes ICU's compact short form, so Spanish reads "24\u00A0mil" and German writes its own separators.
 * Under Robolectric because `android.icu` is not available on the plain JVM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class FormatVoteCountLocaleTest {
    private val english = Locale.forLanguageTag("en-GB")
    private val spanish = Locale.forLanguageTag("es")
    private val german = Locale.forLanguageTag("de")

    @Test fun `english below a thousand is the bare count`() = assertEquals("999", 999.formatVoteCount(english))

    @Test fun `english at a thousand`() = assertEquals("1.0k", 1_000.formatVoteCount(english))

    @Test fun `english at 24 thousand`() = assertEquals("24.0k", 24_000.formatVoteCount(english))

    @Test fun `english at 1_2 million`() = assertEquals("1.2M", 1_200_000.formatVoteCount(english))

    @Test fun `spanish below a thousand is the bare count`() = assertEquals("999", 999.formatVoteCount(spanish))

    @Test fun `spanish at a thousand`() = assertEquals("1\u00A0mil", 1_000.formatVoteCount(spanish))

    @Test fun `spanish at 24 thousand reads 24 mil`() = assertEquals("24\u00A0mil", 24_000.formatVoteCount(spanish))

    @Test fun `spanish at 1_2 million`() = assertEquals("1,2\u00A0M", 1_200_000.formatVoteCount(spanish))

    @Test fun `german below a thousand is the bare count`() = assertEquals("999", 999.formatVoteCount(german))

    @Test fun `german at a thousand`() = assertEquals("1000", 1_000.formatVoteCount(german))

    @Test fun `german at 24 thousand`() = assertEquals("24.000", 24_000.formatVoteCount(german))

    @Test fun `german at 1_2 million`() = assertEquals("1,2\u00A0Mio.", 1_200_000.formatVoteCount(german))
}
