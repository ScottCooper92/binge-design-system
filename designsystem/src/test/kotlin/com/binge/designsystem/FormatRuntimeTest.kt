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
class FormatRuntimeTest {
    @get:Rule
    val rule = createComposeRule()

    private fun runtimes(): List<String> {
        val read = mutableListOf<String>()
        rule.setContent { listOf(135, 45, 60).forEach { read += formatRuntime(it) } }
        rule.waitForIdle()
        return read
    }

    @Test
    fun `a runtime splits into hours and minutes`() {
        assertEquals(listOf("2h 15m", "0h 45m", "1h 0m"), runtimes())
    }

    @Test
    @Config(qualifiers = "es")
    fun `the split reads in the shown language`() {
        assertEquals(listOf("2 h 15 min", "0 h 45 min", "1 h 0 min"), runtimes())
    }
}
