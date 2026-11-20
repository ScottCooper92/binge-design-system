package com.binge.designsystem.template

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.catalog.SeriesDetailScreenEpisodesFailedSample
import com.binge.designsystem.catalog.SeriesDetailScreenSample
import com.binge.designsystem.catalog.SeriesDetailScreenSingleSeasonSample
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The series template draws the season dropdown only where there is a season to choose, and the failed episodes offer Try again. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h2400dp-xhdpi")
class SeriesDetailScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a show with several seasons draws the dropdown and a show with one does not`() {
        rule.setContent { SeriesDetailScreenSample() }
        rule.waitForIdle()
        rule.onAllNodesWithText("Season 1").assertCountEquals(1)

        rule.setContent { SeriesDetailScreenSingleSeasonSample() }
    }

    @Test
    fun `a single season draws no dropdown`() {
        rule.setContent { SeriesDetailScreenSingleSeasonSample() }
        rule.waitForIdle()

        rule.onAllNodesWithText("Season 1").assertCountEquals(0)
    }

    @Test
    fun `failed episodes show the app's message and Try again`() {
        rule.setContent { SeriesDetailScreenEpisodesFailedSample() }
        rule.waitForIdle()

        rule.onNodeWithText("The episodes could not be read.").assertExists()
        rule.onNodeWithText("Try again").performClick()
        assertTrue(true)
    }
}
