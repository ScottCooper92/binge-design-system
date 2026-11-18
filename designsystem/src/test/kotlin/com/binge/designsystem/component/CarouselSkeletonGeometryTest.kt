package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [CarouselSkeleton] reserves what the [MediaCarousel] replacing it fills, with and without a "See all" header, at a phone and a
 * tablet width.
 *
 * Size is checked as well as position: a skeleton shorter than the row it stands in for sits at the same corner and still moves
 * every row below it when the content lands. A header with a "See all" action is the case the header's 48dp floor exists for, so
 * both shapes are held to the one height the skeleton reserves.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CarouselSkeletonGeometryTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val anchor = LayoutAnchors.section(LayoutAnchors.Hub.FIRST_CAROUSEL)

    private fun assertReserved(withSeeAll: Boolean) {
        rule.assertSkeletonReservesGeometry(anchors = listOf(anchor), checkSize = true) { resolved ->
            BingeExpressiveTheme {
                Box(Modifier.fillMaxSize()) {
                    if (resolved) {
                        MediaCarousel(
                            title = "Action",
                            items = List(6) { "Movie ${it + 1}" },
                            itemKey = { it },
                            onMoreClick = if (withSeeAll) ({}) else null,
                            onEndTileClick = null,
                            modifier = Modifier.layoutAnchor(anchor),
                        ) { _, title ->
                            MediaCard(
                                posterUrl = null,
                                title = title,
                                rating = 7.5f,
                                onClick = {},
                                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                            )
                        }
                    } else {
                        CarouselSkeleton(modifier = Modifier.layoutAnchor(anchor))
                    }
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `on a phone the skeleton reserves a carousel with no header action`() = assertReserved(withSeeAll = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `on a phone the skeleton reserves a carousel with a see all header`() = assertReserved(withSeeAll = true)

    @Test
    @Config(qualifiers = "w800dp-h1280dp-xhdpi")
    fun `on a tablet the skeleton reserves a carousel with no header action`() = assertReserved(withSeeAll = false)

    @Test
    @Config(qualifiers = "w800dp-h1280dp-xhdpi")
    fun `on a tablet the skeleton reserves a carousel with a see all header`() = assertReserved(withSeeAll = true)
}
