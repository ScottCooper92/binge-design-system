package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Which side a pushed or popped sheet page comes from, in each layout direction. The transition is hosted on its own, since a
 * sheet's modal window takes its layout direction from the window and not from a local provided around it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetPageTransitionRtlTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var state by mutableStateOf(SheetPageState("root", 1))

    private fun show(direction: LayoutDirection) {
        state = SheetPageState("root", 1)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                BingeExpressiveTheme(dynamicColor = false) {
                    AnimatedSheetPages(
                        state,
                        modifier = Modifier.fillMaxWidth(),
                    ) { shown -> Box(Modifier.fillMaxWidth()) { Text(shown.page) } }
                }
            }
        }
        rule.mainClock.advanceTimeBy(SETTLE_MS)
    }

    private fun go(to: SheetPageState) {
        state = to
        Snapshot.sendApplyNotifications()
        repeat(MIDWAY_FRAMES) { rule.mainClock.advanceTimeByFrame() }
    }

    private fun left(text: String): Dp = rule.onNodeWithText(text).getUnclippedBoundsInRoot().left

    @Test
    fun `a push comes in from the end edge, which is on the right in left-to-right`() {
        show(LayoutDirection.Ltr)
        go(SheetPageState("detail", 2))

        assertTrue("was ${left("detail")} and ${left("root")}", left("detail") > left("root"))
    }

    @Test
    fun `a push comes in from the end edge, which is on the left in right-to-left`() {
        show(LayoutDirection.Rtl)
        go(SheetPageState("detail", 2))

        assertTrue("was ${left("detail")} and ${left("root")}", left("detail") < left("root"))
    }

    @Test
    fun `a pop leaves towards the end edge, mirrored in right-to-left`() {
        show(LayoutDirection.Rtl)
        go(SheetPageState("detail", 2))
        rule.mainClock.advanceTimeBy(SETTLE_MS)

        go(SheetPageState("root", 1))

        assertTrue("was ${left("detail")} and ${left("root")}", left("detail") < left("root"))
    }

    private companion object {
        const val MIDWAY_FRAMES = 4
        const val SETTLE_MS = 2_000L
    }
}
