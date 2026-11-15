package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A [TvSideSheetStack] reopened on a saved path: it opens on the path's last page with focus on the row that last had it
 * there, Back goes to the page below with focus on the row that opened this one, and the stack's state reads, and saves,
 * the path as the user left it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackRestoreTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val focused = mutableStateListOf<String>()
    private lateinit var state: TvSideSheetStackState

    private fun Modifier.record(label: String): Modifier = onFocusChanged { if (it.isFocused) focused.add(label) }

    private fun setSheet(startPath: List<TvSideSheetStep> = emptyList()) {
        composeTestRule.setContent {
            BingeTvTheme {
                state = rememberTvSideSheetStackState("root", startPath)
                TvSideSheetStack(state = state, onDismissRequest = {}) { nav ->
                    TvSideSheetTitle(if (nav.page == "root") "Root title" else "${nav.page} title")
                    (1..ROWS).forEach { index ->
                        val key = "${nav.page}-$index"
                        TvSideSheetRow(
                            label = "${nav.page} $index",
                            onClick = { if (nav.page == "root" && index == OPENER) nav.push("child", key) },
                            modifier = nav.row(key).record(key),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private val rootStep = TvSideSheetStep("root")

    private fun childStep(focusedRow: String?) =
        TvSideSheetStep("child", openedBy = "root-$OPENER", parentTitle = "Root title", focusedRow = focusedRow)

    @Test
    fun `the stack opens on the last page of the path with focus on its remembered row, never on the first`() {
        setSheet(listOf(rootStep, childStep("child-3")))

        composeTestRule.onNodeWithText("child 3").assertIsFocused()
        assertFalse("the first row took focus on the way in: $focused", focused.contains("child-1"))
        assertEquals("focus order: $focused", listOf("child-3"), focused.toList())
        composeTestRule.onNodeWithText("child title").assertExists()
        composeTestRule.onNodeWithText("root 1").assertDoesNotExist()
    }

    @Test
    fun `a remembered row that is gone hands focus to the page's first row`() {
        setSheet(listOf(rootStep, childStep("no-such-row")))

        composeTestRule.onNodeWithText("child 1").assertIsFocused()
    }

    @Test
    fun `a restored page names its parent in the back though the parent was never shown`() {
        setSheet(listOf(rootStep, childStep("child-3")))

        composeTestRule.onAllNodesWithText("Root title").assertCountEquals(1)
    }

    @Test
    fun `Back from a restored page goes to the page below with focus on the row that opened it`() {
        setSheet(listOf(rootStep, childStep("child-3")))

        press(Key.DirectionLeft)

        composeTestRule.onNodeWithText("root $OPENER").assertIsFocused()
        composeTestRule.onNodeWithText("child title").assertDoesNotExist()
    }

    @Test
    fun `the path follows pages opening and closing and the row that last had focus`() {
        setSheet()
        assertEquals(listOf(TvSideSheetStep("root", focusedRow = "root-1")), state.path)

        press(Key.DirectionDown)
        press(Key.DirectionCenter)
        assertEquals(2, state.path.size)
        assertEquals(childStep("child-1"), state.path.last())

        press(Key.DirectionDown)
        assertEquals("child-2", state.path.last().focusedRow)

        press(Key.DirectionLeft)
        assertEquals(1, state.path.size)
        assertEquals("root-$OPENER", state.path.single().focusedRow)
    }

    @Test
    fun `a path saved and restored comes back as it was`() {
        setSheet(listOf(rootStep, childStep("child-3")))
        val saved = with(SaverScope { true }) { with(TvSideSheetStackState.StateSaver) { save(state) } }!!

        val restored = TvSideSheetStackState.StateSaver.restore(saved)!!

        assertEquals(state.path, restored.path)
        assertEquals("child-3", restored.path.last().focusedRow)
    }

    @Test
    fun `an empty path, and one that does not begin at the root, open on the root`() {
        setSheet()
        composeTestRule.onNodeWithText("root 1").assertIsFocused()
        assertEquals(1, state.path.size)

        val elsewhere = TvSideSheetStackState.seeded(listOf(TvSideSheetStep("other"), childStep("child-3")), root = "root")
        assertEquals(listOf("root"), elsewhere.path.map { it.page })
    }

    @Test
    fun `a page named twice in a path ends the path at its first use`() {
        val state = TvSideSheetStackState.seeded(listOf(rootStep, childStep("child-3"), childStep("child-1")), root = "root")

        assertEquals(listOf("root", "child"), state.path.map { it.page })
    }

    private companion object {
        const val ROWS = 5
        const val OPENER = 2
    }
}
