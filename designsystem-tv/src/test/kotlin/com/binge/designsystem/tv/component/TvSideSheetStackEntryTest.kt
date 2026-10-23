package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackEntryTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val focused = mutableStateListOf<String>()

    private fun Modifier.record(label: String): Modifier = onFocusChanged { if (it.isFocused) focused.add(label) }

    @Test
    fun `focus entering the sheet from outside lands on the row the page arrives at, never on the first`() {
        lateinit var enter: () -> Unit
        composeTestRule.setContent {
            val focusManager = LocalFocusManager.current
            val view = LocalView.current
            enter = {
                focusManager.clearFocus(force = true)
                focused.clear()
                view.clearFocus()
                view.requestFocus()
            }
            BingeTvTheme {
                TvSideSheetStack(root = "list", onDismissRequest = {}) { nav ->
                    TvSideSheetTitle("Settings")
                    (1..ROWS).forEach { index ->
                        val key = "row-$index"
                        TvSideSheetRow("Row $index", onClick = {}, modifier = nav.row(key).record(key))
                    }
                    nav.arriveAt("row-3")
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { enter() }
        composeTestRule.waitForIdle()

        assertEquals("focus order: $focused", listOf("row-3"), focused.toList())
        composeTestRule.onNodeWithText("Row 3").assertIsFocused()
        assertFalse("row 1 took focus on the way in: $focused", focused.contains("row-1"))
        assertEquals("row-3", focused.first())
    }

    private companion object {
        const val ROWS = 5
    }
}
