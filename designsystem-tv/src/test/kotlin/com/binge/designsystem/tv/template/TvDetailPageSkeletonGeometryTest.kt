package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.component.TvCardRow
import com.binge.designsystem.tv.component.TvDetailHero
import com.binge.designsystem.tv.component.TvDetailHeroItem
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SECTION = "seasons"

/** The skeleton's card: `tv_skeleton_card_width` wide at 4:3, so the loaded row's cards match it and only the heading can differ. */
private val CardWidth = 140.dp
private const val CARD_RATIO = 4f / 3f
private val HeroAnchor = TvLayoutAnchors.entry(TvLayoutAnchors.HERO_KEY)

/**
 * [TvDetailPageSkeleton] reserves the hero band and puts the first row where [TvDetailPage] does (#369). The row is held
 * to its loaded height as well as its top edge: a heading stand-in shorter than the loaded `titleLarge` line leaves the
 * first row's cards higher than they settle (#575).
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = Application::class,
    // Tall enough that the page's lower rows are not squeezed: the skeleton's row measures to the space it is given.
    qualifiers = "w960dp-h1200dp-television-xhdpi",
    // The loaded hero draws its backdrop wash through MeshSpecification, which Robolectric has no native for.
    shadows = [ShadowMeshSpecification::class],
)
// The row's heading is a TvSectionTitle, and legacy graphics measures text at one pixel per character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvDetailPageSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the skeleton's hero band is the loaded band's size, in its place`() =
        composeTestRule.assertSkeletonReservesGeometry(listOf(HeroAnchor), checkStartEdge = true, checkSize = true) { resolved ->
            BingeTvTheme { if (resolved) Page() else TvDetailPageSkeleton(firstSectionKey = SECTION) }
        }

    @Test
    fun `the skeleton's first row is where the page's first section is, and as tall`() =
        composeTestRule.assertSkeletonReservesGeometry(
            listOf(HeroAnchor, TvLayoutAnchors.entry(SECTION)),
            checkSize = true,
        ) { resolved ->
            BingeTvTheme { if (resolved) Page() else TvDetailPageSkeleton(firstSectionKey = SECTION) }
        }
}

@Composable
private fun Page() {
    TvDetailPage {
        hero {
            TvDetailHero(item = TvDetailHeroItem(title = "Dune: Part Two"), artwork = {}, poster = {}) {
                Box(Modifier.size(48.dp).tvClickable(onFocusChanged = {}, onClick = {}))
            }
        }
        section(SECTION) { onFocused ->
            TvCardRow(
                items = List(4) {
                    it
                },
                key = { it },
                cellWidth = CardWidth,
                heading = "Seasons",
                onCellFocused = onFocused,
            ) { _, _, onFocusChanged, cellModifier ->
                Box(cellModifier.aspectRatio(CARD_RATIO).tvClickable(onFocusChanged = onFocusChanged, onClick = {}))
            }
        }
    }
}
