@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.PaneContent
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.component.BingeBarIconAction
import com.binge.designsystem.component.BingeStatusButton
import com.binge.designsystem.component.BingeStatusButtonEmphasis
import com.binge.designsystem.component.DetailInfoEntry
import com.binge.designsystem.component.DetailPerson
import com.binge.designsystem.component.DetailPoster
import com.binge.designsystem.component.DetailRoundAction
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage
import com.binge.designsystem.template.DetailInfo
import com.binge.designsystem.template.DetailState
import com.binge.designsystem.template.MediaDetail
import com.binge.designsystem.template.MediaDetailCallbacks
import com.binge.designsystem.template.MovieDetailScreen
import com.binge.designsystem.template.PeopleRail
import com.binge.designsystem.template.PersonItem
import com.binge.designsystem.template.PosterItem
import com.binge.designsystem.template.PosterRail
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.template.TagItem
import com.binge.designsystem.template.TagsSection
import com.binge.designsystem.theme.BingeSentiment

private const val PHOTO_URL = "sample://photo"
private const val PHOTO_SIZE_PX = 352
private const val SAMPLE_ROUND_ACTIONS = 2
private const val SAMPLE_RAILS = 2

private val cast = listOf(
    "Amara Okafor" to "Lead",
    "Jonas Lindqvist" to "Supporting",
    "Priya Raman" to "Supporting",
    "Mateo Ferreira" to "Cameo",
)
private val crew = listOf("Sana Haddad" to "Director", "Priya Raman" to "Writer", "Mateo Ferreira" to "Composer")
private val similar = listOf("The Dark Knight", "Inception", "Interstellar", "Tenet", "Dunkirk")

private fun movie() =
    MediaDetail(
        title = MEDIA_PAGE_TITLE,
        tagline = MEDIA_PAGE_TAGLINE,
        genres = MEDIA_PAGE_GENRES,
        stats = MediaPageStats,
        overview = MEDIA_PAGE_OVERVIEW,
        peopleRails = listOf(
            PeopleRail("Cast", cast.map { (name, role) -> PersonItem("cast:$name", DetailPerson(name, role, PHOTO_URL)) }, seeAll = true),
            PeopleRail("Crew", crew.map { (name, job) -> PersonItem("crew:$name", DetailPerson(name, job, PHOTO_URL)) }),
        ),
        posterRails = listOf(
            PosterRail("The Brand New Collection", similar.take(2).map { PosterItem(it, DetailPoster(null, it, 8.1f)) }),
            PosterRail(
                "Similar titles",
                similar.map { title ->
                    val seen = if (title == similar.first()) MediaCardStatus("Watched", BingeSentiment.Positive) else null
                    PosterItem(title, DetailPoster(null, title, 8.0f, status = seen))
                },
                seeAll = true,
            ),
        ),
        tags = TagsSection("Keywords", listOf("superhero", "sequel", "new york", "teenager", "multiverse").map { TagItem(it, it) }),
        info = DetailInfo(
            "Details",
            listOf(
                DetailInfoEntry(Icons.Filled.CalendarMonth, "Released", "31 July 2026"),
                DetailInfoEntry(Icons.Filled.Language, "Original language", "English"),
                DetailInfoEntry(Icons.Filled.Business, "Studio", "Columbia Pictures"),
            ),
        ),
    )

/**
 * A movie's page assembled by the template, at a phone's width: the hero with a share action in the bar, the four stats, a Play primary
 * with Watched and Favourite round actions under it (tapping them in the catalog flips them, as an app's toggles do), the synopsis, a cast
 * and a crew rail, a collection and a similar-titles rail with a watched badge, the keywords and the facts. At the expanded width the
 * cinematic header carries the stats and the synopsis, and the body leaves both out.
 */
@Composable
fun MovieDetailScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        var watched by remember { mutableStateOf(false) }
        var favourite by remember { mutableStateOf(false) }
        WithPreviewImage(PHOTO_SIZE_PX, PHOTO_SIZE_PX) {
            MovieDetailScreen(
                state = DetailState.Content(movie()),
                callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
                primary = { modifier ->
                    BingeStatusButton(
                        label = "Play",
                        onClick = {},
                        sentiment = BingeSentiment.Neutral,
                        modifier = modifier,
                        icon = Icons.Filled.PlayArrow,
                        emphasis = BingeStatusButtonEmphasis.Filled,
                    )
                },
                roundActions = listOf(
                    DetailRoundAction(
                        icon = if (watched) Icons.Filled.Check else Icons.Outlined.CheckCircleOutline,
                        label = "Watched",
                        onClick = { watched = !watched },
                        contentDescription = if (watched) "Mark as not watched" else "Mark as watched",
                    ),
                    DetailRoundAction(
                        icon = if (favourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        label = "Favourite",
                        onClick = { favourite = !favourite },
                        contentDescription = if (favourite) "Remove from favourites" else "Add to favourites",
                    ),
                ),
                topBarActions = { glassBackgroundAlpha ->
                    BingeBarIconAction(Icons.Filled.Share, "Share", onClick = {}, glassBackgroundAlpha = glassBackgroundAlpha)
                },
            )
        }
    }
}

/**
 * The same page in the right pane of two side by side, in a window at the expanded width: it lays out for the pane's width, with the hero and
 * the stacked stats and actions of a phone, not the cinematic header and the row of actions the whole window has room for.
 */
@Composable
fun MovieDetailScreenInPaneSample() {
    Row(Modifier.fillMaxSize()) {
        Spacer(Modifier.weight(1f).fillMaxHeight())
        Box(Modifier.weight(1f).fillMaxHeight()) { PaneContent(innerEdge = PaneEdge.Start) { MovieDetailScreenSample() } }
    }
}

/**
 * The same page while it loads, held to the loaded page's geometry: the hero, the four stat rows, the primary with its two round
 * actions stacked under it, and the overview land where the loaded page puts them, so nothing jumps when the page arrives.
 */
@Composable
fun MovieDetailScreenLoadingSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Loading(stats = MediaPageStats.size, roundActions = SAMPLE_ROUND_ACTIONS, rails = SAMPLE_RAILS),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
        )
    }
}

/**
 * The page's failure in place of the page, on the loading page's own frame so the Back control does not move between them: the app's own
 * message, and Try again because another attempt can help.
 */
@Composable
fun MovieDetailScreenFailedSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Failed("We could not reach the server. Check your connection and try again."),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
        )
    }
}

/**
 * A failure that offers a second action beside Try again, here "Allow" for a permission the failure needs: [DetailState.Failed.secondary]
 * is drawn beside the retry on the same frame.
 */
@Composable
fun MovieDetailScreenFailedWithActionSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Failed(
                "Local network access is off, so the server cannot be reached.",
                secondary = ScreenAction("Allow", {}),
            ),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current, onRetry = {}),
        )
    }
}

/** A failure with a headline of the app's own: [DetailState.Failed.title] replaces the kind's "Something went wrong". */
@Composable
fun MovieDetailScreenFailedTitledSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Failed(
                "We could not reach the server. Check your connection and try again.",
                title = "Can't load this film",
            ),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
        )
    }
}

/**
 * A failure another attempt cannot help, such as a film that no longer exists: the same frame with no Try again, and no message of the
 * app's own, so the kind's glyph, headline and words are used.
 */
@Composable
fun MovieDetailScreenNotFoundSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Failed(
                "This film is no longer listed.",
                retryable = false,
                kind = ErrorKind.NotFound,
            ),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
        )
    }
}

/**
 * The page while it loads for a title that promises little: two stats and no primary action, no round actions and no rails. The skeleton
 * holds only the hero, the stats and the overview, with no carousel placeholder, so a page with no cast or similar titles does not jump.
 */
@Composable
fun MovieDetailScreenLoadingBareSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Loading(stats = 2, roundActions = 0, hasPrimary = false),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
        )
    }
}

/** A title with nothing to play but Watched and Favourite: with no primary the round actions are still drawn, centred in a row of their own. */
@Composable
fun MovieDetailScreenRoundActionsOnlySample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MovieDetailScreen(
            state = DetailState.Content(
                MediaDetail(title = MEDIA_PAGE_TITLE, genres = MEDIA_PAGE_GENRES, stats = MediaPageStats, overview = MEDIA_PAGE_OVERVIEW),
            ),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
            roundActions = listOf(
                DetailRoundAction(Icons.Outlined.CheckCircleOutline, "Watched", onClick = {}),
                DetailRoundAction(Icons.Filled.FavoriteBorder, "Favourite", onClick = {}),
            ),
        )
    }
}

/**
 * A page with little under its hero (the stats, one poster rail and the facts: no cast and no overview), scrolled to its end. The page is
 * kept at least a window tall, so it can scroll the whole hero away and the bar's hand-over finishes: the bar has its scrim and its
 * title, the hero's own title is gone, and nothing is drawn over the content. Scroll it in the catalog to see the hand-over.
 */
@Composable
fun MovieDetailScreenSparseSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        // An initial value past the end is brought back to the end once the page is laid out, so the frame rests where a scroll to the end does.
        val scrollState = rememberScrollState(initial = Int.MAX_VALUE)
        MovieDetailScreen(
            state = DetailState.Content(
                MediaDetail(
                    title = MEDIA_PAGE_TITLE,
                    genres = MEDIA_PAGE_GENRES,
                    stats = MediaPageStats,
                    posterRails = listOf(PosterRail("Similar titles", similar.map { PosterItem(it, DetailPoster(null, it, 8.0f)) })),
                    info = DetailInfo("Details", listOf(DetailInfoEntry(Icons.Filled.CalendarMonth, "Released", "31 July 2026"))),
                ),
            ),
            callbacks = MediaDetailCallbacks(onBack = LocalDemoBack.current),
            scrollState = scrollState,
        )
    }
}
