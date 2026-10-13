package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * The shared rows with every optional line filled and long text, at font scales 1.3 and 2.0. The same fixtures run
 * through the no-clipping assertion in `WorstCaseFixturesTest`; these frames show them.
 */
class WorstCaseSamplesScreenshotTest {
    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun ListRow() {
        ListRowWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun ItemGroup() {
        ItemGroupWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun ItemGroupWideTrailing() {
        ItemGroupWideTrailingWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun CheckboxRow() {
        CheckboxRowWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun BingeChoice() {
        BingeChoiceWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun MediaCard() {
        MediaCardWorstCaseSample()
    }
}
