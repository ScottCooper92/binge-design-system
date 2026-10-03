package com.binge.designsystem.catalogregistry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegistryGeneratorTest {
    private val button = "ButtonSamples.kt" to "/** Buttons. */\n@Composable\nfun ButtonFamilySample() {}"
    private val card = "CardSamples.kt" to "/** A card. */\n@Composable\nfun CardRatedSample() {}\n\n@Composable\nfun CardEmptySample() {}"

    @Test
    fun `lists every sample with its import, by component and then source order`() {
        val out = generateRegistry(listOf(card, button))

        assertTrue("import com.binge.designsystem.catalog.ButtonFamilySample" in out)
        assertTrue("content = { CardEmptySample() }," in out)
        val order = Regex("""id = "(\w+)"""").findAll(out).map { it.groupValues[1] }.toList()
        assertEquals(listOf("ButtonFamilySample", "CardRatedSample", "CardEmptySample"), order)
    }

    @Test
    fun `writes each entry's component and the name it has there`() {
        val out = generateRegistry(listOf(card))

        assertTrue("""group = "Card",""" in out, out)
        assertTrue("""groupName = "Card",""" in out, out)
        assertTrue("""name = "Rated",""" in out, out)
    }

    @Test
    fun `output does not depend on the order the files arrive in`() {
        assertEquals(generateRegistry(listOf(button, card)), generateRegistry(listOf(card, button)))
    }

    @Test
    fun `escapes quotes and dollar signs in a description`() {
        val out = generateRegistry(listOf("QuoteSamples.kt" to "/** Says \"hi\" for \$5. */\n@Composable\nfun QuoteSample() {}"))

        assertTrue("""description = "Says \"hi\" for \$5",""" in out, out)
    }

    @Test
    fun `fails with every unregistrable sample across files rather than the first`() {
        val bad1 = "AAmSamples.kt" to "@Composable\nfun OneSample(x: Int) {}"
        val bad2 = "BBSamples.kt" to "fun TwoSample() {}"

        val thrown = assertThrows(UnregistrableSampleException::class.java) { generateRegistry(listOf(button, bad1, bad2)) }

        assertEquals(2, thrown.problems.size)
        assertTrue("AAmSamples.kt:2:" in thrown.problems[0])
        assertTrue("BBSamples.kt:1:" in thrown.problems[1])
    }

    @Test
    fun `a second catalog gets its own package and registry name`() {
        val out = generateRegistry(
            listOf("TvButtonSamples.kt" to "/** A button. */\n@Composable\nfun TvButtonSample() {}"),
            catalogPackage = "com.binge.designsystem.tv.catalog",
            registryName = "TvCatalogRegistry",
        )

        assertTrue("import com.binge.designsystem.tv.catalog.TvButtonSample" in out)
        assertTrue("val TvCatalogRegistry: List<CatalogEntry> =" in out)
        assertTrue("com.binge.designsystem.catalog.ButtonFamilySample" !in out)
    }

    @Test
    fun `writes each entry's kind`() {
        val out = generateRegistry(listOf("BusyDemos.kt" to "/** Busy. */\n@Composable\nfun BusyDemo() {}", button))

        assertTrue("kind = CatalogKind.Demo," in out)
        assertTrue("kind = CatalogKind.Sample," in out)
    }
}
