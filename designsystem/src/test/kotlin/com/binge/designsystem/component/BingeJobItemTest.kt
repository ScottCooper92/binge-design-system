package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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

private val COPY = JobCopy(run = "Run now", stop = "Stop", running = "Running", succeeded = "Done", failed = "Failed")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeJobItemTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var runs = 0
    private var stops = 0
    private var opens = 0

    private fun show(
        running: Boolean = false,
        canStop: Boolean = true,
        runningDetail: String? = null,
        busy: Boolean = false,
        outcome: JobOutcome? = null,
        openable: Boolean = false,
        selected: Boolean = false,
    ) {
        rule.setContent {
            TestTheme {
                ItemGroup(
                    title = "Jobs",
                    rows =
                        listOf(
                            bingeJobItem(
                                icon = Icons.Filled.Sync,
                                label = "Library scan",
                                idleDetail = "Next run in 2 hours",
                                copy = COPY,
                                running = running,
                                onRun = { runs++ },
                                onStop = if (canStop) ({ stops++ }) else null,
                                runningDetail = runningDetail,
                                busy = busy,
                                outcome = outcome,
                                onOpen = if (openable) ({ opens++ }) else null,
                                selected = selected,
                            ),
                        ),
                )
            }
        }
    }

    @Test
    fun anIdleJobSaysWhenItRunsNextAndRunsOnATap() {
        show()

        rule.onNodeWithText("Next run in 2 hours").assertIsDisplayed()
        rule.onNodeWithContentDescription("Run now").performClick()

        assertEquals(1, runs)
    }

    @Test
    fun aRunningJobSaysSoAndOffersStop() {
        show(running = true)

        rule.onNodeWithText("Running").assertIsDisplayed()
        rule.onNodeWithContentDescription("Stop").performClick()

        assertEquals(1, stops)
        assertEquals(0, runs)
    }

    @Test
    fun aRunningJobThatReportsProgressSaysHowFarItHasGot() {
        show(running = true, runningDetail = "Running 42%")

        rule.onNodeWithText("Running 42%").assertIsDisplayed()
    }

    @Test
    fun aJobThatCannotBeStoppedKeepsItsRunButtonWhileItRuns() {
        show(running = true, canStop = false)

        rule.onNodeWithContentDescription("Stop").assertDoesNotExist()
        rule.onNodeWithContentDescription("Run now").assertExists()
    }

    @Test
    fun aSucceededRunShowsACheckInPlaceOfTheButtons() {
        show(outcome = JobOutcome.Succeeded)

        rule.onNodeWithText("Done").assertIsDisplayed()
        rule.onNodeWithContentDescription("Done").assertIsDisplayed()
        rule.onNodeWithContentDescription("Run now").assertDoesNotExist()
    }

    @Test
    fun aFailedRunSaysSoInTheDetailAndOnTheGlyph() {
        show(outcome = JobOutcome.Failed)

        rule.onNodeWithText("Failed").assertIsDisplayed()
        rule.onNodeWithContentDescription("Failed").assertIsDisplayed()
    }

    @Test
    fun aJobWithAPageOpensOnATapAndAPlainOneDoesNot() {
        show(openable = true)
        rule.onNodeWithText("Library scan").performClick()
        assertEquals(1, opens)
    }

    @Test
    fun aJobWithNoPageTakesNoTapOnItsRow() {
        show(openable = false)
        rule.onNodeWithText("Library scan").performClick()
        assertEquals(0, opens)
    }

    @Test
    fun theJobOpenBesideTheListIsMarkedSelected() {
        show(openable = true, selected = true)

        rule.onNodeWithText("Library scan").assertIsSelected()
    }
}
