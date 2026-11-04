package com.binge.designsystem.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TitleLongClickTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `the small bar title responds to a long press`() = checkTitle(ScreenBar.Small, touch = true)

    @Test
    fun `the collapsing title exposes its labelled accessibility action`() = checkTitle(ScreenBar.Collapsing)

    @Test
    fun `the large title exposes its labelled accessibility action`() = checkTitle(ScreenBar.Large)

    private fun checkTitle(bar: ScreenBar, touch: Boolean = false) {
        var calls = 0
        rule.setContent {
            TestTheme {
                BingeScreenScaffold(
                    title = "Log name",
                    bar = bar,
                    onTitleLongClick = { calls++ },
                    onTitleLongClickLabel = "Show full name",
                ) { Box(Modifier.fillMaxSize()) }
            }
        }
        val title = rule.onNodeWithText("Log name")
        assertEquals("Show full name", title.fetchSemanticsNode().config[SemanticsActions.OnLongClick].label)
        if (touch) {
            title.performTouchInput { longClick() }
        } else {
            title.performSemanticsAction(SemanticsActions.OnLongClick) { it() }
        }
        rule.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun `an ordinary title stays readable with no long press action`() {
        rule.setContent {
            TestTheme {
                BingeScreenScaffold(title = "Log name", bar = ScreenBar.Small) { Box(Modifier.fillMaxSize()) }
            }
        }
        assertFalse(
            rule
                .onNodeWithText("Log name")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsActions.OnLongClick),
        )
    }
}
