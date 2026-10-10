package com.binge.designsystem.component

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val HERO_TITLE_MIN_SIZE = 28.sp
private val CINEMATIC_TITLE_MIN_SIZE = 34.sp

/**
 * The title [DetailHero] and [DetailCinematicHeader] draw, for a `titleContent` slot that falls back to text. Inside the
 * slot the hero already provides the style as `LocalTextStyle` and its colour as `LocalContentColor`, so a plain
 * `Text(title)` matches; pass [titleAutoSize] or [cinematicTitleAutoSize] too for the same one-line-first shrink.
 */
object DetailHeroDefaults {
    /** [DetailHero]'s title style. */
    @Composable
    fun titleStyle(): TextStyle = MaterialTheme.typography.displaySmall

    /** [DetailCinematicHeader]'s title style. */
    @Composable
    fun cinematicTitleStyle(): TextStyle = MaterialTheme.typography.displayMedium

    /** How [DetailHero]'s title shrinks to fit one line before it wraps. */
    @Composable
    fun titleAutoSize(): TextAutoSize =
        OneLineOrWrapAutoSize(max = titleStyle().fontSize, min = HERO_TITLE_MIN_SIZE, step = HeroTitleSizeStep)

    /** How [DetailCinematicHeader]'s title shrinks to fit one line before it wraps. */
    @Composable
    fun cinematicTitleAutoSize(): TextAutoSize =
        OneLineOrWrapAutoSize(max = cinematicTitleStyle().fontSize, min = CINEMATIC_TITLE_MIN_SIZE, step = HeroTitleSizeStep)
}
