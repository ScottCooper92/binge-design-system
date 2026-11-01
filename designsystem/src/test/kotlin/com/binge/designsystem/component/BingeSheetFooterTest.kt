package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FOOTER = "Pinned primary"

/**
 * A page's primary in the side sheet: pinned to the foot of the sheet whatever the page above it holds, with the edge
 * between them drawn only while content is under it; in the bottom form, where the page put it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h900dp")
class BingeSheetFooterTest {
    @get:Rule
    val rule = createComposeRule()

    @Composable
    private fun Page(rows: Int) {
        Column(modifier = Modifier.bingeSheetScroll()) {
            repeat(rows) { Text("Row $it", modifier = Modifier.fillMaxWidth().padding(8.dp)) }
        }
        BingeSheetFooter { Text(FOOTER) }
    }

    private fun showPanel(rows: Int) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeTaskSheetPanel(pages = listOf(ROOT_PAGE), onPush = {}, onBack = {}) { Page(rows) }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a short page's footer is at the foot of the sheet, not under its rows`() {
        showPanel(rows = 2)

        rule.onNodeWithText(FOOTER).assertIsDisplayed()
        val footer = rule.onNodeWithText(FOOTER).getUnclippedBoundsInRoot()
        val lastRow = rule.onNodeWithText("Row 1").getUnclippedBoundsInRoot()
        assertTrue("footer ${footer.top} should be well below the last row ${lastRow.bottom}", footer.top > lastRow.bottom + 100.dp)
    }

    @Test
    fun `the footer is in the same place on a short page and a long one`() {
        var rows by mutableIntStateOf(2)
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeTaskSheetPanel(pages = listOf(ROOT_PAGE), onPush = {}, onBack = {}) { Page(rows) }
            }
        }
        rule.waitForIdle()
        val short = rule.onNodeWithText(FOOTER).getUnclippedBoundsInRoot()

        rows = 60
        rule.waitForIdle()
        val long = rule.onNodeWithText(FOOTER).getUnclippedBoundsInRoot()

        assertEquals(short.top, long.top)
        assertEquals(short.bottom, long.bottom)
    }

    @Test
    fun `a footer the page stops showing is taken away`() {
        var shown by mutableStateOf(true)
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeTaskSheetPanel(pages = listOf(ROOT_PAGE), onPush = {}, onBack = {}) {
                    Text("Body")
                    if (shown) BingeSheetFooter { Text(FOOTER) }
                }
            }
        }
        rule.onNodeWithText(FOOTER).assertIsDisplayed()

        shown = false
        rule.waitForIdle()

        rule.onNodeWithText(FOOTER).assertDoesNotExist()
    }

    @Test
    fun `no edge shows while the page fits`() {
        showPanel(rows = 2)

        rule.onNodeWithTag(SHEET_FOOTER_EDGE_TAG).assertDoesNotExist()
    }

    @Test
    fun `the edge shows while content is under the footer`() {
        showPanel(rows = 60)

        rule.onNodeWithText(FOOTER).assertIsDisplayed()
        rule.onNodeWithTag(SHEET_FOOTER_EDGE_TAG).assertExists()
    }

    @Test
    fun `without a side sheet the footer is drawn where the page put it`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Column {
                    Text("Above")
                    BingeSheetFooter { Text(FOOTER) }
                    Text("Below", modifier = Modifier.height(10.dp))
                }
            }
        }

        val above = rule.onNodeWithText("Above").getUnclippedBoundsInRoot()
        val footer = rule.onNodeWithText(FOOTER).getUnclippedBoundsInRoot()
        val below = rule.onNodeWithText("Below").getUnclippedBoundsInRoot()
        assertTrue(above.bottom <= footer.top && footer.bottom <= below.top)
    }
}
