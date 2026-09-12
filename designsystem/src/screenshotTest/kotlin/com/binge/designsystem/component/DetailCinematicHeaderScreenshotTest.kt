package com.binge.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.preview.ScreenshotTheme

private const val CINEMATIC_PREVIEW_WIDTH_DP = 900

/**
 * Expanded-width cinematic detail header — backdrop + glass top bar + inline poster beside the copy
 * column. Rendered at an expanded (~900dp) canvas because the component is for the side-pane / tablet
 * detail; the phone-only shared multipreviews don't reach that width.
 *
 * The RTL cell is the regression guard for `CinematicSideScrim`'s direction-aware ramp (#2022): the
 * scrim is start-anchored, so an absolute `Brush.horizontalGradient` would put its dense end under the
 * end of the backdrop instead of behind the poster and copy. Nothing here needs artwork for that to
 * bite — `validateDebugScreenshotTest` diffs pixels exactly, so the ramp reverses visibly over the flat
 * placeholder too. #2065.
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
                tagline = "Why do we fall? So we can learn to pick ourselves up.",
                backdropUrl = null,
                posterUrl = null,
                actions = {
                    BingeFilledButton(label = "Add to list", onClick = {})
                },
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
