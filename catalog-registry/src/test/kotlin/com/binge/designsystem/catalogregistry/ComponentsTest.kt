package com.binge.designsystem.catalogregistry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComponentsTest {
    private fun sample(function: String, group: String) = SampleDeclaration(function, group, function, "")

    private fun demo(function: String, group: String) = SampleDeclaration(function, group, function, "", EntryKind.Demo)

    private fun List<ComponentEntry>.byComponent() =
        groupBy { it.component }.mapValues { (_, members) -> members.map { it.declaration.function } }

    @Test
    fun `a demo joins the component its name starts with, the longest one`() {
        val entries = toComponents(
            listOf(
                sample("BingeTopBarSample", "BingeTopBar"),
                sample("BingeTopBarSheetSample", "BingeTopBarSheet"),
                demo("BingeTopBarSheetOpenDemo", "Modal"),
                demo("BingeTopBarEnterAlwaysDemo", "TopBar"),
            ),
        )

        assertEquals(
            mapOf(
                "BingeTopBar" to listOf("BingeTopBarSample", "BingeTopBarEnterAlwaysDemo"),
                "BingeTopBarSheet" to listOf("BingeTopBarSheetSample", "BingeTopBarSheetOpenDemo"),
            ),
            entries.byComponent(),
        )
    }

    @Test
    fun `a demo that names no component keeps its file's group`() {
        val entries = toComponents(listOf(sample("TextButtonSample", "TextButton"), demo("LocalisedStringsDemo", "Locale")))

        assertEquals("Locale", entries.single { it.declaration.kind == EntryKind.Demo }.component)
    }

    @Test
    fun `a samples file whose functions all name a shorter component joins it`() {
        val entries = toComponents(
            listOf(
                sample("NavBarPhoneSample", "NavBar"),
                sample("NavBarArtworkSample", "NavBarTone"),
                sample("NavBarEmptySample", "NavBarTone"),
            ),
        )

        assertEquals(setOf("NavBar"), entries.map { it.component }.toSet())
    }

    @Test
    fun `one function named after its own file keeps the file separate`() {
        val entries = toComponents(
            listOf(
                sample("FilterChipSample", "FilterChip"),
                sample("FilterChipRowSample", "FilterChipRow"),
                sample("FilterChipPagerSample", "FilterChipRow"),
            ),
        )

        assertEquals(listOf("FilterChipRowSample", "FilterChipPagerSample"), entries.byComponent()["FilterChipRow"])
    }

    @Test
    fun `the default variant comes first, then samples and demos in source order`() {
        val entries = toComponents(
            listOf(
                demo("CardFlipDemo", "CardDemos"),
                sample("CardRatedSample", "Card"),
                sample("CardSample", "Card"),
                sample("CardEmptySample", "Card"),
            ),
        )

        assertEquals(listOf("Default", "Rated", "Empty", "Flip"), entries.map { it.variantName })
    }

    @Test
    fun `components sort by the name they are shown under`() {
        val entries = toComponents(listOf(sample("BingeTagSample", "BingeTag"), sample("HintCardSample", "HintCard")))

        assertEquals(listOf("Hint card", "Tag"), entries.map { it.componentName })
    }

    @Test
    fun `names a variant by what it adds to its component`() {
        assertEquals("Default", variantName("BingeTopBarSample", "BingeTopBar"))
        assertEquals("Transparent", variantName("BingeTopBarTransparentSample", "BingeTopBar"))
        assertEquals("Enter always", variantName("BingeTopBarEnterAlwaysDemo", "BingeTopBar"))
        assertEquals("Filled button loading", variantName("FilledButtonLoadingSample", "Button"))
        assertEquals("Tags row", variantName("TagsRowSample", "Tag"))
    }

    @Test
    fun `names a component without the design system's prefix`() {
        assertEquals("Nav floating bar", componentName("BingeNavFloatingBar"))
        assertEquals("Media card", componentName("MediaCard"))
        assertEquals("Bingeworthy", componentName("Bingeworthy"))
    }

    @Test
    fun `a CatalogGroup gathers files under its name, each variant named without the prefix`() {
        val entries = toComponents(
            listOf(
                SampleDeclaration("BingeTopBarDemo", "TopBar", "", "", EntryKind.Demo, catalogGroup = "Top app bars"),
                SampleDeclaration("DetailOverlayTopBarDemo", "TopBar", "", "", EntryKind.Demo, catalogGroup = "Top app bars"),
                sample("BingeTopBarStillSample", "BingeTopBar"),
            ),
        )

        val group = entries.filter { it.component == "Top app bars" }
        assertEquals(listOf("Top bar", "Detail overlay top bar"), group.map { it.variantName })
        assertEquals(setOf("Top app bars"), group.map { it.componentName }.toSet())
        assertEquals("BingeTopBar", entries.single { it.declaration.kind == EntryKind.Sample }.component)
    }

    @Test
    fun `a skeleton follows the real variants, whichever file sorts first`() {
        val entries = toComponents(
            listOf(
                SampleDeclaration("CarouselSkeletonSample", "CarouselSkeleton", "", "", catalogGroup = "Media carousel"),
                SampleDeclaration("MediaCarouselSample", "MediaCarousel", "", "", catalogGroup = "Media carousel"),
            ),
        )

        assertEquals(listOf("MediaCarouselSample", "CarouselSkeletonSample"), entries.map { it.declaration.function })
    }
}
