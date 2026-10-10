package com.binge.designsystem.component

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Every section title is a landmark a screen reader can jump to, with or without a "more" action. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SectionHeaderSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun `a title-only header is a heading`() {
        rule.setContent { BingeExpressiveTheme(dynamicColor = false) { SectionHeader(title = "Trending") } }

        rule.onNodeWithText("Trending").assert(isHeading)
    }

    @Test
    fun `a header with a more action is still a heading`() {
        rule.setContent { BingeExpressiveTheme(dynamicColor = false) { SectionHeader(title = "Trending", onMoreClick = {}) } }

        rule.onNodeWithText("Trending").assert(isHeading)
    }
}
