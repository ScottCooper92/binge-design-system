package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.template.StepHeading
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A screen's or a step's title is a heading, so TalkBack's heading navigation reaches it (#403). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SectionTitleHeadingTest {
    @get:Rule
    val rule = createComposeRule()

    private fun assertOneHeading(title: String, content: @Composable () -> Unit) {
        rule.setContent { BingeExpressiveTheme(dynamicColor = false) { content() } }
        rule.onAllNodes(isHeading()).assertCountEquals(1)
        rule.onAllNodes(isHeading() and hasText(title)).assertCountEquals(1)
    }

    @Test
    fun `a grid screen's title is its heading`() = assertOneHeading("Trending") { GridScreenHeader(title = "Trending", onBack = {}) }

    @Test
    fun `a step's title is its heading, not its kicker or subtitle`() =
        assertOneHeading("Pick your services") {
            StepHeading(title = "Pick your services", kicker = "Step 2", subtitle = "We'll show what you can stream.")
        }

    @Test
    fun `a detail hero's title is its heading`() =
        assertOneHeading("Dune: Part Two") {
            DetailHero(title = "Dune: Part Two", backdropUrl = null, tagline = "Long live the fighters.", metaText = "2024", onBack = {})
        }
}
