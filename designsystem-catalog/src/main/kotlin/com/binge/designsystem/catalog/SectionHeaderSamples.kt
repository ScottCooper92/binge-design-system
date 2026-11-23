package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.preview.ScreenshotTheme

/** Section header with a trailing "More" pill — the carousel-heading variant. */
@Composable
fun SectionHeaderWithMoreSample() {
    ScreenshotTheme {
        SectionHeader(title = "Trending Movies", onMoreClick = {})
    }
}

/** Title-only header — no trailing action, used where the section has nothing to expand into. */
@Composable
fun SectionHeaderTitleOnlySample() {
    ScreenshotTheme {
        SectionHeader(title = "Cast")
    }
}

/** A title too long for the row, beside "See all": one line, ellipsized, with the trailing action kept at its own width. */
@Composable
fun SectionHeaderLongTitleSample() {
    ScreenshotTheme {
        SectionHeader(title = "Because you watched The Lord of the Rings: The Return of the King", onMoreClick = {})
    }
}

/** The same in Spanish, whose "Ver todo" is the longer action: the title ellipsizes in what it leaves. */
@Composable
fun SectionHeaderLongTitleSpanishSample() {
    ScreenshotTheme {
        SectionHeader(title = "Porque viste El Señor de los Anillos: El retorno del Rey", onMoreClick = {})
    }
}
