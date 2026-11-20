package com.binge.designsystem.tv.template

import android.app.Application
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * [TvAvatarGridPage]'s skeleton reserves what its resolved grid fills.
 *
 * The anchor is the first cell, size included. The cell is the circle and the caption block beneath it, so a
 * caption block that is taller or shorter than the card's moves every row after the first; the cell's height is
 * what that drift shows up in, since the rows below sit one cell height plus the row gap further down.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// The caption lines are text, and legacy graphics measures text at one pixel per character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvAvatarGridSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val items = List(12) { TvAvatarGridItem(key = it, name = "Person $it", role = "Role $it", imageUrl = null) }

    @Test
    fun `the skeleton reserves the first cell's position and size`() =
        composeTestRule.assertSkeletonReservesGeometry(
            anchors = listOf(TvLayoutAnchors.GRID_FIRST_CELL),
            checkStartEdge = true,
            checkSize = true,
        ) { resolved ->
            BingeTvTheme(reduceMotion = true) {
                TvAvatarGridPage(
                    heading = "Cast",
                    state = if (resolved) TvAvatarGridState.Ready(items) else TvAvatarGridState.Loading,
                    onItemClick = {},
                )
            }
        }
}
