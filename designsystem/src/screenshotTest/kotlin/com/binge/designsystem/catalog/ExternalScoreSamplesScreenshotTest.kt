package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.LocalePreviews
import com.binge.designsystem.preview.TabletScreenPreview
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the external-score samples: phone, tablet, 1.3x and 2x font, and Spanish. */
class ExternalScoreSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Line() {
        ExternalScoreLineSample()
    }

    @PreviewTest
    @LocalePreviews
    @Composable
    fun LineRottenSpanish() {
        ExternalScoreLineRottenSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Hero() {
        DetailHeroScoresSample()
    }

    @PreviewTest
    @LocalePreviews
    @Composable
    fun HeroSpanish() {
        DetailHeroScoresSpanishSample()
    }

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun CinematicHeader() {
        DetailCinematicHeaderScoresSample()
    }

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun Carousel() {
        HeroCarouselScoresSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun CarouselPhone() {
        HeroCarouselScoresSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun CarouselStatus() {
        HeroCarouselStatusSample()
    }

    @PreviewTest
    @LocalePreviews
    @WorstCaseFontPreviews
    @Composable
    fun HeroLongGenres() {
        DetailHeroLongGenresSample()
    }
}
