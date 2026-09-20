package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenshotTheme

private const val CINEMATIC_PREVIEW_WIDTH_DP = 900

/**
 * Expanded-width cinematic detail header — backdrop + glass top bar + inline poster beside the copy
 * column. Rendered at an expanded (~900dp) canvas because the component is for the side-pane / tablet
 * detail; the phone-only shared multipreviews don't reach that width.
 *
 * The RTL cell is the regression guard for `CinematicSideScrim`'s direction-aware ramp: the
 * scrim is start-anchored, so an absolute `Brush.horizontalGradient` would put its dense end under the
 * end of the backdrop instead of behind the poster and copy. Nothing here needs artwork for that to
 * bite — `validateDebugScreenshotTest` diffs pixels exactly, so the ramp reverses visibly over the flat
 * placeholder too.
 */
class DetailCinematicHeaderScreenshotTest {
    @PreviewTest
    @Preview(name = "exp900-light", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "exp900-dark", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Header() {
        ScreenshotTheme {
            DetailCinematicHeader(
                title = "The Dark Knight",
                genres = listOf("Action", "Crime", "Drama"),
                synopsis = "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon " +
                    "and District Attorney Harvey Dent.",
                stats = listOf(
                    DetailStat(Icons.Filled.Star, "9.0", "Rating"),
                    DetailStat(Icons.Filled.Star, "2008", "Released"),
                    DetailStat(Icons.Filled.Star, "2h 32m", "Runtime"),
                ),
                backdropUrl = null,
                posterUrl = null,
            )
        }
    }

    @PreviewTest
    @Preview(name = "exp900-rtl", widthDp = CINEMATIC_PREVIEW_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun HeaderRtl() {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Header()
        }
    }
}
