package com.binge.designsystem

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class LocalisedTextTest {
    @get:Rule
    val rule = createComposeRule()

    private fun upper(text: String): String {
        var result = ""
        rule.setContent { result = text.uppercaseLocalised() }
        rule.waitForIdle()
        return result
    }

    @Test
    @Config(qualifiers = "en")
    fun `English capitals follow the root rules`() {
        assertEquals("DIZI", upper("dizi"))
    }

    @Test
    @Config(qualifiers = "tr")
    fun `Turkish gives i its dotted capital`() {
        assertEquals("DİZİ", upper("dizi"))
    }

    @Test
    @Config(qualifiers = "tr")
    fun `Turkish gives a dotless i the plain capital`() {
        assertEquals("ILIK", upper("ılık"))
    }
}
