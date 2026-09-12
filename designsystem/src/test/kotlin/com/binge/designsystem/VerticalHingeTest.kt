package com.binge.designsystem

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [rememberVerticalHinge] on a window with no fold — which is every device this repo can test on.
 *
 * It looks like a test of nothing, and it is not: the composable subscribes to `WindowInfoTracker`,
 * whose backing extension is absent here exactly as it is on a non-foldable phone. What this pins is
 * that the absent backend yields *null*, quietly, rather than throwing or hanging — because null is
 * what routes every ordinary window back to the unchanged default split, and a throw here would take
 * out two live screens on hardware that has no hinge at all.
 *
 * The folded case is [PosedFoldTest]'s, which swaps the tracker for `window-testing`'s publisher and
 * poses a hinge. That override is exactly what this test must not have: what is pinned here is the
 * behaviour of the *real* tracker with no platform extension behind it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w1080dp-h800dp-xhdpi")
class VerticalHingeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a window with no fold reports no hinge`() {
        var hinge: VerticalHinge? = VerticalHinge(start = 1.dp, width = 1.dp)

        composeTestRule.setContent { hinge = rememberVerticalHinge() }
        composeTestRule.waitForIdle()

        assertNull("a non-foldable window reported a hinge", hinge)
    }
}
