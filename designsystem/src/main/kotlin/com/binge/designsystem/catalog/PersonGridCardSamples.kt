@file:CatalogGroup("Person grid card")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.PersonGridCard
import com.binge.designsystem.component.PersonGridCardSkeleton
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage

private const val PHOTO_URL = "sample://photo"
private const val PHOTO_WIDTH_PX = 320
private const val PHOTO_HEIGHT_PX = 480

@Composable
private fun CellFrame(content: @Composable () -> Unit) {
    LooseFrame {
        Column(Modifier.width(dimensionResource(R.dimen.person_grid_card_sample_width))) { content() }
    }
}

/** A person in a grid cell: a portrait photo over the name and the role. Tapping it in the catalog counts a tap. */
@Composable
fun PersonGridCardSample() {
    ScreenshotTheme {
        var taps by remember { mutableStateOf(0) }
        CellFrame {
            WithPreviewImage(PHOTO_WIDTH_PX, PHOTO_HEIGHT_PX) {
                PersonGridCard(name = "Amara Okafor", role = "Lead", photoUrl = PHOTO_URL, onClick = { taps++ })
            }
            if (taps > 0) Text("Tapped $taps")
        }
    }
}

/** A name and a role too long for the cell: each stays on one line and ellipsizes. */
@Composable
fun PersonGridCardLongTextSample() {
    ScreenshotTheme {
        CellFrame {
            WithPreviewImage(PHOTO_WIDTH_PX, PHOTO_HEIGHT_PX) {
                PersonGridCard(
                    name = "Maria Fernanda de los Angeles Rodriguez Hernandez",
                    role = "Supervising sound editor and re-recording mixer",
                    photoUrl = PHOTO_URL,
                    onClick = {},
                )
            }
        }
    }
}

/** No photo: a neutral plate with an image glyph stands in, and the name and role are unchanged. */
@Composable
fun PersonGridCardNoPhotoSample() {
    ScreenshotTheme {
        CellFrame { PersonGridCard(name = "Amara Okafor", role = "Lead", photoUrl = null, onClick = {}) }
    }
}

/** No role: the caption still takes the room of two lines, so a row of people stays one height. */
@Composable
fun PersonGridCardNoRoleSample() {
    ScreenshotTheme {
        CellFrame {
            WithPreviewImage(PHOTO_WIDTH_PX, PHOTO_HEIGHT_PX) {
                PersonGridCard(name = "Amara Okafor", role = null, photoUrl = PHOTO_URL, onClick = {})
            }
        }
    }
}

/** The loading stand-in: a portrait plate and a caption that takes the card's own room. */
@Composable
fun PersonGridCardSkeletonSample() {
    ScreenshotTheme {
        CellFrame { PersonGridCardSkeleton() }
    }
}
