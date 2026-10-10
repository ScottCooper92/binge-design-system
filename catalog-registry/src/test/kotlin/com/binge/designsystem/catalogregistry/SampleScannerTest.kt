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
    fun `a sample without a KDoc is a problem with its file and line`() {
        val result = scan("@Composable\nfun BareSample() {}")

        assertTrue(result.samples.isEmpty())
        assertTrue(result.problems.single().startsWith("ButtonSamples.kt:2:"), result.problems.single())
        assertTrue("no KDoc" in result.problems.single())
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

    @Test
    fun `finds a demo and marks its kind, grouping a Demos file like a Samples file`() {
        val result = SampleScanner.scan(
            "TopBarDemos.kt",
            "/** A bar that scrolls. Try it. */\n@Composable\nfun BingeTopBarEnterAlwaysDemo() {}\n\n/** Fixture. */\n@Composable\nfun BingeTopBarSample() {}",
        )

        assertTrue(result.problems.isEmpty())
        assertEquals(
            listOf(
                SampleDeclaration(
                    "BingeTopBarEnterAlwaysDemo",
                    "TopBar",
                    "Binge top bar enter always",
                    "A bar that scrolls",
                    EntryKind.Demo,
                ),
                SampleDeclaration("BingeTopBarSample", "TopBar", "Binge top bar", "Fixture", EntryKind.Sample),
            ),
            result.samples,
        )
    }

    @Test
    fun `a parameterised or non-composable demo fails like a sample, naming it a demo`() {
        val result = scan("@Composable\nfun BusyDemo(x: Int) {}\n\nfun PlainDemo() {}", "BusyDemos.kt")

        assertEquals(2, result.problems.size)
        assertTrue("a demo takes none" in result.problems[0], result.problems[0])
        assertTrue("a catalog demo must be" in result.problems[1], result.problems[1])
    }

    @Test
    fun `a function that merely contains the word is not an entry`() {
        val result = scan("@Composable\nfun DemoReel(x: Int) {}\n\n@Composable\nfun SampleSizeDemoHelper(x: Int) {}")

        assertTrue(result.samples.isEmpty())
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun `a file marked OnePerScreen marks every sample in it, and only that file`() {
        val marked = SampleScanner.scan(
            "TopBarSamples.kt",
            "@file:OnePerScreen\n\npackage x\n\n/** Doc. */\n@Composable\nfun TopBarSample() {}",
        )
        val plain = SampleScanner.scan("ButtonSamples.kt", "package x\n\n/** Doc. */\n@Composable\nfun ButtonSample() {}")

        assertEquals(true, marked.samples.single().onePerScreen)
        assertEquals(false, plain.samples.single().onePerScreen)
    }

    @Test
    fun `a file's CatalogGroup names the group its entries are listed under`() {
        val result = SampleScanner.scan(
            "TopBarDemos.kt",
            "@file:CatalogGroup(\"Top app bars\")\n\npackage x\n\n/** Doc. */\n@Composable\nfun TopBarDemo() {}",
        )

        assertEquals("Top app bars", result.samples.single().catalogGroup)
    }

    @Test
    fun `a ScreenshotOnly file lists nothing, but a bad sample in it still fails`() {
        val result = SampleScanner.scan(
            "TopBarSamples.kt",
            "@file:ScreenshotOnly\n\npackage x\n\n/** Doc. */\n@Composable\nfun TopBarSample() {}\n\nfun BadSample() {}",
        )

        assertEquals(emptyList<SampleDeclaration>(), result.samples)
        assertEquals(1, result.problems.size)
    }

    @Test
    fun `lists a sample whose annotation sits on the same line as its function`() {
        val result = scan("/** Doc. */\n@Composable fun InlineSample() {}")

        assertTrue(result.problems.isEmpty(), result.problems.toString())
        assertEquals("Doc", result.samples.single().description)
    }

    @Test
    fun `a same-line annotation with arguments does not hide the composable or the parameters`() {
        val result =
            scan("/** Doc. */\n@OptIn(ExperimentalFoo::class) @Composable fun InlineSample() {}\n\n@Composable fun WideSample(a: Int) {}")

        assertEquals(listOf("InlineSample"), result.samples.map { it.function })
        assertTrue(result.problems.single().contains("takes parameters"), result.problems.toString())
    }

    @Test
    fun `reports a same-line sample that is not composable`() {
        val result = scan("@Preview fun PlainSample() {}")

        assertTrue(result.samples.isEmpty())
        assertTrue("not @Composable" in result.problems.single(), result.problems.toString())
    }

    @Test
    fun `skips a same-line non-public declaration`() {
        val result = scan("@Composable private fun HiddenSample() {}")

        assertTrue(result.samples.isEmpty())
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun `a blank line between the KDoc and the annotations keeps the description`() {
        val result = scan("/** Doc. */\n\n@Composable\nfun SpacedSample() {}")

        assertEquals("Doc", result.samples.single().description)
    }
}
