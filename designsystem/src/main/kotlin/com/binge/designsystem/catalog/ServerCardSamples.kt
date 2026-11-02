package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.binge.designsystem.component.HubCard
import com.binge.designsystem.component.ServerCard
import com.binge.designsystem.component.ServerCardChip
import com.binge.designsystem.component.ServerCardStat
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

private val health = ServerCardChip("Connected", BingeSentiment.Positive)

@Composable
private fun logo(): Painter = rememberVectorPainter(Icons.Filled.Tv)

private val threeStats =
    listOf(
        ServerCardStat(Icons.Filled.Movie, "128", "Movies"),
        ServerCardStat(Icons.Filled.Tv, "42", "TV"),
        ServerCardStat(Icons.Filled.HourglassEmpty, "7", "Pending"),
    )

/** A server card with every pill: the flags beside the logo and the health pill beside the title, and four totals. */
@Composable
fun ServerCardAllChipsSample() {
    ScreenshotTheme {
        ServerCard(
            title = "Home server",
            logo = logo(),
            subtitle = "Jellyseerr 2.5.2",
            address = "https://requests.example.com",
            chips = listOf(ServerCardChip("Demo", BingeSentiment.Caution), ServerCardChip("Update available", BingeSentiment.Caution)),
            statusChip = health,
            stats = threeStats + ServerCardStat(Icons.Filled.People, "9", "Users"),
        )
    }
}

/** A server card with no pill beside the logo and three totals: the health pill alone. */
@Composable
fun ServerCardPlainSample() {
    ScreenshotTheme {
        ServerCard(
            title = "Home server",
            logo = logo(),
            subtitle = "Jellyseerr 2.5.2",
            address = "https://requests.example.com",
            statusChip = health,
            stats = threeStats,
        )
    }
}

/** A name and an address too long for the card: each holds to one line and ends in an ellipsis. */
@Composable
fun ServerCardLongSample() {
    ScreenshotTheme {
        ServerCard(
            title = "The family media server in the downstairs cupboard",
            logo = logo(),
            subtitle = "Jellyseerr 2.5.2",
            address = "https://requests.a-very-long-hostname.home.example.com:5055/",
            statusChip = health,
            stats = threeStats,
        )
    }
}

/** Totals still loading: a null value shows the placeholder, and the labels hold their place. */
@Composable
fun ServerCardLoadingStatsSample() {
    ScreenshotTheme {
        ServerCard(
            title = "Home server",
            logo = logo(),
            subtitle = "Jellyseerr 2.5.2",
            address = "https://requests.example.com",
            statusChip = health,
            stats = threeStats.map { it.copy(value = null) },
        )
    }
}

/** A card with no subtitle, address, pill or totals: just the logo and the name. */
@Composable
fun ServerCardBareSample() {
    ScreenshotTheme { ServerCard(title = "Home server", logo = logo()) }
}

/** A hub card holding plain content: the surface, the side inset and the section spacing, with the caller's own slot. */
@Composable
fun HubCardSample() {
    ScreenshotTheme {
        HubCard {
            Text("Recently added", style = MaterialTheme.typography.titleMedium)
            Text("Whatever the app wants to show goes in this slot.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
