package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.SOURCE_LOCALE

/** Screenshot coverage for the [DetailHero][com.binge.designsystem.component.DetailHero] catalog sample. */
class DetailHeroSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Hero() {
        DetailHeroSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MetaContent() {
        DetailHeroMetaContentSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleContent() {
        DetailHeroTitleContentSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleFallback() {
        DetailHeroTitleFallbackSample()
    }

    /** Font scale 2.0 on a landscape phone, the shortest band: the copy clears the back button and the band grows (#533). */
    @PreviewTest
    @Preview(
        name = "land-font20",
        device = "spec:width=411dp,height=891dp,orientation=landscape",
        fontScale = 2.0f,
        uiMode = UI_MODE_NIGHT_YES,
        locale = SOURCE_LOCALE,
    )
    @Composable
    fun FullCopy() {
        DetailHeroFullCopySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun RichBackdrop() {
        DetailHeroRichBackdropSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun EyebrowWithActions() {
        DetailHeroEyebrowWithActionsSample()
    }

    /** A 840dp window, the two-pane breakpoint, with the hero in a 360dp pane: side padding stays at the compact band. */
    @PreviewTest
    @Preview(name = "pane360", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun AtPaneWidth() {
        DetailHeroAtPaneWidthSample()
    }
}
