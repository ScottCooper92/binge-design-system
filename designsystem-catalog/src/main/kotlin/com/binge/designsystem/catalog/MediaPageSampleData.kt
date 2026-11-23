package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Shield
import com.binge.designsystem.component.DetailStat

/** The film the media detail page samples show: a title over two lines, a tagline of one and three genres, like the skeleton's placeholder. */
internal const val MEDIA_PAGE_TITLE = "Spider-Man: Brand New Day"
internal const val MEDIA_PAGE_TAGLINE = "A new day, and a very old problem"
internal val MEDIA_PAGE_GENRES = listOf("Action", "Adventure", "Science Fiction")
internal const val MEDIA_PAGE_OVERVIEW =
    "A hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers. " +
        "He is pulled out of a life he took for granted and into a fight that was always his, and he has to decide what he believes."

internal val MediaPageStats = listOf(
    DetailStat(Icons.Filled.Star, "8.4", "24.0k votes"),
    DetailStat(Icons.Filled.CalendarMonth, "2026", "Released"),
    DetailStat(Icons.Filled.Schedule, "2h 12m", "Runtime"),
    DetailStat(Icons.Outlined.Shield, "PG-13", "Rated"),
)
