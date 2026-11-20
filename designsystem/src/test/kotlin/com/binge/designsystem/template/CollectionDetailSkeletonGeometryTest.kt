package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.MEDIA_PAGE_OVERVIEW
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The collection template's loading state holds the overview and the first two parts where the loaded page puts them, and at the size it gives
 * them, at a font scale of 1, 1.3 and 2. The parts are checked for size as well as position (the existing check is of position alone), so a
 * skeleton whose rows are the wrong height cannot pass by starting at the right corner. Sizes are checked, so the graphics mode is native.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CollectionDetailSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors =
        listOf(
            LayoutAnchors.Detail.OVERVIEW,
            LayoutAnchors.Collection.FIRST_ITEM,
            LayoutAnchors.Collection.SECOND_ITEM,
        ).map(LayoutAnchors::section)

    // An overview long enough to fill the skeleton's standing lines when collapsed, as the movie page's test uses: a shorter one is shorter than the
    // skeleton by design, since a skeleton cannot know how much prose is coming.
    private val loaded =
        CollectionDetailState.Content(
            CollectionDetail(
                title = "A collection",
                metaText = "3 films",
                overview = MEDIA_PAGE_OVERVIEW + " " + MEDIA_PAGE_OVERVIEW,
                partsHeading = "Films",
                parts = List(
                    PARTS,
                ) {
                    MediaResult(
                        "p$it",
                        MediaResultKind.Poster,
                        null,
                        "Part $it",
                        rating = 7f,
                        meta = "200$it",
                        blurb = MEDIA_PAGE_OVERVIEW,
                    )
                },
            ),
        )

    private fun check() {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = true) { resolved ->
            ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
                CollectionDetailScreen(
                    state = if (resolved) loaded else CollectionDetailState.Loading(parts = PARTS, hasPartsHeading = true),
                    callbacks = CollectionDetailCallbacks(onBack = {}, onRetry = {}, onOpenPart = {}),
                )
            }
        }
    }

    private companion object {
        const val PARTS = 4
    }

    @Test
    fun `the skeleton holds the overview and the first two parts`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds them at a font scale of 1 point 3`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds them at a font scale of 2`() = check()
}
