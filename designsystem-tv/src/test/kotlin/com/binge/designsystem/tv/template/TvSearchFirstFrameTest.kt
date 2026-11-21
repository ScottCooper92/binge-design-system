package com.binge.designsystem.tv.template

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Search draws its two-pane skeleton from its first frame. The ViewModel's state starts as [SearchUiState] with `isTv` off
 * until its first combined emission, so a layout chosen from that state drew the wrong page first and swapped.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSearchFirstFrameTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `the first state shows the skeleton of the page it becomes`() {
        composeTestRule.setContent { BingeTvTheme(reduceMotion = true) { TvSearchContent(uiState = tvSearchLoadingIdleUiState()) } }

        composeTestRule.onAllNodesWithTag(TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_SUGGESTION)).assertCountEquals(1)
    }
}
