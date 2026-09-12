package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Prototype coverage for the floating nav: the same window buckets the docked shell is captured at,
 * so the two sets can be compared frame for frame.
 */
class BingeNavFloatingBarSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "float-phone-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "float-phone-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FloatingBarPhone() {
        BingeNavFloatingBarPhoneSample()
    }

    @PreviewTest
    @Preview(name = "float-phone-vib-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "float-phone-vib-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FloatingBarPhoneVibrant() {
        BingeNavFloatingBarPhoneVibrantSample()
    }

    @PreviewTest
    @Preview(name = "float-phone-land-light", widthDp = 740, heightDp = 412, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "float-phone-land-dark", widthDp = 740, heightDp = 412, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FloatingBarPhoneLandscape() {
        BingeNavFloatingBarPhoneLandscapeSample()
    }

    @PreviewTest
    @Preview(name = "float-tablet-portrait-light", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "float-tablet-portrait-dark", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FloatingBarTabletPortrait() {
        BingeNavFloatingBarTabletPortraitSample()
    }

    @PreviewTest
    @Preview(name = "text-phone-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "text-phone-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TextFirstPhone() {
        BingeNavFloatingBarPhoneTextFirstSample()
    }

    @PreviewTest
    @Preview(name = "text-tablet-light", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "text-tablet-dark", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun TextFirstTabletPortrait() {
        BingeNavFloatingBarTabletTextFirstSample()
    }

    @PreviewTest
    @Preview(name = "icon-phone-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "icon-phone-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun IconFirstPhone() {
        BingeNavFloatingBarPhoneIconFirstSample()
    }

    @PreviewTest
    @Preview(name = "icon-phone-land-light", widthDp = 740, heightDp = 412, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "icon-phone-land-dark", widthDp = 740, heightDp = 412, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun IconFirstPhoneLandscape() {
        BingeNavFloatingBarPhoneLandscapeIconFirstSample()
    }

    @PreviewTest
    @Preview(name = "icon-tablet-light", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "icon-tablet-dark", widthDp = 834, heightDp = 1194, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun IconFirstTabletPortrait() {
        BingeNavFloatingBarTabletIconFirstSample()
    }

    @PreviewTest
    @Preview(name = "art-standard-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "art-standard-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ArtworkStandard() {
        BingeNavFloatingBarArtworkStandardSample()
    }

    @PreviewTest
    @Preview(name = "art-vibrant-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "art-vibrant-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ArtworkVibrant() {
        BingeNavFloatingBarArtworkVibrantSample()
    }

    @PreviewTest
    @Preview(name = "art-contrast-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "art-contrast-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ArtworkHighContrast() {
        BingeNavFloatingBarArtworkHighContrastSample()
    }

    @PreviewTest
    @Preview(name = "list-contrast-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "list-contrast-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ListHighContrast() {
        BingeNavFloatingBarHighContrastSample()
    }

    @PreviewTest
    @Preview(name = "bg-standard-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bg-standard-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun EmptyStandard() {
        BingeNavFloatingBarEmptyStandardSample()
    }

    @PreviewTest
    @Preview(name = "bg-outlined-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bg-outlined-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun EmptyOutlined() {
        BingeNavFloatingBarEmptyOutlinedSample()
    }

    @PreviewTest
    @Preview(name = "bg-contrast-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bg-contrast-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun EmptyHighContrast() {
        BingeNavFloatingBarEmptyHighContrastSample()
    }

    @PreviewTest
    @Preview(name = "bg-alwaysdark-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "bg-alwaysdark-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun EmptyAlwaysDark() {
        BingeNavFloatingBarEmptyAlwaysDarkSample()
    }

    @PreviewTest
    @Preview(name = "art-alwaysdark-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "art-alwaysdark-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ArtworkAlwaysDark() {
        BingeNavFloatingBarArtworkAlwaysDarkSample()
    }

    @PreviewTest
    @Preview(name = "scrolled-end-light", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "scrolled-end-dark", widthDp = 412, heightDp = 740, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun ScrolledToEnd() {
        BingeNavFloatingBarScrolledToEndSample()
    }
}
