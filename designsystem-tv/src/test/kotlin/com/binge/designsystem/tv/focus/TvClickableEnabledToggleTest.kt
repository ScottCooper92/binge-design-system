package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvCompactButton
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A control that goes disabled while it holds focus (a Retry or a submit whose request is now in flight) keeps that
 * focus, ignores OK while disabled, and takes OK again once re-enabled. Swapping its focus node instead would throw
 * focus out of the page on the press that started the request.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvClickableEnabledToggleTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var enabled by mutableStateOf(true)
    private var clicks = 0

    @Test
    fun `a focused tvClickable keeps focus when it is disabled, and ignores OK until re-enabled`() {
        setControl {
            Box(
                Modifier
                    .testTag(NODE)
                    .size(dimensionResource(R.dimen.tv_compact_button_height))
                    .tvClickable(onFocusChanged = {}, enabled = enabled, onClick = { clicks++ }),
            )
        }

        assertTogglingKeepsFocus()
    }

    @Test
    fun `a focused TvCompactButton keeps focus when it is disabled, and ignores OK until re-enabled`() {
        setControl {
            TvCompactButton(label = "Retry", onClick = { clicks++ }, enabled = enabled, modifier = Modifier.testTag(NODE))
        }

        assertTogglingKeepsFocus()
    }

    private fun assertTogglingKeepsFocus() {
        enabled = false
        rule.waitForIdle()
        rule.onNodeWithTag(NODE).assertIsFocused().assertIsNotEnabled()

        pressOk()
        assertEquals(0, clicks)
        rule.onNodeWithTag(NODE).assertIsFocused()

        enabled = true
        rule.waitForIdle()
        rule.onNodeWithTag(NODE).assertIsFocused().assertIsEnabled()

        pressOk()
        assertEquals(1, clicks)
    }

    private fun setControl(content: @Composable () -> Unit) {
        rule.setContent {
            BingeTvTheme {
                // Another target ahead of the control: with nothing else on the page, focus that drops would land straight back
                // on the control and the test could not tell.
                Column {
                    Box(Modifier.size(dimensionResource(R.dimen.tv_compact_button_height)).focusable())
                    content()
                }
            }
        }
        rule.onNodeWithTag(NODE).requestFocus()
        rule.waitForIdle()
        rule.onNodeWithTag(NODE).assertIsFocused()
    }

    private fun pressOk() {
        rule.onNodeWithTag(NODE).performKeyInput { pressKey(Key.DirectionCenter) }
        rule.waitForIdle()
    }

    private companion object {
        const val NODE = "control"
    }
}
