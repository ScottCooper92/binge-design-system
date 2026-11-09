package com.binge.designsystem.component

import android.app.Application
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w320dp-h640dp")
class ClockTimeRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a 24-hour phone reads the time as hours and minutes`() {
        assertEquals("21:30", clockTimeText(minutes = 21 * 60 + 30, is24Hour = true, locale = Locale.US))
    }

    @Test
    fun `a 12-hour phone reads the time with a period`() {
        val text = clockTimeText(minutes = 21 * 60 + 30, is24Hour = false, locale = Locale.US)

        assertEquals(true, text.startsWith("9:30"))
        assertEquals(true, text.contains("PM", ignoreCase = true))
    }

    @Test
    fun `a whole day reads as midnight`() {
        assertEquals("00:00", clockTimeText(minutes = 1440, is24Hour = true, locale = Locale.US))
    }

    @Test
    fun `fractional hours keep their minutes`() {
        assertEquals("08:30", clockHoursText(hour = 8.5, is24Hour = true, locale = Locale.US))
    }

    @Test
    fun `the row shows the time as its detail and a tap opens the picker`() {
        rule.setContent { TestTheme { ClockTimeRow(label = "Start", minutes = 21 * 60 + 30, onPick = {}, is24Hour = true) } }

        rule.onNodeWithText("21:30").assertExists()
        rule.onNodeWithText("Start").performClick()

        rule.onNodeWithText("OK").assertExists()
    }
}
