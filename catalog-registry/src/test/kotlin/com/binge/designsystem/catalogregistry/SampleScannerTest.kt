package com.binge.designsystem.catalogregistry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SampleScannerTest {
    private fun scan(source: String, file: String = "ButtonSamples.kt") = SampleScanner.scan(file, source.trimIndent())

    @Test
    fun `parses a sample with its group name and first KDoc sentence`() {
        val result = scan(
            """
            /** Filled button in its loading state. Taps are swallowed. */
            @Composable
            fun FilledButtonLoadingSample() {
                ScreenshotTheme { }
            }
            """,
        )

        assertTrue(result.problems.isEmpty())
        assertEquals(
            listOf(SampleDeclaration("FilledButtonLoadingSample", "Button", "Filled button loading", "Filled button in its loading state")),
            result.samples,
        )
    }

    @Test
    fun `takes the first sentence of a multi-line KDoc and tidies links`() {
        val result = scan(
            """
            /**
             * A destructive outlined button beside an
             * ordinary one, see [BingeOutlinedButton]. The pair is the sample.
             *
             * @see Other
             */
            @Composable
            fun OutlinedButtonDestructiveSample() {}
            """,
        )

        assertEquals("A destructive outlined button beside an ordinary one, see BingeOutlinedButton", result.samples.single().description)
    }

    @Test
    fun `does not split a sentence on an abbreviation`() {
        val result = scan(
            """
            /** Chips for e.g. genres, in a row. Then more. */
            @Composable
            fun GenreChipsSample() {}
            """,
        )

        assertEquals("Chips for e.g. genres, in a row", result.samples.single().description)
    }

    @Test
    fun `handles a file with several samples and an annotation between the KDoc and the function`() {
        val result = scan(
            """
            /** First. */
            @Composable
            fun FirstSample() {}

            private fun helper() {}

            /** Second. */
            @OptIn(ExperimentalMaterial3Api::class)
            @Composable
            fun SecondSample() {}
            """,
        )

        assertEquals(listOf("FirstSample", "SecondSample"), result.samples.map { it.function })
        assertEquals(listOf("First", "Second"), result.samples.map { it.description })
    }

    @Test
    fun `a sample without a KDoc gets an empty description`() {
        val result = scan("@Composable\nfun BareSample() {}")

        assertEquals("", result.samples.single().description)
    }

    @Test
    fun `rejects a parameterised sample with its file and line`() {
        val result = scan(
            """
            /** Doc. */
            @Composable
            fun TonedSample(tone: Tone) {}
            """,
        )

        assertTrue(result.samples.isEmpty())
        assertEquals(1, result.problems.size)
        assertTrue(result.problems.single().startsWith("ButtonSamples.kt:3:"), result.problems.single())
        assertTrue("tone: Tone" in result.problems.single())
    }

    @Test
    fun `rejects a parameter list that spans lines`() {
        val result = scan("@Composable\nfun WideSample(\n    a: Int,\n    b: Int,\n) {}")

        assertTrue(result.problems.single().contains("takes parameters"))
    }

    @Test
    fun `rejects a public sample that is not composable`() {
        val result = scan("fun PlainSample() {}")

        assertTrue(result.problems.single().startsWith("ButtonSamples.kt:1:"))
        assertTrue("not @Composable" in result.problems.single())
    }

    @Test
    fun `skips non-public declarations and functions that do not end in Sample`() {
        val result = scan(
            """
            @Composable
            private fun HiddenSample(tone: Tone) {}

            @Composable
            internal fun InternalSample(tone: Tone) {}

            @Composable
            fun Helper(tone: Tone) {}

            @Composable
            fun SampleData() {}
            """,
        )

        assertTrue(result.samples.isEmpty())
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun `derives groups and display names`() {
        assertEquals("BingeConfirmDialog", SampleScanner.groupOf("BingeConfirmDialogSamples.kt"))
        assertEquals("Button", SampleScanner.groupOf("catalog/ButtonSamples.kt"))
        assertEquals("Button family", SampleScanner.displayName("ButtonFamilySample"))
        assertEquals("TV card row", SampleScanner.displayName("TVCardRowSample"))
        assertEquals("Media card rated", SampleScanner.displayName("MediaCardRatedSample"))
    }
}
