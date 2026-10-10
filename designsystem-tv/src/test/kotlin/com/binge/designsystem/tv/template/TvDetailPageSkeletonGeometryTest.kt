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

private const val SECTION = "seasons"
private val HeroAnchor = TvLayoutAnchors.entry(TvLayoutAnchors.HERO_KEY)

/** [TvDetailPageSkeleton] reserves the hero band and puts the first row where [TvDetailPage] does (#369). */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = Application::class,
    qualifiers = "w960dp-h540dp-television-xhdpi",
    // The loaded hero draws its backdrop wash through MeshSpecification, which Robolectric has no native for.
    shadows = [ShadowMeshSpecification::class],
)
class TvDetailPageSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the skeleton's hero band is the loaded band's size, in its place`() =
        composeTestRule.assertSkeletonReservesGeometry(listOf(HeroAnchor), checkStartEdge = true, checkSize = true) { resolved ->
            BingeTvTheme { if (resolved) Page() else TvDetailPageSkeleton(firstSectionKey = SECTION) }
        }

    @Test
    fun `the skeleton's first row starts where the page's first section does`() =
        composeTestRule.assertSkeletonReservesGeometry(listOf(HeroAnchor, TvLayoutAnchors.entry(SECTION))) { resolved ->
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
                cellWidth = 120.dp,
                heading = "Seasons",
                onCellFocused = onFocused,
            ) { _, _, onFocusChanged, cellModifier ->
                Box(cellModifier.aspectRatio(2f / 3f).tvClickable(onFocusChanged = onFocusChanged, onClick = {}))
            }
        }
    }
}
