package com.binge.designsystem.template

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import com.binge.designsystem.catalog.MEDIA_PAGE_OVERVIEW
import com.binge.designsystem.catalog.MEDIA_PAGE_TITLE
import com.binge.designsystem.catalog.MediaPageStats
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createTakeDownAndroidComposeRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The status bar's icons on a title page in the light theme, read from the activity's window. At rest the hero is under the bar, so the icons are light
 * (`isAppearanceLightStatusBars` is false: it names the bar's background, not the icons); once the page has scrolled past the bar's hand-off the
 * theme's own background is behind them, so they follow it. The loading page draws no hero image, so its icons follow the theme at rest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-notnight-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailStatusBarIconsTest {
    @get:Rule
    val rule = createTakeDownAndroidComposeRule<ComponentActivity>()

    private val scroll = ScrollState(0)

    private val loaded = DetailState.Content(MediaDetail(title = MEDIA_PAGE_TITLE, stats = MediaPageStats, overview = MEDIA_PAGE_OVERVIEW))

    private fun show(page: @Composable () -> Unit) {
        rule.setContent { ScreenshotTheme(modifier = Modifier.fillMaxSize()) { page() } }
        rule.waitForIdle()
    }

    /** True where the bar wants dark icons, which is what the window calls a light status bar. */
    private fun darkIcons(): Boolean {
        val window = rule.activity.window
        return WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars
    }

    private fun scrollPastHandOff() {
        rule.runOnIdle { runBlocking { scroll.scrollTo(Int.MAX_VALUE) } }
        rule.waitForIdle()
    }

    private val callbacks = MediaDetailCallbacks(onBack = {})

    @Test
    fun `a movie page keeps its icons light at rest and hands them to the theme once scrolled`() {
        show { MovieDetailScreen(state = loaded, callbacks = callbacks, scrollState = scroll) }
        assertEquals("light icons over the hero at rest", false, darkIcons())

        scrollPastHandOff()
        assertEquals("the theme's icons once past the hand-off", true, darkIcons())
    }

    @Test
    fun `a series page keeps its icons light at rest and hands them to the theme once scrolled`() {
        show {
            SeriesDetailScreen(
                state = loaded,
                series = null,
                callbacks = callbacks,
                seriesCallbacks = SeriesDetailCallbacks(onSelectSeason = {}),
                scrollState = scroll,
            )
        }
        assertEquals("light icons over the hero at rest", false, darkIcons())

        scrollPastHandOff()
        assertEquals("the theme's icons once past the hand-off", true, darkIcons())
    }

    @Test
    fun `a loading movie page follows the theme at rest`() {
        show { MovieDetailScreen(state = DetailState.Loading(), callbacks = callbacks, scrollState = scroll) }
        assertEquals("the theme's icons, with no hero image under the bar", true, darkIcons())
    }

    @Test
    @Config(qualifiers = "w1000dp-h700dp-notnight-xhdpi")
    fun `a movie page's cinematic header at the expanded width keeps its icons light too`() {
        show { MovieDetailScreen(state = loaded, callbacks = callbacks, scrollState = scroll) }
        assertEquals("light icons over the cinematic header at rest", false, darkIcons())

        scrollPastHandOff()
        assertEquals("the theme's icons once past the hand-off", true, darkIcons())
    }
}
