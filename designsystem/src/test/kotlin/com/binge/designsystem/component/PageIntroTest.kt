package com.binge.designsystem.component

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class PageIntroTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the intro is read as page context and is not a heading`() {
        rule.setContent { TestTheme { PageIntro("What this page is for.") } }
        rule.onNodeWithText("What this page is for.").assertIsDisplayed()
        rule.onNodeWithText("What this page is for.").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Heading))
    }
}
