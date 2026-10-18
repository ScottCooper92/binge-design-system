package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.nav.LocalTvTopInset
import com.binge.designsystem.tv.template.TvDetailPage
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val HERO = "hero"
private const val POSTER = "poster"
private const val ACTIONS = "actions"
private const val SECTION = "section"

/** A real top bar's height: the top overscan, an icon button, and the bar's bottom gap. */
private val TopBar = 95.dp

/** `tv_detail_page_hero_height`, as a value, so the test fails when the dimen moves rather than following it. */
private val HeroHeight = 424.dp
private val ActionHeight = 48.dp
private const val TOLERANCE = 0.5f

/** `tv_detail_page_hero_content_top` and `tv_detail_page_hero_content_top_under_bar`, as values. */
private val UsualContentTop = 60.dp
private val UnderBarContentTop = 16.dp
private val SectionGap = 32.dp
private val ScreenHeight = 540.dp

/** The least the first section may show beneath the band at rest under a bar, for it to read as a cue that the page goes on. */
private val MinPeek = 24.dp

/**
 * Under a host's top bar the hero's content starts below the bar, with a tighter gap than usual, since the bar already
 * separates it. The band grows by the bar less what that gap gives back: the poster row keeps its height, the fixed-width
 * poster ends above the action row rather than under it, and the first section still peeks at rest. With no bar the band
 * is as it was.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = Application::class,
    qualifiers = "w960dp-h540dp-television-xhdpi",
    // The hero draws its backdrop wash through MeshSpecification, which Robolectric has no native for.
    shadows = [ShadowMeshSpecification::class],
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvDetailHeroTopBarTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `with no top bar the band is its usual height and the poster ends above the actions`() {
        show(inset = Dp.Hairline)

        assertEquals(HeroHeight.value, heroHeight().value, TOLERANCE)
        assertPosterAboveActions()
    }

    @Test
    fun `under a top bar the band grows by the bar less the gap it gives back, and the poster still ends above the actions`() {
        show(inset = TopBar)

        assertPosterAboveActions()
        assertEquals((HeroHeight + TopBar - (UsualContentTop - UnderBarContentTop)).value, heroHeight().value, TOLERANCE)
        assertEquals((TopBar + UnderBarContentTop).value, bounds(POSTER).top.value, TOLERANCE)
    }

    @Test
    fun `under a top bar the first section still peeks beneath the band at rest`() {
        rule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalTvTopInset provides TopBar) {
                    TvDetailPage(respectTopInset = true) {
                        hero {
                            TvDetailHero(item = TvDetailHeroItem(title = "Dune: Part Two"), artwork = {}, poster = {}) {
                                Box(Modifier.size(ActionHeight).tvClickable(onFocusChanged = {}, onClick = {}))
                            }
                        }
                        section("first") { Box(Modifier.fillMaxWidth().height(ScreenHeight).testTag(SECTION)) }
                    }
                }
            }
        }
        rule.waitForIdle()

        val sectionTop = bounds(SECTION).top
        assertEquals((HeroHeight + TopBar - (UsualContentTop - UnderBarContentTop) + SectionGap).value, sectionTop.value, TOLERANCE)
        assertTrue("the first section starts at $sectionTop, leaving under $MinPeek of the screen", ScreenHeight - sectionTop >= MinPeek)
    }

    private fun show(inset: Dp) {
        rule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalTvTopInset provides inset) {
                    TvDetailHero(
                        item = TvDetailHeroItem(title = "Dune: Part Two", overline = "Movie"),
                        artwork = {},
                        poster = { Box(Modifier.fillMaxSize().testTag(POSTER)) },
                        modifier = Modifier.testTag(HERO),
                    ) {
                        Box(Modifier.size(ActionHeight).testTag(ACTIONS).tvClickable(onFocusChanged = {}, onClick = {}))
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun bounds(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun heroHeight(): Dp = bounds(HERO).let { it.bottom - it.top }

    private fun assertPosterAboveActions() {
        val posterBottom = bounds(POSTER).bottom
        val actionsTop = bounds(ACTIONS).top
        assertTrue("the poster ends at $posterBottom, below the actions' top at $actionsTop", posterBottom <= actionsTop)
    }
}
