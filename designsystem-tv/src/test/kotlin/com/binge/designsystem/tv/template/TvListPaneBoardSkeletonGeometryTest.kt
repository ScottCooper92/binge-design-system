package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.runtime.Composable
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * [TvListPaneBoardSkeleton] puts its first row where [TvListPaneBoard] puts the board's, at the same size. Everything
 * the skeleton must reserve sits above that row — the title band, the column's bleed and the first group's title — so
 * a shortfall in any of them moves it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// The group title and the rows are text, and legacy graphics measures text at one pixel per character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvListPaneBoardSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the skeleton's first row is the board's first row, in its place`() =
        composeTestRule.assertSkeletonReservesGeometry(
            listOf(TvLayoutAnchors.LIST_PANE_FIRST_ROW),
            checkStartEdge = true,
            checkSize = true,
        ) { resolved ->
            BingeTvTheme { if (resolved) Board() else TvListPaneBoardSkeleton(title = TITLE) }
        }
}

private const val TITLE = "Settings"

@Composable
private fun Board() {
    TvListPaneBoard(
        title = TITLE,
        groups =
            listOf(
                TvPaneGroup(
                    title = "Playback",
                    rows =
                        listOf(
                            TvPaneRow(key = "quality", label = "Default quality", body = "The quality a new request asks for."),
                            TvPaneRow(key = "subtitles", label = "Subtitles", body = "Whether titles start with subtitles on."),
                        ),
                ),
            ),
        focusedKey = "quality",
        onFocusRow = {},
    )
}
