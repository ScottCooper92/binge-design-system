package com.binge.designsystem.catalogregistry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ComponentIndexTest {
    private fun scan(source: String) = ComponentIndex.scan("Thing.kt", source.trimIndent())

    private val tagged =
        """
        /**
         * Pick one value from a list in a sheet. Rows are checkable.
         *
         * @need pick one value from a long list
         * @group sheets
         * @replaces a hand-built LazyColumn of rows
         */
        @Composable
        fun BingeChoiceSheet() {}
        """

    @Test
    fun `reads the tags and the first KDoc sentence`() {
        val result = scan(tagged)

        assertTrue(result.problems.isEmpty())
        assertEquals(
            listOf(
                IndexEntry(
                    name = "BingeChoiceSheet",
                    group = IndexGroup.Sheets,
                    need = "pick one value from a long list",
                    replaces = "a hand-built LazyColumn of rows",
                    description = "Pick one value from a list in a sheet",
                    sample = null,
                ),
            ),
            result.entries,
        )
    }

    @Test
    fun `an untagged public composable is a problem`() {
        val result = scan("/** A thing. */\n@Composable\nfun Thing() {}")

        assertEquals(1, result.problems.size)
        assertTrue("Thing.kt:3: public Thing has no @need or @group" in result.problems.single())
    }

    @Test
    fun `an unknown group is a problem`() {
        val result = scan("/**\n * A thing.\n *\n * @need a thing\n * @group widgets\n */\n@Composable\nfun Thing() {}")

        assertTrue("unknown @group 'widgets'" in result.problems.single())
    }

    @Test
    fun `internal, private, preview, sample, demo and lower-case functions are not components`() {
        val result = scan(
            """
            @Composable
            internal fun Hidden() {}
            @Composable
            private fun Secret() {}
            @Composable
            fun ThingPreview() {}
            @Composable
            fun ThingSample() {}
            @Composable
            fun ThingDemo() {}
            @Composable
            fun rememberThing() {}
            fun NotComposable() {}
            """,
        )

        assertTrue(result.entries.isEmpty())
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun `a composable marked as the design system's internal API is not a component`() {
        val result = scan(
            """
            @Composable
            @DesignSystemInternalApi
            fun Shared() {}
            @DesignSystemInternalApi
            @Composable
            fun AlsoShared() {}
            """,
        )

        assertTrue(result.entries.isEmpty())
        assertTrue(result.problems.isEmpty())
    }

    @Test
    fun `a scoped composable counts`() {
        val result = scan("/**\n * A bar.\n *\n * @need a bar\n * @group bars\n */\n@Composable\nfun BoxScope.OverlayBar() {}")

        assertEquals("OverlayBar", result.entries.single().name)
    }

    @Test
    fun `the sample is the tag, else the Sample named for it, else the first one it prefixes`() {
        val entry = scan(tagged).entries.single()

        assertEquals(
            "BingeChoiceSheetSample",
            ComponentIndex.sampleFor(entry, setOf("BingeChoiceSheetSample", "BingeChoiceSheetMultiSample")),
        )
        assertEquals("BingeChoiceSheetMultiSample", ComponentIndex.sampleFor(entry, setOf("ZSample", "BingeChoiceSheetMultiSample")))
        assertEquals("Other", ComponentIndex.sampleFor(entry.copy(sample = "Other"), emptySet()))
        assertNull(ComponentIndex.sampleFor(entry, setOf("ZSample")))
    }

    @Test
    fun `a sample tag that names no catalog sample is a problem`() {
        val source = tagged.trimIndent().replace(" * @group sheets", " * @group sheets\n * @sample NoSuchSample")
        val problems = ComponentIndex.problems(listOf("A.kt" to source), emptyList())

        assertEquals(listOf("BingeChoiceSheet: @sample NoSuchSample is not a catalog sample"), problems)
    }

    @Test
    fun `renders groups in order, rows sorted by need, and lists an overload once`() {
        val sources = listOf("A.kt" to (tagged.trimIndent() + "\n" + tagged.trimIndent()))
        val catalog = listOf("BingeChoiceSheetSamples.kt" to "/** The sheet. */\n@Composable\nfun BingeChoiceSheetSample() {}")

        val first = ComponentIndex.render(sources, catalog)

        assertEquals(1, Regex("`BingeChoiceSheet`").findAll(first).count())
        assertTrue("## Sheets and pickers" in first)
        assertTrue(
            "| pick one value from a long list | `BingeChoiceSheet` | Pick one value from a list in a sheet | " +
                "`BingeChoiceSheetSample` | a hand-built LazyColumn of rows |" in first,
        )
        assertEquals(first, ComponentIndex.render(sources.reversed(), catalog))
    }

    @Test
    fun `an overload with a need of its own gets its own row`() {
        val overload = tagged.trimIndent().replace("Pick one value from a list in a sheet", "Pick several values").replace(
            "pick one value from a long list",
            "pick several values from a long list",
        )
        val sources = listOf("A.kt" to (tagged.trimIndent() + "\n" + overload))

        val index = ComponentIndex.render(sources, emptyList())

        assertEquals(2, Regex("`BingeChoiceSheet`").findAll(index).count())
        assertTrue("| pick several values from a long list | `BingeChoiceSheet` | Pick several values |" in index)
    }

    @Test
    fun `a sample in a screenshot-only file is still the sample to copy`() {
        val sources = listOf("A.kt" to tagged.trimIndent())
        val catalog = listOf(
            "BingeChoiceSheetSamples.kt" to
                "@file:ScreenshotOnly\n\npackage x\n\n/** The sheet. */\n@Composable\nfun BingeChoiceSheetSample() {}",
        )

        assertTrue("`BingeChoiceSheetSample`" in ComponentIndex.render(sources, catalog))
        assertTrue(ComponentIndex.problems(sources, catalog).isEmpty())
    }

    @Test
    fun `a composable annotated on the same line is checked too`() {
        val result = scan("@Composable fun Thing() {}\n@Composable internal fun Quiet() {}")

        assertEquals(1, result.problems.size)
        assertTrue("public Thing has no @need" in result.problems.single())
    }
}
