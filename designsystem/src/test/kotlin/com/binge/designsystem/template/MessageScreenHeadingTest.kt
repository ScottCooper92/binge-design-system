package com.binge.designsystem.template

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.testing.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class MessageScreenHeadingTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the headline is the one heading, and the body is not`() {
        rule.setContent {
            TestTheme {
                MessageScreen(headline = "Nothing here", body = "Add something to see it.")
            }
        }

        rule.onAllNodes(isHeading()).assertCountEquals(1)
        rule.onAllNodes(isHeading() and hasText("Nothing here")).assertCountEquals(1)
    }

    @Test
    fun `a message without a headline has no heading`() {
        rule.setContent {
            TestTheme { MessageScreen(body = "Add something to see it.") }
        }

        rule.onAllNodes(isHeading()).assertCountEquals(0)
    }
}
