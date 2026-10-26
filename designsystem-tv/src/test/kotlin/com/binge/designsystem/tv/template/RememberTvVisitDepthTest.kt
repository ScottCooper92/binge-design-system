package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.binge.designsystem.testing.createKeyboardComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The report every TV page shares: once, as the page leaves, with the deepest index reached. The immersive hub and grid
 * use it, and so do Binge's pages built from neither, so the contract is pinned here rather than per page.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class RememberTvVisitDepthTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val reported = mutableListOf<Pair<String, Int?>>()
    private var shown by mutableStateOf(true)
    private var reloading by mutableStateOf(false)
    private var reporter by mutableStateOf("first")
    private lateinit var depth: TvVisitDepth

    private fun setPage() {
        composeTestRule.setContent {
            if (shown) {
                val name = reporter
                depth = rememberTvVisitDepth { reported += name to it }
                // What a page draws below the tracker: a reload swaps it out without ending the visit.
                if (!reloading) BasicText("Results")
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun reach(index: Int) {
        composeTestRule.runOnIdle { depth.reach(index) }
    }

    private fun leave() {
        shown = false
        composeTestRule.waitForIdle()
    }

    @Test
    fun `nothing is reported while the page is shown`() {
        setPage()
        reach(3)

        assertTrue("reported before leaving: $reported", reported.isEmpty())
    }

    @Test
    fun `leaving reports the deepest index reached, exactly once`() {
        setPage()
        reach(3)
        reach(1)

        leave()

        assertEquals(listOf("first" to 3), reported)
    }

    @Test
    fun `a page focus never entered reports no depth`() {
        setPage()

        leave()

        assertEquals(listOf<Pair<String, Int?>>("first" to null), reported)
    }

    /** A reload below the tracker is the same visit, and the depth it carried survives it. */
    @Test
    fun `a reload below the tracker neither reports nor resets the depth`() {
        setPage()
        reach(4)

        reloading = true
        composeTestRule.waitForIdle()
        reloading = false
        composeTestRule.waitForIdle()
        reach(1)
        assertTrue("a reload reported a visit: $reported", reported.isEmpty())

        leave()

        assertEquals(listOf("first" to 4), reported)
    }

    /** The report goes to the callback of the last composition, not the first. */
    @Test
    fun `leaving reports to the latest callback`() {
        setPage()
        reach(2)
        reporter = "second"
        composeTestRule.waitForIdle()

        leave()

        assertEquals(listOf("second" to 2), reported)
    }
}
