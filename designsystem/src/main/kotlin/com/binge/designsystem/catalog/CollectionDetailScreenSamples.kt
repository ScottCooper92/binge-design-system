@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.BingeMessageDialog
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.component.StatusChip
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage
import com.binge.designsystem.template.CollectionDetail
import com.binge.designsystem.template.CollectionDetailCallbacks
import com.binge.designsystem.template.CollectionDetailScreen
import com.binge.designsystem.template.CollectionDetailState
import com.binge.designsystem.template.ScreenFooter
import com.binge.designsystem.theme.BingeSentiment

private const val POSTER_WIDTH_PX = 400
private const val POSTER_HEIGHT_PX = 600
private const val OVERVIEW =
    "Three friends, one lighthouse and a coastline that keeps changing. The series follows what they find there, and what it costs them to leave."
private const val BLURB =
    "The friends return to the coast to find the lighthouse lit again. Nobody admits to having lit it, and the tide tables no longer agree with the sea."

private fun parts(blurbs: Boolean = true) =
    listOf("The Lighthouse", "The Lighthouse II: Low Tide", "The Lighthouse III: Dead Reckoning").mapIndexed { i, name ->
        MediaResult(
            key = "p$i",
            kind = MediaResultKind.Poster,
            imageUrl = null,
            name = name,
            rating = if (i == 2) null else 7f + i,
            meta = (2019 + i * 2).toString(),
            blurb = if (blurbs) BLURB else null,
        )
    }

private fun sampleCollection(
    overview: String? = OVERVIEW,
    parts: List<MediaResult> = parts(),
    posterUrl: String? = null,
) = CollectionDetail(
    title = "The Lighthouse Collection",
    posterUrl = posterUrl,
    metaText = "3 movies",
    overview = overview,
    partsHeading = "Movies",
    parts = parts,
    emptyTitle = "No movies yet",
    emptyMessage = "This collection has no titles to show.",
)

@Composable
private fun SampleScreen(state: CollectionDetailState, withFooter: Boolean = false) {
    var info by remember { mutableStateOf<String?>(null) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        CollectionDetailScreen(
            state = state,
            callbacks = CollectionDetailCallbacks(onBack = LocalDemoBack.current, onRetry = {}, onOpenPart = { info = it }),
            partTrailing = { part ->
                if (part.key != "p1") {
                    { StatusChip("Available", BingeSentiment.Positive) }
                } else {
                    null
                }
            },
            footer =
                if (withFooter) {
                    {
                        ScreenFooter {
                            Button(
                                onClick = { info = "Request all" },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Request all") }
                        }
                    }
                } else {
                    null
                },
        )
        info?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { info = null }) }
    }
}

/** A collection's page: the overview that expands, then each part as a row with its status, rating, year and a three-line blurb, and a pinned request button. */
@Composable
fun CollectionDetailReadySample() = SampleScreen(CollectionDetailState.Content(sampleCollection()), withFooter = true)

/** The page on its way: the hero's plate, a block of prose and four placeholder rows in the loaded page's own room. */
@Composable
fun CollectionDetailLoadingSample() = SampleScreen(CollectionDetailState.Loading())

/** The page failed: the message over the back control, with Try again. */
@Composable
fun CollectionDetailFailedSample() = SampleScreen(CollectionDetailState.Failed(ErrorKind.Network))

/** A collection with no titles: the app's own words over the back control. */
@Composable
fun CollectionDetailEmptySample() = SampleScreen(CollectionDetailState.Content(sampleCollection(parts = emptyList())))

/** A collection with a poster and no backdrop, as a hand-made box set often is: the poster is the picture behind the hero, and beside the title at an expanded width. */
@Composable
fun CollectionDetailPosterOnlySample() =
    WithPreviewImage(POSTER_WIDTH_PX, POSTER_HEIGHT_PX) {
        SampleScreen(CollectionDetailState.Content(sampleCollection(posterUrl = "sample://poster")))
    }

/** A collection with no overview and no blurbs: those blocks take no room, and the rows stay as short as their text. */
@Composable
fun CollectionDetailPlainSample() =
    SampleScreen(CollectionDetailState.Content(sampleCollection(overview = null, parts = parts(blurbs = false))))
