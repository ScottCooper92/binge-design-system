@file:CatalogGroup("Category tiles")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.BackdropTile
import com.binge.designsystem.component.LogoTile
import com.binge.designsystem.component.MediaCarousel
import com.binge.designsystem.component.backdropTileHeight
import com.binge.designsystem.component.categoryTileHeight
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage

private const val BACKDROP_URL = "sample://backdrop"
private const val LOGO_URL = "sample://logo"
private const val BACKDROP_WIDTH_PX = 640
private const val BACKDROP_HEIGHT_PX = 360
private const val LOGO_WIDTH_PX = 400
private const val LOGO_HEIGHT_PX = 160

/**
 * Lets a tile take its own width. The screenshot surface hands its child tight constraints, which would
 * stretch a fixed-width tile to the frame; a row or column in an app never does.
 */
@Composable
internal fun LooseFrame(content: @Composable () -> Unit) {
    Box(Modifier.wrapContentSize(Alignment.TopStart)) { content() }
}

private val genres = listOf("Action")
private val studios = listOf("Sample Pictures")

/** A genre tile: a backdrop with the name on its scrim. Tapping it in the catalog counts a tap. */
@Composable
fun BackdropTileSample() {
    ScreenshotTheme {
        var taps by remember { mutableStateOf(0) }
        Column {
            WithPreviewImage(BACKDROP_WIDTH_PX, BACKDROP_HEIGHT_PX) {
                BackdropTile(title = "Action", imageUrl = BACKDROP_URL, onClick = { taps++ })
            }
            if (taps > 0) Text("Tapped $taps")
        }
    }
}

/**
 * The same tile with `showTitle = false`: the picture alone, for artwork that already has its name drawn in. The name is still the
 * accessibility label.
 */
@Composable
fun BackdropTilePictureOnlySample() {
    ScreenshotTheme {
        WithPreviewImage(BACKDROP_WIDTH_PX, BACKDROP_HEIGHT_PX) {
            LooseFrame {
                BackdropTile(title = "Action", imageUrl = BACKDROP_URL, onClick = {}, showTitle = false)
            }
        }
    }
}

/** A name too long for one line: it wraps to two lines and then ellipsises, over the same scrim. */
@Composable
fun BackdropTileLongTitleSample() {
    ScreenshotTheme {
        WithPreviewImage(BACKDROP_WIDTH_PX, BACKDROP_HEIGHT_PX) {
            LooseFrame {
                BackdropTile(
                    title = "Science Fiction and Fantasy Adventure Features",
                    imageUrl = BACKDROP_URL,
                    onClick = {},
                )
            }
        }
    }
}

/** No image URL: a solid surface tile that still carries the name. */
@Composable
fun BackdropTileNoImageSample() {
    ScreenshotTheme {
        LooseFrame {
            BackdropTile(title = "Documentary", imageUrl = null, onClick = {})
        }
    }
}

/** A studio or network tile: the logo fitted inside the tile, never cropped. */
@Composable
fun LogoTileSample() {
    ScreenshotTheme {
        WithPreviewImage(LOGO_WIDTH_PX, LOGO_HEIGHT_PX) {
            LooseFrame {
                LogoTile(name = "Sample Pictures", logoUrl = LOGO_URL, onClick = {})
            }
        }
    }
}

/** No logo URL: the tile shows the name as text, so it is never blank. */
@Composable
fun LogoTileNoLogoSample() {
    ScreenshotTheme {
        LooseFrame {
            LogoTile(name = "Example Studio", logoUrl = null, onClick = {})
        }
    }
}

/** A row of each kind in a [MediaCarousel], with its trailing tile, to show the rhythm and spacing. */
@Composable
fun CategoryTileRowSample() {
    ScreenshotTheme {
        WithPreviewImage(BACKDROP_WIDTH_PX, BACKDROP_HEIGHT_PX) {
            Column {
                MediaCarousel(
                    title = "Genres",
                    items = genres,
                    itemKey = { it },
                    onMoreClick = {},
                    endTileHeight = backdropTileHeight(),
                ) { _, genre ->
                    BackdropTile(title = genre, imageUrl = BACKDROP_URL, onClick = {})
                }
                MediaCarousel(
                    title = "Studios",
                    items = studios,
                    itemKey = { it },
                    onMoreClick = {},
                    endTileHeight = categoryTileHeight(),
                ) { _, studio ->
                    LogoTile(name = studio, logoUrl = null, onClick = {})
                }
            }
        }
    }
}
