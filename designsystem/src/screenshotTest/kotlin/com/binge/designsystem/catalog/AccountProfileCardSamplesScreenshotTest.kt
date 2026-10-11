package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [AccountProfileCard][com.binge.designsystem.component.AccountProfileCard] catalog samples. */
class AccountProfileCardSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Interactive() {
        AccountProfileCardInteractiveSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Static() {
        AccountProfileCardStaticSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Column() {
        AccountProfileCardColumnSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tagged() {
        AccountProfileCardTaggedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ColumnTagged() {
        AccountProfileCardColumnTaggedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithImage() {
        AccountProfileCardWithImageSample()
    }
}
