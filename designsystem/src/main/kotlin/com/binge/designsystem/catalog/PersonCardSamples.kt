@file:CatalogGroup("Person card")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.component.MediaCarousel
import com.binge.designsystem.component.PersonCard
import com.binge.designsystem.component.PersonCardSkeleton
import com.binge.designsystem.component.PersonCardStyle
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage

private const val PHOTO_URL = "sample://photo"
private const val PHOTO_SIZE_PX = 352

private val cast = listOf(
    Triple("Amara Okafor", "Lead", true),
    Triple("Jonas Lindqvist", "Supporting", true),
    Triple("Priya Raman", "Director", false),
    Triple("Mateo Ferreira", "Composer", true),
    Triple("Sana Haddad", "Editor", false),
)

/** A cast tile: a round photo over the name and a one-line subtitle. Tapping it in the catalog counts a tap. */
@Composable
fun PersonCardSample() {
    ScreenshotTheme {
        var taps by remember { mutableStateOf(0) }
        Column {
            WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
                PersonCard(name = "Amara Okafor", subtitle = "Lead", photoUrl = PHOTO_URL, onClick = { taps++ })
            }
            if (taps > 0) Text("Tapped $taps")
        }
    }
}

/** A name too long for one line: it wraps to two lines and then ellipsizes. */
@Composable
fun PersonCardLongNameSample() {
    ScreenshotTheme {
        LooseFrame {
            WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
                PersonCard(
                    name = "Maria Fernanda de los Angeles Rodriguez Hernandez",
                    subtitle = "Lead",
                    photoUrl = PHOTO_URL,
                    onClick = {},
                )
            }
        }
    }
}

/** A subtitle too long for the tile: it stays on one line and ellipsizes. */
@Composable
fun PersonCardLongSubtitleSample() {
    ScreenshotTheme {
        LooseFrame {
            WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
                PersonCard(
                    name = "Amara Okafor",
                    subtitle = "Supervising sound editor and re-recording mixer",
                    photoUrl = PHOTO_URL,
                    onClick = {},
                )
            }
        }
    }
}

/** No photo: the person's initials on a neutral surface. */
@Composable
fun PersonCardNoPhotoSample() {
    ScreenshotTheme {
        LooseFrame {
            PersonCard(name = "Amara Okafor", subtitle = "Lead", photoUrl = null, onClick = {})
        }
    }
}

/** No subtitle: the caption still takes the same room, so a row of people stays one height. */
@Composable
fun PersonCardNoSubtitleSample() {
    ScreenshotTheme {
        LooseFrame {
            WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
                PersonCard(name = "Amara Okafor", subtitle = null, photoUrl = PHOTO_URL, onClick = {})
            }
        }
    }
}

/** A row of people in a [MediaCarousel]: some with a photo, some on their initials, and one name that wraps. */
@Composable
fun PersonCardRowSample() {
    ScreenshotTheme {
        WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
            MediaCarousel(title = "Cast", items = cast, itemKey = { it.first }) { _, (name, subtitle, hasPhoto) ->
                PersonCard(
                    name = name,
                    subtitle = subtitle,
                    photoUrl = if (hasPhoto) PHOTO_URL else null,
                    onClick = {},
                )
            }
        }
    }
}

/**
 * A short row of people with its see-all tile. The tile is as tall as the cards, which `endTileHeight = Dp.Unspecified` takes from the
 * tallest card the row has composed, so it follows the font scale; see the worst-case frames at 1.3x and 2x.
 */
@Composable
fun PersonCardRowEndTileSample() {
    ScreenshotTheme {
        WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
            MediaCarousel(
                title = "Cast",
                items = cast.take(2),
                itemKey = { it.first },
                onMoreClick = {},
                endTileHeight = Dp.Unspecified,
            ) { _, (name, subtitle, hasPhoto) ->
                PersonCard(name = name, subtitle = subtitle, photoUrl = if (hasPhoto) PHOTO_URL else null, onClick = {})
            }
        }
    }
}

/** The loading stand-in: a round plate and a caption that takes the card's own room. */
@Composable
fun PersonCardSkeletonSample() {
    ScreenshotTheme {
        LooseFrame { PersonCardSkeleton() }
    }
}

/** The compact cast tile, as a detail page's cast row draws it: a round photo at the cast avatar size, the name on one line and the role on one. */
@Composable
fun PersonCardCompactSample() {
    ScreenshotTheme {
        var taps by remember { mutableStateOf(0) }
        Column {
            WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
                PersonCard(
                    name = "Amara Okafor",
                    subtitle = "Lead",
                    photoUrl = PHOTO_URL,
                    onClick = { taps++ },
                    style = PersonCardStyle.Compact,
                )
            }
            if (taps > 0) Text("Tapped $taps")
        }
    }
}

/** A name and a role too long for the compact tile: each stays on one line and ellipsizes. */
@Composable
fun PersonCardCompactLongNameSample() {
    ScreenshotTheme {
        LooseFrame {
            WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
                PersonCard(
                    name = "Maria Fernanda de los Angeles Rodriguez Hernandez",
                    subtitle = "Supervising sound editor and re-recording mixer",
                    photoUrl = PHOTO_URL,
                    onClick = {},
                    style = PersonCardStyle.Compact,
                )
            }
        }
    }
}

/** No photo: the person's initials on a neutral surface, in the compact tile. */
@Composable
fun PersonCardCompactNoPhotoSample() {
    ScreenshotTheme {
        LooseFrame {
            PersonCard(name = "Amara Okafor", subtitle = "Lead", photoUrl = null, onClick = {}, style = PersonCardStyle.Compact)
        }
    }
}

/** A cast row of compact tiles in a [MediaCarousel]: some with a photo, some on their initials. See the worst-case frames at 1.3x and 2x. */
@Composable
fun PersonCardCompactRowSample() {
    ScreenshotTheme {
        WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
            MediaCarousel(title = "Cast", items = cast, itemKey = { it.first }) { _, (name, subtitle, hasPhoto) ->
                PersonCard(
                    name = name,
                    subtitle = subtitle,
                    photoUrl = if (hasPhoto) PHOTO_URL else null,
                    onClick = {},
                    style = PersonCardStyle.Compact,
                )
            }
        }
    }
}

/** The compact tile's loading stand-in: a round plate at the cast avatar size and a two-line caption, the tile's own room. */
@Composable
fun PersonCardCompactSkeletonSample() {
    ScreenshotTheme {
        LooseFrame { PersonCardSkeleton(style = PersonCardStyle.Compact) }
    }
}
