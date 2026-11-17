package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the category-tile catalog samples — [BackdropTile] and [LogoTile] and the row
 * that hosts them. The long-title and no-image cases also render at the worst-case font scales.
 */
class CategoryTileSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Backdrop() {
        BackdropTileSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun BackdropLongTitle() {
        BackdropTileLongTitleSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun BackdropNoImage() {
        BackdropTileNoImageSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Logo() {
        LogoTileSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LogoNoLogo() {
        LogoTileNoLogoSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TileRows() {
        CategoryTileRowSample()
    }
}
