package com.binge.designsystem.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A hero page with little under its hero can still scroll the whole height of the hero, so the bar's hand-over (its surface and its title)
 * can always finish. Without that, a short page stops part way and the bar's title sits half in over the content, with no surface behind it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroDetailShortPageTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun scrollRangeOf(contentHeight: Int): Pair<Int, Int> {
        var scroll: ScrollState? = null
        var heroPx = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val state = rememberScrollState()
                scroll = state
                heroPx = with(LocalDensity.current) { HERO_HEIGHT.toPx() }.toInt()
                HeroDetailScreen(
                    title = "A title",
                    onBack = {},
                    scrollState = state,
                    heroHeight = HERO_HEIGHT,
                    hero = { Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(Color.DarkGray)) },
                ) {
                    Box(Modifier.fillMaxWidth().height(contentHeight.dp)) { Text("Content") }
                }
            }
        }
        rule.waitForIdle()
        return scroll!!.maxValue to heroPx
    }

    @Test
    fun `a page with almost nothing under its hero can scroll the whole hero away`() {
        val (max, heroPx) = scrollRangeOf(contentHeight = 40)

        assertTrue("the page scrolls $max px, the hero is $heroPx px", max >= heroPx)
    }

    @Test
    fun `a page with no content at all can scroll the whole hero away`() {
        val (max, heroPx) = scrollRangeOf(contentHeight = 0)

        assertTrue("the page scrolls $max px, the hero is $heroPx px", max >= heroPx)
    }

    private companion object {
        val HERO_HEIGHT = 300.dp
    }
}
