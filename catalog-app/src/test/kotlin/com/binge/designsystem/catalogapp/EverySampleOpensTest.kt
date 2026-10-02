package com.binge.designsystem.catalogapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.catalogapp.registry.CatalogRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The epic's "each opens": every registered sample composes in the detail view's box, under each
 * override. Catches a sample that needs bounded space the box does not give, or that throws under a
 * forced configuration, which no screenshot of the sample alone would show.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class EverySampleOpensTest {
    @get:Rule
    val rule = createComposeRule()

    private var index by mutableIntStateOf(0)

    @Test
    fun `every sample composes under forced dark, rtl and the largest font`() {
        val overrides = SampleOverrides(dark = true, fontScale = FontScalePresets.last(), rtl = true)
        rule.setContent { WithOverrides(overrides, CatalogRegistry[index].content) }

        val failures = mutableListOf<String>()
        CatalogRegistry.indices.forEach { i ->
            index = i
            runCatching { rule.waitForIdle() }.onFailure { failures += "${CatalogRegistry[i].id}: ${it.message?.lineSequence()?.first()}" }
        }

        assertTrue("${failures.size} samples failed to compose:\n" + failures.joinToString("\n"), failures.isEmpty())
    }
}
