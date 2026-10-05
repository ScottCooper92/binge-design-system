@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvCardRow
import com.binge.designsystem.tv.component.TvDetailAction
import com.binge.designsystem.tv.component.TvDetailActionRow
import com.binge.designsystem.tv.component.TvDetailHero
import com.binge.designsystem.tv.component.TvDetailHeroItem
import com.binge.designsystem.tv.component.TvHeroOverview
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.nav.LocalTvHostedAsOverlay
import com.binge.designsystem.tv.template.TvDetailPage
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SAMPLE_CARD_COUNT = 8
private const val SECTION_KEY = "seasons"

private val SampleHeroItem =
    TvDetailHeroItem(
        title = "A title with a long enough name to wrap",
        overline = "Movie  ·  4K",
        overview =
            "A synopsis that runs long enough to fill the three lines the hero reserves for it, whatever the data returns, " +
                "so a short one cannot pull the facts line up under the title and a long one is clamped with an ellipsis.",
        facts = listOf("2026", "Pending", "Claude"),
        certification = "12A",
    )

/** Stands a page in for an overlay above the rail: nothing is cleared beside it, so every edge takes the overscan. */
@Composable
private fun OverlayHosted(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTvHostedAsOverlay provides true, content = content)
}

@Composable
private fun sampleActions(): List<TvDetailAction> =
    listOf(
        TvDetailAction("Approve", Icons.Filled.Check, onClick = {}, isPrimary = true, initiallyFocused = true),
        TvDetailAction("Decline", Icons.Filled.Close, onClick = {}, showLabel = true),
        TvDetailAction("Block", Icons.Filled.Block, onClick = {}, showLabel = true),
        TvDetailAction("Open", Icons.AutoMirrored.Filled.OpenInNew, onClick = {}),
    )

@Composable
private fun SampleHero(entryFocus: FocusRequester, synopsisFocused: Boolean = false) {
    TvDetailHero(
        item = SampleHeroItem,
        artwork = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.background)),
                        ),
            )
        },
        overview = if (synopsisFocused) {
            TvHeroOverview(isFocused = true, onFocusChanged = {}, onClick = {})
        } else {
            null
        },
        poster = { Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) },
    ) {
        TvDetailActionRow(actions = sampleActions(), entryFocus = entryFocus)
    }
}

@Composable
private fun SampleDetailPage(initialFocusedSectionKey: String? = null, synopsisFocused: Boolean = false) {
    val entryFocus = remember { FocusRequester() }
    OverlayHosted {
        TvDetailPage(entryFocus = entryFocus, initialFocusedSectionKey = initialFocusedSectionKey) {
            hero { SampleHero(entryFocus, synopsisFocused) }
            section(SECTION_KEY) { onFocused ->
                SampleCardRow(heading = "Seasons requested", onFocused = onFocused)
            }
            section("details") { onFocused ->
                SampleCardRow(heading = "Details", onFocused = onFocused)
            }
        }
    }
}

@Composable
private fun SampleCardRow(heading: String, onFocused: (Any) -> Unit) {
    TvCardRow(
        items = List(SAMPLE_CARD_COUNT) { it },
        key = { it },
        cellWidth = dimensionResource(TvR.dimen.tv_immersive_card_width),
        heading = heading,
        onCellFocused = onFocused,
    ) { _, isFocused, onFocusChanged, cellModifier ->
        Box(
            modifier =
                cellModifier
                    .aspectRatio(SAMPLE_CARD_RATIO)
                    .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                    .clip(BingeShapes.MediaCard)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .tvClickable(onFocusChanged = onFocusChanged, onClick = {}),
        )
    }
}

private const val SAMPLE_CARD_RATIO = 4f / 3f

/** A detail page at rest: the hero band with its poster, copy and action row, and the first row peeking beneath it. */
@Composable
fun TvDetailPageSample() {
    SampleDetailPage()
}

/** Focus in the first section: it is pulled to a fixed rest under where the hero was, the hero scrolled away above it. */
@Composable
fun TvDetailPageFocusedSample() {
    SampleDetailPage(initialFocusedSectionKey = SECTION_KEY)
}

/** The synopsis as a focusable block: the hero's one focus stop when a band has no action row, drawn with its ring. */
@Composable
fun TvDetailPageSynopsisFocusedSample() {
    SampleDetailPage(synopsisFocused = true)
}

/** The action row: one labelled primary, labelled secondary actions where a bare glyph would not name them, and icons. */
@Composable
fun TvDetailActionRowSample() {
    Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(DesR.dimen.padding_l))) {
        TvDetailActionRow(actions = sampleActions(), entryFocus = remember { FocusRequester() })
    }
}
