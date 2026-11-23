package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the containers / chrome catalog samples. Each renders the shared
 * `…Sample()`, the component's one public fixture. See the convention KDoc on [MediaCardRatedSample].
 */
class BingeActionFooterSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Footer() {
        BingeActionFooterSample()
    }

    /** The band's absence is the thing under test, so it needs its own frame beside [Footer]. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FooterOnPage() {
        BingeActionFooterOnPageSample()
    }

    /** The `leadingIcon` variant. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FooterWithIcon() {
        BingeActionFooterWithIconSample()
    }

    /** The `loading` variant: spinner in place of the label. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FooterLoading() {
        BingeActionFooterLoadingSample()
    }

    /** The `destructive` variant: the button in the error tone. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FooterDestructive() {
        BingeActionFooterDestructiveSample()
    }

    /** The `failed` variant: the error tone with the short reason as the label, at the worst-case font scales too. */
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun FooterFailed() {
        BingeActionFooterFailedSample()
    }

    /** The same failure in Spanish, at 1x and 2x: the longer reason wraps at 2x and the button grows. */
    @PreviewTest
    @Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es")
    @Preview(name = "es-font20", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f, locale = "es", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FooterFailedSpanish() {
        BingeActionFooterFailedSpanishSample()
    }
}
