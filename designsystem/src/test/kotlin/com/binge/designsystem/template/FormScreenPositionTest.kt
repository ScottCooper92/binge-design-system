package com.binge.designsystem.template

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.InMemoryStateRestorationTester
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FormScreenPositionTest {
    @get:Rule val rule = createTakeDownComposeRule()

    @Test fun `loading child pages and saved state restore the content and bar as one position`() {
        var page by mutableStateOf("main")
        var loading by mutableStateOf(false)
        val restoration = InMemoryStateRestorationTester(rule)
        restoration.setContent {
            val waiting: (@Composable (PaddingValues) -> Unit)? = if (loading) ({ Text("Loading") }) else null
            TestTheme {
                FormScreen(title = page, bar = ScreenBar.Small, positionKey = page, notReady = waiting) {
                    repeat(60) { Text("$page row $it", Modifier.height(56.dp)) }
                }
            }
        }
        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.waitForIdle()
        val scroll = position()
        val bar = titleTop("main")
        assertTrue("The form actually scrolled", scroll > 0f)
        assertTrue("The bar actually slid away", bar < 0f)

        rule.runOnIdle { loading = true }
        rule.onNodeWithText("Loading").assertIsDisplayed()
        rule.runOnIdle { loading = false }
        assertEquals(scroll, position(), 1f)
        assertEquals(bar, titleTop("main"), 1f)

        rule.runOnIdle { page = "child" }
        assertEquals(0f, position(), 1f)
        rule.onNodeWithText("child").assertIsDisplayed()
        assertTrue(titleTop("child") >= 0f)
        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.runOnIdle { page = "main" }
        assertEquals(scroll, position(), 1f)
        assertEquals(bar, titleTop("main"), 1f)

        restoration.emulateSavedInstanceStateRestore()
        assertEquals(scroll, position(), 1f)
        assertEquals(bar, titleTop("main"), 1f)
    }

    private fun position(): Float {
        rule.waitForIdle()
        return rule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange]
            .value()
    }

    private fun titleTop(title: String): Float =
        rule
            .onNodeWithText(title)
            .getUnclippedBoundsInRoot()
            .top.value
}
