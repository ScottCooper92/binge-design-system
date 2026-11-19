package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A choice list swaps between failed and ready on the sheet's state switch (#1272), in both of the lists a sheet holds. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeChoiceListStateSwitchTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val failed = BingeChoiceList.Failed(message = "Could not load", retryLabel = "Retry", onRetry = {})
    private val ready = BingeChoiceList.Ready(listOf(BingeChoice(1, "Alpha"), BingeChoice(2, "Beta")))
    private var list: BingeChoiceList<Int> by mutableStateOf(failed)

    private fun show(reduceMotion: Boolean, multi: Boolean) {
        list = failed
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                if (multi) {
                    MultiChoiceList(
                        choices = list,
                        chosen = setOf(1),
                        leading = setOf(1),
                        filterPlaceholder = null,
                        onCheckedChange = { _, _ -> },
                    )
                } else {
                    SingleChoiceList(choices = list, selected = 1) {}
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
    }

    private fun retryArrives() {
        list = ready
        Snapshot.sendApplyNotifications()
    }

    private fun assertCrossFadesThenSettles() {
        retryArrives()
        repeat(MIDWAY_FRAMES) { rule.mainClock.advanceTimeByFrame() }
        rule.onAllNodesWithText("Could not load").assertCountEquals(1)
        rule.onAllNodesWithText("Alpha").assertCountEquals(1)
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onAllNodesWithText("Could not load").assertCountEquals(0)
        rule.onAllNodesWithText("Alpha").assertCountEquals(1)
    }

    private fun assertSwapsAtOnce() {
        retryArrives()
        rule.mainClock.advanceTimeByFrame()
        rule.onAllNodesWithText("Could not load").assertCountEquals(0)
        rule.onAllNodesWithText("Alpha").assertCountEquals(1)
    }

    @Test
    fun `a single-choice list cross-fades from failed to ready`() {
        show(reduceMotion = false, multi = false)
        assertCrossFadesThenSettles()
    }

    @Test
    fun `a multi-choice list cross-fades from failed to ready`() {
        show(reduceMotion = false, multi = true)
        assertCrossFadesThenSettles()
    }

    @Test
    fun `under reduced motion a single-choice list swaps at once`() {
        show(reduceMotion = true, multi = false)
        assertSwapsAtOnce()
    }

    @Test
    fun `under reduced motion a multi-choice list swaps at once`() {
        show(reduceMotion = true, multi = true)
        assertSwapsAtOnce()
    }

    private companion object {
        const val MIDWAY_FRAMES = 4
    }
}
