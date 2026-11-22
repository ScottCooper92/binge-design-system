package com.binge.designsystem.component

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
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

private const val TITLE = "Seasons"
private const val BACK = "Back"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetPageHeaderTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var backs = 0

    private fun show(
        title: String? = TITLE,
        enabled: Boolean = true,
        backLabel: String? = null,
    ) {
        rule.setContent {
            TestTheme { BingeSheetPageHeader(onBack = { backs++ }, title = title, enabled = enabled, backLabel = backLabel) }
        }
        rule.waitForIdle()
    }

    @Test
    fun `back fires onBack`() {
        show()

        rule.onNodeWithContentDescription(BACK).performClick()

        assertEquals(1, backs)
    }

    @Test
    fun `the title is a heading for accessibility`() {
        show()

        rule.onNodeWithText(TITLE).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test
    fun `with no title the bar is back alone and has no heading`() {
        show(title = null)

        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertCountEquals(0)
        rule.onNodeWithContentDescription(BACK).assertHasClickAction()
    }

    @Test
    fun `a disabled bar does not fire onBack`() {
        show(enabled = false)

        rule.onNodeWithContentDescription(BACK).performClick()

        assertEquals(0, backs)
    }

    @Test
    fun `with a back label the arrow and the name are one target`() {
        show(backLabel = "Request")

        rule.onAllNodes(hasClickAction()).assertCountEquals(1)
        rule.onNodeWithText("Request").performClick()

        assertEquals(1, backs)
    }
}
