package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What the dock handle does when it is tapped, and the actions it offers a screen reader. Material draws
 * its own handle with these behaviours, and a docking sheet gives it up for its own, so a regression here
 * (a tap that closes a part-way sheet instead of expanding it) is invisible to every frame.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetDockHandleTest {
    @get:Rule
    val rule = createComposeRule()

    private var dismissed = 0

    private val hasExpand = SemanticsMatcher.keyIsDefined(SemanticsActions.Expand)
    private val hasCollapse = SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse)
    private val hasDismiss = SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)

    private fun showDockingSheet(skipPartiallyExpanded: Boolean, gesturesEnabled: Boolean = true) {
        dismissed = 0
        rule.setContent {
            TestTheme {
                BingeBottomSheet(
                    onDismissRequest = { dismissed++ },
                    skipPartiallyExpanded = skipPartiallyExpanded,
                    gesturesEnabled = gesturesEnabled,
                    dockable = true,
                ) {
                    BingeSheetDockingHeader(
                        header = { Text("Header") },
                        dockedTopBar = { Text("Docked") },
                    )
                    Box(Modifier.height(TALL_CONTENT))
                }
            }
        }
        rule.waitForIdle()
    }

    private fun handle() = rule.onNode(hasDismiss)

    /** The handle's own click, by its semantics: a touch click on it did not reach the toggle under Robolectric. */
    private fun tapHandle() {
        handle().performSemanticsAction(SemanticsActions.OnClick)
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        rule.waitForIdle()
    }

    @Test
    fun `a tap on the handle of a part-way sheet expands it, and the next tap collapses it`() {
        showDockingSheet(skipPartiallyExpanded = false)
        handle().assert(hasExpand)

        tapHandle()

        assertEquals(0, dismissed)
        handle().assert(hasCollapse)

        tapHandle()

        assertEquals(0, dismissed)
        handle().assert(hasExpand)
    }

    @Test
    fun `a tap on the handle of a sheet with no part-way anchor closes it`() {
        showDockingSheet(skipPartiallyExpanded = true)
        rule.onAllNodes(hasExpand or hasCollapse).assertCountEquals(0)

        tapHandle()

        assertEquals(1, dismissed)
    }

    @Test
    fun `the screen reader actions expand and collapse without closing`() {
        showDockingSheet(skipPartiallyExpanded = false)

        handle().performSemanticsAction(SemanticsActions.Expand)
        rule.waitForIdle()
        handle().assert(hasCollapse)

        handle().performSemanticsAction(SemanticsActions.Collapse)
        rule.waitForIdle()
        handle().assert(hasExpand)
        assertEquals(0, dismissed)
    }

    @Test
    fun `the dismiss action closes the sheet`() {
        showDockingSheet(skipPartiallyExpanded = false)

        handle().performSemanticsAction(SemanticsActions.Dismiss)
        rule.waitForIdle()

        assertEquals(1, dismissed)
    }

    @Test
    fun `a locked sheet offers no handle actions and ignores a tap`() {
        showDockingSheet(skipPartiallyExpanded = false, gesturesEnabled = false)
        rule.onAllNodes(hasDismiss or hasExpand or hasCollapse).assertCountEquals(0)

        rule.onNode(hasClickAction() and isNotEnabled()).performSemanticsAction(SemanticsActions.OnClick)
        rule.waitForIdle()

        assertEquals(0, dismissed)
    }

    private companion object {
        val TALL_CONTENT = 1200.dp
        const val SETTLE_MILLIS = 2_000L
    }
}
