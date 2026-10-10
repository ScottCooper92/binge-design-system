package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TRAILING = "trailing"
private const val TOLERANCE = 1f
private val Gutter = 16.dp
private val OverlayEnd = 24.dp

/** A header and a carousel clear an end inset a shell or hub publishes, as they clear the start one. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SectionHeaderOverlayEndTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the header's trailing content clears the published end inset`() {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(end = OverlayEnd)) {
                    SectionHeader(
                        title = "Trending",
                        horizontalPadding = Gutter,
                        trailingContent = { Box(Modifier.size(Gutter).testTag(TRAILING)) },
                    )
                }
            }
        }
        rule.waitForIdle()

        val width = rule
            .onRoot()
            .getBoundsInRoot()
            .right.value
        assertEquals(
            width - Gutter.value - OverlayEnd.value,
            rule
                .onNodeWithTag(TRAILING)
                .getBoundsInRoot()
                .right.value,
            TOLERANCE,
        )
    }

    @Test
    fun `with no published end inset the header keeps its own gutter`() {
        rule.setContent {
            TestTheme {
                SectionHeader(
                    title = "Trending",
                    horizontalPadding = Gutter,
                    trailingContent = { Box(Modifier.size(Gutter).testTag(TRAILING)) },
                )
            }
        }
        rule.waitForIdle()

        val width = rule
            .onRoot()
            .getBoundsInRoot()
            .right.value
        assertEquals(
            width - Gutter.value,
            rule
                .onNodeWithTag(TRAILING)
                .getBoundsInRoot()
                .right.value,
            TOLERANCE,
        )
    }
}
