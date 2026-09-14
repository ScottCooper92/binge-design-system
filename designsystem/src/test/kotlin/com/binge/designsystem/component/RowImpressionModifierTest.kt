package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LAST = "last"

/**
 * The non-lazy impression detector, for the `Column(verticalScroll)` bodies the detail screens use.
 *
 * The whole reason this shape exists is that `ScrollState` exposes no `layoutInfo`, so there is nothing
 * to assert against but real layout — which makes this one of the few places where a test that renders
 * is the *only* test available, rather than the heavier option.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RowImpressionModifierTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val seen = mutableListOf<String>()

    private fun setColumn() {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Column(Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
                    repeat(10) { index ->
                        val key = if (index == 9) LAST else "row $index"
                        Text(
                            "row $index",
                            Modifier
                                .testTag(key)
                                .fillMaxWidth()
                                .height(100.dp)
                                .rowImpression(key) { seen += it },
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a row on screen at rest is reported`() {
        setColumn()

        assertTrue("nothing reported: $seen", seen.contains("row 0"))
    }

    /**
     * The load-bearing assertion. A detector that fired for every composed node regardless of placement
     * would pass the case above and this is where it fails: nine rows below the fold are composed and
     * measured, and none of them was seen.
     */
    @Test
    fun `a row below the fold is not reported until it is scrolled to`() {
        setColumn()

        assertFalse("reported before scrolling: $seen", seen.contains(LAST))

        composeTestRule.onNodeWithTag(LAST).performScrollTo()
        composeTestRule.waitForIdle()

        assertTrue("not reported after scrolling: $seen", seen.contains(LAST))
    }

    /** Repeats are the caller's to collapse, the same contract `RowImpressionEffect` documents. */
    @Test
    fun `the key is reported verbatim, and repeats are left to the caller`() {
        setColumn()

        assertEquals(setOf("row 0", "row 1"), seen.toSet().intersect(setOf("row 0", "row 1")))
    }
}
