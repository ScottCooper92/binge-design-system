package com.binge.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [ListRowAvatar] and [ListRowLogo] are exactly as wide and as tall as a [ListRowPoster], at any font scale, so a
 * person or company row lines up with a title row beside it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
class ListRowLeadingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Row {
                        ListRowPoster(null, null, Modifier.testTag("poster"))
                        ListRowAvatar(null, Modifier.testTag("avatar"))
                        ListRowLogo(null, Modifier.testTag("logo"))
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun assertSameSizeAsPoster(fontScale: Float) {
        show(fontScale)
        val poster = rule.onNodeWithTag("poster").getBoundsInRoot()
        listOf("avatar", "logo").forEach { tag ->
            val bounds = rule.onNodeWithTag(tag).getBoundsInRoot()
            assertEquals("$tag width", poster.right - poster.left, bounds.right - bounds.left)
            assertEquals("$tag height", poster.bottom - poster.top, bounds.bottom - bounds.top)
        }
    }

    @Test fun `they are the poster's size at 1_0`() = assertSameSizeAsPoster(1.0f)

    @Test fun `they are the poster's size at 2_0`() = assertSameSizeAsPoster(2.0f)

    @Test
    fun `the poster height follows the poster's aspect ratio`() {
        show(1.0f)
        val avatar = rule.onNodeWithTag("avatar").getBoundsInRoot()
        assertEquals(72f / CARD_ASPECT_RATIO, (avatar.bottom - avatar.top).value, 0.5f)
    }

    @Test
    fun `a custom width gives a custom height in the same proportion`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { ListRowAvatar(null, Modifier.testTag("avatar"), width = 48.dp) }
        }
        val avatar = rule.onNodeWithTag("avatar").getBoundsInRoot()
        assertEquals(48f, (avatar.right - avatar.left).value, 0.5f)
        assertEquals(48f / CARD_ASPECT_RATIO, (avatar.bottom - avatar.top).value, 0.5f)
    }
}
