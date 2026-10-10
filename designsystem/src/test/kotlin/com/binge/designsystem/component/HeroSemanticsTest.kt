package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The hero announces a trending rank only for an item that carries one (#365). The rank used to be the slide's
 * position, so any list fed to the hero claimed "#1 trending today" on its first slide.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class HeroSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun describe(item: HeroItem): String {
        var description = ""
        rule.setContent { description = heroContentDescription(item) }
        rule.waitForIdle()
        return description
    }

    @Test
    fun `an unranked item announces no trending rank`() {
        val description = describe(HeroItem(id = 1, imageUrl = null, title = "Inception", rating = null))

        assertFalse(description, description.contains("trending"))
    }

    @Test
    fun `a ranked item announces its own rank`() {
        val description = describe(HeroItem(id = 1, imageUrl = null, title = "Inception", rating = null, rank = 3))

        assertTrue(description, description.contains("#3 trending today"))
    }
}
