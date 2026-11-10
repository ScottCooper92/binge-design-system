package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Where a Material 3 modal sheet puts its drag handle, below its own top, with a 150px top inset passed as `contentWindowInsets`.
 * Expanded, the handle sits one inset down, clear of the status bar. Part-way, it sits at the top: the inset follows the sheet's
 * offset and is not a constant band, so a part-way sheet has no inset-height gap above its handle. `sheetWashOverdraw` relies on
 * this (#1097): a part-way sheet's wash reaches up over the handle only. If a Material update makes the inset constant, this test
 * fails and the wash's top overdraw has to add the inset at every position.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
class SheetTopInsetPositionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val inset = 150

    /** The handle's top and the content's top, each measured down from the sheet's top, at [value]. */
    private fun measure(value: SheetValue): Pair<Float, Float> {
        var offset = Float.NaN
        var handleTop = Float.NaN
        var contentTop = Float.NaN
        rule.setContent {
            TestTheme {
                val state =
                    rememberBottomSheetState(
                        initialValue = SheetValue.Hidden,
                        enabledValues = setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded),
                    )
                val scope = rememberCoroutineScope()
                ModalBottomSheet(
                    onDismissRequest = {},
                    sheetState = state,
                    contentWindowInsets = { WindowInsets(top = inset) },
                    dragHandle = {
                        Box(Modifier.onGloballyPositioned { handleTop = it.positionInWindow().y }) { BottomSheetDefaults.DragHandle() }
                    },
                ) {
                    Box(
                        Modifier.height(2000.dp).onGloballyPositioned {
                            contentTop = it.positionInWindow().y
                            offset = state.requireOffset()
                        },
                    )
                }
                LaunchedEffect(value) {
                    scope.launch { if (value == SheetValue.Expanded) state.expand() else state.partialExpand() }
                }
            }
        }
        rule.mainClock.advanceTimeBy(3_000)
        rule.waitForIdle()
        return (handleTop - offset) to (contentTop - offset)
    }

    @Test
    fun `expanded, the handle starts one top inset below the sheet's top`() {
        val (handleFromTop, _) = measure(SheetValue.Expanded)
        assertEquals(inset.toFloat(), handleFromTop, 1f)
    }

    @Test
    fun `part-way, the handle starts at the sheet's top, so the top inset leaves no band above it`() {
        val (handleFromTop, _) = measure(SheetValue.PartiallyExpanded)
        assertEquals(0f, handleFromTop, 1f)
    }
}
