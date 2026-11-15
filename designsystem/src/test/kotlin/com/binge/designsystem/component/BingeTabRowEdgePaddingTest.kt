package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [BingeTabRow]'s `edgePadding`: left alone it is the screen's content inset, as before the parameter existed, and
 * zero takes that room away. The first tab starts one `edgePadding` in from the row, so each case reads its left edge.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTabRowEdgePaddingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var inset = 0.dp

    @Composable
    private fun Row(edgePadding: (Dp) -> Dp?) {
        BingeExpressiveTheme {
            inset = resolvedContentInset()
            val padding = edgePadding(inset)
            val tabs = listOf("One", "Two", "Three")
            if (padding == null) {
                BingeTabRow(tabs = tabs, selectedIndex = 0, onSelect = {})
            } else {
                BingeTabRow(tabs = tabs, selectedIndex = 0, onSelect = {}, edgePadding = padding)
            }
        }
    }

    private fun firstTabLeft(): Float =
        rule
            .onAllNodes(hasClickAction())
            .onFirst()
            .getUnclippedBoundsInRoot()
            .left.value

    @Test
    fun theDefaultIsTheScreenInsetAsBefore() {
        rule.setContent { Row { null } }
        assertTrue("the inset must be non-zero for this test to mean anything", inset > 0.dp)
        assertEquals(inset.value, firstTabLeft(), 0.01f)
    }

    @Test
    fun passingTheInsetChangesNothing() {
        rule.setContent { Row { it } }
        assertEquals(inset.value, firstTabLeft(), 0.01f)
    }

    @Test
    fun zeroTakesTheEdgePaddingAway() {
        rule.setContent { Row { 0.dp } }
        assertEquals(0f, firstTabLeft(), 0.01f)
    }
}
