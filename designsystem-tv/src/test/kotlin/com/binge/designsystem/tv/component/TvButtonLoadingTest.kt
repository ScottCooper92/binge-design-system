package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A button whose action is in flight takes no second OK, keeps focus so the remote does not lose its place, and says
 * it is busy without losing its name. The confirm dialog's extra slot renders, and does not take arrival focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvButtonLoadingTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var clicks = 0

    private val isBusy = SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate)

    @Test
    fun `a loading TvButton ignores OK and keeps focus`() {
        setControl { TvButton(label = LABEL, onClick = { clicks++ }, loading = true, modifier = Modifier.testTag(NODE)) }

        pressOk()

        assertEquals(0, clicks)
        rule.onNodeWithTag(NODE).assertIsFocused().assertIsEnabled()
    }

    @Test
    fun `an idle TvButton takes OK`() {
        setControl { TvButton(label = LABEL, onClick = { clicks++ }, modifier = Modifier.testTag(NODE)) }

        pressOk()

        assertEquals(1, clicks)
    }

    @Test
    fun `a loading TvButton keeps its label as its name and says it is busy`() {
        setControl { TvButton(label = LABEL, onClick = {}, loading = true, modifier = Modifier.testTag(NODE)) }

        rule.onNodeWithTag(NODE).assert(hasText(LABEL)).assert(isBusy)
    }

    @Test
    fun `a loading TvIconButton ignores OK and keeps focus`() {
        setControl {
            TvIconButton(icon = Icons.Filled.Add, label = LABEL, onClick = { clicks++ }, loading = true, modifier = Modifier.testTag(NODE))
        }

        pressOk()

        assertEquals(0, clicks)
        rule.onNodeWithTag(NODE).assertIsFocused().assertIsEnabled()
    }

    @Test
    fun `an idle TvIconButton takes OK`() {
        setControl { TvIconButton(icon = Icons.Filled.Add, label = LABEL, onClick = { clicks++ }, modifier = Modifier.testTag(NODE)) }

        pressOk()

        assertEquals(1, clicks)
    }

    @Test
    fun `a loading TvIconButton keeps its label as its name and says it is busy`() {
        setControl {
            TvIconButton(icon = Icons.Filled.Add, label = LABEL, onClick = {}, loading = true, modifier = Modifier.testTag(NODE))
        }

        rule.onNodeWithTag(NODE).assert(hasContentDescription(LABEL)).assert(isBusy)
    }

    @Test
    fun `the confirm dialog shows its extra content, and focus still arrives on dismiss`() {
        rule.setContent {
            BingeTvTheme {
                TvConfirmDialogContent(
                    title = "Delete this request?",
                    message = "It is removed for everyone.",
                    confirmLabel = "Delete",
                    dismissLabel = "Cancel",
                    destructive = true,
                    onConfirm = {},
                    onDismissRequest = {},
                    extraContent = { TvCheckboxRow(label = EXTRA, checked = false, onCheckedChange = {}) },
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText(EXTRA).assertExists()
        rule.onNodeWithText("Cancel").assertIsFocused()
    }

    private fun setControl(content: @Composable () -> Unit) {
        rule.setContent { BingeTvTheme { content() } }
        rule.onNodeWithTag(NODE).requestFocus()
        rule.waitForIdle()
    }

    private fun pressOk() {
        rule.onNodeWithTag(NODE).performKeyInput { pressKey(Key.DirectionCenter) }
        rule.waitForIdle()
    }

    private companion object {
        const val NODE = "control"
        const val LABEL = "Refresh"
        const val EXTRA = "Also block this title"
    }
}
