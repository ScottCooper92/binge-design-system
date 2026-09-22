package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeTheme

/** Standard top bar — single-row, non-collapsing title with plain nav/action glyphs when opaque. */
@OptIn(ExperimentalMaterial3Api::class)
class BingeTopBarScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAndBack() {
        ScreenshotTheme {
            BingeTopBar(title = "Popular Movies", onBack = {})
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Scrimmed() {
        ScreenshotTheme {
            TransparentBingeTopBarSample(scrimFraction = 1f)
        }
    }

    /**
     * Just below [scrimmedTitleColor]'s 0.75 switch fraction: the title stays `onSurface`. Paired
     * with [TitleAfterScrimSwitch] to straddle the step (#93) — 0 and 1 alone give the same title
     * colour under the old blend and the new step, by construction, so neither proves the switch
     * actually lands at 0.75.
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleBeforeScrimSwitch() {
        ScreenshotTheme {
            TransparentBingeTopBarSample(scrimFraction = 0.5f)
        }
    }

    /** Just past [scrimmedTitleColor]'s 0.75 switch fraction: the title has stepped to the scrimmed colour. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleAfterScrimSwitch() {
        ScreenshotTheme {
            TransparentBingeTopBarSample(scrimFraction = 0.9f)
        }
    }

    /**
     * A fully scrimmed bar over a fixed-height, distinctly coloured frame with room below its own
     * bottom edge — proving [TopBarScrim]'s tail fades gradually into that room instead of cutting
     * off at the bar's boundary (#94).
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ScrimTailFade() {
        ScreenshotTheme {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.top_bar_scrim_tail_preview_height))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
            ) {
                BingeTopBar(
                    title = "Popular Movies",
                    onBack = {},
                    containerColor = Color.Transparent,
                    scrimFraction = 1f,
                    scrimColor = BingeTheme.colors.scrim,
                    scrimForegroundColor = BingeTheme.colors.onScrim,
                )
            }
        }
    }
}
