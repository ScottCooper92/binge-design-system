package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** `opensPartWay` is `skipPartiallyExpanded = false` and `dockable = true` together: a part-way sheet with a dock handle. */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w320dp-h640dp")
class BingeSheetOpensPartWayTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val hasExpand = SemanticsMatcher.keyIsDefined(SemanticsActions.Expand)
    private val hasCollapse = SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse)

    private fun show(opensPartWay: Boolean) {
        rule.setContent {
            TestTheme {
                BingeBottomSheet(onDismissRequest = {}, opensPartWay = opensPartWay) {
                    BingeSheetDockingHeader(header = { Text("Header") }, dockedTopBar = { Text("Docked") })
                    Box(Modifier.height(FLOATING_CONTENT))
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a sheet that opens part way has a handle that can expand it`() {
        show(opensPartWay = true)

        rule.onNodeWithText("Header").assertExists()
        rule.onAllNodes(hasExpand).assertCountEquals(1)
    }

    @Test
    fun `a sheet that does not open part way has no handle that expands or collapses it`() {
        show(opensPartWay = false)

        rule.onAllNodes(hasExpand or hasCollapse).assertCountEquals(0)
    }

    private companion object {
        val FLOATING_CONTENT = 420.dp
    }
}
