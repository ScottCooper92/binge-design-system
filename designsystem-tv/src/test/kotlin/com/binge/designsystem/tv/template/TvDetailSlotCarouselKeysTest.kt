package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * An app's own item keys are text too. The strip's tail and See-all cards carry the reserved `binge:` prefix, so an app whose keys equal the plain
 * strings those cards once used still lays out, instead of crashing on a duplicate lazy key.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailSlotCarouselKeysTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `an app's keys equal to the strip's old plain keys do not collide with its tail and see-all cards`() {
        val peek: List<@Composable BoxScope.() -> Unit> = listOf({ Box(androidx.compose.ui.Modifier) })
        composeTestRule.setContent {
            BingeTvTheme {
                TvDetailSlotCarousel(
                    items = listOf("first", "tv-slot-carousel-see-all", "tv-slot-carousel-tail"),
                    key = { it },
                    aspect = 1f,
                    slotHeight = 120.dp,
                    stripHeight = 80.dp,
                    onClick = {},
                    image = {},
                    caption = { Text(it) },
                    seeAll = TvSlotCarouselSeeAll(label = "See all", peek = peek, onClick = {}),
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("first").assertExists()
    }
}
