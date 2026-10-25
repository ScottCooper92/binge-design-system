package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Focus entering a [tvEntryFocusGroup] through the window lands on its entry child, and on nothing before it.
 *
 * A window takes focus when the app starts or resumes, or a dialog over it closes. The view then asks Compose for
 * its initial focus, which enters the root's first group. [tvLateEntryFocusGroup] exists because an `onEnter`
 * request was followed by the group's own entry onto its first child. This pins that [tvEntryFocusGroup]'s
 * `onEnter` does not flash its first child on that path: the entry child is the only node that takes focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvEntryFocusGroupWindowEntryTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val focused = mutableStateListOf<String>()

    private fun Modifier.record(label: String): Modifier = onFocusChanged { if (it.isFocused) focused.add(label) }

    @Test
    fun `window entry into a column group lands on the entry child, never on the first`() {
        val entry = FocusRequester()
        lateinit var enter: () -> Unit
        composeTestRule.setContent {
            enter = windowEntry()
            BingeTvTheme {
                Column(Modifier.tvEntryFocusGroup(entry)) {
                    (0 until CELLS).forEach { index -> Cell(index, entry) }
                }
            }
        }

        enterAndAssertOnlyTarget(enter)
    }

    @Test
    fun `window entry into a lazy row group lands on the entry child, never on the first`() {
        val entry = FocusRequester()
        lateinit var enter: () -> Unit
        composeTestRule.setContent {
            enter = windowEntry()
            BingeTvTheme {
                LazyRow(Modifier.tvEntryFocusGroup(entry)) {
                    items(CELLS) { index -> Cell(index, entry) }
                }
            }
        }

        enterAndAssertOnlyTarget(enter)
    }

    @Composable
    private fun windowEntry(): () -> Unit {
        val focusManager = LocalFocusManager.current
        val view = LocalView.current
        return {
            focusManager.clearFocus(force = true)
            focused.clear()
            view.clearFocus()
            view.requestFocus()
        }
    }

    @Composable
    private fun Cell(index: Int, entry: FocusRequester) {
        val key = "cell-$index"
        Box(
            Modifier
                .size(CELL_SIZE_DP.dp)
                .then(if (index == TARGET) Modifier.focusRequester(entry) else Modifier)
                .record(key)
                .testTag(key)
                .focusable(),
        )
    }

    private fun enterAndAssertOnlyTarget(enter: () -> Unit) {
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { enter() }
        composeTestRule.waitForIdle()

        assertEquals("focus order: ${focused.toList()}", listOf("cell-$TARGET"), focused.toList())
        composeTestRule.onNodeWithTag("cell-$TARGET").assertIsFocused()
    }

    private companion object {
        const val CELLS = 5
        const val TARGET = 2
        const val CELL_SIZE_DP = 40
    }
}
