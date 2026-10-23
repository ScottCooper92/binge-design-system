package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** The components the TV style sets, in `TvStyle.Monochrome`. Each pairs with the accent frame of the sample it wraps. */
class TvMonochromeStyleSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheet() = Frame { TvMonochromeSideSheetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetDestructive() = Frame { TvMonochromeSideSheetDestructiveSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetConfirm() = Frame { TvMonochromeSideSheetConfirmSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun infoCard() = Frame { TvMonochromeInfoCardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetNestedPage() = Frame { TvMonochromeSideSheetNestedPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowIcon() = Frame { TvMonochromeRowIconSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowIconSideSheet() = Frame { TvMonochromeRowIconSideSheetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switch() = Frame { TvMonochromeSwitchSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switchOff() = Frame { TvMonochromeSwitchOffSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switchDestructive() = Frame { TvMonochromeSwitchDestructiveSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun button() = Frame { TvMonochromeButtonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun confirmDialog() = Frame { TvMonochromeConfirmDialogSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun choiceRow() = Frame { TvMonochromeChoiceRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun optionGroup() = Frame { TvMonochromeOptionGroupSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun checkboxRow() = Frame { TvMonochromeCheckboxRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun focusIndicator() = Frame { TvMonochromeFocusIndicatorSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabRowFocused() = Frame { TvMonochromeTabRowFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun textField() = Frame { TvMonochromeTextFieldSample() }
}

@Composable
private fun Frame(content: @Composable () -> Unit) {
    TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { content() }
}
