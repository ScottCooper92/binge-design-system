package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.FilterFixture
import com.binge.designsystem.tv.catalog.SampleFilterPane
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.binge.designsystem.tv.R as TvR

/**
 * The pane before the page has a state reserves what the resolved pane fills.
 *
 * The anchor is the pane's first category row, which sits under the header and so moves if the header's plate is the wrong height.
 * It is one row, so it is held to its size as well as its place.
 *
 * `NATIVE` graphics because the category row holds two lines of text, which legacy mode measures at one pixel per character.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvFilterSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `the pane lands where the resolved pane's does`() {
        composeTestRule.assertSkeletonReservesGeometry(
            anchors = listOf(TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CATEGORY)),
            checkSize = true,
        ) { resolved ->
            BingeTvTheme(reduceMotion = true) {
                if (resolved) {
                    SampleFilterPane(state = FilterFixture(), onState = {}, entry = remember { FocusRequester() }, onOpenServices = {})
                } else {
                    Box(Modifier.width(dimensionResource(TvR.dimen.tv_filter_pane_width)).fillMaxHeight()) { TvFilterPaneSkeleton() }
                }
            }
        }
    }
}
