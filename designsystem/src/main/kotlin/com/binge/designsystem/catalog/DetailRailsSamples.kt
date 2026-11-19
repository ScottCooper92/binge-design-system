@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailPeopleLine
import com.binge.designsystem.component.DetailPeopleRail
import com.binge.designsystem.component.DetailPerson
import com.binge.designsystem.component.DetailPoster
import com.binge.designsystem.component.DetailPosterRail
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage
import com.binge.designsystem.theme.BingeSentiment

private const val PHOTO_URL = "sample://photo"
private const val PHOTO_SIZE_PX = 352

private val people = listOf(
    "Amara Okafor" to "Lead",
    "Jonas Lindqvist" to "Supporting",
    "Priya Raman" to "Director",
    "Mateo Ferreira" to "Composer",
    "Sana Haddad" to "Editor",
)

private val titles = listOf("The Dark Knight", "Inception", "Interstellar", "Tenet", "Dunkirk", "Oppenheimer")

/** A cast row under a detail page's overview: round photos over a name and a role, with the heading as a link to the full list. */
@Composable
fun DetailPeopleRailSample() {
    ScreenshotTheme {
        WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
            DetailPeopleRail(
                title = "Cast",
                people = people,
                itemKey = { it.first },
                person = { DetailPerson(name = it.first, subtitle = it.second, photoUrl = PHOTO_URL) },
                onOpen = {},
                onSeeAll = {},
            )
        }
    }
}

/** A row of similar titles with a status badge on the first and a see-all tile at its end. */
@Composable
fun DetailPosterRailSample() {
    ScreenshotTheme {
        DetailPosterRail(
            title = "Similar",
            items = titles,
            itemKey = { it },
            poster = { title ->
                DetailPoster(
                    posterUrl = null,
                    title = title,
                    rating = 8.5f,
                    status = if (title == titles.first()) MediaCardStatus("Available", BingeSentiment.Positive) else null,
                )
            },
            onOpen = {},
            onSeeAll = {},
        )
    }
}

/** The cast as one line of names and roles, with no photos: each name is a tap target, and the heading links to the full list. */
@Composable
fun DetailPeopleLineSample() {
    ScreenshotTheme {
        DetailPeopleLine(
            title = "Cast",
            people = people,
            itemKey = { it.first },
            person = { DetailPerson(name = it.first, subtitle = it.second, photoUrl = null) },
            onOpen = {},
            onSeeAll = {},
        )
    }
}
