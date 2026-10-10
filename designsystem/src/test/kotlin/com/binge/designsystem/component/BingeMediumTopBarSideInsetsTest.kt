package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val TOLERANCE = 1f

/** A camera cutout down the start side, as a phone held in landscape has. */
private val Cutout = 40.dp

/** The medium bar takes its side insets from `paneSideInsets()`, as the body below it does (#352). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h400dp")
class BingeMediumTopBarSideInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a side cutout moves the back button in by the cutout's width`() {
        var cutout by mutableStateOf(0.dp)
        rule.setContent {
            TestTheme {
                WithWindowInsets({ cutout.toCutoutInsets(this) }) {
                    BingeMediumTopBar(title = "Settings", onBack = {})
                }
            }
        }
        rule.waitForIdle()
        val without = rule.backButtonLeft()

        cutout = Cutout
        rule.waitForIdle()

        assertEquals(Cutout.value, rule.backButtonLeft() - without, TOLERANCE)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.backButtonLeft(): Float =
        onNodeWithContentDescription(RuntimeEnvironment.getApplication().getString(R.string.cd_navigate_back))
            .getBoundsInRoot()
            .left.value

    private fun Dp.toCutoutInsets(density: androidx.compose.ui.unit.Density): WindowInsetsCompat =
        WindowInsetsCompat
            .Builder()
            .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(with(density) { roundToPx() }, 0, 0, 0))
            .build()
}
