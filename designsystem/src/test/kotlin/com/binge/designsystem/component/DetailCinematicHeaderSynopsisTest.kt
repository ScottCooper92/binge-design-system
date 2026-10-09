package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SYNOPSIS = "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon."

/** The cinematic header's full-synopsis sheet stays open across a recreation, as [ExpandableOverview]'s expansion does (#377). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailCinematicHeaderSynopsisTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the full synopsis sheet survives a recreation`() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                // The synopsis on its own: the header's backdrop draws a mesh Robolectric cannot build.
                CinematicSynopsis(SYNOPSIS, initiallyOverflowing = true)
            }
        }

        rule.onNodeWithText("Show more").performClick()
        rule.waitForIdle()
        assertEquals("the header's synopsis and the sheet's", 2, synopsisCount())

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals("the sheet came back with the header", 2, synopsisCount())
    }

    private fun synopsisCount() = rule.onAllNodesWithText(SYNOPSIS).fetchSemanticsNodes().size
}
