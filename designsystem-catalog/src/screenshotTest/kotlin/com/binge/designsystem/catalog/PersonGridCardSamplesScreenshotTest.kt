package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the person grid card: the cell, its long-text, no-photo and no-role variants, and its skeleton. */
class PersonGridCardSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Default() {
        PersonGridCardSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongText() {
        PersonGridCardLongTextSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun NoPhoto() {
        PersonGridCardNoPhotoSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun NoRole() {
        PersonGridCardNoRoleSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Skeleton() {
        PersonGridCardSkeletonSample()
    }
}
