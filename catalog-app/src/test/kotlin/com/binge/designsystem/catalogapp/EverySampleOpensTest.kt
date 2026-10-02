package com.binge.designsystem.catalogapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.overrides.WithOverrides
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.CatalogRegistry
import com.binge.designsystem.catalogapp.registry.TvCatalogRegistry
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The epic's "each opens": every registered sample and demo composes in a box that fills the screen
 * and centres, as the detail view's does, under one combined override: dark, RTL and the largest
 * font. Catches an entry that needs bounded space that box does not give, or that throws under a
 * forced configuration, which no screenshot of a sample alone would show.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class EverySampleOpensTest {
    @get:Rule
    val rule = createComposeRule()

    private var index by mutableIntStateOf(0)

    @Test
    fun `every sample composes under forced dark, rtl and the largest font`() {
        assertEverySampleComposes(CatalogRegistry) { it() }
    }

    @Test
    fun `every TV sample composes under forced rtl and the largest font`() {
        assertEverySampleComposes(TvCatalogRegistry) { BingeTvTheme(content = it) }
    }

    /** [host] is whatever the real activity wraps around a sample: the TV theme, for the TV registry. */
    private fun assertEverySampleComposes(registry: List<CatalogEntry>, host: @Composable (@Composable () -> Unit) -> Unit) {
        val overrides = SampleOverrides(dark = true, fontScale = FontScalePresets.last(), rtl = true)
        rule.setContent {
            host {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    WithOverrides(overrides, registry[index].content)
                }
            }
        }

        val failures = mutableListOf<String>()
        registry.indices.forEach { i ->
            index = i
            runCatching { rule.waitForIdle() }.onFailure { failures += "${registry[i].id}: ${it.message?.lineSequence()?.first()}" }
        }

        assertTrue("${failures.size} samples failed to compose:\n" + failures.joinToString("\n"), failures.isEmpty())
    }
}
