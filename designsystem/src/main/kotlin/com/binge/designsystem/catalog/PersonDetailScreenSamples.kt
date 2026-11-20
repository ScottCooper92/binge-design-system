@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.BingeMessageDialog
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.component.PersonFact
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.CreditSection
import com.binge.designsystem.template.CreditsState
import com.binge.designsystem.template.GridItem
import com.binge.designsystem.template.PersonDetail
import com.binge.designsystem.template.PersonDetailCallbacks
import com.binge.designsystem.template.PersonDetailScreen
import com.binge.designsystem.template.PersonDetailState
import com.binge.designsystem.theme.BingeSentiment
import kotlinx.coroutines.delay

private const val CREDITS_PAGE = 8
private const val LOAD_MILLIS = 900L

private val CreditTitles = listOf("Arrival", "Past Lives", "The Dark Knight", "Heat", "Paddington 2", "Drive", "Aliens", "Her")
private const val BIOGRAPHY =
    "Born in a small town and raised on the stage, she moved to the city at nineteen and took whatever work she could find. " +
        "Her first film role came two years later. Since then she has worked across drama, comedy and the occasional thriller, " +
        "and has directed two short films of her own."

private fun credits(
    prefix: String,
    count: Int = CREDITS_PAGE,
    from: Int = 0,
): List<GridItem> =
    List(count) { i ->
        val n = from + i
        GridItem(
            key = "$prefix-$n",
            posterUrl = null,
            title = CreditTitles[n % CreditTitles.size],
            rating = if (n % 4 == 3) null else 6f + (n % 4),
            status = if (n % 3 == 0) MediaCardStatus("Available", BingeSentiment.Positive) else null,
            subtitle = if (n % 2 == 0) "Maya" else "Director",
        )
    }

private fun sampleSections(
    morePaging: Boolean = false,
    appending: Boolean = false,
    appendFailed: Boolean = false,
) = listOf(
    CreditSection("acting", "Acting", credits("a"), hasMore = morePaging, appending = appending, appendFailed = appendFailed),
    CreditSection("crew", "Directing", credits("d", 4)),
)

private fun sampleFacts() =
    listOf(
        PersonFact("14 March 1985 (41 years old)", "Born"),
        PersonFact("Lisbon, Portugal", "Place of birth"),
        PersonFact("Also known as Maya R."),
    )

private fun samplePerson(credits: CreditsState = CreditsState.Ready(sampleSections()), biography: String? = BIOGRAPHY) =
    PersonDetail(
        name = "Maya Rodrigues",
        eyebrow = "Acting",
        facts = sampleFacts(),
        biography = biography,
        credits = credits,
    )

@Composable
private fun SampleScreen(state: PersonDetailState, seeAll: Boolean = false) {
    var info by remember { mutableStateOf<String?>(null) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        PersonDetailScreen(
            state = state,
            callbacks =
                PersonDetailCallbacks(
                    onBack = LocalDemoBack.current,
                    onRetry = {},
                    onOpenCredit = { info = it },
                    onSeeAll = if (seeAll) ({ info = "See all: $it" }) else null,
                ),
        )
        info?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { info = null }) }
    }
}

/** A person's page: portrait, name and facts, a biography that expands, and two rows of credits with the role under each title. */
@Composable
fun PersonDetailReadySample() = SampleScreen(PersonDetailState.Content(samplePerson()))

/** The page on its way: a portrait plate, three fact lines, a block of prose and two rows, in the loaded page's own room. */
@Composable
fun PersonDetailLoadingSample() = SampleScreen(PersonDetailState.Loading())

/**
 * The loading page with hints at what the profile will say, so its name and facts are as tall as the loaded ones at any font size, wrapping included. The
 * hints are the loaded sample's own strings; an app hints with the longest typical ones.
 */
@Composable
fun PersonDetailLoadingHintedSample() =
    SampleScreen(PersonDetailState.Loading(expectedEyebrow = "Acting", expectedName = "Maya Rodrigues", expectedFacts = sampleFacts()))

/** The page failed: the message over the back control, with Try again. */
@Composable
fun PersonDetailFailedSample() = SampleScreen(PersonDetailState.Failed(ErrorKind.Network))

/** The person is here but the credits are still on their way: two rows of plates under the biography. */
@Composable
fun PersonDetailCreditsLoadingSample() = SampleScreen(PersonDetailState.Content(samplePerson(CreditsState.Loading)))

/** The credits failed: the person stays, and a line says so with Try again. */
@Composable
fun PersonDetailCreditsFailedSample() = SampleScreen(PersonDetailState.Content(samplePerson(CreditsState.Failed())))

/** A person with no biography and no photo: the section is left out and the initials stand in for the portrait. */
@Composable
fun PersonDetailPlainSample() = SampleScreen(PersonDetailState.Content(samplePerson(biography = null)))

/** A person with no titles: the app's own words stand where the rows would, under the profile and the biography. */
@Composable
fun PersonDetailCreditsEmptySample() =
    SampleScreen(PersonDetailState.Content(samplePerson(CreditsState.Empty("No titles to show for Maya yet."))))

/** A long filmography: each row ends in a See all tile and its heading is a link, so the app can open the whole list. The default draws neither. */
@Composable
fun PersonDetailSeeAllSample() = SampleScreen(PersonDetailState.Content(samplePerson()), seeAll = true)

/** Live: scroll the acting row to its end and the next page loads after a short wait; pull the page down to refresh it. */
@Composable
fun PersonDetailDemo() {
    var acting by remember { mutableStateOf(credits("a")) }
    var appending by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(appending) {
        if (appending) {
            delay(LOAD_MILLIS)
            acting = acting + credits("a", CREDITS_PAGE, acting.size)
            appending = false
        }
    }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(LOAD_MILLIS)
            acting = credits("a")
            refreshing = false
        }
    }
    val sections =
        listOf(
            CreditSection("acting", "Acting", acting, hasMore = acting.size < CREDITS_PAGE * 3, appending = appending),
            CreditSection("crew", "Directing", credits("d", 4)),
        )
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        PersonDetailScreen(
            state = PersonDetailState.Content(
                PersonDetail(
                    name = "Maya Rodrigues",
                    eyebrow = "Acting",
                    facts = sampleFacts(),
                    biography = BIOGRAPHY,
                    credits = CreditsState.Ready(sections),
                    refreshing = refreshing,
                ),
            ),
            callbacks =
                PersonDetailCallbacks(
                    onBack = LocalDemoBack.current,
                    onRetry = {},
                    onOpenCredit = { info = it },
                    onLoadMoreCredits = { if (it == 0) appending = true },
                    onRefresh = { refreshing = true },
                ),
        )
        info?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { info = null }) }
    }
}
